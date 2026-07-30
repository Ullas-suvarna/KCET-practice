package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

data class TestCardItem(
    val id: String,
    val title: String,
    val category: String, // Full Mock, PYQ, Chapter, Daily
    val questionCount: Int,
    val durationMins: Int,
    val isFree: Boolean = false
)

@Composable
fun PracticeScreen(
    isPremiumUser: Boolean,
    onStartFreeTest: () -> Unit,
    onLockedFeatureClick: () -> Unit
) {
    val testCards = listOf(
        TestCardItem("t1", "KCET Grand Sample CBT Test 1", "Free Mock Test", 20, 20, isFree = true),
        TestCardItem("t2", "KCET Full Mock Test 2 (PCM)", "Full Mock Tests", 180, 180),
        TestCardItem("t3", "KCET Full Mock Test 3 (PCB)", "Full Mock Tests", 180, 180),
        TestCardItem("t4", "KCET 2025 Official Solved Paper", "Previous Year Papers", 180, 180),
        TestCardItem("t5", "KCET 2024 Official Solved Paper", "Previous Year Papers", 180, 180),
        TestCardItem("t6", "Physics: Electrostatics & Current Electricity", "Chapter-wise Tests", 30, 30),
        TestCardItem("t7", "Chemistry: Organic Named Reactions", "Chapter-wise Tests", 30, 30),
        TestCardItem("t8", "Mathematics: Matrices & Calculus", "Chapter-wise Tests", 30, 30),
        TestCardItem("t9", "Daily Practice Questions (DPP Series 12)", "Daily Practice", 15, 15)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Text(
                text = "PRACTICE TESTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mock CBT Exams",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prepare with Computer-Based Test mock exams designed as per official KEA exam guidelines.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        // Free Test Section Header
        item {
            Text(
                text = "🎁 Free Sample Test",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }

        // Free Test Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "FREE UNLOCKED",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Text(
                            text = "20 Questions • 20 Mins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "KCET Grand Sample CBT Test 1",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Physics, Chemistry & Mathematics combined entrance paper with full answer key.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onStartFreeTest,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Start Test Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Locked Premium Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🔒 Premium Mock Tests & PYQs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        // Cards list
        items(testCards.filter { !it.isFree }) { test ->
            val canAccess = isPremiumUser

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .clickable {
                        if (canAccess) {
                            onStartFreeTest()
                        } else {
                            onLockedFeatureClick()
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canAccess) Color(0xFFD1FAE5) else Color(0xFFF3E8FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (canAccess) Icons.Default.Assignment else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (canAccess) Color(0xFF10B981) else Color(0xFF9333EA),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = test.category.uppercase(),
                            fontSize = 10.sp,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = test.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${test.questionCount} Questions • ${test.durationMins} Mins",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (canAccess) Color(0xFF10B981) else Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = if (canAccess) "OPEN" else "PRO",
                            fontSize = 11.sp,
                            color = if (canAccess) Color.White else Color(0xFF4F46E5),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

