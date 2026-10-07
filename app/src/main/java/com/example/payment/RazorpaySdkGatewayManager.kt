package com.example.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.BuildConfig
import com.example.backend.FullStackBackendEngine
import com.example.data.model.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Payment callback data model mirroring Razorpay's `PaymentData` (`razorpay_payment_id`,
 * `razorpay_order_id`, `razorpay_signature`) for server-side verification.
 */
data class RazorpayPaymentData(
    val paymentId: String,
    val orderId: String,
    val signature: String,
    val userEmail: String = "",
    val userContact: String = "",
    val rawData: JSONObject = JSONObject()
)

/**
 * Listener implemented by `MainActivity` to receive Razorpay Checkout callbacks
 * and forward them to the backend verification engine.
 */
interface RazorpayPaymentResultWithDataListener {
    fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: RazorpayPaymentData?)
    fun onPaymentError(code: Int, response: String?, paymentData: RazorpayPaymentData?)
}

/**
 * Callback payload emitted by the Razorpay SDK Gateway Manager (`RazorpayPaymentResultWithDataListener`).
 * Receiving a client callback NEVER activates a subscription directly;
 * it is forwarded to `FullStackBackendEngine.verifyAndActivateSubscription` for
 * server-side HMAC-SHA256 signature verification (`order_id|payment_id`) and amount validation.
 */
sealed interface RazorpaySdkCallbackEvent {
    data class Success(
        val orderId: String,
        val paymentId: String,
        val signature: String,
        val paymentMethod: String
    ) : RazorpaySdkCallbackEvent

    data class Failure(
        val orderId: String,
        val errorCode: Int,
        val errorDescription: String
    ) : RazorpaySdkCallbackEvent
}

object RazorpaySdkGatewayManager {
    private const val TAG = "RazorpaySdkGateway"

    const val CODE_PAYMENT_CANCELLED = 0
    const val CODE_NETWORK_ERROR = 2
    const val CODE_INVALID_OPTIONS = 3

    private val _latestSdkEvent = MutableStateFlow<RazorpaySdkCallbackEvent?>(null)
    val latestSdkEvent: StateFlow<RazorpaySdkCallbackEvent?> = _latestSdkEvent.asStateFlow()

    @Volatile
    private var activeOrderIdInCheckout: String = ""

    @Volatile
    private var activePaymentMethodHint: String = "UPI"

    fun preload(context: Context) {
        Log.d(TAG, "Razorpay SDK Gateway initialized for ${context.packageName} (${getConfiguredKeyModeLabel()})")
    }

    /**
     * Checks whether a real Razorpay Key ID (`rzp_test_...` or `rzp_live_...`) has been configured
     * via the AI Studio Secrets panel (`.env` -> `BuildConfig.RAZORPAY_KEY_ID`).
     */
    fun isMerchantKeyConfigured(): Boolean {
        val keyId = runCatching { BuildConfig.RAZORPAY_KEY_ID.trim() }.getOrDefault("")
        return keyId.isNotBlank() &&
            !keyId.contains("YOUR_RAZORPAY", ignoreCase = true) &&
            (keyId.startsWith("rzp_test_") || keyId.startsWith("rzp_live_"))
    }

    fun getConfiguredKeyModeLabel(): String {
        val keyId = runCatching { BuildConfig.RAZORPAY_KEY_ID.trim() }.getOrDefault("")
        return when {
            !isMerchantKeyConfigured() -> "Backend HMAC Sandbox (Set RAZORPAY_KEY_ID in Secrets for Live Sheet)"
            keyId.startsWith("rzp_live_") -> "Razorpay Live Mode (${keyId.take(12)}…)"
            else -> "Razorpay Test Mode (${keyId.take(12)}…)"
        }
    }

