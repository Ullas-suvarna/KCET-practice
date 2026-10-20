package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@Composable
fun DeveloperIntroScreen(
    onFinishIntro: () -> Unit,
    onSkip: (() -> Unit)? = null
) {
    val handleFinish = onSkip ?: onFinishIntro

    BackHandler {
        handleFinish()
    }

    val scope = rememberCoroutineScope()
    var isFinishing by remember { mutableStateOf(false) }

    // Screen transition slide-out
    val screenSlideOffsetX = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    val triggerTransition = {
        if (!isFinishing) {
            isFinishing = true
            scope.launch {
                launch {
                    screenSlideOffsetX.animateTo(-600f, tween(320, easing = FastOutSlowInEasing))
                }
                launch {
                    screenAlpha.animateTo(0f, tween(260))
                }
                delay(300)
                handleFinish()
            }
        }
    }

    // Interactive mouse / touch parallax position
    var mouseX by remember { mutableFloatStateOf(0.5f) }
    var mouseY by remember { mutableFloatStateOf(0.5f) }

    // Infinite animations for glowing rings, particles, and arrow
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val energyRingRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    val arrowSlideX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_slide"
    )

    // Staggered entrance animations
    val contentEntranceAlpha = remember { Animatable(0f) }
    val contentEntranceOffsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch {
            contentEntranceAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            contentEntranceOffsetY.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060312))
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val isWideScreen = maxWidth >= 768.dp

        // Layered Futuristic Dark Canvas Background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Deep Midnight Base Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070314),
                        Color(0xFF0A0620),
                        Color(0xFF0F0B30),
                        Color(0xFF160F3F),
                        Color(0xFF090418)
                    )
                )
            )

            // 2. Cybernetic Grid Dots (Top-Left & Right)
            val dotSpacing = 32.dp.toPx()
            val dotRadius = 1.2.dp.toPx()
            val gridCols = (canvasW / dotSpacing).toInt()
            val gridRows = (canvasH * 0.45f / dotSpacing).toInt()

            for (r in 0 until gridRows) {
                for (c in 0 until gridCols) {
                    if ((r + c) % 2 == 0) {
                        val alphaFactor = ((1f - (r.toFloat() / gridRows)) * 0.18f).coerceIn(0f, 0.25f)
                        drawCircle(
                            color = Color(0xFF818CF8).copy(alpha = alphaFactor),
                            radius = dotRadius,
                            center = Offset(c * dotSpacing + 16.dp.toPx(), r * dotSpacing + 16.dp.toPx())
                        )
                    }
                }
            }

            // 3. Atmospheric Luminous Orbs
            // Top Right Cyan/Purple Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.22f * glowAlpha),
                        Color(0xFF6366F1).copy(alpha = 0.15f * glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.85f, canvasH * 0.2f),
                    radius = canvasW * 0.55f
                ),
                radius = canvasW * 0.55f,
                center = Offset(canvasW * 0.85f, canvasH * 0.2f)
            )

            // Bottom Left Magenta / Violet Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD946EF).copy(alpha = 0.20f * glowAlpha),
                        Color(0xFF8B5CF6).copy(alpha = 0.12f * glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.15f, canvasH * 0.75f),
                    radius = canvasW * 0.6f
                ),
                radius = canvasW * 0.6f,
                center = Offset(canvasW * 0.15f, canvasH * 0.75f)
            )
        }

        // Main Scrollable Container to guarantee fit on any screen size
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .offset { IntOffset(screenSlideOffsetX.value.roundToInt(), 0) }
                .graphicsLayer { alpha = screenAlpha.value }
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWideScreen) 32.dp else 18.dp, vertical = 12.dp)
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // -------------------------------------------------------------
            // 1. TOP BAR: Page Indicators + SKIP Button
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtle glowing grid accent
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(0.6f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0..2) {
                            for (j in 0..2) {
                                drawCircle(
                                    color = Color(0xFFA78BFA),
                                    radius = 1.2.dp.toPx(),
                                    center = Offset(i * 6.dp.toPx() + 4.dp.toPx(), j * 6.dp.toPx() + 4.dp.toPx())
                                )
                            }
                        }
                    }
                }

                // 3 Glowing Page Indicator Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pill 1: Active
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFD946EF), Color(0xFF8B5CF6))
                                )
                            )
                            .shadow(8.dp, spotColor = Color(0xFFD946EF))
                    )

                    // Pill 2: Inactive
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF312E81).copy(alpha = 0.5f))
                    )

                    // Pill 3: Inactive
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF312E81).copy(alpha = 0.5f))
                    )
                }

                // SKIP -> Button
                Surface(
                    onClick = { triggerTransition() },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F0B26).copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.6f), Color(0xFFA855F7).copy(alpha = 0.6f)))
                    ),
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SKIP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Skip",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -------------------------------------------------------------
            // 2. HERO BRANDING & 3D HOLOGRAPHIC LOGO SECTION
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentEntranceAlpha.value
                        translationY = contentEntranceOffsetY.value
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Welcome to / KCET / GEN Z / Tagline
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(end = 8.dp)
                    ) {
                        // "Welcome to" with script styling & pink underline flourish
                        Column {
                            Text(
                                text = "Welcome to",
                                fontSize = 23.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF8FAFC),
                                letterSpacing = 0.5.sp
                            )

                            // Pink underline brush stroke
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .width(72.dp)
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // "KCET" (Bold display font)
                        Text(
                            text = "KCET",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                            letterSpacing = 2.sp,
                            lineHeight = 44.sp,
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = 12f
                            }
                        )

                        // "GEN Z" (Vibrant Gradient: Cyan -> Violet -> Magenta)
                        Text(
                            text = "GEN Z",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 3.sp,
                            lineHeight = 44.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF8B5CF6),
                                        Color(0xFFEC4899),
                                        Color(0xFFFF007A)
                                    )
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tagline Pill: "YOUR SMART COMPANION FOR KCET SUCCESS."
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0A0724).copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        Color(0xFFA855F7).copy(alpha = 0.25f)
                                    )
                                )
                            )
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("YOUR ")
                                    withStyle(
                                        SpanStyle(
                                            color = Color(0xFF00E5FF),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    ) {
                                        append("SMART")
                                    }
                                    append(" COMPANION\nFOR KCET SUCCESS.")
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1),
                                letterSpacing = 1.2.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Micro Tag: ">>> PREPARE • PRACTICE • PROGRESS"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ">>> ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEC4899),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PREPARE  •  PRACTICE  •  PROGRESS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Right Column: 3D Holographic "KG" Graduation Cap Emblem Portal
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Multi-tiered glowing circular pedestal below emblem
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Outer glowing disc
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899).copy(alpha = 0.45f * glowAlpha),
                                        Color(0xFF8B5CF6).copy(alpha = 0.25f * glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                topLeft = Offset(w * 0.05f, h * 0.2f),
                                size = androidx.compose.ui.geometry.Size(w * 0.9f, h * 0.7f)
                            )

                            // Inner bright ring
                            drawOval(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6), Color(0xFFEC4899))
                                ),
                                topLeft = Offset(w * 0.18f, h * 0.35f),
                                size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.4f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Holographic Hexagonal Frame with 3D Cap & "KG"
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(pulseScale)
                                .align(Alignment.Center)
                        ) {
                            // Holographic Hexagon Ring Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.width * 0.46f

                                val hexPath = Path()
                                for (i in 0..5) {
                                    val angle = Math.toRadians((i * 60 - 30).toDouble())
                                    val x = (center.x + radius * cos(angle)).toFloat()
                                    val y = (center.y + radius * sin(angle)).toFloat()
                                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                                }
                                hexPath.close()

                                // Hexagon Glow Border
                                drawPath(
                                    path = hexPath,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF8B5CF6),
                                            Color(0xFFEC4899),
                                            Color(0xFF00E5FF)
                                        )
                                    ),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Inner Glass Plate with Graduation Cap & "KG"
                            Surface(
                                modifier = Modifier
                                    .size(98.dp)
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF090620).copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(Color(0xFF38BDF8).copy(alpha = 0.6f), Color(0xFFEC4899).copy(alpha = 0.6f))
                                    )
                                ),
                                shadowElevation = 14.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Graduation Cap",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier
                                            .size(34.dp)
                                            .shadow(8.dp, spotColor = Color(0xFF38BDF8))
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "KG",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        style = androidx.compose.ui.text.TextStyle(
                                            brush = Brush.linearGradient(
                                                listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFA78BFA))
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------
            // 3. THREE PREMIUM FEATURE CARDS (AI Doubts, Smart Practice, Track Progress)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentEntranceAlpha.value
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: AI-Powered Doubt Solver
                FeatureGlassCard(
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF8B5CF6),
                    iconVector = Icons.Default.Memory,
                    iconBgBrush = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))),
                    prefixTitle = "AI-Powered",
                    prefixColor = Color(0xFFF472B6),
                    mainTitle = "DOUBT SOLVER",
                    description = "Get instant answers and detailed explanations powered by AI.",
                    mouseX = mouseX,
                    mouseY = mouseY
                )

                // Card 2: Smart Practice
                FeatureGlassCard(
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF06B6D4),
                    iconVector = Icons.Default.TrackChanges,
                    iconBgBrush = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4))),
                    prefixTitle = "Smart",
                    prefixColor = Color(0xFF38BDF8),
                    mainTitle = "PRACTICE",
                    description = "Practice KCET questions smartly with topic-wise tests and quizzes.",
                    mouseX = mouseX,
                    mouseY = mouseY
                )

                // Card 3: Track Progress
                FeatureGlassCard(
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFFEC4899),
                    iconVector = Icons.Default.EmojiEvents,
                    iconBgBrush = Brush.linearGradient(listOf(Color(0xFFDB2777), Color(0xFFE11D48))),
                    prefixTitle = "Track",
                    prefixColor = Color(0xFFFB7185),
                    mainTitle = "PROGRESS",
                    description = "Track your performance and improve every single day.",
                    mouseX = mouseX,
                    mouseY = mouseY
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -------------------------------------------------------------
            // 4. CREATOR / DEVELOPER IDENTITY CARD (Cyberpunk Skyline & Glowing Identity)
            // -------------------------------------------------------------
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentEntranceAlpha.value
                    },
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0B0726).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.7f),
                            Color(0xFF8B5CF6).copy(alpha = 0.8f),
                            Color(0xFFEC4899).copy(alpha = 0.7f)
                        )
                    )
                ),
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                ) {
                    // Cyberpunk Developer Art Background Image
                    Image(
                        painter = painterResource(id = R.drawable.img_developer_card_bg_1788180145090),
                        contentDescription = "Developer Workspace",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )

                    // Gradient Overlay for Crystal Clear Typography Contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF090518).copy(alpha = 0.95f),
                                        Color(0xFF0D082A).copy(alpha = 0.82f),
                                        Color(0xFF130938).copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )

                    // Developer Card Foreground Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Pill: </> APP BUILT BY
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E103A).copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                Color(0xFFA855F7).copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "</>",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF472B6)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APP BUILT BY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Creator Name: "Ullas U Suvarna" (or Ullas Suvarna) with Blue Verified Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ullas Suvarna",
                                fontSize = 25.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Verified Badge Checkmark
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0EA5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Creator",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Institution: 🎓 ENGINEERING STUDENT – MITE
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ENGINEERING STUDENT – MITE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBAE6FD),
                                letterSpacing = 0.8.sp
                            )
                        }

                        // Signature Quote: "❝ Building the future, one line of code at a time. ❞"
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "❝ ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEC4899)
                            )
                            Text(
                                text = "Building the future, one line of code at a time.",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE2E8F0).copy(alpha = 0.85f)
                            )
                            Text(
                                text = " ❞",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEC4899)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // -------------------------------------------------------------
            // 5. INTERACTIVE "SWIPE TO CONTINUE" CTA SLIDER
            // -------------------------------------------------------------
            SwipeToContinueSlider(
                onSwipeComplete = { triggerTransition() },
                arrowSlideX = arrowSlideX
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Micro Caption: ✦ LEVEL UP YOUR FUTURE ✦
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "✦",
                    fontSize = 9.sp,
                    color = Color(0xFFEC4899)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LEVEL  UP  YOUR  FUTURE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✦",
                    fontSize = 9.sp,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}

