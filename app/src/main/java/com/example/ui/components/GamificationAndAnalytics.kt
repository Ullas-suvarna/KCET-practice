package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.AnalyticsSummary
import com.example.data.BadgeItem
import com.example.data.LeaderboardEntry
import com.example.data.Subject
import com.example.data.SubjectAnalytics
import com.example.data.TopicAccuracy

data class LeaderboardBadgeInfo(
    val title: String,
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color
)

fun getLeaderboardBadgeInfo(rank: Int): LeaderboardBadgeInfo? {
    return when (rank) {
        1 -> LeaderboardBadgeInfo(
            title = "👑 GrandMaster",
            containerColor = Color(0xFFFFD700).copy(alpha = 0.25f),
            contentColor = Color(0xFFB45309),
            borderColor = Color(0xFFF59E0B)
        )
        in 2..4 -> LeaderboardBadgeInfo(
            title = "🏆 Master",
            containerColor = Color(0xFFEDE9FE),
            contentColor = Color(0xFF7C3AED),
            borderColor = Color(0xFF8B5CF6)
        )
        in 5..9 -> LeaderboardBadgeInfo(
            title = "💎 Diamond",
            containerColor = Color(0xFFE0F2FE),
            contentColor = Color(0xFF0284C7),
            borderColor = Color(0xFF38BDF8)
        )
        in 10..15 -> LeaderboardBadgeInfo(
            title = "🥈 Silver",
            containerColor = Color(0xFFF1F5F9),
            contentColor = Color(0xFF334155),
            borderColor = Color(0xFF64748B)
        )
        else -> LeaderboardBadgeInfo(
            title = "🥉 Bronze",
            containerColor = Color(0xFFFFEDD5),
            contentColor = Color(0xFFC2410C),
            borderColor = Color(0xFFEA580C)
        )
    }
}

