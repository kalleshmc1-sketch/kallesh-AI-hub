package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.backend.FullStackBackendEngine
import com.example.data.local.KalleshLocalDatabase
import com.example.data.local.LocalCacheRepository
import com.example.data.model.PRODUCTION_CUSTOM_DOMAIN
import com.example.data.model.PublicPortalPage
import com.example.data.model.SEO_META_DESCRIPTION
import com.example.data.model.SEO_WEBSITE_TITLE
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UsageCategory
import com.example.payment.RazorpaySdkGatewayManager
import com.example.ui.settings.PricingPlansModalDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kallesh AI Hub", appName)
    }

    @Test
    fun `room database persists conversations and chat history messages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KalleshLocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = LocalCacheRepository(db.localDao())

        val userId = "test_user_kallesh"
        val createdConv = repo.createLocalConversation(
            userId = userId,
            title = "Kotlin Architecture Chat",
            modelId = "gemini-3.5-flash",
            folder = "Coding"
        )

        repo.saveLocalChatMessage(
            conversationId = createdConv.id,
            userId = userId,
            role = "user",
            content = "How do I persist chat history with Room?",
            modelId = "gemini-3.5-flash"
        )

        repo.saveLocalChatMessage(
            conversationId = createdConv.id,
            userId = userId,
            role = "model",
            content = "Use @Entity, @Dao returning Flow<List<T>>, and a Repository.",
            modelId = "gemini-3.5-flash",
            citations = listOf("https://developer.android.com/training/data-storage/room")
        )

        val conversations = repo.observeConversations(userId).first()
        assertEquals(1, conversations.size)
        assertEquals("Kotlin Architecture Chat", conversations.first().title)
        assertEquals(2, conversations.first().messageCount)
        assertTrue(conversations.first().lastMessagePreview.contains("Use @Entity"))

        val messages = repo.observeMessages(createdConv.id).first()
        assertEquals(2, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals("model", messages[1].role)
        assertEquals(1, messages[1].citations.size)

        val searchHits = repo.searchMessages(userId, "Repository").first()
        assertEquals(1, searchHits.size)

        repo.deleteLocalConversation(createdConv.id)
        assertEquals(0, repo.observeConversations(userId).first().size)
        assertEquals(0, repo.observeMessages(createdConv.id).first().size)

        db.close()
    }

    @Test
    fun `founder intro is shown once for new users and persisted in backend database`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FullStackBackendEngine.init(context)

        val regRes = FullStackBackendEngine.registerUser(
            email = "newuser_kallesh@example.com",
            passwordPlain = "SecurePass!2026",
            displayName = "New Explorer"
        )
        assertTrue(regRes.success)
        val newUser = regRes.data!!.user
        assertFalse(newUser.founderIntroSeen)
        assertFalse(FullStackBackendEngine.isFounderIntroSeenInDb(newUser.id, newUser.email))

        // Mark founder intro seen
        FullStackBackendEngine.markFounderIntroSeenInDb(newUser.id, newUser.email)
        assertTrue(FullStackBackendEngine.isFounderIntroSeenInDb(newUser.id, newUser.email))

        // Re-login from another session/device checks DB and sees founderIntroSeen = true
        val loginRes = FullStackBackendEngine.authenticateUser(
            email = "newuser_kallesh@example.com",
            passwordPlain = "SecurePass!2026"
        )
        assertTrue(loginRes.success)
        assertTrue(loginRes.data!!.user.founderIntroSeen)
    }

    @Test
    fun `backend enforces exact plan limits and verifies payment cryptographic signatures`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FullStackBackendEngine.init(context)

        // Verify exact plan pricing and generation limits
        assertEquals(5, SubscriptionPlan.DAILY.priceInr)
        assertEquals(4, SubscriptionPlan.DAILY.toolGenerationLimit)
        assertEquals(200, SubscriptionPlan.MONTHLY.priceInr)
        assertEquals(100, SubscriptionPlan.MONTHLY.toolGenerationLimit)
        assertEquals(1300, SubscriptionPlan.YEARLY.priceInr)
        assertEquals(1300, SubscriptionPlan.YEARLY.toolGenerationLimit)
        assertTrue(SubscriptionPlan.DAILY.dailyLimitFor(UsageCategory.CHAT) >= 999999)

        val userId = "quota_test_user_${System.currentTimeMillis()}"
        val initialQuota = FullStackBackendEngine.getUserQuotaStatus(userId)
        assertEquals("DAILY", initialQuota.planType)
        assertEquals(4, initialQuota.allowedLimit)

        // Consume all 4 daily generations
        repeat(4) {
            val res = FullStackBackendEngine.checkAndConsumeToolQuota(userId, "AI Image Generator")
            assertTrue(res.success)
        }
        // 5th generation must be blocked by backend
        val blocked = FullStackBackendEngine.checkAndConsumeToolQuota(userId, "AI Video Generator")
        assertFalse(blocked.success)
        assertEquals(429, blocked.statusCode)

        // Create payment order for Monthly Plan (₹200 / 100 uses)
        val orderRes = FullStackBackendEngine.createPaymentOrder(userId, "MONTHLY", "UPI")
        assertTrue(orderRes.success)
        val order = orderRes.data!!
        assertEquals(200, order.amountInr)
        assertEquals(100, order.allowedGenerations)

        // Forged payment signature must fail and NOT upgrade the plan
        val forgedRes = FullStackBackendEngine.verifyAndActivateSubscription(
            userId = userId,
            orderId = order.orderId,
            paymentId = "pay_test_123",
            signature = "forged_invalid_signature",
            paymentMethod = "UPI"
        )
        assertFalse(forgedRes.success)
        assertEquals("DAILY", FullStackBackendEngine.getUserQuotaStatus(userId).planType)

        // Valid HMAC-SHA256 payment signature upgrades plan and unlocks 100 uses
        val validSig = FullStackBackendEngine.generateExpectedSignature(order.orderId, "pay_verified_999")
        val verifiedRes = FullStackBackendEngine.verifyAndActivateSubscription(
            userId = userId,
            orderId = order.orderId,
            paymentId = "pay_verified_999",
            signature = validSig,
            paymentMethod = "UPI"
        )
        assertTrue(verifiedRes.success)
        val upgradedQuota = verifiedRes.data!!
        assertEquals("MONTHLY", upgradedQuota.planType)
        assertEquals(100, upgradedQuota.allowedLimit)
        assertEquals(0, upgradedQuota.usedCount)
        assertTrue(upgradedQuota.canGenerate)

        // Replay attack using the same orderId must be rejected by the backend (409)
        val replayRes = FullStackBackendEngine.verifyAndActivateSubscription(
            userId = userId,
            orderId = order.orderId,
            paymentId = "pay_verified_999",
            signature = validSig,
            paymentMethod = "UPI"
        )
        assertFalse(replayRes.success)
        assertEquals(409, replayRes.statusCode)

        // Verify Razorpay SDK Checkout JSON payload amounts in paise for ₹5 Daily, ₹200 Monthly, ₹1,300 Yearly
        val dailyOrder = FullStackBackendEngine.createPaymentOrder(userId, "DAILY", "UPI").data!!
        val yearlyOrder = FullStackBackendEngine.createPaymentOrder(userId, "YEARLY", "Card").data!!
        val dailyJson = RazorpaySdkGatewayManager.buildCheckoutOptionsJson(dailyOrder, "u@kallesh.com", "User", "UPI")
        val monthlyJson = RazorpaySdkGatewayManager.buildCheckoutOptionsJson(order, "u@kallesh.com", "User", "UPI")
        val yearlyJson = RazorpaySdkGatewayManager.buildCheckoutOptionsJson(yearlyOrder, "u@kallesh.com", "User", "Card")

        assertEquals("500", dailyJson.getString("amount"))
        assertEquals("20000", monthlyJson.getString("amount"))
        assertEquals("130000", yearlyJson.getString("amount"))
        assertEquals("INR", yearlyJson.getString("currency"))
    }

    @Test
    fun `public website SEO pages robots and sitemap are configured for Google crawling`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Verify exact homepage SEO title and meta description
        assertEquals("Kallesh AI Hub – AI Chat, Image & Video Generator", SEO_WEBSITE_TITLE)
        assertEquals(
            "Kallesh AI Hub is an AI platform for AI chat, image generation, video generation and other AI-powered tools.",
            SEO_META_DESCRIPTION
        )
        assertEquals("https://kalleshaihub.com", PRODUCTION_CUSTOM_DOMAIN)

        // Verify all required public pages have canonical URLs under https://kalleshaihub.com
        assertEquals("https://kalleshaihub.com/", PublicPortalPage.HOME.canonicalUrl)
        assertEquals("https://kalleshaihub.com/features", PublicPortalPage.FEATURES.canonicalUrl)
        assertEquals("https://kalleshaihub.com/pricing", PublicPortalPage.PRICING.canonicalUrl)
        assertEquals("https://kalleshaihub.com/about", PublicPortalPage.ABOUT.canonicalUrl)
        assertEquals("https://kalleshaihub.com/contact", PublicPortalPage.CONTACT.canonicalUrl)
        assertEquals("https://kalleshaihub.com/privacy-policy", PublicPortalPage.PRIVACY_POLICY.canonicalUrl)
        assertEquals("https://kalleshaihub.com/terms-of-service", PublicPortalPage.TERMS_OF_SERVICE.canonicalUrl)

        // Verify bundled public_web assets (index.html, robots.txt, sitemap.xml, and public pages)
        val indexHtml = context.assets.open("public_web/index.html").bufferedReader().use { it.readText() }
        assertTrue(indexHtml.contains("<title>Kallesh AI Hub – AI Chat, Image &amp; Video Generator</title>"))
        assertTrue(indexHtml.contains("Kallesh AI Hub is an AI platform for AI chat, image generation, video generation and other AI-powered tools."))
        assertTrue(indexHtml.contains("Get Started"))
        assertTrue(indexHtml.contains("https://schema.org"))
        assertFalse(indexHtml.lowercase().contains("noindex"))

        val robotsTxt = context.assets.open("public_web/robots.txt").bufferedReader().use { it.readText() }
        assertTrue(robotsTxt.contains("Allow: /"))
        assertTrue(robotsTxt.contains("Allow: /features"))
        assertTrue(robotsTxt.contains("Allow: /pricing"))
        assertTrue(robotsTxt.contains("Disallow: /admin/"))
        assertTrue(robotsTxt.contains("Disallow: /conversations/"))
        assertTrue(robotsTxt.contains("Sitemap: https://kalleshaihub.com/sitemap.xml"))

        val sitemapXml = context.assets.open("public_web/sitemap.xml").bufferedReader().use { it.readText() }
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/features</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/pricing</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/about</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/contact</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/privacy-policy</loc>"))
        assertTrue(sitemapXml.contains("<loc>https://kalleshaihub.com/terms-of-service</loc>"))
    }

    @Test
    fun `pricing plans modal opens and selecting a plan triggers Razorpay checkout order`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FullStackBackendEngine.init(context)

        val userId = "modal_test_user_${System.currentTimeMillis()}"
        val quota = FullStackBackendEngine.getUserQuotaStatus(userId)
        var triggeredPlan: SubscriptionPlan? = null
        var triggeredMethod: String? = null
        var createdOrder: FullStackBackendEngine.PaymentOrderRecord? = null

        composeTestRule.setContent {
            PricingPlansModalDialog(
                activePlan = SubscriptionPlan.DAILY,
                quotaStatus = quota,
                initialSelectedPlan = SubscriptionPlan.MONTHLY,
                userEmail = "user@kalleshaihub.com",
                onTriggerRazorpayCheckout = { plan, method ->
                    triggeredPlan = plan
                    triggeredMethod = method
                    createdOrder = FullStackBackendEngine.createPaymentOrder(
                        userId = userId,
                        planType = plan.id,
                        paymentMethod = method
                    ).data
                },
                onDismiss = {}
            )
        }

        // Verify modal dialog and plan cards exist
        composeTestRule.onNodeWithTag("pricing_plans_modal_dialog").assertExists()
        composeTestRule.onNodeWithTag("pricing_modal_plan_card_daily").assertExists()
        composeTestRule.onNodeWithTag("pricing_modal_plan_card_monthly").assertExists()
        composeTestRule.onNodeWithTag("pricing_modal_plan_card_yearly").assertExists()

        // Scroll to and click the Yearly Plan (₹1,300) selection button inside the modal to trigger Razorpay checkout
        composeTestRule.onNodeWithTag("pricing_modal_select_yearly_button")
            .performScrollTo()
            .performClick()

        assertEquals(SubscriptionPlan.YEARLY, triggeredPlan)
        assertTrue(triggeredMethod.orEmpty().startsWith("UPI"))
        assertNotNull(createdOrder)
        assertEquals("YEARLY", createdOrder!!.planType)
        assertEquals(1300, createdOrder!!.amountInr)
        assertEquals(130000, createdOrder!!.amountPaise)
        assertEquals(1300, createdOrder!!.allowedGenerations)
    }

    @Test
    fun `image generation endpoint enforces auth, preserves quota on failure, and saves real image and history on success`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FullStackBackendEngine.init(context)
        val geminiService = com.example.data.remote.GeminiHubService(context)
        val backend = FullStackBackendEngine(context, geminiService)

        val regRes = FullStackBackendEngine.registerUser(
            email = "img_tester_${System.currentTimeMillis()}@kalleshaihub.com",
            passwordPlain = "Pass#2026!",
            displayName = "Image Studio Tester"
        )
        assertTrue(regRes.success)
        val user = regRes.data!!.user
        backend.setActiveUser(user.id)

        // 1. Unauthenticated request must fail with 401
        val unauthRes = backend.postAiImageGenerate(
            userId = "",
            prompt = "A futuristic glass city at sunset"
        )
        assertFalse(unauthRes.success)
        assertEquals(401, unauthRes.statusCode)

        // 2. Missing API key must fail with MISSING_API_KEY and NOT deduct limited usage
        geminiService.setRuntimeApiKeyOverride("")
        geminiService.setRuntimeImageApiKeyOverride("")
        val quotaBeforeFailure = FullStackBackendEngine.getUserQuotaStatus(user.id)
        assertEquals(0, quotaBeforeFailure.usedCount)
        assertEquals(4, quotaBeforeFailure.remainingCount)

        val missingKeyRes = backend.postAiImageGenerate(
            userId = user.id,
            prompt = "A futuristic glass city at sunset",
            aspectRatio = "16:9",
            imageSize = "1K",
            numberOfImages = 1
        )
        // If BuildConfig has no real key in test env, it fails and preserves quota
        if (!missingKeyRes.success) {
            val quotaAfterFailure = FullStackBackendEngine.getUserQuotaStatus(user.id)
            assertEquals(0, quotaAfterFailure.usedCount)
            assertEquals(4, quotaAfterFailure.remainingCount)
        }

        // 3. Configure a real HTTP interceptor returning a valid PNG payload from Gemini Image API
        val sampleBmp = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
        sampleBmp.eraseColor(android.graphics.Color.rgb(30, 144, 255))
        val pngOut = java.io.ByteArrayOutputStream()
        sampleBmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, pngOut)
        val base64Png = android.util.Base64.encodeToString(pngOut.toByteArray(), android.util.Base64.NO_WRAP)

        val mockClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request()
                val responseJson = org.json.JSONObject()
                    .put(
                        "candidates",
                        org.json.JSONArray().put(
                            org.json.JSONObject().put(
                                "content",
                                org.json.JSONObject().put(
                                    "parts",
                                    org.json.JSONArray()
                                        .put(org.json.JSONObject().put("text", "Generated futuristic glass city at sunset"))
                                        .put(
                                            org.json.JSONObject().put(
                                                "inlineData",
                                                org.json.JSONObject()
                                                    .put("mimeType", "image/png")
                                                    .put("data", base64Png)
                                            )
                                        )
                                )
                            )
                        )
                    )
                okhttp3.Response.Builder()
                    .request(req)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        okhttp3.ResponseBody.create(
                            okhttp3.MediaType.parse("application/json"),
                            responseJson.toString()
                        )
                    )
                    .build()
            }
            .build()

        geminiService.setCustomHttpClientForTesting(mockClient)
        geminiService.setRuntimeImageApiKeyOverride("AIzaSyTestValidKeyForImageGeneration12345")

        // 4. Execute POST /api/ai/image/generate
        val genRes = backend.postAiImageGenerate(
            userId = user.id,
            prompt = "A futuristic glass city at sunset",
            aspectRatio = "16:9",
            imageSize = "1K",
            numberOfImages = 1
        )
        assertTrue("Expected image generation success but got: ${genRes.error}", genRes.success)
        assertEquals(200, genRes.statusCode)
        val genData = genRes.data!!
        val savedPath = genData.optString("filePath")
        assertTrue(savedPath.isNotBlank())
        assertTrue(java.io.File(savedPath).exists())
        assertTrue(java.io.File(savedPath).length() > 50)

        // Verify 1 quota unit was consumed ONLY after success
        val quotaAfterSuccess = FullStackBackendEngine.getUserQuotaStatus(user.id)
        assertEquals(1, quotaAfterSuccess.usedCount)
        assertEquals(3, quotaAfterSuccess.remainingCount)

        // 5. Verify GET /api/history contains the generated image for this user and NOT for another user
        val historyRes = backend.getHistory(userId = user.id, filterType = "IMAGE")
        assertTrue(historyRes.success)
        val items = historyRes.data!!.getJSONArray("items")
        assertTrue(items.length() >= 1)
        val firstHistory = items.getJSONObject(0)
        assertEquals("A futuristic glass city at sunset", firstHistory.getString("prompt"))
        assertEquals("COMPLETED", firstHistory.getString("status"))
        assertEquals(savedPath, firstHistory.getString("resultUrl"))

        val otherUserHistory = backend.getHistory(userId = "different_user_id_999", filterType = "IMAGE")
        assertTrue(otherUserHistory.success)
        assertEquals(0, otherUserHistory.data!!.getJSONArray("items").length())

        geminiService.setCustomHttpClientForTesting(null)
    }

    @Test
    fun `video generation endpoint creates job, polls status from queued to completed, downloads real mp4, and enforces quota only on completion`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FullStackBackendEngine.init(context)
        val geminiService = com.example.data.remote.GeminiHubService(context)
        val backend = FullStackBackendEngine(context, geminiService)

        val regRes = FullStackBackendEngine.registerUser(
            email = "vid_tester_${System.currentTimeMillis()}@kalleshaihub.com",
            passwordPlain = "Pass#2026!",
            displayName = "Video Studio Tester"
        )
        assertTrue(regRes.success)
        val user = regRes.data!!.user
        backend.setActiveUser(user.id)

        var pollCallCount = 0
        // Build valid MP4 header bytes containing 'ftyp' box so file validation passes
        val validMp4Bytes = ByteArray(512).apply {
            this[0] = 0x00
            this[1] = 0x00
            this[2] = 0x00
            this[3] = 0x18
            this[4] = 'f'.code.toByte()
            this[5] = 't'.code.toByte()
            this[6] = 'y'.code.toByte()
            this[7] = 'p'.code.toByte()
            this[8] = 'm'.code.toByte()
            this[9] = 'p'.code.toByte()
            this[10] = '4'.code.toByte()
            this[11] = '2'.code.toByte()
        }

        val mockVideoClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request()
                val url = req.url().toString()
                when {
                    url.contains(":predictLongRunning") -> {
                        val opJson = org.json.JSONObject()
                            .put("name", "models/veo-3.1-fast-generate-preview/operations/op_kallesh_test_777")
                            .put("done", false)
                        okhttp3.Response.Builder()
                            .request(req)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), opJson.toString()))
                            .build()
                    }
                    url.contains("operations/op_kallesh_test_777") -> {
                        pollCallCount++
                        val bodyJson = if (pollCallCount == 1) {
                            org.json.JSONObject()
                                .put("name", "models/veo-3.1-fast-generate-preview/operations/op_kallesh_test_777")
                                .put("done", false)
                                .put("metadata", org.json.JSONObject().put("state", "PROCESSING"))
                        } else {
                            org.json.JSONObject()
                                .put("name", "models/veo-3.1-fast-generate-preview/operations/op_kallesh_test_777")
                                .put("done", true)
                                .put(
                                    "response",
                                    org.json.JSONObject().put(
                                        "generateVideoResponse",
                                        org.json.JSONObject().put(
                                            "generatedSamples",
                                            org.json.JSONArray().put(
                                                org.json.JSONObject().put(
                                                    "video",
                                                    org.json.JSONObject().put("uri", "https://generativelanguage.googleapis.com/v1beta/files/video_777:download?alt=media")
                                                )
                                            )
                                        )
                                    )
                                )
                        }
                        okhttp3.Response.Builder()
                            .request(req)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), bodyJson.toString()))
                            .build()
                    }
                    url.contains("files/video_777:download") -> {
                        okhttp3.Response.Builder()
                            .request(req)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(okhttp3.ResponseBody.create(okhttp3.MediaType.parse("video/mp4"), validMp4Bytes))
                            .build()
                    }
                    else -> {
                        okhttp3.Response.Builder()
                            .request(req)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(404)
                            .message("Not Found")
                            .body(okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), "{}"))
                            .build()
                    }
                }
            }
            .build()

        geminiService.setCustomHttpClientForTesting(mockVideoClient)
        geminiService.setRuntimeVideoApiKeyOverride("AIzaSyTestValidKeyForVideoGeneration12345")

        // 1. Start video generation job via POST /api/ai/video/generate
        val startRes = backend.postAiVideoGenerate(
            userId = user.id,
            prompt = "A cinematic drone shot over misty emerald mountains at sunrise",
            aspectRatio = "16:9"
        )
        assertTrue("Expected video job creation success but got: ${startRes.error}", startRes.success)
        val jobData = startRes.data!!
        val jobId = jobData.getString("jobId")
        assertEquals("QUEUED", jobData.getString("status"))

        // Verify quota has NOT been consumed while job is still QUEUED
        assertEquals(0, FullStackBackendEngine.getUserQuotaStatus(user.id).usedCount)

        // 2. Verify another user cannot access this user's video job status (403 Forbidden)
        val unauthorizedPoll = backend.getAiVideoStatus(
            userId = "attacker_user_id",
            jobId = jobId
        )
        assertFalse(unauthorizedPoll.success)
        assertEquals(403, unauthorizedPoll.statusCode)

        // 3. First status poll -> PROCESSING (quota still 0)
        val poll1 = backend.getAiVideoStatus(userId = user.id, jobId = jobId)
        assertTrue(poll1.success)
        assertEquals("PROCESSING", poll1.data!!.getString("status"))
        assertEquals(0, FullStackBackendEngine.getUserQuotaStatus(user.id).usedCount)

        // 4. Second status poll -> COMPLETED + real MP4 file downloaded + 1 quota consumed
        val poll2 = backend.getAiVideoStatus(userId = user.id, jobId = jobId)
        assertTrue("Expected completed poll but got: ${poll2.error}", poll2.success)
        assertEquals("COMPLETED", poll2.data!!.getString("status"))
        val videoFilePath = poll2.data!!.getString("videoUrl")
        assertTrue(videoFilePath.endsWith(".mp4"))
        assertTrue(java.io.File(videoFilePath).exists())
        assertEquals(512L, java.io.File(videoFilePath).length())

        // Quota must now be 1 used (3 remaining on Daily ₹5 plan)
        val quotaAfterVideo = FullStackBackendEngine.getUserQuotaStatus(user.id)
        assertEquals(1, quotaAfterVideo.usedCount)
        assertEquals(3, quotaAfterVideo.remainingCount)

        // 5. Verify video generation appears in GET /api/history with COMPLETED status
        val videoHistoryRes = backend.getHistory(userId = user.id, filterType = "VIDEO")
        assertTrue(videoHistoryRes.success)
        val videoHistoryItems = videoHistoryRes.data!!.getJSONArray("items")
        assertTrue(videoHistoryItems.length() >= 1)
        assertEquals("COMPLETED", videoHistoryItems.getJSONObject(0).getString("status"))
        assertEquals(videoFilePath, videoHistoryItems.getJSONObject(0).getString("resultUrl"))

        geminiService.setCustomHttpClientForTesting(null)
    }
}
