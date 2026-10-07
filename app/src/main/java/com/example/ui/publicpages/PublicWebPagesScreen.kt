package com.example.ui.publicpages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BillingCycle
import com.example.data.model.PRODUCTION_API_ENDPOINTS
import com.example.data.model.PRODUCTION_CUSTOM_DOMAIN
import com.example.data.model.PublicPortalPage
import com.example.data.model.SEO_META_DESCRIPTION
import com.example.data.model.SEO_WEBSITE_TITLE
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UsageCategory
import com.example.data.model.UsagePeriod
import com.example.data.model.UserProfile
import com.example.ui.home.copyToClipboard
import com.example.ui.viewmodel.CreativeStudioTab
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import com.example.ui.viewmodel.ProductivityToolTab

private fun PublicPortalPage.icon(): ImageVector = when (this) {
    PublicPortalPage.HOME -> Icons.Default.Home
    PublicPortalPage.AI_CHAT -> Icons.AutoMirrored.Filled.Chat
    PublicPortalPage.AI_IMAGE_GENERATOR -> Icons.Default.Image
    PublicPortalPage.AI_VIDEO_GENERATOR -> Icons.Default.Movie
    PublicPortalPage.FEATURES -> Icons.Default.AutoAwesome
    PublicPortalPage.PRICING -> Icons.Default.WorkspacePremium
    PublicPortalPage.ABOUT -> Icons.Default.Info
    PublicPortalPage.CONTACT -> Icons.Default.Email
    PublicPortalPage.PRIVACY_POLICY -> Icons.Default.PrivacyTip
    PublicPortalPage.TERMS_OF_SERVICE -> Icons.Default.Gavel
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicWebPagesScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val context = LocalContext.current
    val selectedPage by viewModel.selectedPublicPage.collectAsStateWithLifecycle()
    val selectedBillingCycle by viewModel.selectedBillingCycle.collectAsStateWithLifecycle()
    val selectedUsagePeriod by viewModel.selectedUsagePeriod.collectAsStateWithLifecycle()
    val platformFeatures by viewModel.platformFeatures.collectAsStateWithLifecycle()
    var showSeoSourceInspector by remember { mutableStateOf(false) }

    val activePlan = userProfile?.subscriptionPlan ?: SubscriptionPlan.FREE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("public_web_pages_screen")
    ) {
        // Top Public Website Navigation Bar (All 10 Public Pages + Open Chat CTA)
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = PRODUCTION_CUSTOM_DOMAIN,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { showSeoSourceInspector = !showSeoSourceInspector },
                            modifier = Modifier.testTag("toggle_seo_inspector_button")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showSeoSourceInspector) "Hide SEO Tags" else "SEO & Sitemap")
                        }
                        Button(
                            onClick = { viewModel.openChatOrActiveConversation() },
                            modifier = Modifier.testTag("public_nav_open_chat_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Chat")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublicPortalPage.entries.forEach { page ->
                        FilterChip(
                            selected = selectedPage == page,
                            onClick = { viewModel.openPublicPortalPage(page, openInPortalViewer = true) },
                            leadingIcon = {
                                Icon(page.icon(), contentDescription = null, modifier = Modifier.size(15.dp))
                            },
                            label = { Text(page.navLabel) },
                            modifier = Modifier.testTag("public_page_tab_${page.name.lowercase()}")
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Semantic Heading + Canonical URL Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0A1124), Color(0xFF152244), Color(0xFF1E1B4B))
                                )
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF6366F1))),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "CANONICAL: ${selectedPage.canonicalUrl}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        copyToClipboard(context, selectedPage.canonicalUrl)
                                        viewModel.showStatus("Copied canonical URL: ${selectedPage.canonicalUrl}")
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy URL", color = Color(0xFF00E5FF), style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Semantic H1 Heading
                            Text(
                                text = selectedPage.h1Heading,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )

                            // Meta Description
                            Text(
                                text = selectedPage.metaDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openChatOrActiveConversation() },
                                    modifier = Modifier.testTag("page_header_open_chat_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open AI Chat")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.openCreativeStudio(CreativeStudioTab.IMAGE) }
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Image Studio", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.openCreativeStudio(CreativeStudioTab.VIDEO) }
                                ) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Video Studio", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Collapsible SEO Metadata, OpenGraph, Structured Data, robots.txt & sitemap.xml Inspector
            item {
                AnimatedVisibility(visible = showSeoSourceInspector) {
                    SeoAndDeploymentMetadataCard(
                        page = selectedPage,
                        onCopyText = { label, value ->
                            copyToClipboard(context, value)
                            viewModel.showStatus("Copied $label to clipboard.")
                        }
                    )
                }
            }

            // Page-Specific Content
            item {
                when (selectedPage) {
                    PublicPortalPage.HOME -> PublicHomePageContent(viewModel)
                    PublicPortalPage.AI_CHAT -> PublicAiChatPageContent(viewModel)
                    PublicPortalPage.AI_IMAGE_GENERATOR -> PublicAiImageGeneratorPageContent(viewModel)
                    PublicPortalPage.AI_VIDEO_GENERATOR -> PublicAiVideoGeneratorPageContent(viewModel)
                    PublicPortalPage.FEATURES -> PublicFeaturesPageContent(viewModel, platformFeatures.size)
                    PublicPortalPage.PRICING -> PublicPricingPageContent(
                        viewModel = viewModel,
                        activePlan = activePlan,
                        billingCycle = selectedBillingCycle,
                        usagePeriod = selectedUsagePeriod
                    )
                    PublicPortalPage.ABOUT -> PublicAboutPageContent(viewModel)
                    PublicPortalPage.CONTACT -> PublicContactPageContent(viewModel, userProfile)
                    PublicPortalPage.PRIVACY_POLICY -> PublicPrivacyPolicyPageContent()
                    PublicPortalPage.TERMS_OF_SERVICE -> PublicTermsOfServicePageContent()
                }
            }

            // Production API Endpoints & Backend Architecture Reference
            item {
                ProductionApiEndpointsCard()
            }

            // Footer Navigation with All 10 Public Pages
            item {
                PublicFooterNavigationCard(
                    onSelectPage = { page -> viewModel.openPublicPortalPage(page, openInPortalViewer = true) },
                    onOpenChat = { viewModel.openChatOrActiveConversation() }
                )
            }
        }
    }
}

