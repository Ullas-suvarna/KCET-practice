package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.15f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White,
                Color(0xFFF8FAFC)
            )
        )
    }

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFC7D2FE).copy(alpha = borderAlpha + 0.3f)
        )
    )

    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = shape, ambientColor = Color(0xFF6366F1).copy(alpha = 0.08f), spotColor = Color(0xFF6366F1).copy(alpha = 0.08f))
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        content = content
    )
}

