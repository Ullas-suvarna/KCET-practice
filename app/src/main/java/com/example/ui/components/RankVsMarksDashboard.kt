package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RankVsMarksItem

@Composable
fun RankVsMarksDashboard(
    rankVsMarksList: List<RankVsMarksItem>,
    modifier: Modifier = Modifier,
    isPremiumUser: Boolean = false,
    onUpgradeClick: () -> Unit = {}
) {
    var userMarksInput by remember { mutableStateOf("145") }
    var sliderVal by remember { mutableFloatStateOf(145f) }
    var isExpandedTable by remember { mutableStateOf(false) }

    // Estimate rank from Firebase list based on userMarksInput
    val marksInt = userMarksInput.toIntOrNull() ?: sliderVal.toInt()
    val matchedRange = rankVsMarksList.find { item ->
        marksInt >= item.minMarks && marksInt <= item.maxMarks
    } ?: rankVsMarksList.lastOrNull()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPremiumUser) Color(0xFFEEF2FF) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPremiumUser) Icons.Default.Analytics else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isPremiumUser) Color(0xFF4F46E5) else Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KCET Marks vs Rank Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Previous Years Cutoff Trends (Live Firebase Data)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPremiumUser) Color(0xFFDCFCE7) else Color(0xFFFEF08A),
                    modifier = Modifier.clickable { if (!isPremiumUser) onUpgradeClick() }
                ) {
                    Text(
                        text = if (isPremiumUser) "2025 DATA" else "PRO 🔒",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPremiumUser) Color(0xFF15803D) else Color(0xFF854D0E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Premium Lock Overlay Card if not premium
            if (!isPremiumUser) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpgradeClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👑 Premium Feature",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Unlock KCET Marks vs Rank 2025 Analytics & College Predictor",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Button(
                            onClick = onUpgradeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Unlock ₹299", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estimator Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Instant Rank Predictor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Text(
                            text = "Score: $marksInt / 180",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderVal,
                        onValueChange = {
                            sliderVal = it
                            userMarksInput = it.toInt().toString()
                        },
                        valueRange = 30f..180f,
                        steps = 150,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4F46E5),
                            activeTrackColor = Color(0xFF6366F1),
                            inactiveTrackColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Predicted Rank Output Card
                    if (isPremiumUser) {
                        if (matchedRange != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ESTIMATED RANK RANGE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4338CA)
                                        )
                                        Text(
                                            text = "Rank ${matchedRange.expectedRankRange}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1B4B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "💡 ${matchedRange.description}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF3730A3),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Locked State for non-premium user
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                                .clickable { onUpgradeClick() }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ESTIMATED RANK RANGE 🔒 PRO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "Rank Prediction Locked 🔒",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF78350F)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Unlock KCET Premium (₹299) to view exact predicted rank for $marksInt/180 score and college match trends.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309),
                                        lineHeight = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = onUpgradeClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle Full Previous Years Table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable { isExpandedTable = !isExpandedTable }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Previous Year Cutoff Table (${rankVsMarksList.size} Ranges)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                }

                Icon(
                    imageVector = if (isExpandedTable) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF475569)
                )
            }

            // Expanded Table View
            AnimatedVisibility(visible = isExpandedTable) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Table Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "KCET Marks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Expected Rank",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "Scope & Colleges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.weight(2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    rankVsMarksList.forEachIndexed { idx, item ->
                        val isHighlighted = marksInt >= item.minMarks && marksInt <= item.maxMarks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isHighlighted) Color(0xFFEEF2FF) else if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                .border(
                                    width = if (isHighlighted) 1.dp else 0.dp,
                                    color = if (isHighlighted) Color(0xFF6366F1) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!isPremiumUser) onUpgradeClick()
                                }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.marksRange,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (isHighlighted) Color(0xFF4F46E5) else Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isPremiumUser) item.expectedRankRange else "🔒 PRO Only",
                                fontWeight = if (isHighlighted || !isPremiumUser) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (!isPremiumUser) Color(0xFFD97706) else if (isHighlighted) Color(0xFF15803D) else Color(0xFF334155),
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = if (isPremiumUser) item.description else "Unlock KCET Premium Pass to reveal full 2025/2026 cutoff & college mapping",
                                fontSize = 10.sp,
                                color = if (!isPremiumUser) Color(0xFFB45309) else Color(0xFF64748B),
                                lineHeight = 13.sp,
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
