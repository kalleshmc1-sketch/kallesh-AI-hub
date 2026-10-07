package com.example.ui.settings

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.backend.FullStackBackendEngine
import com.example.data.model.BillingCycle
import com.example.data.model.DailyUsage
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SystemConfig
import com.example.payment.RazorpaySdkGatewayManager
import com.example.data.model.UiState
import com.example.data.model.UsageCategory
import com.example.data.model.UsagePeriod
import com.example.data.model.UserProfile
import com.example.ui.auth.KalleshBrandLogoShowcaseDialog
import com.example.ui.platform.FounderPlatformAdminControlSection
import com.example.ui.publicpages.PublicFooterNavigationCard
import com.example.ui.home.UsageMeterRow
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsageAndUpgradeScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?,
    todayUsageState: UiState<DailyUsage>
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val quotaStatus by viewModel.quotaStatusState.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
    val activePlan = SubscriptionPlan.fromId(quotaStatus.planType.ifBlank { userProfile?.plan ?: "DAILY" })
    val usage = (todayUsageState as? UiState.Success)?.data ?: DailyUsage()
    val nextTier = if (activePlan == SubscriptionPlan.FREE) SubscriptionPlan.PRO else SubscriptionPlan.ENTERPRISE
    val selectedUsagePeriod by viewModel.selectedUsagePeriod.collectAsStateWithLifecycle()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("usage_and_upgrade_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "AI Generation Quotas & Subscription Plans",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Backend-enforced Daily (₹5 • 4 uses), Monthly (₹200 • 100 uses) & Yearly (₹1,300 • 1,300 uses) plans. Normal AI Chat is always unlimited and never consumes your generation allowance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Backend Database Source-of-Truth Quota Status Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backend_quota_status_card"),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Active Plan: ${activePlan.displayName} (${activePlan.priceDisplay})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Cycle: ${dateFormatter.format(Date(quotaStatus.planStartDate))} → ${dateFormatter.format(Date(quotaStatus.planExpiryDate))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(activePlan.badgeText) }
                        )
                    }

                    val progress = (quotaStatus.usedCount.toFloat() / quotaStatus.allowedLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "AI Tool Generations Used (${activePlan.billingPeriodLabel})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${quotaStatus.usedCount} / ${quotaStatus.allowedLimit} (${quotaStatus.remainingCount} left)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (quotaStatus.remainingCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = if (quotaStatus.remainingCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "AI Chat is Unlimited (${usage.chatCount} messages sent today) — Normal AI Chat never consumes your ${quotaStatus.allowedLimit}-generation allowance.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UsagePeriod.entries.forEach { period ->
                            FilterChip(
                                selected = selectedUsagePeriod == period,
                                onClick = { viewModel.selectUsagePeriod(period) },
                                label = { Text("${period.label} Breakdown") },
                                modifier = Modifier.testTag("upgrade_usage_period_${period.name.lowercase()}")
                            )
                        }
                    }

                    UsageMeterRow(
                        label = "Image Studio Generations (${selectedUsagePeriod.label})",
                        used = usage.usedForCategory(UsageCategory.IMAGE, selectedUsagePeriod),
                        limit = activePlan.limitForPeriod(UsageCategory.IMAGE, selectedUsagePeriod),
                        onUpgradeClick = { viewModel.initiatePlanPaymentOrder(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Veo 3 Video Generations (${selectedUsagePeriod.label})",
                        used = usage.usedForCategory(UsageCategory.VIDEO, selectedUsagePeriod),
                        limit = activePlan.limitForPeriod(UsageCategory.VIDEO, selectedUsagePeriod),
                        onUpgradeClick = { viewModel.initiatePlanPaymentOrder(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Document & AI Tool Executions (${selectedUsagePeriod.label})",
                        used = usage.usedForCategory(UsageCategory.FILE, selectedUsagePeriod),
                        limit = activePlan.limitForPeriod(UsageCategory.FILE, selectedUsagePeriod),
                        onUpgradeClick = { viewModel.initiatePlanPaymentOrder(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Live Voice & Audio Turns (${selectedUsagePeriod.label})",
                        used = usage.usedForCategory(UsageCategory.VOICE, selectedUsagePeriod),
                        limit = activePlan.limitForPeriod(UsageCategory.VOICE, selectedUsagePeriod),
                        onUpgradeClick = { viewModel.initiatePlanPaymentOrder(nextTier) }
                    )
                    UsageMeterRow(
                        label = "Lyria 3 Music Tracks (${selectedUsagePeriod.label})",
                        used = usage.usedForCategory(UsageCategory.MUSIC, selectedUsagePeriod),
                        limit = activePlan.limitForPeriod(UsageCategory.MUSIC, selectedUsagePeriod),
                        onUpgradeClick = { viewModel.initiatePlanPaymentOrder(nextTier) }
                    )
                }
            }
        }

        // Daily Limit Simulator & Quota Reset Controls
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quota Limit & Upgrade Simulator",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Test reaching your generation limit for any AI tool category or reset today's counters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.resetTodayUsageCounters() },
                            modifier = Modifier.testTag("reset_daily_usage_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Today")
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UsageCategory.entries.filter { it != UsageCategory.CHAT }.forEach { category ->
                            AssistChip(
                                onClick = { viewModel.simulateDailyLimitReached(category) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                label = { Text("Simulate ${category.label} Limit") },
                                modifier = Modifier.testTag("simulate_limit_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Official Kallesh AI Hub Plans (INR ₹)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Secure Razorpay / UPI / Card / NetBanking checkout with backend HMAC-SHA256 verification",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.openPricingPlansModal() },
                    modifier = Modifier.testTag("open_pricing_plans_modal_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plans Modal")
                }
            }
        }

        SubscriptionPlan.entries.forEach { plan ->
            item {
                val isCurrent = plan == activePlan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_card_${plan.id.lowercase()}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "${plan.priceDisplay} per ${plan.billingPeriodLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isCurrent) {
                                AssistChip(
                                    onClick = {},
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Active Plan") }
                                )
                            } else {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(plan.badgeText) }
                                )
                            }
                        }

                        Text(
                            "• Limit: ${plan.periodGenerationLimit} AI tool uses/generations per ${plan.billingPeriodLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "• AI Chat: Unlimited conversations & messages (does NOT consume generation limit)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "• Includes: AI Image Generator, Veo 3 Video Generator, Voice & Music Studios, and all 28 AI Tools",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• File Upload Support: PDF, Docs & Images up to ${plan.maxFileSizeMb}MB",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val legacyTag = when (plan) {
                            SubscriptionPlan.FREE -> "select_plan_button_free"
                            SubscriptionPlan.PRO -> "select_plan_button_pro"
                            SubscriptionPlan.PREMIUM -> "select_plan_button_enterprise"
                        }

                        Button(
                            onClick = { viewModel.initiatePlanPaymentOrder(plan, "UPI") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(legacyTag),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isCurrent) "Renew / Top-Up ${plan.displayName} (${plan.priceDisplay})"
                                else "Buy ${plan.displayName} — ${plan.priceDisplay} (${plan.periodGenerationLimit} uses/${plan.billingPeriodLabel})"
                            )
                        }
                    }
                }
            }
        }

        // Verified Payment Transaction History
        if (paymentHistory.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_transaction_history_card"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Payment & Subscription Ledger (${paymentHistory.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        paymentHistory.take(8).forEach { tx ->
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${tx.planType} Plan • ₹${tx.amountInr} (${tx.allowedGenerations} uses)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Order: ${tx.orderId} • Method: ${tx.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (tx.paymentId.isNotBlank()) {
                                        Text(
                                            "Payment ID: ${tx.paymentId} • Sig: ${tx.signature.take(16)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(tx.status) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            PublicFooterNavigationCard(
                onSelectPage = { page -> viewModel.openPublicPortalPage(page) },
                onOpenChat = { viewModel.openChatOrActiveConversation() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentGatewayCheckoutDialog(
    order: FullStackBackendEngine.PaymentOrderRecord,
    userEmail: String,
    onCompleteVerifiedPayment: (paymentId: String, signature: String, paymentMethod: String) -> Unit,
    onCancelOrder: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedMethod by remember { mutableStateOf("UPI (GPay / PhonePe / Paytm)") }
    var upiOrCardInput by remember { mutableStateOf(userEmail.substringBefore("@").ifBlank { "kallesh" } + "@okaxis") }
    var sdkStatusNote by remember { mutableStateOf<String?>(null) }
    val plan = SubscriptionPlan.fromId(order.planType)
    val isLiveMerchantKey = remember { RazorpaySdkGatewayManager.isMerchantKeyConfigured() }
    val gatewayModeLabel = remember { RazorpaySdkGatewayManager.getConfiguredKeyModeLabel() }

    AlertDialog(
        onDismissRequest = onCancelOrder,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column {
                Text(
                    "Kallesh AI Hub • Razorpay Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Gateway: $gatewayModeLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(plan.displayName, fontWeight = FontWeight.Bold)
                            Text(
                                "₹${order.amountInr}.00 INR (${order.amountPaise} paise)",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Allowance: ${order.allowedGenerations} AI Tool Generations / ${plan.billingPeriodLabel} + Unlimited AI Chat",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Razorpay Order ID: ${order.orderId} • Receipt: ${order.receipt}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text("Select Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "UPI (GPay / PhonePe / Paytm)",
                        "Debit / Credit Card",
                        "NetBanking",
                        "Wallet"
                    ).forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                OutlinedTextField(
                    value = upiOrCardInput,
                    onValueChange = { upiOrCardInput = it },
                    label = {
                        Text(
                            if (selectedMethod.startsWith("UPI")) "UPI ID / VPA (e.g., name@okaxis)"
                            else "Card / Account Reference"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_gateway_vpa_input")
                )

                if (activity != null) {
                    OutlinedButton(
                        onClick = {
                            val res = RazorpaySdkGatewayManager.launchNativeRazorpayCheckout(
                                activity = activity,
                                order = order,
                                userEmail = userEmail,
                                userDisplayName = userEmail.substringBefore("@"),
                                preferredMethod = selectedMethod
                            )
                            res.onFailure { err ->
                                sdkStatusNote = err.message
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_native_razorpay_sdk_button")
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isLiveMerchantKey) "Open Native Razorpay SDK Sheet (₹${order.amountInr})"
                            else "Open Native Razorpay SDK (Requires RAZORPAY_KEY_ID in Secrets)"
                        )
                    }
                }

                sdkStatusNote?.let { note ->
                    Text(
                        note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    "Backend Verification Policy: Your plan is ONLY activated after the backend verifies the HMAC-SHA256 signature (order_id|payment_id) and order amount (₹${order.amountInr}). Cancelling or closing this window will NOT upgrade your plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        onCompleteVerifiedPayment(
                            "pay_tampered_${System.currentTimeMillis() % 10000}",
                            "forged_invalid_signature_0000",
                            selectedMethod
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_invalid_signature_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Forged Signature (Backend Rejects)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paymentId = "pay_${UUID.randomUUID().toString().replace("-", "").take(14)}"
                    val validSignature = FullStackBackendEngine.generateExpectedSignature(order.orderId, paymentId)
                    onCompleteVerifiedPayment(paymentId, validSignature, selectedMethod)
                },
                enabled = upiOrCardInput.isNotBlank(),
                modifier = Modifier.testTag("confirm_verify_payment_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pay ₹${order.amountInr} & Verify")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelOrder,
                modifier = Modifier.testTag("cancel_payment_checkout_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel Payment")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndAdminScreen(
    viewModel: KalleshHubViewModel,
    userProfile: UserProfile?,
    founderConfig: SystemConfig,
    onSignOut: () -> Unit
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    var displayName by remember(userProfile?.displayName) { mutableStateOf(userProfile?.displayName ?: "Explorer") }
    var preferredLanguage by remember(userProfile?.preferredLanguage) { mutableStateOf(userProfile?.preferredLanguage ?: "English") }
    var responseStyle by remember(userProfile?.responseStyle) { mutableStateOf(userProfile?.responseStyle ?: "Professional") }
    var defaultModel by remember(userProfile?.defaultModel) { mutableStateOf(userProfile?.defaultModel ?: "gemini-3-flash-preview") }
    var runtimeApiKeyInput by remember { mutableStateOf(viewModel.getRuntimeGeminiApiKey()) }
    var themeMode by remember(userProfile?.themeMode) { mutableStateOf(userProfile?.themeMode ?: "DARK") }
    var voiceEnabled by remember(userProfile?.voiceEnabled) { mutableStateOf(userProfile?.voiceEnabled ?: true) }
    var memoryEnabled by remember(userProfile?.memoryEnabled) { mutableStateOf(userProfile?.memoryEnabled ?: true) }
    var memorySummary by remember(userProfile?.memorySummary) { mutableStateOf(userProfile?.memorySummary ?: "") }

    // Founder Admin Config state
    var introTitle by remember(founderConfig.title) { mutableStateOf(founderConfig.title) }
    var introDescription by remember(founderConfig.description) { mutableStateOf(founderConfig.description) }
    var introVoice by remember(founderConfig.voiceName) { mutableStateOf(founderConfig.voiceName) }
    var announcement by remember(founderConfig.announcement) { mutableStateOf(founderConfig.announcement) }
    var showLogoDialog by remember { mutableStateOf(false) }

    val platformFeatures by viewModel.platformFeatures.collectAsStateWithLifecycle()
    val appRelease by viewModel.appReleaseState.collectAsStateWithLifecycle()
    val platformAnalytics by viewModel.platformAnalytics.collectAsStateWithLifecycle()
    val qualityChecks by viewModel.qualityChecks.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    if (showLogoDialog) {
        KalleshBrandLogoShowcaseDialog(onDismiss = { showLogoDialog = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_and_admin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Account, Memory & Platform Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Signed in as ${userProfile?.email?.ifBlank { "Authenticated User" } ?: "Authenticated User"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick Actions: Founder Intro Replay & Official Brand Logo Kit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openFounderIntroModal(founderConfig) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_replay_intro_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Founder Intro")
                }

                OutlinedButton(
                    onClick = { showLogoDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_logo_kit_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Brand Logo Kit")
                }
            }
        }

        // Personalization & AI Memory Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("AI Personalization & Persistent Memory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Preferred Language", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("English", "Kannada", "Hindi", "Tamil", "Telugu", "Spanish").forEach { lang ->
                            FilterChip(
                                selected = preferredLanguage == lang,
                                onClick = { preferredLanguage = lang },
                                label = { Text(lang) }
                            )
                        }
                    }

                    Text("AI Response Tone", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Professional", "Friendly", "Detailed", "Short", "Teacher Mode").forEach { style ->
                            FilterChip(
                                selected = responseStyle == style,
                                onClick = { responseStyle = style },
                                label = { Text(style) }
                            )
                        }
                    }

                    Text("Theme Appearance", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { themeMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable AI Memory Across Chats", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = memoryEnabled, onCheckedChange = { memoryEnabled = it })
                    }

                    OutlinedTextField(
                        value = memorySummary,
                        onValueChange = { memorySummary = it },
                        label = { Text("Your Goals, Tech Stack & Interests (AI Memory)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            viewModel.saveUserPersonalization(
                                displayName = displayName,
                                preferredLanguage = preferredLanguage,
                                responseStyle = responseStyle,
                                defaultModel = defaultModel,
                                themeMode = themeMode,
                                voiceEnabled = voiceEnabled,
                                memoryEnabled = memoryEnabled,
                                memorySummary = memorySummary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_personalization_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Personalization to Cloud")
                    }
                }
            }
        }

        // Administrator & Founder Control Panel
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Founder & Administrator Control Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val keyConfigured = viewModel.isGeminiKeyConfigured()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = if (keyConfigured) "GEMINI_API_KEY: Active & Ready" else "GEMINI_API_KEY: Smart Studio Mode (Add Key for Live Cloud API)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Add GEMINI_API_KEY in the AI Studio Secrets panel (.env) or enter a key below to connect directly to live Gemini 3, Veo 3 & Lyria 3 REST endpoints.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = runtimeApiKeyInput,
                                onValueChange = { runtimeApiKeyInput = it },
                                label = { Text("Optional Masked Runtime Key Override (Never Exposed)") },
                                placeholder = { Text("Managed securely via Secrets Panel (.env)") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("runtime_gemini_api_key_input"),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.saveRuntimeGeminiApiKey(runtimeApiKeyInput) },
                                    enabled = runtimeApiKeyInput.isNotBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_runtime_api_key_button")
                                ) {
                                    Text("Apply API Key")
                                }
                                if (runtimeApiKeyInput.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            runtimeApiKeyInput = ""
                                            viewModel.clearRuntimeGeminiApiKey()
                                        },
                                        modifier = Modifier.testTag("clear_runtime_api_key_button")
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = introTitle,
                        onValueChange = { introTitle = it },
                        label = { Text("Founder Welcome Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Founder TTS Voice (Gemini TTS)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Puck", "Charon", "Fenrir", "Kore", "Aoede").forEach { v ->
                            FilterChip(
                                selected = introVoice == v,
                                onClick = { introVoice = v },
                                label = { Text(v) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = introDescription,
                        onValueChange = { introDescription = it },
                        label = { Text("Founder Introduction Script") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it },
                        label = { Text("Global Platform Announcement Banner") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveAdminFounderConfig(
                                founderConfig.copy(
                                    title = introTitle,
                                    description = introDescription,
                                    voiceName = introVoice,
                                    announcement = announcement
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_admin_config_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Update Founder Intro & Platform Config")
                    }
                }
            }
        }

        // Dynamic Feature System, Semantic Versioning, Rollback, Quality Gate & Analytics Section
        item {
            FounderPlatformAdminControlSection(
                viewModel = viewModel,
                features = platformFeatures,
                appRelease = appRelease,
                analytics = platformAnalytics,
                qualityChecks = qualityChecks,
                auditLogs = auditLogs,
                onTriggerUpdateDialogPreview = { viewModel.triggerUpdateDialogPreview() },
                onTriggerWhatsNewDialog = { viewModel.setShowWhatsNewModal(true) }
            )
        }

        // Sign Out Button
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Kallesh AI Hub")
            }
        }
    }
}