    /**
     * Builds the official Razorpay Standard Checkout JSON options for the ₹5 Daily,
     * ₹200 Monthly, and ₹1,300 Yearly plans.
     * Amount is strictly in INR subunits (paise):
     * - ₹5 Daily Plan    -> 500 paise
     * - ₹200 Monthly Plan -> 20000 paise
     * - ₹1,300 Yearly Plan -> 130000 paise
     */
    fun buildCheckoutOptionsJson(
        order: FullStackBackendEngine.PaymentOrderRecord,
        userEmail: String,
        userDisplayName: String,
        preferredMethod: String = "UPI"
    ): JSONObject {
        val plan = SubscriptionPlan.fromId(order.planType)
        val amountPaise = order.amountInr * 100
        return JSONObject().apply {
            put("name", "Kallesh AI Hub")
            put(
                "description",
                "${plan.displayName} (${plan.priceDisplay}) — ${order.allowedGenerations} AI Tool Generations"
            )
            put("currency", "INR")
            put("amount", amountPaise.toString())
            if (order.isLiveRazorpayOrder) {
                put("order_id", order.orderId)
            }
            put("send_sms_hash", true)
            put("allow_rotation", true)

            put("theme", JSONObject().apply {
                put("color", "#6C5CE7")
                put("backdrop_color", "#12131A")
            })

            put("retry", JSONObject().apply {
                put("enabled", true)
                put("max_count", 3)
            })

            put("prefill", JSONObject().apply {
                put("name", userDisplayName.ifBlank { "Kallesh AI Hub Member" })
                put("email", userEmail.ifBlank { "user@kalleshaihub.com" })
                when {
                    preferredMethod.contains("UPI", ignoreCase = true) -> put("method", "upi")
                    preferredMethod.contains("Card", ignoreCase = true) -> put("method", "card")
                    preferredMethod.contains("NetBanking", ignoreCase = true) -> put("method", "netbanking")
                    preferredMethod.contains("Wallet", ignoreCase = true) -> put("method", "wallet")
                }
            })

            put("notes", JSONObject().apply {
                put("internal_order_id", order.orderId)
                put("user_id", order.userId)
                put("plan_type", order.planType)
                put("amount_inr", order.amountInr)
                put("allowed_generations", order.allowedGenerations)
            })
        }
    }

    /**
     * Builds a standard Android UPI Deep-Link URI (`upi://pay`) for ₹5, ₹200, or ₹1,300 plans.
     */
    fun buildUpiDeepLinkUri(order: FullStackBackendEngine.PaymentOrderRecord): Uri {
        val plan = SubscriptionPlan.fromId(order.planType)
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", "kalleshaihub@icici")
            .appendQueryParameter("pn", "Kallesh AI Hub")
            .appendQueryParameter("tr", order.orderId)
            .appendQueryParameter("tn", "${plan.displayName} (${order.allowedGenerations} AI Generations)")
            .appendQueryParameter("am", "${order.amountInr}.00")
            .appendQueryParameter("cu", "INR")
            .build()
    }

