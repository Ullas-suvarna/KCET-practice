package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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

    val infiniteTransition = rememberInfiniteTransition(label = "shutdown_ambient")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phone_float"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF04081B),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04071B),
                            Color(0xFF090F2E),
                            Color(0xFF070A22)
                        )
                    )
                )
        ) {
            // Cosmic corner flares & layered bottom aurora waves
            CosmicShutdownBackdrop(
                glowPulse = glowPulse,
                modifier = Modifier.fillMaxSize()
            )

            // Main centered scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 460.dp)
                            .fillMaxWidth()
                            .padding(top = 28.dp, bottom = 16.dp)
                            .testTag("system_shutdown_card"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top KG neon emblem
                        KgNeonEmblem(
                            glowPulse = glowPulse,
                            modifier = Modifier.size(96.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Brand Wordmark: KCET GenZ
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color.White,
                                        fontSize = 31.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontStyle = FontStyle.Italic
                                    )
                                ) {
                                    append("KCET Gen")
                                }
                                withStyle(
                                    SpanStyle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFC084FC),
                                                Color(0xFF60A5FA),
                                                Color(0xFF22D3EE)
                                            )
                                        ),
                                        fontSize = 35.sp,
                                        fontWeight = FontWeight.Black,
                                        fontStyle = FontStyle.Italic
                                    )
                                ) {
                                    append("Z")
                                }
                            },
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "PREP  \u2022  PRACTICE  \u2022  EXCEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 3.2.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // 3D-style glowing smartphone + power icon + orbital ring + purple minus badge
                        ServiceUnavailablePhoneIllustration(
                            floatOffsetDp = floatOffset,
                            glowPulse = glowPulse,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Headline: Service Not / Available
                        Text(
                            text = "Service Not",
                            fontSize = 34.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Available",
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE879F9),
                                        Color(0xFF818CF8),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                fontSize = 34.sp,
                                lineHeight = 40.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "KCET GenZ is currently shut down.\nIt may be temporary or permanent.\nPlease check back later.",
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Horizontal divider with center glowing (i) badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF6366F1).copy(alpha = 0.65f)
                                            )
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            InfoCircleBadge(modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF6366F1).copy(alpha = 0.65f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Thank you for your support! + curved underline
                        Text(
                            text = "Thank you for your support!",
                            fontSize = 17.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF1F5F9),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Canvas(
                            modifier = Modifier
                                .width(150.dp)
                                .height(10.dp)
                        ) {
                            if (size.minDimension <= 1f) return@Canvas
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.72f)
                                quadraticTo(
                                    size.width * 0.48f,
                                    -size.height * 0.1f,
                                    size.width,
                                    size.height * 0.45f
                                )
                            }
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFC084FC),
                                        Color(0xFF818CF8),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Bottom footer anchored cleanly above navigation bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp, top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF60A5FA).copy(alpha = 0.35f),
                                        Color(0xFFC084FC).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "KCET GenZ  \u2022  Your Exam Companion",
                        fontSize = 12.sp,
                        letterSpacing = 0.7.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Discrete Admin Emergency Access '!' Button (top-right safe area)
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
                    containerColor = Color(0xFF141B3A).copy(alpha = 0.78f),
                    contentColor = Color(0xFFC084FC)
                ),
                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.55f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 16.dp)
                    .size(48.dp)
                    .testTag("shutdown_admin_access_button")
                    .semantics { contentDescription = "Admin emergency access" }
            ) {
                Text(
                    text = "!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE9D5FF)
                )
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

@Composable
private fun CosmicShutdownBackdrop(
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        // Top-left diagonal purple/magenta aurora flare
        val topLeftWave = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.42f, 0f)
            cubicTo(
                w * 0.28f, h * 0.09f,
                w * 0.14f, h * 0.14f,
                0f, h * 0.22f
            )
            close()
        }
        drawPath(
            path = topLeftWave,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFD946EF).copy(alpha = 0.55f * glowPulse),
                    Color(0xFF7C3AED).copy(alpha = 0.35f),
                    Color(0xFF1E3A8A).copy(alpha = 0.12f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w * 0.4f, h * 0.22f)
            )
        )

        // Top-right deep royal-blue glow sweep
        val topRightWave = Path().apply {
            moveTo(w * 0.55f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.28f)
            cubicTo(
                w * 0.85f, h * 0.18f,
                w * 0.72f, h * 0.08f,
                w * 0.55f, 0f
            )
            close()
        }
        drawPath(
            path = topRightWave,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2563EB).copy(alpha = 0.48f * glowPulse),
                    Color(0xFF1E3A8A).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(w * 0.92f, h * 0.06f),
                radius = w * 0.65f
            )
        )

        // Subtle central ambient aura behind the phone
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3B82F6).copy(alpha = 0.16f * glowPulse),
                    Color(0xFF7C3AED).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.41f),
                radius = w * 0.52f
            ),
            radius = w * 0.52f,
            center = Offset(w * 0.5f, h * 0.41f)
        )

        // Bottom back wave (rich violet/purple rising from right)
        val backBottomWave = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.91f)
            cubicTo(
                w * 0.32f, h * 0.93f,
                w * 0.66f, h * 0.80f,
                w, h * 0.82f
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = backBottomWave,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1B4B).copy(alpha = 0.9f),
                    Color(0xFF7C3AED).copy(alpha = 0.62f * glowPulse),
                    Color(0xFFC026D3).copy(alpha = 0.48f * glowPulse)
                ),
                start = Offset(w * 0.25f, h * 0.96f),
                end = Offset(w, h * 0.82f)
            )
        )

        // Bottom front wave (vibrant electric cyan-blue from left sweeping right)
        val frontBottomWave = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.84f)
            cubicTo(
                w * 0.26f, h * 0.80f,
                w * 0.58f, h * 0.95f,
                w, h * 0.88f
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = frontBottomWave,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0EA5E9).copy(alpha = 0.78f * glowPulse),
                    Color(0xFF2563EB).copy(alpha = 0.55f),
                    Color(0xFF4F46E5).copy(alpha = 0.45f)
                ),
                start = Offset(0f, h * 0.84f),
                end = Offset(w, h * 0.95f)
            )
        )

        // Soft glowing rim highlight along front bottom wave
        val crestPath = Path().apply {
            moveTo(0f, h * 0.84f)
            cubicTo(
                w * 0.26f, h * 0.80f,
                w * 0.58f, h * 0.95f,
                w, h * 0.88f
            )
        }
        drawPath(
            path = crestPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.6f),
                    Color(0xFF818CF8).copy(alpha = 0.35f),
                    Color(0xFFC084FC).copy(alpha = 0.5f)
                )
            ),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun KgNeonEmblem(
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFFC026D3),
            Color(0xFFF472B6)
        )
    )
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0xFF7C3AED),
                spotColor = Color(0xFF38BDF8)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF070D24))
            .border(width = 2.dp, brush = borderBrush, shape = RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width
            val h = size.height
            if (w <= 2f || h <= 2f) return@Canvas

            val neonGrad = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF22D3EE),
                    Color(0xFF60A5FA),
                    Color(0xFFA855F7),
                    Color(0xFFEC4899)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            // Soft glow behind emblem
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.25f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w / 2f, h / 2f)
            )

            // Graduation Cap mortarboard diamond at top
            val capPath = Path().apply {
                moveTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.92f, h * 0.23f)
                lineTo(w * 0.50f, h * 0.39f)
                lineTo(w * 0.08f, h * 0.23f)
                close()
            }
            drawPath(
                path = capPath,
                brush = neonGrad,
                style = Stroke(width = 2.6.dp.toPx(), join = StrokeJoin.Round)
            )
            // Cap skull curve
            val skullPath = Path().apply {
                moveTo(w * 0.24f, h * 0.30f)
                lineTo(w * 0.24f, h * 0.43f)
                quadraticTo(w * 0.50f, h * 0.52f, w * 0.76f, h * 0.43f)
                lineTo(w * 0.76f, h * 0.30f)
            }
            drawPath(
                path = skullPath,
                brush = neonGrad,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )
            // Cap tassel on right
            drawLine(
                brush = neonGrad,
                start = Offset(w * 0.87f, h * 0.25f),
                end = Offset(w * 0.87f, h * 0.45f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                brush = neonGrad,
                radius = 2.6.dp.toPx(),
                center = Offset(w * 0.87f, h * 0.47f)
            )

            // Stylized 'K' on left
            val kStroke = 3.2.dp.toPx()
            val kPath = Path().apply {
                moveTo(w * 0.24f, h * 0.48f)
                lineTo(w * 0.24f, h * 0.89f)
                moveTo(w * 0.47f, h * 0.49f)
                lineTo(w * 0.25f, h * 0.69f)
                lineTo(w * 0.48f, h * 0.89f)
            }
            drawPath(
                path = kPath,
                brush = neonGrad,
                style = Stroke(width = kStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Stylized 'G' on right
            val gRect = Rect(
                left = w * 0.51f,
                top = h * 0.49f,
                right = w * 0.82f,
                bottom = h * 0.89f
            )
            drawArc(
                brush = neonGrad,
                startAngle = -42f,
                sweepAngle = -278f,
                useCenter = false,
                topLeft = gRect.topLeft,
                size = gRect.size,
                style = Stroke(width = kStroke, cap = StrokeCap.Round)
            )
            val gBar = Path().apply {
                moveTo(w * 0.68f, h * 0.70f)
                lineTo(w * 0.82f, h * 0.70f)
                lineTo(w * 0.82f, h * 0.85f)
            }
            drawPath(
                path = gBar,
                brush = neonGrad,
                style = Stroke(width = kStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
private fun ServiceUnavailablePhoneIllustration(
    floatOffsetDp: Float,
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        val centerX = w * 0.5f
        val centerY = h * 0.49f + floatOffsetDp.dp.toPx()
        val unit = minOf(w, h)

        // Floor shadow glow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.28f * glowPulse),
                    Color(0xFF7C3AED).copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(centerX, h * 0.90f),
                radius = unit * 0.65f
            ),
            topLeft = Offset(centerX - unit * 0.62f, h * 0.83f),
            size = Size(unit * 1.24f, unit * 0.14f)
        )

        // Back orbital arc
        drawOrbitalRing(
            center = Offset(centerX, centerY + unit * 0.04f),
            rx = unit * 0.68f,
            ry = unit * 0.20f,
            startAngle = 175f,
            sweepAngle = 195f,
            glowPulse = glowPulse
        )

        // Small ambient floating orbs & accent rays
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)),
                center = Offset(centerX - unit * 0.62f, centerY - unit * 0.16f),
                radius = 6.dp.toPx()
            ),
            radius = 5.5.dp.toPx(),
            center = Offset(centerX - unit * 0.62f, centerY - unit * 0.16f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFA855F7), Color(0xFF31105E)),
                center = Offset(centerX + unit * 0.72f, centerY + unit * 0.20f),
                radius = 4.dp.toPx()
            ),
            radius = 3.5.dp.toPx(),
            center = Offset(centerX + unit * 0.72f, centerY + unit * 0.20f)
        )
        // Top-right purple spark rays
        drawLine(
            color = Color(0xFFA855F7),
            start = Offset(centerX + unit * 0.45f, centerY - unit * 0.34f),
            end = Offset(centerX + unit * 0.51f, centerY - unit * 0.44f),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF7C3AED),
            start = Offset(centerX + unit * 0.52f, centerY - unit * 0.25f),
            end = Offset(centerX + unit * 0.62f, centerY - unit * 0.30f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Tilted 3D-style smartphone
        val phoneW = unit * 0.50f
        val phoneH = unit * 0.78f
        val phoneTopLeft = Offset(centerX - phoneW / 2f, centerY - phoneH / 2f)
        val phoneRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())

        rotate(degrees = -7f, pivot = Offset(centerX, centerY)) {
            // Left metallic side thickness edge
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF93C5FD),
                        Color(0xFF6366F1),
                        Color(0xFF38BDF8)
                    )
                ),
                topLeft = Offset(phoneTopLeft.x - 4.dp.toPx(), phoneTopLeft.y + 2.dp.toPx()),
                size = Size(phoneW, phoneH),
                cornerRadius = phoneRadius
            )

            // Main dark phone chassis
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0E1638),
                        Color(0xFF070B20),
                        Color(0xFF141840)
                    ),
                    start = phoneTopLeft,
                    end = Offset(phoneTopLeft.x + phoneW, phoneTopLeft.y + phoneH)
                ),
                topLeft = phoneTopLeft,
                size = Size(phoneW, phoneH),
                cornerRadius = phoneRadius
            )

            // Neon metallic bezel rim
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFBAE6FD),
                        Color(0xFF818CF8),
                        Color(0xFF60A5FA)
                    ),
                    start = phoneTopLeft,
                    end = Offset(phoneTopLeft.x + phoneW, phoneTopLeft.y + phoneH)
                ),
                topLeft = phoneTopLeft,
                size = Size(phoneW, phoneH),
                cornerRadius = phoneRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Top speaker notch pill
            drawRoundRect(
                color = Color(0xFF2E3A6B),
                topLeft = Offset(centerX - phoneW * 0.16f, phoneTopLeft.y + 8.dp.toPx()),
                size = Size(phoneW * 0.32f, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Glowing Power icon on phone screen
            val pwrRadius = phoneW * 0.25f
            val pwrCenter = Offset(centerX, centerY - phoneH * 0.03f)
            // Soft white/cyan glow behind power icon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF60A5FA).copy(alpha = 0.28f * glowPulse),
                        Color.Transparent
                    ),
                    center = pwrCenter,
                    radius = pwrRadius * 1.9f
                ),
                radius = pwrRadius * 1.9f,
                center = pwrCenter
            )
            drawArc(
                color = Color(0xFFF8FAFC),
                startAngle = -52f,
                sweepAngle = 284f,
                useCenter = false,
                topLeft = Offset(pwrCenter.x - pwrRadius, pwrCenter.y - pwrRadius),
                size = Size(pwrRadius * 2f, pwrRadius * 2f),
                style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawLine(
                color = Color(0xFFF8FAFC),
                start = Offset(pwrCenter.x, pwrCenter.y - pwrRadius * 1.18f),
                end = Offset(pwrCenter.x, pwrCenter.y - pwrRadius * 0.06f),
                strokeWidth = 5.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Front orbital arc sweeping across lower half of phone
        drawOrbitalRing(
            center = Offset(centerX, centerY + unit * 0.04f),
            rx = unit * 0.68f,
            ry = unit * 0.20f,
            startAngle = -15f,
            sweepAngle = 195f,
            glowPulse = glowPulse
        )

        // Overlapping 3D glowing purple minus/unavailable circle badge at bottom-right
        val badgeCenter = Offset(centerX + unit * 0.24f, centerY + unit * 0.23f)
        val badgeRadius = unit * 0.165f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFA855F7).copy(alpha = 0.45f * glowPulse),
                    Color.Transparent
                ),
                center = badgeCenter,
                radius = badgeRadius * 1.7f
            ),
            radius = badgeRadius * 1.7f,
            center = badgeCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFC084FC),
                    Color(0xFF9333EA),
                    Color(0xFF5B21B6)
                ),
                center = Offset(badgeCenter.x - badgeRadius * 0.3f, badgeCenter.y - badgeRadius * 0.35f),
                radius = badgeRadius * 1.45f
            ),
            radius = badgeRadius,
            center = badgeCenter
        )
        drawCircle(
            color = Color(0xFFE9D5FF).copy(alpha = 0.5f),
            radius = badgeRadius,
            center = badgeCenter,
            style = Stroke(width = 1.2.dp.toPx())
        )
        // Bold white tilted horizontal minus bar
        rotate(degrees = 10f, pivot = badgeCenter) {
            drawLine(
                color = Color.White,
                start = Offset(badgeCenter.x - badgeRadius * 0.46f, badgeCenter.y),
                end = Offset(badgeCenter.x + badgeRadius * 0.46f, badgeCenter.y),
                strokeWidth = 4.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawOrbitalRing(
    center: Offset,
    rx: Float,
    ry: Float,
    startAngle: Float,
    sweepAngle: Float,
    glowPulse: Float
) {
    val ringBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFFC026D3)
        ),
        startX = center.x - rx,
        endX = center.x + rx
    )
    rotate(degrees = -16f, pivot = center) {
        // Outer glow stroke
        drawArc(
            brush = ringBrush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2f, ry * 2f),
            alpha = 0.28f * glowPulse,
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
        // Sharp neon core stroke
        drawArc(
            brush = ringBrush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2f, ry * 2f),
            alpha = 0.92f,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun InfoCircleBadge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        val center = Offset(w / 2f, h / 2f)
        val radius = minOf(w, h) * 0.45f
        drawCircle(
            color = Color(0xFF7C3AED).copy(alpha = 0.16f),
            radius = radius,
            center = center
        )
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE879F9), Color(0xFF818CF8))
            ),
            radius = radius,
            center = center,
            style = Stroke(width = 1.6.dp.toPx())
        )
        // Dot of 'i'
        drawCircle(
            color = Color(0xFFE879F9),
            radius = 1.4.dp.toPx(),
            center = Offset(center.x, center.y - radius * 0.36f)
        )
        // Stem of 'i'
        drawLine(
            color = Color(0xFFE879F9),
            start = Offset(center.x, center.y - radius * 0.08f),
            end = Offset(center.x, center.y + radius * 0.44f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
