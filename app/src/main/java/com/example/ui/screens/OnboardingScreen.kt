package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPageData(
    val titlePart1: String,
    val titlePart2: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            titlePart1 = "Practice Real",
            titlePart2 = "KCET Mock Tests",
            description = "Experience official CBT interface with timed 20-minute practice papers, instant detailed answers and performance charts."
        ),
        OnboardingPageData(
            titlePart1 = "Learn Complete",
            titlePart2 = "KCET Counselling Process",
            description = "Step-by-step guidance on KEA Document Verification, Secret Key generation, Option Entry strategies, Mock Allotments, and Seat Matrix."
        ),
        OnboardingPageData(
            titlePart1 = "Your Success",
            titlePart2 = "Starts Here!",
            description = "Everything you need to crack KCET – in one powerful app. Let's achieve your dream together!"
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070314),
                        Color(0xFF0C0724),
                        Color(0xFF130D38),
                        Color(0xFF09041A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Skip button on top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage < pages.size - 1) {
                    Surface(
                        onClick = onFinishOnboarding,
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F0B26).copy(alpha = 0.8f),
                        border = BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.6f), Color(0xFFA855F7).copy(alpha = 0.6f)))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Skip",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
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
            }

            // Illustration Graphics Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = { fadeIn() with fadeOut() },
                    label = "illustration_animation"
                ) { page ->
                    when (page) {
                        0 -> OnboardingMockTestIllustration()
                        1 -> OnboardingCounsellingIllustration()
                        else -> OnboardingSuccessIllustration()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val safePageIndex = currentPage.coerceIn(0, pages.size - 1)
                val currentData = pages[safePageIndex]

                Text(
                    text = currentData.titlePart1,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentData.titlePart2,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF00E5FF)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Small Gradient Line Rule
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentData.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF8B5CF6)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC084FC)),
                        modifier = Modifier
                            .height(48.dp)
                            .width(130.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Previous",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier
                            .height(48.dp)
                            .width(130.dp)
                    ) {
                        Text(
                            text = "Previous",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right Button
                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = { currentPage++ },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .height(48.dp)
                            .width(130.dp)
                    ) {
                        Text(
                            text = "Next",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onFinishOnboarding,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .height(48.dp)
                            .width(150.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------
   Illustration Composable 1: KCET Mock Test Mockup Card (Page 0)
   ------------------------------------------------------------------------- */
@Composable
private fun OnboardingMockTestIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft Light Blue Ambient Background Circle
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF))
        )

        // Main White Elevated Test Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KCET Mock Test",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "01:28:45",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2x2 Score Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Physics Tile
                    CardTile(
                        title = "Physics",
                        score = "32 / 45",
                        progress = 0.71f,
                        progressColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    // Chemistry Tile
                    CardTile(
                        title = "Chemistry",
                        score = "28 / 45",
                        progress = 0.62f,
                        progressColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mathematics Tile
                    CardTile(
                        title = "Mathematics",
                        score = "36 / 45",
                        progress = 0.80f,
                        progressColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    // Performance Graph Tile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Performance",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Simple Wave Curve Mockup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Badges & Decorations
        // Top Left Blue Document Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = 12.dp)
                .size(42.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2563EB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Bottom Right Growth Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-12).dp, y = (-12).dp)
                .size(38.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Bottom Left Stack of Books Graphic
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = 14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Potted Plant
            Text("🪴", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(4.dp))
            // Stack of 3 Books
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp, 10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2563EB))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(58.dp, 10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(62.dp, 10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF59E0B))
                )
            }
        }
    }
}

@Composable
private fun CardTile(
    title: String,
    score: String,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = score,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = Color(0xFFE2E8F0)
            )
        }
    }
}

/* -------------------------------------------------------------------------
   Illustration Composable 2: KCET Counselling & Seat Matrix (Page 1)
   ------------------------------------------------------------------------- */
@Composable
private fun OnboardingCounsellingIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft Glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF))
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Card: Counselling Checklist
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(210.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "KCET Counselling",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    CounsellingItem(
                        icon = Icons.Default.VerifiedUser,
                        label = "Document Verification",
                        iconColor = Color(0xFF10B981)
                    )
                    CounsellingItem(
                        icon = Icons.Default.Key,
                        label = "Secret Key Generation",
                        iconColor = Color(0xFF8B5CF6)
                    )
                    CounsellingItem(
                        icon = Icons.Default.FormatListNumbered,
                        label = "Option Entry",
                        iconColor = Color(0xFF2563EB)
                    )
                    CounsellingItem(
                        icon = Icons.Default.People,
                        label = "Mock Allotments",
                        iconColor = Color(0xFFF59E0B)
                    )
                    CounsellingItem(
                        icon = Icons.Default.TableChart,
                        label = "Seat Matrix",
                        iconColor = Color(0xFFEC4899)
                    )
                }
            }

            // Right Card: Phone Mockup Seat Matrix
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(220.dp)
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(6.dp)
                ) {
                    // Top Phone Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Seat Matrix",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Phone Screen Content
                    SeatMatrixRow("Engineering", "12,450 Seats")
                    SeatMatrixRow("Medical", "5,230 Seats")
                    SeatMatrixRow("BDS", "1,410 Seats")
                    SeatMatrixRow("AYUSH", "980 Seats")
                    SeatMatrixRow("Agriculture", "2,150 Seats")
                }
            }
        }

        // Graduation Cap Overlay at Bottom Right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (4).dp)
        ) {
            Text("🎓", fontSize = 42.sp)
        }
    }
}

@Composable
private fun CounsellingItem(
    icon: ImageVector,
    label: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun SeatMatrixRow(branch: String, seats: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = branch,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = seats,
                fontSize = 7.5.sp,
                color = Color(0xFF64748B)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2563EB))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "View",
                color = Color.White,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* -------------------------------------------------------------------------
   Illustration Composable 3: Your Success Starts Here (Page 2)
   ------------------------------------------------------------------------- */
@Composable
private fun OnboardingSuccessIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Light Ambient Circle Background
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF))
        )

        // Center Large Blue Graduation Cap Shield Circle
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2563EB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        // Books Stack & Plant at Base of Center Shield
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text("🪴", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2563EB))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(86.dp, 12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(92.dp, 12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF59E0B))
                )
            }
        }

        // 4 Floating Orbit Badges
        // Top Left: Smart Practice
        FloatingBadge(
            icon = Icons.Default.CheckCircle,
            label = "Smart Practice",
            badgeColor = Color(0xFF10B981),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 16.dp, y = 16.dp)
        )

        // Top Right: Track Progress
        FloatingBadge(
            icon = Icons.Default.ShowChart,
            label = "Track Progress",
            badgeColor = Color(0xFF8B5CF6),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = 16.dp)
        )

        // Center Left: Expert Faculty
        FloatingBadge(
            icon = Icons.Default.People,
            label = "Expert Faculty",
            badgeColor = Color(0xFFF59E0B),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp, y = 20.dp)
        )

        // Center Right: Trusted & Secure
        FloatingBadge(
            icon = Icons.Default.Security,
            label = "Trusted & Secure",
            badgeColor = Color(0xFFEC4899),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp, y = 20.dp)
        )
    }
}

@Composable
private fun FloatingBadge(
    icon: ImageVector,
    label: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
    }
}
