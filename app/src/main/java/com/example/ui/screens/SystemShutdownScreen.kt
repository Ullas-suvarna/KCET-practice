package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SystemShutdownScreen(
    onVerifySecurityKey: suspend (String) -> Boolean,
    onAdminDirectLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showKeyDialog by remember { mutableStateOf(false) }
    var enteredKey by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var keyError by remember { mutableStateOf<String?>(null) }

    val submitKey: () -> Unit = {
        val candidate = enteredKey.trim()
        if (candidate.isEmpty()) {
            keyError = "Please enter the security key."
        } else if (!isVerifying) {
            isVerifying = true
            keyError = null
            scope.launch {
                val matched = try {
                    onVerifySecurityKey(candidate)
                } catch (_: Exception) {
                    false
                }
                isVerifying = false
                if (matched) {
                    showKeyDialog = false
                    enteredKey = ""
                    keyError = null
                    onAdminDirectLogin()
                } else {
                    keyError = "Incorrect security key. Access denied."
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Emergency Admin Access '!' Button
            FilledTonalButton(
                onClick = {
                    enteredKey = ""
                    keyError = null
                    keyVisible = false
                    showKeyDialog = true
                },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color(0xFF111827)
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(52.dp)
                    .testTag("shutdown_admin_access_button")
                    .semantics { contentDescription = "Admin emergency access" }
            ) {
                Text(
                    text = "!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Center Maintenance / Service Unavailable Card
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .testTag("system_shutdown_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = Color(0xFFEF4444).copy(alpha = 0.16f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "System Shut Down",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFCA5A5),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Service Not Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "KCET Gen Z Student and Mentor portals are currently shut down for maintenance. Please try again later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Portal Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "• Student Portal: Not Available",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "• Mentor Portal: Not Available",
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isVerifying) showKeyDialog = false
            },
            modifier = Modifier.testTag("shutdown_security_key_dialog"),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF4F46E5).copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Security Key",
                        tint = Color(0xFF4F46E5)
                    )
                }
            },
            title = {
                Text(
                    text = "Enter Security Key",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the Admin security key to directly sign in to the Admin Portal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = enteredKey,
                        onValueChange = {
                            enteredKey = it
                            keyError = null
                        },
                        label = { Text("Security Key") },
                        singleLine = true,
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (keyVisible) "Hide security key" else "Show security key"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submitKey() }),
                        isError = keyError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shutdown_security_key_input")
                    )
                    if (keyError != null) {
                        Text(
                            text = keyError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("shutdown_security_key_error")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = submitKey,
                    enabled = !isVerifying,
                    modifier = Modifier.testTag("shutdown_security_key_submit")
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Unlock Admin Portal")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { if (!isVerifying) showKeyDialog = false },
                    enabled = !isVerifying
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
