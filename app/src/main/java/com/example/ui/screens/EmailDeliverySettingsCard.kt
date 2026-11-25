package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.EmailDeliveryUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmailDeliverySettingsSection(
    uiState: EmailDeliveryUiState,
    onRefresh: () -> Unit,
    onEnableEmailJs: () -> Unit,
    onDisableEmailJs: () -> Unit,
    onEnableCustomVerification: () -> Unit = {},
    onDisableCustomVerification: () -> Unit = {},
    onDismissFeedback: () -> Unit
) {
    val context = LocalContext.current

    // Automatically fetch live backend settings when opening Email Delivery Settings
    LaunchedEffect(Unit) {
        onRefresh()
    }

    // Show Toast notification whenever backend confirms a state change
    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val settings = uiState.settings
    val isBusy = uiState.isLoading || uiState.isUpdating
    val isEmailJsEnabled = settings?.customEmailEnabled == true

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner Card matching KCET GenZ Admin Panel aesthetic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_delivery_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF10B981), Color(0xFF38BDF8))
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (isEmailJsEnabled) {
                                        Color(0xFF10B981).copy(alpha = 0.2f)
                                    } else {
                                        Color(0xFF6366F1).copy(alpha = 0.25f)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Delivery Settings",
                                tint = if (isEmailJsEnabled) Color(0xFF34D399) else Color(0xFFA5B4FC),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Email Delivery Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Shared EmailJS & Firebase Auth Delivery Config",
                                fontSize = 11.5.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }

                    // Status Pill Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            settings == null && uiState.isLoading -> Color(0xFF475569)
                            settings == null -> Color(0xFF64748B)
                            isEmailJsEnabled -> Color(0xFF10B981)
                            else -> Color(0xFFD97706)
                        },
                        modifier = Modifier.testTag("emailjs_header_status_badge")
                    ) {
                        Text(
                            text = when {
                                settings == null && uiState.isLoading -> "LOADING"
                                settings == null -> "UNSYNCED"
                                isEmailJsEnabled -> "ENABLED"
                                else -> "DISABLED"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Firebase Session Verification Info Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                            Color(0xFF10B981).copy(alpha = 0.45f)
                        } else {
                            Color(0xFFF59E0B).copy(alpha = 0.55f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                                Icons.Default.VerifiedUser
                            } else {
                                Icons.Default.Security
                            },
                            contentDescription = "Firebase Admin Auth Status",
                            tint = if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                                Color(0xFF34D399)
                            } else {
                                Color(0xFFFBBF24)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isFirebaseSignedIn) {
                                    "Firebase User: ${uiState.firebaseAuthEmail ?: "Signed In"}"
                                } else {
                                    "Firebase Authentication: Not Signed In"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    !uiState.isFirebaseSignedIn ->
                                        "Sign in with a verified Firebase Admin account to manage backend settings."
                                    uiState.isFirebaseEmailVerified ->
                                        "email_verified = true • Authenticated via Firebase ID Token"
                                    else ->
                                        "email_verified = false • Verify email before modifying settings"
                                },
                                fontSize = 10.5.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // Main Email Delivery Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_delivery_settings_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Backend Configuration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Synchronized with kcet-password-reset.vercel.app & Firestore app_config/email_delivery",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !isBusy,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("refresh_email_status_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4F46E5)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Status",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                if (isBusy) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_delivery_loading_indicator"),
                        color = Color(0xFF4F46E5),
                        trackColor = Color(0xFFE0E7FF)
                    )
                }

                // Success Banner
                if (!uiState.successMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_delivery_success_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.successMessage,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = onDismissFeedback,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.heightIn(min = 36.dp)
                            ) {
                                Text("OK", fontSize = 11.sp, color = Color(0xFF065F46))
                            }
                        }
                    }
                }

                // Error Banner
                if (!uiState.errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_delivery_error_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.errorMessage,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF991B1B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Current Settings Details
                if (settings != null) {
                    val statusColor = if (settings.emailjsStatus == "ENABLED") Color(0xFF059669) else Color(0xFFD97706)
                    val statusBg = if (settings.emailjsStatus == "ENABLED") Color(0xFFECFDF5) else Color(0xFFFFFBEB)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = statusBg,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (settings.customEmailEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "EmailJS Status",
                                tint = statusColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EmailJS Status",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = settings.emailjsStatus,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor,
                                    modifier = Modifier.testTag("emailjs_status_value")
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor
                            ) {
                                Text(
                                    text = settings.activeProvider.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    EmailSettingInfoRow(
                        icon = Icons.Default.MarkEmailRead,
                        label = "Active Provider",
                        value = settings.activeProvider,
                        valueColor = if (settings.activeProvider == "emailjs") Color(0xFF059669) else Color(0xFF4F46E5),
                        testTag = "active_provider_value"
                    )

                    EmailSettingInfoRow(
                        icon = Icons.Default.PowerSettingsNew,
                        label = "Custom Email Enabled",
                        value = settings.customEmailEnabled.toString(),
                        valueColor = if (settings.customEmailEnabled) Color(0xFF059669) else Color(0xFFDC2626),
                        testTag = "custom_email_enabled_value"
                    )

                    EmailSettingInfoRow(
                        icon = Icons.Default.Warning,
                        label = "Last Failure Reason",
                        value = settings.lastFailureReason?.takeIf { it.isNotBlank() } ?: "None",
                        valueColor = if (!settings.lastFailureReason.isNullOrBlank()) Color(0xFFDC2626) else Color(0xFF334155),
                        testTag = "last_failure_reason_value"
                    )

                    EmailSettingInfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Last Updated Time",
                        value = settings.lastUpdatedTime,
                        valueColor = Color(0xFF1E293B),
                        testTag = "last_updated_time_value"
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.isLoading) {
                                    "Fetching current Email Delivery Settings from backend..."
                                } else {
                                    "No backend configuration loaded yet. Tap Refresh Status to fetch live settings."
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Text(
                    text = "Control EmailJS Delivery Provider",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                // Action Buttons (Enable EmailJS / Disable EmailJS)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEnableEmailJs,
                        enabled = !isBusy && settings?.customEmailEnabled != true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("enable_emailjs_button")
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Enable EmailJS",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Enable EmailJS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onDisableEmailJs,
                        enabled = !isBusy && settings?.customEmailEnabled != false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("disable_emailjs_button")
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Disable EmailJS",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Disable EmailJS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Changes update Firestore app_config/email_delivery via the backend API and stay synchronized with the Web Admin Panel.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Separate Setting: Custom Email Verification (customVerificationEmailEnabled)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Email Verification",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Controls custom verification email delivery on registration and resend requests independently from password reset.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val customVerifEnabled = settings?.customVerificationEmailEnabled == true
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            settings == null -> Color(0xFF64748B)
                            customVerifEnabled -> Color(0xFF10B981)
                            else -> Color(0xFFD97706)
                        },
                        modifier = Modifier.testTag("custom_verification_status_badge")
                    ) {
                        Text(
                            text = when {
                                settings == null -> "UNSYNCED"
                                customVerifEnabled -> "ENABLED"
                                else -> "DISABLED"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (settings != null) {
                    EmailSettingInfoRow(
                        icon = Icons.Default.VerifiedUser,
                        label = "Custom Verification Email Enabled",
                        value = settings.customVerificationEmailEnabled.toString(),
                        valueColor = if (settings.customVerificationEmailEnabled) Color(0xFF059669) else Color(0xFFDC2626),
                        testTag = "custom_verification_enabled_value"
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEnableCustomVerification,
                        enabled = !isBusy && settings?.customVerificationEmailEnabled != true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("enable_custom_verification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Enable Custom Verification",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Custom Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onDisableCustomVerification,
                        enabled = !isBusy && settings?.customVerificationEmailEnabled != false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("disable_custom_verification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Disable Custom Verification",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Disable Custom Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailSettingInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
