package com.example.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.backend.FullStackBackendEngine
import com.example.data.model.SubscriptionPlan
import com.example.payment.RazorpaySdkGatewayManager

/**
 * Interactive Pricing Plans Modal Component for Kallesh AI Hub.
 * Displays the official ₹5 Daily, ₹200 Monthly, and ₹1,300 Yearly plans,
 * allows selecting a plan and preferred payment method, and triggers the
 * backend-verified Razorpay checkout flow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PricingPlansModalDialog(
    activePlan: SubscriptionPlan,
    quotaStatus: FullStackBackendEngine.UserQuotaStatus,
    initialSelectedPlan: SubscriptionPlan = SubscriptionPlan.PRO,
    userEmail: String = "",
    onTriggerRazorpayCheckout: (plan: SubscriptionPlan, paymentMethod: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedPlan by remember(initialSelectedPlan) { mutableStateOf(initialSelectedPlan) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI (GPay / PhonePe / Paytm)") }
    val gatewayModeLabel = remember { RazorpaySdkGatewayManager.getConfiguredKeyModeLabel() }
    val scrollState = rememberScrollState()

    val paymentMethods = remember {
        listOf(
            "UPI (GPay / PhonePe / Paytm)",
            "Debit / Credit Card",
            "NetBanking",
            "Wallet"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("pricing_plans_modal_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0B1120)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF6366F1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Kallesh AI Hub Pricing Plans",
                                tint = Color(0xFF070A12),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Choose Your Kallesh AI Hub Plan",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Instant Razorpay Checkout • Backend HMAC-SHA256 Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pricing_modal_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Pricing Modal",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Current Active Plan & Quota Banner
                Surface(
                    color = Color(0xFF131D35),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Plan: ${activePlan.displayName} (${activePlan.priceDisplay}/${activePlan.billingPeriodLabel})",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tool Generations Remaining: ${quotaStatus.remainingCount} / ${quotaStatus.allowedLimit} • AI Chat: Unlimited",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = activePlan.badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Plan Cards (Daily ₹5, Monthly ₹200, Yearly ₹1,300)
                SubscriptionPlan.entries.forEach { plan ->
                    val isSelected = selectedPlan == plan
                    val isCurrent = activePlan == plan
                    val highlightBadge = when (plan) {
                        SubscriptionPlan.FREE -> "STARTER • ₹5 / DAY"
                        SubscriptionPlan.PRO -> "MOST POPULAR • ₹200 / MO"
                        SubscriptionPlan.PREMIUM -> "BEST VALUE • ₹1,300 / YR"
                    }
                    val amountPaise = plan.priceInr * 100

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedPlan = plan }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = if (isSelected) {
                                    Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF6366F1)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("pricing_modal_plan_card_${plan.id.lowercase()}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF14213D) else Color(0xFF0F172A)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = highlightBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    if (isCurrent) {
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "$amountPaise paise",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = plan.displayName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${plan.periodGenerationLimit} AI Tool Generations per ${plan.billingPeriodLabel}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = plan.priceDisplay,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = " / ${plan.billingPeriodLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PlanFeatureBullet("Unlimited AI Chat messages & conversation history (never consumes quota)")
                                PlanFeatureBullet("${plan.periodGenerationLimit} uses across AI Image Generator, Veo 3 Video, Music & 28 AI Tools")
                                PlanFeatureBullet("Upload & analyze PDFs, documents, and images up to ${plan.maxFileSizeMb}MB")
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Button(
                                onClick = {
                                    selectedPlan = plan
                                    onTriggerRazorpayCheckout(plan, selectedPaymentMethod)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pricing_modal_select_${plan.id.lowercase()}_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                                    contentColor = if (isSelected) Color(0xFF070A12) else Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isCurrent) {
                                        "Renew ${plan.displayName} with Razorpay (${plan.priceDisplay})"
                                    } else {
                                        "Select ${plan.displayName} & Pay ${plan.priceDisplay}"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Preferred Payment Method Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preferred Razorpay Payment Method",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method) },
                                modifier = Modifier.testTag(
                                    "pricing_modal_method_${method.substringBefore(" ").lowercase()}"
                                )
                            )
                        }
                    }
                }

                // Primary Razorpay Checkout Trigger CTA
                Button(
                    onClick = {
                        onTriggerRazorpayCheckout(selectedPlan, selectedPaymentMethod)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("pricing_modal_proceed_razorpay_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF070A12)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proceed to Razorpay Checkout • ${selectedPlan.priceDisplay} (${selectedPlan.displayName})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Security & Gateway Verification Footnote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gateway: $gatewayModeLabel • Verified on backend before plan activation",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pricing_modal_cancel_button")
                    ) {
                        Text("Maybe Later", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanFeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
        )
    }
}