@Composable
private fun PublicHomePageContent(viewModel: KalleshHubViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "What is Kallesh AI Hub?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kallesh AI Hub is an all-in-one AI platform for AI chat, AI image generation, AI video generation, voice synthesis, music creation, code generation, and document intelligence. Built on Google Gemini 3, Veo 3, Lyria 3, Cloud Firestore, and local Room persistence, Kallesh AI Hub works seamlessly across desktop, tablet, and mobile devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Core AI Platform Capabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("• Multi-Model AI Chat with live Google Search & Google Maps grounding and persistent conversation history.", style = MaterialTheme.typography.bodySmall)
                Text("• AI Image Generator & Photo Editor supporting 1:1, 16:9, 9:16, 4:3, and 3:4 aspect ratios up to 4K resolution.", style = MaterialTheme.typography.bodySmall)
                Text("• AI Video Generator powered by Veo 3 for cinematic 16:9 landscape and 9:16 vertical text-to-video and photo animation.", style = MaterialTheme.typography.bodySmall)
                Text("• 28+ Specialized AI Tools including Lyria 3 Music Lab, Gemini Live Voice, Kallesh Code AI, Document & PDF Analyzer, and Study AI.", style = MaterialTheme.typography.bodySmall)
                Text("• Usage Plans & How to Get Started: Authenticate to access Unlimited AI Chat and choose Daily Plan (₹5/day • 4 AI tool generations), Monthly Plan (₹200/month • 100 AI tool generations), or Yearly Plan (₹1,300/year • 1,300 AI tool generations).", style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startNewChat() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("public_home_get_started_button")
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Started")
                    }
                    Button(
                        onClick = { viewModel.openChatOrActiveConversation() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open AI Chat")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.openCreativeStudio(CreativeStudioTab.IMAGE) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Image")
                    }
                    OutlinedButton(
                        onClick = { viewModel.openCreativeStudio(CreativeStudioTab.VIDEO) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Video")
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicAiChatPageContent(viewModel: KalleshHubViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Multi-Model AI Chat & Research Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Converse with Gemini 3 Flash, Gemini 3.1 Pro, and Vision AI models with real-time streaming responses, Markdown code blocks, file/image uploads, voice transcription, and verified citations from Google Search and Google Maps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("• Automatic chat history persistence in Cloud Firestore & Local Room Database v3", style = MaterialTheme.typography.bodySmall)
            Text("• Start a New Chat anytime, organize chats into folders, pin important threads, or export to Markdown", style = MaterialTheme.typography.bodySmall)
            Text("• Built-in Read Aloud (TTS), message editing, regeneration, and response rating", style = MaterialTheme.typography.bodySmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.openChatOrActiveConversation() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("public_chat_page_open_chat_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Chat Now")
                }
                OutlinedButton(
                    onClick = { viewModel.startNewChat() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("public_chat_page_new_chat_button")
                ) {
                    Text("Start New Chat")
                }
            }
        }
    }
}

@Composable
private fun PublicAiImageGeneratorPageContent(viewModel: KalleshHubViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("AI Image Generator & Photo Editing Studio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Create photorealistic images, futuristic neon artwork, 3D glass-metal renders, digital illustrations, and natural-language photo edits using Gemini 3.1 Flash Image and Gemini 2.5 Flash Image.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("• Aspect ratios: 1:1 Square, 16:9 Landscape, 9:16 Vertical, 4:3, and 3:4", style = MaterialTheme.typography.bodySmall)
            Text("• Resolution presets from 512px up to 4K with reference image upload for AI photo editing", style = MaterialTheme.typography.bodySmall)
            Text("• Automatically saves generated artwork metadata to your Personal AI Workspace", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = { viewModel.openCreativeStudio(CreativeStudioTab.IMAGE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("public_image_page_launch_button")
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch AI Image Generator")
            }
        }
    }
}

@Composable
private fun PublicAiVideoGeneratorPageContent(viewModel: KalleshHubViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Veo 3 AI Video Generator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Generate cinematic 16:9 landscape and 9:16 vertical videos from text prompts or animate uploaded reference photos using Google Veo 3 (veo-3.1-fast-generate-preview).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("• Supports both Text-to-Video and Photo-to-Video animation workflows", style = MaterialTheme.typography.bodySmall)
            Text("• Asynchronous job queue with real-time status polling and HD keyframe storyboard previews", style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = { viewModel.openCreativeStudio(CreativeStudioTab.VIDEO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("public_video_page_launch_button")
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch AI Video Generator")
            }
        }
    }
}

@Composable
private fun PublicFeaturesPageContent(viewModel: KalleshHubViewModel, totalFeatures: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("$totalFeatures+ Modular AI Features in One Unified Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Kallesh AI Hub combines conversational AI, creative studios, developer tools, and document intelligence with automatic Primary → Secondary model fallback routing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("• Conversational AI: Multi-Model Chat, Web Research Agent, Prompt Improver, Multi-Model Compare", style = MaterialTheme.typography.bodySmall)
            Text("• Creative Studios: AI Image Generator, Veo 3 Video Generator, Lyria 3 Music Lab, Live Voice & TTS", style = MaterialTheme.typography.bodySmall)
            Text("• Productivity & Code: Kallesh Code AI (13 languages), PDF & Document Analyzer, Study Flashcards & Quizzes, Translator", style = MaterialTheme.typography.bodySmall)
            Text("• Enterprise Platform: Dynamic Feature Registry, Semantic Versioning, Staged Rollouts, One-Click Rollback & Quality Gate", style = MaterialTheme.typography.bodySmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.openProductivityTool(ProductivityToolTab.HUB) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Explore All $totalFeatures+ AI Tools")
                }
                OutlinedButton(
                    onClick = { viewModel.openChatOrActiveConversation() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Open AI Chat")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicPricingPageContent(
    viewModel: KalleshHubViewModel,
    activePlan: SubscriptionPlan,
    billingCycle: BillingCycle,
    usagePeriod: UsagePeriod
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Billing Cycle & Quota Period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BillingCycle.entries.forEach { cycle ->
                        FilterChip(
                            selected = billingCycle == cycle,
                            onClick = { viewModel.selectBillingCycle(cycle) },
                            label = { Text("${cycle.label} (${cycle.discountBadge})") }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UsagePeriod.entries.forEach { period ->
                        FilterChip(
                            selected = usagePeriod == period,
                            onClick = { viewModel.selectUsagePeriod(period) },
                            label = { Text("${period.label} Limits") }
                        )
                    }
                }
                Button(
                    onClick = { viewModel.openPricingPlansModal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("public_pricing_open_modal_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Pricing Plans & Razorpay Checkout Modal")
                }
            }
        }

        SubscriptionPlan.entries.forEach { plan ->
            val isCurrent = plan == activePlan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = plan.priceForBillingCycle(billingCycle),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isCurrent) {
                            AssistChip(
                                onClick = {},
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text("Current Plan") }
                            )
                        }
                    }

                    val periodSuffix = usagePeriod.label.lowercase()
                    Text("• ${plan.limitForPeriod(UsageCategory.CHAT, usagePeriod)} AI Chat Messages / $periodSuffix", style = MaterialTheme.typography.bodySmall)
                    Text("• ${plan.limitForPeriod(UsageCategory.IMAGE, usagePeriod)} AI Image Generations / $periodSuffix", style = MaterialTheme.typography.bodySmall)
                    Text("• ${plan.limitForPeriod(UsageCategory.VIDEO, usagePeriod)} Veo 3 Video Generations / $periodSuffix", style = MaterialTheme.typography.bodySmall)
                    Text("• ${plan.limitForPeriod(UsageCategory.FILE, usagePeriod)} Document & PDF Analyses / $periodSuffix (up to ${plan.maxFileSizeMb}MB)", style = MaterialTheme.typography.bodySmall)
                    Text("• ${plan.limitForPeriod(UsageCategory.VOICE, usagePeriod)} Live Voice & TTS Turns / $periodSuffix", style = MaterialTheme.typography.bodySmall)
                    Text("• ${plan.limitForPeriod(UsageCategory.MUSIC, usagePeriod)} Lyria 3 Music Tracks / $periodSuffix", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.selectSubscriptionPlan(plan) },
                        enabled = !isCurrent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isCurrent) "Active Plan" else "Upgrade to ${plan.displayName}")
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicAboutPageContent(viewModel: KalleshHubViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("About Kallesh AI Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Founded by Kallesh MC, Kallesh AI Hub was created with a clear mission: to bring world-class conversational AI, image generation, video synthesis, voice interaction, music composition, and developer productivity tools together into one fast, accessible, and secure platform.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Production Architecture & Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("• Authentication: Google OAuth 2.0 Credential Manager & Firebase Authentication", style = MaterialTheme.typography.bodySmall)
            Text("• Cloud & Local Databases: Cloud Firestore real-time collections + Room SQLite v3 offline persistence", style = MaterialTheme.typography.bodySmall)
            Text("• Zero Secret Exposure: API keys are injected server/build-side and never exposed in frontend code", style = MaterialTheme.typography.bodySmall)
            Text("• Official Domain Ready: Configured for https://kalleshaihub.com with deep linking, Open Graph, and JSON-LD structured data", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PublicContactPageContent(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?
) {
    var name by remember(userProfile?.displayName) { mutableStateOf(userProfile?.displayName ?: "") }
    var email by remember(userProfile?.email) { mutableStateOf(userProfile?.email ?: "") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Contact Kallesh AI Hub Support", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Have questions about Kallesh Pro/Enterprise plans, custom domain deployment (https://kalleshaihub.com), or API partnerships? Send us a message below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_name_input"),
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Your Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_email_input"),
                singleLine = true
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g., Enterprise Plan, Technical Support)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_subject_input"),
                singleLine = true
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("How can we help you?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_message_input"),
                minLines = 4
            )

            Button(
                onClick = {
                    viewModel.submitContactInquiry(name, email, subject, message)
                    subject = ""
                    message = ""
                },
                enabled = name.isNotBlank() && email.isNotBlank() && message.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_submit_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Message to Support")
            }
        }
    }
}

@Composable
private fun PublicPrivacyPolicyPageContent() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Privacy Policy — Kallesh AI Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Effective Date: September 2026 • Domain: https://kalleshaihub.com", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                "1. Information We Collect: When you authenticate with Kallesh AI Hub, we store your basic account profile (display name, email, subscription tier, and personalization preferences) in Google Cloud Firestore and your device's encrypted Room database.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "2. Chat History & Generated Media: Your AI conversations, prompts, and generated image/video/music metadata are scoped strictly to your authenticated userId and protected by Firestore Security Rules.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "3. API Key & Secret Security: Private API keys and backend credentials are never exposed in frontend code or client bundles.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "4. Data Control & Deletion: You can edit, export, or permanently delete individual messages, conversations, or your entire chat history at any time from the AI Chat History drawer.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PublicTermsOfServicePageContent() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Terms of Service — Kallesh AI Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Effective Date: September 2026 • Domain: https://kalleshaihub.com", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                "1. Acceptance of Terms: By accessing Kallesh AI Hub (https://kalleshaihub.com or the Kallesh AI Hub application), you agree to comply with these Terms of Service and all applicable laws.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "2. Subscription Plans & Usage Quotas: Daily (₹5/day • 4 AI tool generations), Monthly (₹200/month • 100 AI tool generations), and Yearly (₹1,300/year • 1,300 AI tool generations) quotas are enforced on the backend, while AI Chat remains unlimited. When a tool generation quota is reached, you may upgrade your plan via backend-verified checkout or wait for the next cycle.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "3. Responsible AI Usage: Users may not use AI Chat, AI Image Generator, or Veo 3 Video Generator to produce unlawful, harmful, or deceptive content.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "4. Platform Updates & Rollbacks: Kallesh AI Hub continuously delivers new AI features via staged rollouts and semantic version releases while preserving user data.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SeoAndDeploymentMetadataCard(
    page: PublicPortalPage,
    onCopyText: (String, String) -> Unit
) {
    val htmlHeadSnippet = remember(page) {
        """
        <title>${page.seoTitle}</title>
        <meta name="description" content="${page.metaDescription}" />
        <link rel="canonical" href="${page.canonicalUrl}" />
        <meta property="og:type" content="website" />
        <meta property="og:site_name" content="Kallesh AI Hub" />
        <meta property="og:title" content="${page.seoTitle}" />
        <meta property="og:description" content="${page.metaDescription}" />
        <meta property="og:url" content="${page.canonicalUrl}" />
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="twitter:title" content="${page.seoTitle}" />
        <meta name="twitter:description" content="${page.metaDescription}" />
        """.trimIndent()
    }

    val jsonLdSnippet = remember(page) {
        """
        {
          "@context": "https://schema.org",
          "@graph": [
            {
              "@type": "WebSite",
              "name": "Kallesh AI Hub",
              "alternateName": "$SEO_WEBSITE_TITLE",
              "url": "$PRODUCTION_CUSTOM_DOMAIN/"
            },
            {
              "@type": "SoftwareApplication",
              "name": "Kallesh AI Hub",
              "description": "$SEO_META_DESCRIPTION",
              "url": "${page.canonicalUrl}",
              "applicationCategory": "ArtificialIntelligenceApplication",
              "operatingSystem": "Web, Android, Desktop",
              "founder": {
                "@type": "Person",
                "name": "Kallesh MC"
              },
              "offers": [
                { "@type": "Offer", "name": "Daily Plan", "price": "5", "priceCurrency": "INR" },
                { "@type": "Offer", "name": "Monthly Plan", "price": "200", "priceCurrency": "INR" },
                { "@type": "Offer", "name": "Yearly Plan", "price": "1300", "priceCurrency": "INR" }
              ]
            }
          ]
        }
        """.trimIndent()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("SEO, Open Graph, JSON-LD & Sitemap Spec", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { onCopyText("HTML SEO Metadata", htmlHeadSnippet) }) {
                    Text("Copy Meta Tags")
                }
            }

            Surface(
                color = Color(0xFF090D16),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = htmlHeadSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Surface(
                color = Color(0xFF090D16),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = jsonLdSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Text(
                text = "Static Web Bundle Included: assets/public_web/index.html, assets/public_web/robots.txt, and assets/public_web/sitemap.xml ready for custom domain https://kalleshaihub.com.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProductionApiEndpointsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Connected Backend & AI API Endpoints", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                "All AI features are connected to authenticated backend services and Gemini / Veo / Lyria REST pipelines with automatic fallback and zero frontend secret exposure.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PRODUCTION_API_ENDPOINTS.forEach { ep ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${ep.method} ${ep.path}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(ep.description, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Engine: ${ep.upstreamModelOrService}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicFooterNavigationCard(
    onSelectPage: (PublicPortalPage) -> Unit,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = SEO_WEBSITE_TITLE,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$PRODUCTION_CUSTOM_DOMAIN • Founded by Kallesh MC",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E5FF)
                    )
                }
                AssistChip(
                    onClick = onOpenChat,
                    label = { Text("Open Chat", color = Color.White) }
                )
            }

            Text(
                text = SEO_META_DESCRIPTION,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PublicPortalPage.entries.forEach { page ->
                    TextButton(
                        onClick = { onSelectPage(page) },
                        modifier = Modifier.testTag("footer_link_${page.name.lowercase()}")
                    ) {
                        Text(page.navLabel, color = Color(0xFFE2E8F0), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
