package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UiState
import com.example.payment.RazorpayPaymentData
import com.example.payment.RazorpayPaymentResultWithDataListener
import com.example.payment.RazorpaySdkCallbackEvent
import com.example.payment.RazorpaySdkGatewayManager
import com.example.ui.KalleshMainShell
import com.example.ui.auth.AuthLandingScreen
import com.example.ui.auth.SecureAuthManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KalleshHubViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity(), RazorpayPaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RazorpaySdkGatewayManager.preload(applicationContext)
        val initialDeepLinkPath = intent?.data?.path
        setContent {
            KalleshAiHubRoot(
                application = application,
                initialDeepLinkPath = initialDeepLinkPath
            )
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: RazorpayPaymentData?) {
        RazorpaySdkGatewayManager.onSdkPaymentSuccess(razorpayPaymentId, paymentData)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: RazorpayPaymentData?) {
        RazorpaySdkGatewayManager.onSdkPaymentError(code, response, paymentData)
    }
}

@Composable
fun KalleshAiHubRoot(
    application: android.app.Application,
    initialDeepLinkPath: String? = null
) {
    val auth = remember { Firebase.auth }
    var currentUserId by remember {
        mutableStateOf(
            SecureAuthManager.getSavedSession(application)?.uid
                ?: if (!initialDeepLinkPath.isNullOrBlank()) "kallesh_founder_preview" else null
        )
    }
    var pendingRoutePath by remember { mutableStateOf(initialDeepLinkPath) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val savedSession = SecureAuthManager.getSavedSession(application)
            if (savedSession != null) {
                currentUserId = savedSession.uid
            } else {
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null && !fbUser.isAnonymous) {
                    currentUserId = fbUser.uid
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val activeUid = currentUserId
    if (activeUid.isNullOrBlank()) {
        MyApplicationTheme(darkTheme = true) {
            AuthLandingScreen(
                onAuthSuccess = {
                    currentUserId = SecureAuthManager.getSavedSession(application)?.uid
                        ?: Firebase.auth.currentUser?.uid
                        ?: "kallesh_authenticated_user"
                },
                onInstantPreviewAccess = {
                    val saved = SecureAuthManager.getSavedSession(application)
                    currentUserId = saved?.uid
                        ?: Firebase.auth.currentUser?.uid
                        ?: "kallesh_founder_preview"
                },
                onOpenPublicRoute = { routePath ->
                    pendingRoutePath = routePath
                    val saved = SecureAuthManager.getSavedSession(application)
                    currentUserId = saved?.uid
                        ?: Firebase.auth.currentUser?.uid
                        ?: "kallesh_founder_preview"
                }
            )
        }
    } else {
        val hubViewModel: KalleshHubViewModel = viewModel(
            key = "kallesh_hub_$activeUid",
            factory = KalleshHubViewModel.Factory(application, activeUid)
        )
        androidx.compose.runtime.LaunchedEffect(pendingRoutePath) {
            val route = pendingRoutePath
            if (!route.isNullOrBlank()) {
                hubViewModel.handleDeepLinkPath(route)
                pendingRoutePath = null
            }
        }
        val sdkPaymentEvent by RazorpaySdkGatewayManager.latestSdkEvent.collectAsStateWithLifecycle()
        androidx.compose.runtime.LaunchedEffect(sdkPaymentEvent) {
            when (val ev = sdkPaymentEvent) {
                is RazorpaySdkCallbackEvent.Success -> {
                    hubViewModel.verifyAndCompletePlanPayment(
                        orderId = ev.orderId,
                        paymentId = ev.paymentId,
                        signature = ev.signature,
                        paymentMethod = ev.paymentMethod
                    )
                    RazorpaySdkGatewayManager.consumeSdkEvent()
                }
                is RazorpaySdkCallbackEvent.Failure -> {
                    hubViewModel.cancelOrFailPlanPayment(ev.errorDescription)
                    RazorpaySdkGatewayManager.consumeSdkEvent()
                }
                null -> Unit
            }
        }
        val profileState by hubViewModel.userProfileState.collectAsStateWithLifecycle()
        val themePref = (profileState as? UiState.Success)?.data?.themeMode ?: "DARK"
        val useDark = when (themePref.uppercase()) {
            "LIGHT" -> false
            "SYSTEM" -> isSystemInDarkTheme()
            else -> true
        }

        MyApplicationTheme(darkTheme = useDark) {
            KalleshMainShell(
                viewModel = hubViewModel,
                onSignedOut = {
                    SecureAuthManager.clearSession(application)
                    currentUserId = null
                }
            )
        }
    }
}