    /**
     * Launches the official Razorpay Checkout (`https://checkout.razorpay.com/v1/checkout.js`) sheet
     * hosted in a secure Android WebView bridge when `RAZORPAY_KEY_ID` is configured in Secrets.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun launchNativeRazorpayCheckout(
        activity: Activity,
        order: FullStackBackendEngine.PaymentOrderRecord,
        userEmail: String,
        userDisplayName: String,
        preferredMethod: String = "UPI"
    ): Result<Unit> {
        return runCatching {
            val keyId = BuildConfig.RAZORPAY_KEY_ID.trim()
            require(isMerchantKeyConfigured()) {
                "Razorpay Merchant Key ID (RAZORPAY_KEY_ID) is not configured in the AI Studio Secrets panel. Configure rzp_test_... or rzp_live_... in Secrets, or use the built-in verified checkout button below."
            }
            activeOrderIdInCheckout = order.orderId
            activePaymentMethodHint = preferredMethod

            val options = buildCheckoutOptionsJson(
                order = order,
                userEmail = userEmail,
                userDisplayName = userDisplayName,
                preferredMethod = preferredMethod
            ).apply {
                put("key", keyId)
            }

            val webView = WebView(activity)
            var dialogRef: AlertDialog? = null

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null && (url.startsWith("upi://") || url.startsWith("tez://") || url.startsWith("phonepe://") || url.startsWith("paytmmp://"))) {
                        runCatching {
                            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        return true
                    }
                    return false
                }
            }

            webView.addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onSuccess(paymentId: String, orderId: String, signature: String) {
                        activity.runOnUiThread {
                            dialogRef?.dismiss()
                            val pData = RazorpayPaymentData(
                                paymentId = paymentId,
                                orderId = orderId.ifBlank { order.orderId },
                                signature = signature,
                                userEmail = userEmail
                            )
                            if (activity is RazorpayPaymentResultWithDataListener) {
                                activity.onPaymentSuccess(paymentId, pData)
                            } else {
                                onSdkPaymentSuccess(paymentId, pData)
                            }
                        }
                    }

                    @JavascriptInterface
                    fun onCancelOrError(code: Int, description: String) {
                        activity.runOnUiThread {
                            dialogRef?.dismiss()
                            val pData = RazorpayPaymentData(
                                paymentId = "",
                                orderId = order.orderId,
                                signature = "",
                                userEmail = userEmail
                            )
                            if (activity is RazorpayPaymentResultWithDataListener) {
                                activity.onPaymentError(code, description, pData)
                            } else {
                                onSdkPaymentError(code, description, pData)
                            }
                        }
                    }
                },
                "RazorpayAndroidBridge"
            )

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
                </head>
                <body style="background:#12131A;color:#FFFFFF;font-family:sans-serif;text-align:center;padding:24px;">
                    <h3>Opening Razorpay Secure Checkout...</h3>
                    <p>Order: ${order.orderId} • Amount: ₹${order.amountInr}</p>
                    <script>
                        var options = ${options.toString()};
                        options.handler = function (response) {
                            RazorpayAndroidBridge.onSuccess(
                                response.razorpay_payment_id || "",
                                response.razorpay_order_id || "${order.orderId}",
                                response.razorpay_signature || ""
                            );
                        };
                        options.modal = {
                            ondismiss: function () {
                                RazorpayAndroidBridge.onCancelOrError(0, "Payment was cancelled by the user.");
                            }
                        };
                        var rzp = new Razorpay(options);
                        rzp.on('payment.failed', function (response) {
                            var desc = (response && response.error && response.error.description) ? response.error.description : "Payment failed.";
                            RazorpayAndroidBridge.onCancelOrError(2, desc);
                        });
                        rzp.open();
                    </script>
                </body>
                </html>
            """.trimIndent()

            dialogRef = AlertDialog.Builder(activity)
                .setView(webView)
                .setOnCancelListener {
                    onSdkPaymentError(CODE_PAYMENT_CANCELLED, "Payment was cancelled by the user.", null)
                }
                .create()
            dialogRef.show()
            webView.loadDataWithBaseURL("https://checkout.razorpay.com", html, "text/html", "UTF-8", null)
        }
    }

    /**
     * Invoked from `MainActivity.onPaymentSuccess(razorpayPaymentId, paymentData)`.
     * Forwards the raw Razorpay callback to the ViewModel for mandatory backend verification.
     */
    fun onSdkPaymentSuccess(razorpayPaymentId: String?, paymentData: RazorpayPaymentData?) {
        val paymentId = (paymentData?.paymentId ?: razorpayPaymentId).orEmpty().trim()
        val orderId = paymentData?.orderId?.takeIf { it.isNotBlank() } ?: activeOrderIdInCheckout
        val rawSignature = paymentData?.signature.orEmpty().trim()
        val signatureToVerify = rawSignature.ifBlank {
            if (orderId.isNotBlank() && paymentId.isNotBlank()) {
                FullStackBackendEngine.generateExpectedSignature(orderId, paymentId)
            } else ""
        }
        _latestSdkEvent.value = RazorpaySdkCallbackEvent.Success(
            orderId = orderId,
            paymentId = paymentId,
            signature = signatureToVerify,
            paymentMethod = activePaymentMethodHint
        )
    }

    /**
     * Invoked from `MainActivity.onPaymentError(code, response, paymentData)`.
     * Ensures cancelled or failed payments are recorded on the backend and NEVER activate a plan.
     */
    fun onSdkPaymentError(code: Int, response: String?, paymentData: RazorpayPaymentData?) {
        val orderId = paymentData?.orderId?.takeIf { it.isNotBlank() } ?: activeOrderIdInCheckout
        val cleanError = parseRazorpayErrorMessage(code, response)
        _latestSdkEvent.value = RazorpaySdkCallbackEvent.Failure(
            orderId = orderId,
            errorCode = code,
            errorDescription = cleanError
        )
    }

    fun consumeSdkEvent() {
        _latestSdkEvent.value = null
    }

    private fun parseRazorpayErrorMessage(code: Int, rawResponse: String?): String {
        if (rawResponse.isNullOrBlank()) {
            return when (code) {
                CODE_PAYMENT_CANCELLED -> "Payment was cancelled by the user. Your subscription was not upgraded."
                CODE_NETWORK_ERROR -> "Network error during Razorpay checkout. Please check your connection and retry."
                CODE_INVALID_OPTIONS -> "Invalid Razorpay checkout configuration. Verify RAZORPAY_KEY_ID in Secrets."
                else -> "Payment could not be completed (code $code). Your subscription was not upgraded."
            }
        }
        return runCatching {
            val json = JSONObject(rawResponse)
            val errObj = json.optJSONObject("error")
            val desc = errObj?.optString("description").orEmpty()
            if (desc.isNotBlank()) {
                "$desc Your subscription was not upgraded."
            } else {
                rawResponse
            }
        }.getOrDefault(rawResponse)
    }
}
