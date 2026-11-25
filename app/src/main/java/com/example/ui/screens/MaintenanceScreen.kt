package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MaintenanceConfig
import com.example.data.MaintenanceControlConfig
import com.example.ui.theme.KcetIndigoPrimary
import com.example.ui.theme.KcetVioletSecondary
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MaintenanceScreen(
    config: MaintenanceConfig,
    remainingMillisFlow: StateFlow<Long>,
    serverTimeOffsetMs: Long
) {
    // Prevent back button bypass while Maintenance Mode is active
    BackHandler(enabled = true) { /* Maintenance Mode locks normal navigation */ }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red < 0.3f && colorScheme.background.blue < 0.3f
    val backgroundBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0B1020),
                    Color(0xFF15193B),
                    Color(0xFF0F172A)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF5F7FF),
                    Color(0xFFEEF2FF),
                    Color(0xFFF8FAFF)
                )
            )
        }
    }

    val cardContainerColor = if (isDark) Color(0xFF1A2238) else Color.White
    val cardBorderColor = if (isDark) {
        KcetIndigoPrimary.copy(alpha = 0.35f)
    } else {
        KcetIndigoPrimary.copy(alpha = 0.14f)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("maintenance_screen"),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 540.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Maintenance Illustration (Interlocking Rotating Gears + Tool Emblem)
                    MaintenanceIllustration(
                        isDark = isDark,
                        modifier = Modifier
                            .size(164.dp)
                            .testTag("maintenance_illustration")
                            .semantics { contentDescription = "Application maintenance illustration" }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. Status Pill with subtle pulsing dot
                    MaintenanceStatusBadge(isDark = isDark)

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Heading
                    Text(
                        text = "We'll Be Back Soon!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("maintenance_heading")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Friendly Description
                    Text(
                        text = "We're making some improvements to give you a better experience. KCET Gen Z will be back shortly.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        ),
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("maintenance_description")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 5, 6, 7. Isolated Countdown Card (ONLY this subtree recomposes at 1Hz)
                    MaintenanceCountdownCard(
                        endTimeMillis = config.maintenanceEndTime,
                        remainingMillisFlow = remainingMillisFlow,
                        serverTimeOffsetMs = serverTimeOffsetMs,
                        isDark = isDark,
                        cardContainerColor = cardContainerColor,
                        cardBorderColor = cardBorderColor
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 8. Footer Message
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = "Thank you for your patience 💙",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .testTag("maintenance_footer")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceIllustration(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "maintenance_gears")
    val primaryGearAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primary_gear_rotation"
    )
    val secondaryGearAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondary_gear_rotation"
    )

    val haloColor = if (isDark) KcetIndigoPrimary.copy(alpha = 0.24f) else KcetIndigoPrimary.copy(alpha = 0.12f)
    val ringColor = if (isDark) KcetVioletSecondary.copy(alpha = 0.35f) else KcetIndigoPrimary.copy(alpha = 0.22f)
    val mainGearColor = if (isDark) Color(0xFF818CF8) else KcetIndigoPrimary
    val smallGearColor = if (isDark) Color(0xFFA78BFA) else KcetVioletSecondary
    val innerHoleColor = if (isDark) Color(0xFF15193B) else Color(0xFFEEF2FF)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = size.minDimension
            val center = Offset(size.width / 2f, size.height / 2f)

            // Soft atmospheric halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(haloColor, Color.Transparent),
                    center = center,
                    radius = minDim * 0.5f
                ),
                radius = minDim * 0.5f,
                center = center
            )

            // Dashed/subtle outer orbital accent ring
            drawCircle(
                color = ringColor,
                radius = minDim * 0.43f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Large primary gear
            val bigGearCenter = Offset(center.x - minDim * 0.08f, center.y + minDim * 0.04f)
            rotate(degrees = primaryGearAngle, pivot = bigGearCenter) {
                drawGear(
                    center = bigGearCenter,
                    outerRadius = minDim * 0.24f,
                    innerRadius = minDim * 0.175f,
                    holeRadius = minDim * 0.075f,
                    teeth = 8,
                    fillColor = mainGearColor,
                    holeColor = innerHoleColor
                )
            }

            // Interlocking smaller gear
            val smallGearCenter = Offset(center.x + minDim * 0.19f, center.y - minDim * 0.15f)
            rotate(degrees = secondaryGearAngle, pivot = smallGearCenter) {
                drawGear(
                    center = smallGearCenter,
                    outerRadius = minDim * 0.15f,
                    innerRadius = minDim * 0.105f,
                    holeRadius = minDim * 0.045f,
                    teeth = 7,
                    fillColor = smallGearColor,
                    holeColor = innerHoleColor
                )
            }
        }

        // Foreground tool badge anchored cleanly at bottom-right of illustration
        Surface(
            shape = CircleShape,
            color = if (isDark) Color(0xFF1E293B) else Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(1.5.dp, KcetIndigoPrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 12.dp)
                .size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF818CF8) else KcetIndigoPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawGear(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    holeRadius: Float,
    teeth: Int,
    fillColor: Color,
    holeColor: Color
) {
    val path = Path()
    val stepAngle = (2.0 * PI) / teeth
    for (i in 0 until teeth) {
        val baseAngle = i * stepAngle
        val a0 = baseAngle
        val a1 = baseAngle + stepAngle * 0.18
        val a2 = baseAngle + stepAngle * 0.34
        val a3 = baseAngle + stepAngle * 0.66
        val a4 = baseAngle + stepAngle * 0.82

        val p0 = Offset(center.x + innerRadius * cos(a0).toFloat(), center.y + innerRadius * sin(a0).toFloat())
        val p1 = Offset(center.x + outerRadius * cos(a1).toFloat(), center.y + outerRadius * sin(a1).toFloat())
        val p2 = Offset(center.x + outerRadius * cos(a2).toFloat(), center.y + outerRadius * sin(a2).toFloat())
        val p3 = Offset(center.x + innerRadius * cos(a3).toFloat(), center.y + innerRadius * sin(a3).toFloat())
        val p4 = Offset(center.x + innerRadius * cos(a4).toFloat(), center.y + innerRadius * sin(a4).toFloat())

        if (i == 0) path.moveTo(p0.x, p0.y) else path.lineTo(p0.x, p0.y)
        path.lineTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
        path.lineTo(p3.x, p3.y)
        path.lineTo(p4.x, p4.y)
    }
    path.close()
    drawPath(path = path, color = fillColor)
    drawCircle(color = holeColor, radius = holeRadius, center = center)
    drawCircle(
        color = fillColor.copy(alpha = 0.6f),
        radius = holeRadius * 0.55f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

@Composable
private fun MaintenanceStatusBadge(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "maintenance_status_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val badgeBg = if (isDark) Color(0xFF312E81).copy(alpha = 0.65f) else Color(0xFFEEF2FF)
    val badgeBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFFC7D2FE)
    val badgeTextColor = if (isDark) Color(0xFFC7D2FE) else Color(0xFF3730A3)
    val dotColor = if (isDark) Color(0xFF38BDF8) else KcetIndigoPrimary

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeBorder),
        modifier = Modifier.testTag("maintenance_status_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha * 0.4f)
                        .background(dotColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(dotColor, CircleShape)
                )
            }
            Text(
                text = "Maintenance in progress",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                ),
                color = badgeTextColor
            )
        }
    }
}

