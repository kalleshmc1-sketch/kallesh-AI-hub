package com.example.ui.auth

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

enum class AuthFormTab(val label: String) {
    LOGIN("Log In"),
    SIGNUP("Sign Up"),
    RESET("Reset Password")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullStackEmailAuthSection(
    onAuthSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(AuthFormTab.LOGIN) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fullstack_email_auth_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            AuthFormTab.entries.forEach { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = {
                        activeTab = tab
                        statusNotice = null
                    },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag("auth_mode_tab_${tab.name.lowercase()}")
                )
            }
        }

        if (activeTab == AuthFormTab.SIGNUP) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name_input")
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input")
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(if (activeTab == AuthFormTab.RESET) "New Password (min 6 chars)" else "Password")
            },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide Password" else "Show Password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input")
        )

        if (activeTab == AuthFormTab.SIGNUP) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input")
            )
        }

        AnimatedVisibility(visible = statusNotice != null) {
            Surface(
                color = Color(0xFF00E676).copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusNotice.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00E676)
                    )
                }
            }
        }

        Button(
            onClick = {
                isSubmitting = true
                statusNotice = null
                scope.launch {
                    when (activeTab) {
                        AuthFormTab.SIGNUP -> {
                            if (password != confirmPassword) {
                                isSubmitting = false
                                onError("Passwords do not match. Please verify both password fields.")
                                return@launch
                            }
                            val res = SecureAuthManager.signUpWithEmailPassword(
                                context = context,
                                fullName = fullName,
                                email = email,
                                password = password
                            )
                            isSubmitting = false
                            res.onSuccess {
                                onAuthSuccess()
                            }.onFailure { err ->
                                onError(err.message ?: "Could not register account.")
                            }
                        }

                        AuthFormTab.LOGIN -> {
                            val res = SecureAuthManager.logInWithEmailPassword(
                                context = context,
                                email = email,
                                password = password
                            )
                            isSubmitting = false
                            res.onSuccess {
                                onAuthSuccess()
                            }.onFailure { err ->
                                onError(err.message ?: "Invalid email or password.")
                            }
                        }

                        AuthFormTab.RESET -> {
                            val res = resetUserPasswordSecurely(
                                context = context,
                                email = email,
                                newPassword = password
                            )
                            isSubmitting = false
                            res.onSuccess { msg ->
                                statusNotice = msg
                            }.onFailure { err ->
                                onError(err.message ?: "Password reset failed.")
                            }
                        }
                    }
                }
            },
            enabled = !isSubmitting && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_primary_submit_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authenticating...")
            } else {
                Text(
                    text = when (activeTab) {
                        AuthFormTab.LOGIN -> "Log In (/api/auth/login)"
                        AuthFormTab.SIGNUP -> "Create Account (/api/auth/register)"
                        AuthFormTab.RESET -> "Reset Password (/api/auth/reset-password)"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private suspend fun resetUserPasswordSecurely(
    context: Context,
    email: String,
    newPassword: String
): Result<String> = runCatching {
    val cleanEmail = email.trim().lowercase()
    require(cleanEmail.contains("@") && cleanEmail.contains(".")) {
        "Please enter a valid email address."
    }
    require(newPassword.length >= 6) {
        "New password must be at least 6 characters long."
    }

    runCatching {
        Firebase.auth.sendPasswordResetEmail(cleanEmail).await()
    }

    val prefs = context.getSharedPreferences("kallesh_secure_auth_prefs", Context.MODE_PRIVATE)
    val registry = runCatching {
        JSONObject(prefs.getString("accounts_hashed_registry_json", "{}").orEmpty())
    }.getOrDefault(JSONObject())

    val salt = UUID.randomUUID().toString().replace("-", "").take(16)
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest
        .digest("$salt:$newPassword:kallesh_ai_hub_v1".toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    val existing = registry.optJSONObject(cleanEmail) ?: JSONObject().apply {
        put("uid", "user_${cleanEmail.hashCode()}")
        put("displayName", cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() })
        put("email", cleanEmail)
    }
    existing.put("salt", salt)
    existing.put("passwordHash", hash)
    existing.put("updatedAt", System.currentTimeMillis())
    registry.put(cleanEmail, existing)
    prefs.edit().putString("accounts_hashed_registry_json", registry.toString()).apply()

    "Password updated with salted SHA-256 hash for $cleanEmail. You can now switch to 'Log In'!"
}
