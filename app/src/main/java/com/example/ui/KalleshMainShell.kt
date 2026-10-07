package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.PublicPortalPage
import com.example.data.model.SystemConfig
import com.example.data.model.UiState
import com.example.platform.FeatureBadge
import com.example.ui.auth.FounderWelcomeDialog
import com.example.ui.auth.KalleshBrandLogoShowcaseDialog
import com.example.ui.auth.signOutUser
import com.example.ui.home.HomeHubScreen
import com.example.ui.home.MainChatScreen
import com.example.ui.platform.AppUpdateAvailableDialog
import com.example.ui.platform.DailyLimitReachedUpgradeDialog
import com.example.ui.platform.NewFeatureDiscoveryDialog
import com.example.ui.platform.WhatsNewChangelogDialog
import com.example.ui.publicpages.PublicWebPagesScreen
import com.example.ui.settings.PaymentGatewayCheckoutDialog
import com.example.ui.settings.PricingPlansModalDialog
import com.example.ui.settings.SettingsAndAdminScreen
import com.example.ui.settings.UsageAndUpgradeScreen
import com.example.ui.studios.CreativeStudiosHubScreen
import com.example.ui.tools.PersonalWorkspaceScreen
import com.example.ui.tools.ProductivityToolsScreen
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel

private fun HubSection.icon(): ImageVector = when (this) {
    HubSection.HOME -> Icons.Default.Home
    HubSection.CHAT -> Icons.AutoMirrored.Filled.Chat
    HubSection.STUDIOS -> Icons.Default.AutoAwesome
    HubSection.TOOLS -> Icons.Default.Build
    HubSection.WORKSPACE -> Icons.Default.FolderSpecial
    HubSection.UPGRADE -> Icons.Default.WorkspacePremium
    HubSection.PUBLIC_PORTAL -> Icons.Default.Public
    HubSection.SETTINGS -> Icons.Default.Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalleshMainShell(
    viewModel: KalleshHubViewModel,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val selectedCreativeTab by viewModel.selectedCreativeTab.collectAsStateWithLifecycle()
    val selectedProductivityTab by viewModel.selectedProductivityTab.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val enableSearchGrounding by viewModel.enableSearchGrounding.collectAsStateWithLifecycle()
    val enableMapsGrounding by viewModel.enableMapsGrounding.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingReplyText by viewModel.streamingReplyText.collectAsStateWithLifecycle()
    val chatSearchQuery by viewModel.chatSearchQuery.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val todayUsageState by viewModel.todayUsageState.collectAsStateWithLifecycle()
    val workspaceItemsState by viewModel.workspaceItemsState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val founderConfigState by viewModel.founderConfigState.collectAsStateWithLifecycle()
    val cachedPrompts by viewModel.cachedPrompts.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val studioResultText by viewModel.studioResultText.collectAsStateWithLifecycle()
    val latestImagePath by viewModel.latestGeneratedImagePath.collectAsStateWithLifecycle()
    val latestMusicPath by viewModel.latestGeneratedMusicPath.collectAsStateWithLifecycle()
    val liveVoiceHistory by viewModel.liveVoiceHistory.collectAsStateWithLifecycle()
    val showFounderModalManual by viewModel.showFounderModalManual.collectAsStateWithLifecycle()
    val isPlayingAudio by viewModel.audioVoiceManager.isPlaying.collectAsStateWithLifecycle()
    val isMuted by viewModel.audioVoiceManager.isMuted.collectAsStateWithLifecycle()
    val appRelease by viewModel.appReleaseState.collectAsStateWithLifecycle()
    val showAppUpdateModal by viewModel.showAppUpdateModal.collectAsStateWithLifecycle()
    val discoveryFeature by viewModel.discoveryFeatureToShow.collectAsStateWithLifecycle()
    val showWhatsNewModal by viewModel.showWhatsNewModal.collectAsStateWithLifecycle()
    val changelogs by viewModel.changelogs.collectAsStateWithLifecycle()
    val platformFeatures by viewModel.platformFeatures.collectAsStateWithLifecycle()
    val limitReachedAlert by viewModel.limitReachedAlert.collectAsStateWithLifecycle()
    val showPricingPlansModal by viewModel.showPricingPlansModal.collectAsStateWithLifecycle()
    val preselectedModalPlan by viewModel.preselectedModalPlan.collectAsStateWithLifecycle()
    val quotaStatusState by viewModel.quotaStatusState.collectAsStateWithLifecycle()
    val activePaymentOrder by viewModel.activePaymentOrder.collectAsStateWithLifecycle()

    val userProfile = (userProfileState as? UiState.Success)?.data
    val founderConfig = (founderConfigState as? UiState.Success)?.data ?: SystemConfig()
    val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
    val unreadCount = notifications.count { !it.isRead }

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLogoShowcase by remember { mutableStateOf(false) }

    val shouldShowFirstTimeFounderWelcome =
        (userProfile != null && !userProfile.hasSeenFounderIntro && founderConfig.showOnFirstLogin) ||
            showFounderModalManual

    if (shouldShowFirstTimeFounderWelcome) {
        FounderWelcomeDialog(
            config = founderConfig,
            isPlayingAudio = isPlayingAudio,
            isMuted = isMuted,
            onPlayPause = {
                if (isPlayingAudio) {
                    viewModel.audioVoiceManager.togglePauseResume()
                } else {
                    viewModel.playFounderIntroductionAudio(founderConfig)
                }
            },
            onReplay = { viewModel.playFounderIntroductionAudio(founderConfig) },
            onToggleMute = { viewModel.audioVoiceManager.toggleMute() },
            onStartExploring = { viewModel.completeOrSkipFounderIntro() }
        )
    }

    if (showLogoShowcase) {
        KalleshBrandLogoShowcaseDialog(onDismiss = { showLogoShowcase = false })
    }

    if (showAppUpdateModal && !shouldShowFirstTimeFounderWelcome) {
        AppUpdateAvailableDialog(
            release = appRelease,
            onUpdateNow = { viewModel.performAppUpdateNow() },
            onUpdateLater = { viewModel.dismissAppUpdateLater() }
        )
    }

    if (discoveryFeature != null && !showAppUpdateModal && !shouldShowFirstTimeFounderWelcome) {
        NewFeatureDiscoveryDialog(
            feature = discoveryFeature!!,
            onTryNow = { viewModel.dismissNewFeatureDiscovery(it, tryNow = true) },
            onMaybeLater = { viewModel.dismissNewFeatureDiscovery(it, tryNow = false) }
        )
    }

    if (showWhatsNewModal) {
        WhatsNewChangelogDialog(
            installedVersion = appRelease.installedVersion,
            changelogs = changelogs,
            highlightedFeatures = platformFeatures.filter { it.badge != FeatureBadge.NONE },
            onTryFeature = { feat ->
                viewModel.launchDynamicFeature(feat, onOpenInToolRunner = {})
            },
            onDismiss = { viewModel.setShowWhatsNewModal(false) }
        )
    }

    if (limitReachedAlert != null) {
        DailyLimitReachedUpgradeDialog(
            alert = limitReachedAlert!!,
            onUpgradeToPlan = { plan -> viewModel.upgradePlanFromLimitAlert(plan) },
            onOpenUpgradeScreen = { viewModel.openPricingPlansModal() },
            onResetDailyQuota = { viewModel.resetTodayDailyUsage() },
            onDismiss = { viewModel.dismissLimitReachedAlert() }
        )
    }

    if (showPricingPlansModal) {
        val activePlan = com.example.data.model.SubscriptionPlan.fromId(
            quotaStatusState.planType.ifBlank { userProfile?.plan ?: "DAILY" }
        )
        PricingPlansModalDialog(
            activePlan = activePlan,
            quotaStatus = quotaStatusState,
            initialSelectedPlan = preselectedModalPlan,
            userEmail = userProfile?.email.orEmpty(),
            onTriggerRazorpayCheckout = { selectedPlan, paymentMethod ->
                viewModel.initiatePlanPaymentOrder(selectedPlan, paymentMethod)
            },
            onDismiss = {
                viewModel.dismissPricingPlansModal()
            }
        )
    }

    activePaymentOrder?.let { pendingOrder ->
        PaymentGatewayCheckoutDialog(
            order = pendingOrder,
            userEmail = userProfile?.email.orEmpty(),
            onCompleteVerifiedPayment = { paymentId, signature, method ->
                viewModel.verifyAndCompletePlanPayment(
                    orderId = pendingOrder.orderId,
                    paymentId = paymentId,
                    signature = signature,
                    paymentMethod = method
                )
            },
            onCancelOrder = {
                viewModel.cancelOrFailPlanPayment("Payment was cancelled or not completed.")
            }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notifications & Job Alerts") },
            text = {
                if (notifications.isEmpty()) {
                    Text("No notifications yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { n ->
                            Card(
                                onClick = { viewModel.markNotificationAsRead(n.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(n.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Bottom bar displays 6 core sections; Settings & Upgrade are accessible from TopAppBar + Navigation
    val bottomSections = listOf(
        HubSection.HOME,
        HubSection.CHAT,
        HubSection.STUDIOS,
        HubSection.TOOLS,
        HubSection.WORKSPACE,
        HubSection.UPGRADE
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clickable { showLogoShowcase = true }
                                .testTag("top_bar_brand_header")
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kallesh_ref_logo),
                                contentDescription = "Kallesh AI Hub Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "KALLESH ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "AI HUB",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = currentSection.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentSection != HubSection.CHAT) {
                            Surface(
                                onClick = { viewModel.openChatOrActiveConversation() },
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.testTag("top_bar_open_chat_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Open Chat",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Surface(
                            onClick = { viewModel.setShowWhatsNewModal(true) },
                            color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                            shape = CircleShape,
                            modifier = Modifier.testTag("top_bar_version_badge")
                        ) {
                            Text(
                                text = if (appRelease.hasUpdatePending) "v${appRelease.installedVersion} ↑" else "v${appRelease.installedVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            onClick = { viewModel.openPricingPlansModal() },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.testTag("top_bar_plan_badge")
                        ) {
                            Text(
                                text = userProfile?.subscriptionPlan?.badgeText ?: "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier.testTag("top_bar_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }

                        IconButton(
                            onClick = { viewModel.navigateToSection(HubSection.SETTINGS) },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
                        bottomSections.forEach { section ->
                            NavigationBarItem(
                                selected = currentSection == section,
                                onClick = { viewModel.navigateToSection(section) },
                                icon = { Icon(section.icon(), contentDescription = section.title) },
                                label = { Text(section.title, maxLines = 1) },
                                modifier = Modifier.testTag("nav_item_${section.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        HubSection.entries.forEach { section ->
                            NavigationRailItem(
                                selected = currentSection == section,
                                onClick = { viewModel.navigateToSection(section) },
                                icon = { Icon(section.icon(), contentDescription = section.title) },
                                label = { Text(section.title) },
                                modifier = Modifier.testTag("rail_item_${section.name.lowercase()}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Public Website & SEO Navigation Bar (All 10 Public Pages)
                    Surface(
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("public_top_navigation_strip")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PublicPortalPage.entries.forEach { page ->
                                TextButton(
                                    onClick = { viewModel.openPublicPortalPage(page) },
                                    modifier = Modifier.testTag("header_nav_${page.name.lowercase()}")
                                ) {
                                    Text(
                                        text = page.navLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = statusBannerMessage != null) {
                        val msgText = statusBannerMessage.orEmpty()
                        val isLimitBanner = msgText.contains("limit", ignoreCase = true) ||
                            msgText.contains("upgrade", ignoreCase = true)
                        val isErrorBanner = msgText.contains("fail", ignoreCase = true) ||
                            msgText.contains("error", ignoreCase = true) ||
                            msgText.contains("could not", ignoreCase = true)
                        Surface(
                            color = if (isLimitBanner || isErrorBanner) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msgText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isErrorBanner) {
                                    TextButton(
                                        onClick = { viewModel.retryLastFailedAction() },
                                        modifier = Modifier.testTag("banner_retry_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retry", fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (isLimitBanner) {
                                    TextButton(
                                        onClick = {
                                            viewModel.clearStatusBanner()
                                            viewModel.navigateToSection(HubSection.UPGRADE)
                                        },
                                        modifier = Modifier.testTag("banner_upgrade_plan_button")
                                    ) {
                                        Icon(
                                            Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upgrade Plan", fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.clearStatusBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss Banner", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentSection) {
                            HubSection.HOME -> HomeHubScreen(
                                viewModel = viewModel,
                                userProfile = userProfile,
                                todayUsageState = todayUsageState,
                                conversationsState = conversationsState,
                                founderConfig = founderConfig,
                                prompts = cachedPrompts
                            )
                            HubSection.CHAT -> MainChatScreen(
                                viewModel = viewModel,
                                conversationsState = conversationsState,
                                messagesState = messagesState,
                                activeConversationId = activeConversationId,
                                selectedModel = selectedModel,
                                enableSearchGrounding = enableSearchGrounding,
                                enableMapsGrounding = enableMapsGrounding,
                                isGenerating = isGenerating,
                                streamingReplyText = streamingReplyText,
                                chatSearchQuery = chatSearchQuery,
                                selectedFolder = selectedFolder
                            )
                            HubSection.STUDIOS -> CreativeStudiosHubScreen(
                                viewModel = viewModel,
                                selectedTab = selectedCreativeTab,
                                isGenerating = isGenerating,
                                latestImagePath = latestImagePath,
                                latestMusicPath = latestMusicPath,
                                liveVoiceHistory = liveVoiceHistory,
                                workspaceItemsState = workspaceItemsState,
                                isPlayingAudio = isPlayingAudio
                            )
                            HubSection.TOOLS -> ProductivityToolsScreen(
                                viewModel = viewModel,
                                selectedTab = selectedProductivityTab,
                                isGenerating = isGenerating,
                                studioResultText = studioResultText
                            )
                            HubSection.WORKSPACE -> PersonalWorkspaceScreen(
                                viewModel = viewModel,
                                workspaceItemsState = workspaceItemsState,
                                prompts = cachedPrompts
                            )
                            HubSection.UPGRADE -> UsageAndUpgradeScreen(
                                viewModel = viewModel,
                                userProfile = userProfile,
                                todayUsageState = todayUsageState
                            )
                            HubSection.PUBLIC_PORTAL -> PublicWebPagesScreen(
                                viewModel = viewModel,
                                userProfile = userProfile
                            )
                            HubSection.SETTINGS -> SettingsAndAdminScreen(
                                viewModel = viewModel,
                                userProfile = userProfile,
                                founderConfig = founderConfig,
                                onSignOut = {
                                    signOutUser(
                                        credentialManager = credentialManager,
                                        onSignOutComplete = onSignedOut,
                                        scope = scope,
                                        context = context
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