/**
 * Premium Glassmorphism Feature Card Component
 */
@Composable
private fun FeatureGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    iconVector: ImageVector,
    iconBgBrush: Brush,
    prefixTitle: String,
    prefixColor: Color,
    mainTitle: String,
    description: String,
    mouseX: Float,
    mouseY: Float
) {
    // Subtle tilt from mouse cursor
    val tiltX = (mouseY - 0.5f) * 4f
    val tiltY = (mouseX - 0.5f) * -4f

    Surface(
        modifier = modifier
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0B0722).copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.65f),
                    accentColor.copy(alpha = 0.15f)
                )
            )
        ),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon in glowing circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgBrush)
                    .shadow(10.dp, spotColor = accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = mainTitle,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prefix Title in Script / Italic
            Text(
                text = prefixTitle,
                fontSize = 12.5.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = prefixColor,
                textAlign = TextAlign.Center
            )

            // Main Title (Bold Uppercase)
            Text(
                text = mainTitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = description,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF94A3B8),
                lineHeight = 12.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Futuristic Interactive Swipe-To-Continue Slider CTA
 */
@Composable
private fun SwipeToContinueSlider(
    onSwipeComplete: () -> Unit,
    arrowSlideX: Float
) {
    val scope = rememberCoroutineScope()
    var dragHandleOffset by remember { mutableFloatStateOf(0f) }
    var sliderTrackWidth by remember { mutableFloatStateOf(0f) }
    val maxDragWidth = (sliderTrackWidth - 64f).coerceAtLeast(100f)

    val handleOffsetAnim = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF2563EB),
                        Color(0xFF7C3AED),
                        Color(0xFFD946EF),
                        Color(0xFFFF007A)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF67E8F9), Color(0xFFC084FC), Color(0xFFF472B6))
                ),
                RoundedCornerShape(29.dp)
            )
            .shadow(16.dp, spotColor = Color(0xFFD946EF))
            .onSizeChanged { size ->
                sliderTrackWidth = size.width.toFloat()
            }
            .clickable {
                // Clicking also allows continuing seamlessly
                onSwipeComplete()
            }
            .testTag("swipe_to_continue_slider"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Center Text: SWIPE TO CONTINUE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SWIPE TO CONTINUE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(18.dp)
                    .offset { IntOffset(x = arrowSlideX.roundToInt(), y = 0) }
            )
        }

        // Draggable Circular Handle on the Track
        Box(
            modifier = Modifier
                .offset { IntOffset(x = dragHandleOffset.roundToInt(), y = 0) }
                .padding(start = 4.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF2563EB)
                        )
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
                .shadow(12.dp, spotColor = Color(0xFF00E5FF))
                .pointerInput(sliderTrackWidth) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragHandleOffset >= maxDragWidth * 0.6f) {
                                scope.launch {
                                    val anim = Animatable(dragHandleOffset)
                                    anim.animateTo(maxDragWidth, tween(150)) {
                                        dragHandleOffset = value
                                    }
                                    onSwipeComplete()
                                }
                            } else {
                                scope.launch {
                                    val anim = Animatable(dragHandleOffset)
                                    anim.animateTo(0f, tween(200, easing = FastOutSlowInEasing)) {
                                        dragHandleOffset = value
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                val anim = Animatable(dragHandleOffset)
                                anim.animateTo(0f, tween(150)) {
                                    dragHandleOffset = value
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragHandleOffset = (dragHandleOffset + dragAmount.x).coerceIn(0f, maxDragWidth)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe Handle",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