@Composable
private fun MaintenanceCountdownCard(
    endTimeMillis: Long,
    remainingMillisFlow: StateFlow<Long>,
    serverTimeOffsetMs: Long,
    isDark: Boolean,
    cardContainerColor: Color,
    cardBorderColor: Color
) {
    val remainingMillis by remainingMillisFlow.collectAsStateWithLifecycle()
    val parts = remember(remainingMillis) {
        MaintenanceControlConfig.formatCountdownParts(remainingMillis)
    }
    val expectedBackText = remember(endTimeMillis, serverTimeOffsetMs) {
        MaintenanceControlConfig.formatExpectedBackText(
            endTimeMillis = endTimeMillis,
            nowMillis = System.currentTimeMillis() + serverTimeOffsetMs
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("maintenance_countdown_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimated time remaining",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("maintenance_timer_label")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownDigitBox(
                    value = parts.hoursFormatted,
                    unitLabel = "HRS",
                    isDark = isDark,
                    modifier = Modifier.testTag("maintenance_timer_hours")
                )
                CountdownSeparator(isDark = isDark)
                CountdownDigitBox(
                    value = parts.minutesFormatted,
                    unitLabel = "MIN",
                    isDark = isDark,
                    modifier = Modifier.testTag("maintenance_timer_minutes")
                )
                CountdownSeparator(isDark = isDark)
                CountdownDigitBox(
                    value = parts.secondsFormatted,
                    unitLabel = "SEC",
                    isDark = isDark,
                    modifier = Modifier.testTag("maintenance_timer_seconds")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFF),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF334155) else Color(0xFFE0E7FF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF818CF8) else KcetIndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = expectedBackText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("maintenance_expected_back")
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownDigitBox(
    value: String,
    unitLabel: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val boxGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF283252), Color(0xFF1E2640))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))
            )
        }
    }
    val boxBorder = if (isDark) Color(0xFF4F46E5).copy(alpha = 0.45f) else Color(0xFFC7D2FE)
    val digitColor = if (isDark) Color.White else Color(0xFF1E1B4B)
    val labelColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, boxBorder),
            modifier = Modifier.widthIn(min = 76.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(boxGradient)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(220)) { height -> height / 3 } +
                            fadeIn(animationSpec = tween(220)))
                            .togetherWith(
                                slideOutVertically(animationSpec = tween(180)) { height -> -height / 3 } +
                                    fadeOut(animationSpec = tween(180))
                            )
                    },
                    label = "countdown_digit_transition"
                ) { targetDigit ->
                    Text(
                        text = targetDigit,
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFeatureSettings = "tnum",
                            letterSpacing = 1.sp
                        ),
                        color = digitColor,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = unitLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = labelColor
        )
    }
}

@Composable
private fun CountdownSeparator(isDark: Boolean) {
    Text(
        text = ":",
        style = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        ),
        color = if (isDark) Color(0xFF818CF8) else KcetIndigoPrimary,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .padding(bottom = 20.dp)
    )
}