@Composable
fun GrandmasterBigBadgeDialog(
    studentName: String = "Grandmaster Student",
    points: Int = 1250,
    onDismiss: () -> Unit,
    onChallengeClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFF59E0B),
                            Color(0xFFEC4899),
                            Color(0xFFFFD700)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "👑 STATEWIDE RANK #1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Badge Visual
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color(0xFFFFD700).copy(alpha = 0.9f))
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFF59E0B),
                                    Color(0xFFEF4444),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFFFD700)
                                )
                            )
                        )
                        .border(3.5.dp, Color(0xFFFFD700), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_grandmaster_badge),
                        contentDescription = "Grandmaster Elite Badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "GrandMaster Elite Medal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to $studentName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ $points Total Points • KCET Top Ranker",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This golden medal honors extraordinary dedication, highest score, and subject mastery across KCET Physics, Chemistry, and Mathematics tests.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (onChallengeClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChallengeClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("⚔️ Challenge 1 vs 1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF451A03)
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MasterBigBadgeDialog(
    studentName: String = "Master Student",
    rank: Int = 2,
    points: Int = 1180,
    onDismiss: () -> Unit,
    onChallengeClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E1065),
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFC084FC),
                            Color(0xFF8B5CF6),
                            Color(0xFFF59E0B),
                            Color(0xFFC084FC)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFC084FC).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC084FC))
                    ) {
                        Text(
                            text = "🏆 STATEWIDE RANK #$rank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE9D5FF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Master Badge Visual
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color(0xFF8B5CF6).copy(alpha = 0.9f))
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFFC084FC),
                                    Color(0xFFF59E0B),
                                    Color(0xFF38BDF8),
                                    Color(0xFF8B5CF6)
                                )
                            )
                        )
                        .border(3.5.dp, Color(0xFFC084FC), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_master_badge),
                        contentDescription = "Master Badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Master Tier Medal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to $studentName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE9D5FF),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ $points Total Points • KCET Top Master Ranker",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This prestigious Master badge recognizes elite academic excellence, exceptional test accuracy, and continuous high scores among Karnataka top engineering aspirants.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (onChallengeClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChallengeClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("⚔️ Challenge 1 vs 1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DiamondBigBadgeDialog(
    studentName: String = "Diamond Student",
    rank: Int = 5,
    points: Int = 1050,
    onDismiss: () -> Unit,
    onChallengeClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF0284C7).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF082F49),
                            Color(0xFF0C4A6E),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF0284C7),
                            Color(0xFFBAE6FD),
                            Color(0xFF0284C7)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "💎 STATEWIDE RANK #$rank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Diamond Badge Visual
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color(0xFF0284C7).copy(alpha = 0.9f))
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF0284C7),
                                    Color(0xFFBAE6FD),
                                    Color(0xFF0EA5E9),
                                    Color(0xFF38BDF8)
                                )
                            )
                        )
                        .border(3.5.dp, Color(0xFF38BDF8), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_diamond_badge),
                        contentDescription = "Diamond Badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Diamond Tier Medal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to $studentName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBAE6FD),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ $points Total Points • KCET Diamond Honor Student",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DD3FC)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This brilliant Diamond badge honors superior analytical problem solving, high speed accuracy, and top-tier standing in Karnataka engineering entrance ranks.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (onChallengeClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChallengeClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("⚔️ Challenge 1 vs 1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SilverBigBadgeDialog(
    studentName: String = "Silver Student",
    rank: Int = 10,
    points: Int = 850,
    onDismiss: () -> Unit,
    onChallengeClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF64748B).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E293B),
                            Color(0xFF334155),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFE2E8F0),
                            Color(0xFF94A3B8),
                            Color(0xFFCBD5E1),
                            Color(0xFF64748B)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF94A3B8).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "🥈 STATEWIDE RANK #$rank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Silver Badge Visual
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color(0xFF94A3B8).copy(alpha = 0.9f))
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFE2E8F0),
                                    Color(0xFF94A3B8),
                                    Color(0xFFF1F5F9),
                                    Color(0xFF64748B),
                                    Color(0xFFE2E8F0)
                                )
                            )
                        )
                        .border(3.5.dp, Color(0xFFE2E8F0), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_silver_badge),
                        contentDescription = "Silver Badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Silver Tier Medal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to $studentName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ $points Total Points • KCET Silver Achiever",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This sleek Silver badge recognizes strong performance, steady topic accuracy, and active participation in KCET preparation.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (onChallengeClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChallengeClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("⚔️ Challenge 1 vs 1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64748B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun BronzeBigBadgeDialog(
    studentName: String = "Bronze Student",
    rank: Int = 16,
    points: Int = 650,
    onDismiss: () -> Unit,
    onChallengeClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFFEA580C).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF431407),
                            Color(0xFF7C2D12),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFDBA74),
                            Color(0xFFEA580C),
                            Color(0xFFFFEDD5),
                            Color(0xFFC2410C)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEA580C).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74))
                    ) {
                        Text(
                            text = "🥉 STATEWIDE RANK #$rank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFEDD5),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Bronze Badge Visual
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color(0xFFEA580C).copy(alpha = 0.9f))
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFDBA74),
                                    Color(0xFFEA580C),
                                    Color(0xFFFFEDD5),
                                    Color(0xFFC2410C),
                                    Color(0xFFFDBA74)
                                )
                            )
                        )
                        .border(3.5.dp, Color(0xFFFDBA74), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bronze_badge),
                        contentDescription = "Bronze Badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bronze Tier Medal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Awarded to $studentName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEDD5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ $points Total Points • KCET Bronze Contender",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFED7AA)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This resilient Bronze badge represents active learning progress, test completions, and continuous climb up the KCET ranks.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (onChallengeClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChallengeClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("⚔️ Challenge 1 vs 1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC2410C),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    leaderboardEntries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier,
    onChallengeStudent: ((LeaderboardEntry) -> Unit)? = null
) {
    var bigBadgeStudent by remember { mutableStateOf<LeaderboardEntry?>(null) }

    if (bigBadgeStudent != null) {
        val student = bigBadgeStudent!!
        val challengeAction: (() -> Unit)? = if (!student.isCurrentUser && onChallengeStudent != null) {
            { onChallengeStudent(student) }
        } else null

        when {
            student.rank == 1 -> {
                GrandmasterBigBadgeDialog(
                    studentName = student.name,
                    points = student.points,
                    onDismiss = { bigBadgeStudent = null },
                    onChallengeClick = challengeAction
                )
            }
            student.rank in 2..4 -> {
                MasterBigBadgeDialog(
                    studentName = student.name,
                    rank = student.rank,
                    points = student.points,
                    onDismiss = { bigBadgeStudent = null },
                    onChallengeClick = challengeAction
                )
            }
            student.rank in 5..9 -> {
                DiamondBigBadgeDialog(
                    studentName = student.name,
                    rank = student.rank,
                    points = student.points,
                    onDismiss = { bigBadgeStudent = null },
                    onChallengeClick = challengeAction
                )
            }
            student.rank in 10..15 -> {
                SilverBigBadgeDialog(
                    studentName = student.name,
                    rank = student.rank,
                    points = student.points,
                    onDismiss = { bigBadgeStudent = null },
                    onChallengeClick = challengeAction
                )
            }
            else -> {
                BronzeBigBadgeDialog(
                    studentName = student.name,
                    rank = student.rank,
                    points = student.points,
                    onDismiss = { bigBadgeStudent = null },
                    onChallengeClick = challengeAction
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color(0xFF6366F1).copy(alpha = 0.2f))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                )
            )
            .border(2.dp, Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF59E0B))), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "KCET Elite Leaderboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Statewide Rank & Badge Standings",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF4F46E5),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "⚡ 2026 Batch",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier Guide Summary Bar (GrandMaster, Master, Diamond, Silver, Bronze preview)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    // Grandmaster pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(0.8.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                            .clickable {
                                bigBadgeStudent = leaderboardEntries.find { it.rank == 1 }
                                    ?: LeaderboardEntry(1, "Ananya Rao", "Under 100", 1450, 95.2f, 18, "AR")
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_grandmaster_badge),
                            contentDescription = "GrandMaster",
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GrandMaster (#1)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    }
                }

                item {
                    // Master pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDE9FE))
                            .border(0.8.dp, Color(0xFF8B5CF6), RoundedCornerShape(8.dp))
                            .clickable {
                                bigBadgeStudent = leaderboardEntries.find { it.rank in 2..4 }
                                    ?: LeaderboardEntry(2, "Rohan Kulkarni", "Under 250", 1320, 91.0f, 15, "RK")
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_master_badge),
                            contentDescription = "Master",
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Master (#2–#4)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                    }
                }

                item {
                    // Diamond pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .border(0.8.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                            .clickable {
                                bigBadgeStudent = leaderboardEntries.find { it.rank in 5..9 }
                                    ?: LeaderboardEntry(5, "Suhas Patil", "Under 1200", 980, 82.5f, 11, "SP")
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_diamond_badge),
                            contentDescription = "Diamond",
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diamond (#5–#9)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                    }
                }

                item {
                    // Silver pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(0.8.dp, Color(0xFF64748B), RoundedCornerShape(8.dp))
                            .clickable {
                                bigBadgeStudent = leaderboardEntries.find { it.rank in 10..15 }
                                    ?: LeaderboardEntry(10, "Kavya Murthy", "Under 2500", 850, 78.0f, 9, "KM")
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_silver_badge),
                            contentDescription = "Silver",
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Silver (#10–#15)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    }
                }

                item {
                    // Bronze pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEDD5))
                            .border(0.8.dp, Color(0xFFEA580C), RoundedCornerShape(8.dp))
                            .clickable {
                                bigBadgeStudent = leaderboardEntries.find { it.rank > 15 }
                                    ?: LeaderboardEntry(16, "Vikas Gowda", "Under 5000", 620, 71.0f, 6, "VG")
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_bronze_badge),
                            contentDescription = "Bronze",
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bronze (#16+)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leaderboard Items
            leaderboardEntries.forEach { entry ->
                LeaderboardItemRow(
                    entry = entry,
                    onBadgeClick = { selected ->
                        bigBadgeStudent = selected
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun LeaderboardItemRow(
    entry: LeaderboardEntry,
    onBadgeClick: (LeaderboardEntry) -> Unit = {}
) {
    val badgeInfo = getLeaderboardBadgeInfo(entry.rank)

    val fullBgColor = when (entry.rank) {
        1 -> Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFEE140))) // Gold
        2, 3, 4 -> Brush.horizontalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))) // Master Purple
        in 5..9 -> Brush.horizontalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))) // Diamond Blue
        in 10..15 -> Brush.horizontalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1))) // Silver Slate
        else -> Brush.horizontalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))) // Bronze Orange
    }

    val textColor = when (entry.rank) {
        1 -> Color(0xFF78350F)
        2, 3, 4 -> Color(0xFF5B21B6)
        in 5..9 -> Color(0xFF0369A1)
        in 10..15 -> Color(0xFF334155)
        else -> Color(0xFF9A3412)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (entry.isCurrentUser || entry.rank <= 3) 3.dp else 1.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(fullBgColor)
            .border(
                width = if (entry.rank == 1) 1.5.dp else 1.dp,
                color = if (entry.rank == 1) Color(0xFFD97706) else textColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onBadgeClick(entry) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank number / crown
                Text(
                    text = if (entry.rank == 1) "👑 #1" else "#${entry.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    modifier = Modifier.width(44.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Name on first row, badge on second row (Column layout)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (entry.isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4F46E5)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Second row: Badge
                    if (entry.rank == 1) {
                        // First Grandmaster Student custom badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBadgeClick(entry) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_grandmaster_badge),
                                    contentDescription = "GrandMaster Badge",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(0.5.dp, Color(0xFFD97706), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "👑 GrandMaster",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else if (entry.rank in 2..4) {
                        // Master Student custom badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F3FF),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF8B5CF6)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBadgeClick(entry) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_master_badge),
                                    contentDescription = "Master Badge",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(0.5.dp, Color(0xFF7C3AED), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🏆 Master",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6D28D9)
                                )
                            }
                        }
                    } else if (entry.rank in 5..9) {
                        // Diamond Student custom badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0F9FF),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF0284C7)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBadgeClick(entry) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_diamond_badge),
                                    contentDescription = "Diamond Badge",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(0.5.dp, Color(0xFF0284C7), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "💎 Diamond",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    } else if (entry.rank in 10..15) {
                        // Silver Student custom badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF64748B)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBadgeClick(entry) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_silver_badge),
                                    contentDescription = "Silver Badge",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(0.5.dp, Color(0xFF64748B), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🥈 Silver",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    } else {
                        // Bronze Student custom badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF7ED),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFEA580C)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBadgeClick(entry) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_bronze_badge),
                                    contentDescription = "Bronze Badge",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(0.5.dp, Color(0xFFEA580C), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🥉 Bronze",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                }
            }

            // XP (minimum information)
            Text(
                text = "${entry.points} XP",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
fun BadgesSection(
    badges: List<BadgeItem>,
    modifier: Modifier = Modifier,
    onUnlockAllBadges: () -> Unit = {}
) {
    val allUnlocked = badges.isNotEmpty() && badges.all { it.isUnlocked }
    val unlockedCount = badges.count { it.isUnlocked }
    var selectedBadgeFilter by remember { mutableStateOf(0) }

    val filteredBadges = when (selectedBadgeFilter) {
        1 -> badges.filter { it.isVeryDifficult }
        2 -> badges.filter { it.isUnlocked }
        3 -> badges.filter { !it.isUnlocked }
        else -> badges
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (allUnlocked) Color(0xFFFEF3C7) else Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (allUnlocked) Icons.Default.EmojiEvents else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (allUnlocked) Color(0xFFD97706) else Color(0xFF9333EA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Milestone Badges ($unlockedCount/${badges.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (allUnlocked) "🎉 All 15 Milestone Badges Achieved!" else "Includes 2 Very Difficult Elite Badges for dedicated study",
                        fontSize = 11.sp,
                        color = if (allUnlocked) Color(0xFF059669) else Color(0xFF64748B),
                        fontWeight = if (allUnlocked) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badge Sub-Tab Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val filters = listOf(
                    "All (${badges.size})",
                    "🔥 Very Hard (2)",
                    "🔓 Unlocked",
                    "🔒 Locked"
                )
                filters.forEachIndexed { index, label ->
                    val isSel = selectedBadgeFilter == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isSel) {
                                    if (index == 1) Color(0xFF7C3AED) else Color(0xFF4F46E5)
                                } else Color.Transparent
                            )
                            .clickable { selectedBadgeFilter = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated Callout Banner for Very Difficult Badges
            if (selectedBadgeFilter == 1 || selectedBadgeFilter == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF312E81), Color(0xFF581C87), Color(0xFF9A3412))
                            )
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DEDICATED VERY DIFFICULT ELITE CHALLENGES (2)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFDE68A),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Targeted for top KCET rank candidates: Complete 180-Q Mock Tests & achieve 2,500+ XP to prove state mastery!",
                                fontSize = 10.5.sp,
                                color = Color.White,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (allUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5),
                                    Color(0xFF7C3AED),
                                    Color(0xFFD97706)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OUTSTANDING ACHIEVEMENT!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFDE68A),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "All 15 Milestones Achieved! 🎉",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "You have unlocked all preparation milestone badges. This shows your exceptional commitment to master the KCET computer-based test curriculum!",
                            fontSize = 12.sp,
                            color = Color(0xFFE0E7FF),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredBadges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No badges match the selected filter.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                filteredBadges.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { badge ->
                            BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardDetailBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFFDE68A))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BadgeRewardCongratsDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎉 Start Enjoying Free Premium Access", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("👑 🥳 🏆", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CONGRATULATIONS!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4F46E5)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "All Milestone Badges Achieved Completely! 🎯",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Your dedication to solving KCET mock tests has earned you a FREE LIFETIME PREMIUM PRO PASS! Here is what is unlocked for you:",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEEF2FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("✨ Unlimited 180-Question Full PCM CBT Mock Papers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3730A3))
                        Text("✨ Full Step-by-Step Solutions & PYQ Answer Key Access", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3730A3))
                        Text("✨ Free Priority Slot for 1-to-1 Senior Mentorship Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3730A3))
                        Text("✨ Advanced Subject Mastery & Speed Analytics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3730A3))
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun BadgeCard(badge: BadgeItem, modifier: Modifier = Modifier) {
    val bg = when {
        badge.isVeryDifficult && badge.isUnlocked -> Color(0xFFFFFBEB)
        badge.isVeryDifficult -> Color(0xFFFAF5FF)
        badge.isUnlocked -> Color(0xFFF8FAFC)
        else -> Color(0xFFF1F5F9).copy(alpha = 0.5f)
    }

    val borderCol = when {
        badge.isVeryDifficult && badge.isUnlocked -> Color(0xFFF59E0B)
        badge.isVeryDifficult -> Color(0xFFC084FC)
        badge.isUnlocked -> Color(0xFFC7D2FE)
        else -> Color(0xFFE2E8F0)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (badge.isVeryDifficult) 2.dp else 1.dp, borderCol, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 24.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        badge.isUnlocked -> Color(0xFF10B981)
                        badge.isVeryDifficult -> Color(0xFF9333EA)
                        else -> Color(0xFF94A3B8)
                    }
                ) {
                    Text(
                        text = when {
                            badge.isUnlocked -> "UNLOCKED"
                            badge.isVeryDifficult -> "🔥 VERY HARD"
                            else -> "LOCKED"
                        },
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge.isVeryDifficult) {
                    Text("👑 ", fontSize = 12.sp)
                }
                Text(
                    text = badge.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color(0xFF0F172A) else Color(0xFF475569)
                )
            }

            Text(
                text = badge.description,
                fontSize = 10.5.sp,
                color = Color(0xFF64748B),
                lineHeight = 13.5.sp
            )

            if (!badge.isUnlocked) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${(badge.progress * 100).toInt()}%",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isVeryDifficult) Color(0xFF9333EA) else Color(0xFF4F46E5)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = badge.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = if (badge.isVeryDifficult) Color(0xFF9333EA) else Color(0xFF4F46E5),
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }
    }
}

@Composable
fun SubjectPerformanceBreakdown(
    subjectBreakdown: List<SubjectAnalytics>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Subject Performance Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Accuracy & speed across KCET subjects",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            subjectBreakdown.forEach { subj ->
                val barColor = when (subj.subject) {
                    Subject.PHYSICS -> Color(0xFF3B82F6)
                    Subject.CHEMISTRY -> Color(0xFFEC4899)
                    Subject.MATHEMATICS -> Color(0xFF8B5CF6)
                    Subject.BIOLOGY -> Color(0xFF10B981)
                }

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subj.subject.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${subj.correctCount}/${subj.totalQuestions} Correct (${String.format("%.0f", subj.accuracyPercentage)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = barColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = (subj.accuracyPercentage / 100f).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = barColor.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⏱️ Speed: ${subj.avgTimePerQuestionSec.toInt()}s / question",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = if (subj.accuracyPercentage >= 70) "🔥 Strong Area" else "⚠️ Needs Practice",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subj.accuracyPercentage >= 70) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicMasteryCard(
    topicAccuracies: List<TopicAccuracy>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFF4338CA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Topic-Wise Mastery Analytics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Detailed conceptual mastery rating",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            topicAccuracies.forEach { topic ->
                val statusBg = when (topic.status) {
                    "Strong" -> Color(0xFFD1FAE5)
                    "Moderate" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFFEE2E2)
                }
                val statusText = when (topic.status) {
                    "Strong" -> Color(0xFF047857)
                    "Moderate" -> Color(0xFFB45309)
                    else -> Color(0xFFB91C1C)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${topic.subject.name} • ${topic.topicName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Score: ${topic.correct}/${topic.total} (${String.format("%.0f", topic.accuracy)}%)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = topic.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovementPlanCard(
    weakAreas: List<String>,
    recommendations: List<String>,
    onOpenResources: () -> Unit,
    onOpenPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Personalized Improvement Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Identified gaps and recommended action items",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "🎯 Areas Needing Focus:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(4.dp))

            weakAreas.forEach { area ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = area,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 Actionable Recommendations:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5)
            )

            Spacer(modifier = Modifier.height(4.dp))

            recommendations.forEach { rec ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                    Text(
                        text = rec,
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenResources,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("Study Material", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenPractice,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("Practice MCQs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
