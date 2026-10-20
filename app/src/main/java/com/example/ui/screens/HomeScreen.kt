package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Announcement
import com.example.data.BadgeItem
import com.example.data.ImportantDateItem
import com.example.data.RankVsMarksItem
import com.example.data.TestResultEntity
import com.example.data.UserProfile
import com.example.ui.components.GrandmasterBigBadgeDialog
import com.example.ui.components.MasterBigBadgeDialog
import com.example.ui.components.DiamondBigBadgeDialog
import com.example.ui.components.SilverBigBadgeDialog
import com.example.ui.components.BronzeBigBadgeDialog
import com.example.ui.components.RankVsMarksDashboard
import com.example.viewmodel.BottomTab

import androidx.compose.material.icons.filled.Menu

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    latestResult: TestResultEntity?,
    announcements: List<Announcement>,
    importantDates: List<ImportantDateItem> = emptyList(),
    rankVsMarksList: List<RankVsMarksItem> = emptyList(),
    userPoints: Int = 450,
    userRank: Int = 5,
    testTimeDisplay: String = "0.5 hrs",
    badges: List<BadgeItem> = emptyList(),
    bookmarkedCount: Int = 0,
    unreadNotificationCount: Int = 0,
    liveChatPrice: Int = 50,
    subscriptionPrice: Int = 299,
    onStartFreeTest: () -> Unit,
    onUpgradeClick: () -> Unit,
    onNavigateTab: (BottomTab) -> Unit,
    onOpenSeniorChat: () -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    var showBigGrandmasterBadge by remember { mutableStateOf(false) }
    var showBigMasterBadge by remember { mutableStateOf(false) }
    var showBigDiamondBadge by remember { mutableStateOf(false) }
    var showBigSilverBadge by remember { mutableStateOf(false) }
    var showBigBronzeBadge by remember { mutableStateOf(false) }

    if (showBigGrandmasterBadge) {
        GrandmasterBigBadgeDialog(
            studentName = if (userRank == 1) userProfile.name else "Ananya Rao (Statewide #1)",
            points = if (userRank == 1) userPoints else 1450,
            onDismiss = { showBigGrandmasterBadge = false }
        )
    }

    if (showBigMasterBadge) {
        MasterBigBadgeDialog(
            studentName = if (userRank in 2..4) userProfile.name else "Rohan Kulkarni (Statewide #2)",
            rank = if (userRank in 2..4) userRank else 2,
            points = if (userRank in 2..4) userPoints else 1320,
            onDismiss = { showBigMasterBadge = false }
        )
    }

    if (showBigDiamondBadge) {
        DiamondBigBadgeDialog(
            studentName = if (userRank in 5..9) userProfile.name else "Pooja Hegde (Statewide #5)",
            rank = if (userRank in 5..9) userRank else 5,
            points = if (userRank in 5..9) userPoints else 1050,
            onDismiss = { showBigDiamondBadge = false }
        )
    }

    if (showBigSilverBadge) {
        SilverBigBadgeDialog(
            studentName = if (userRank in 10..15) userProfile.name else "Kavya Murthy (Statewide #10)",
            rank = if (userRank in 10..15) userRank else 10,
            points = if (userRank in 10..15) userPoints else 850,
            onDismiss = { showBigSilverBadge = false }
        )
    }

    if (showBigBronzeBadge) {
        BronzeBigBadgeDialog(
            studentName = if (userRank > 15) userProfile.name else "Vikas Gowda (Statewide #16)",
            rank = if (userRank > 15) userRank else 16,
            points = if (userRank > 15) userPoints else 620,
            onDismiss = { showBigBronzeBadge = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header Bar with 3-Lines Drawer Menu & Notification Bell
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer Sidebar",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "KCET GenZ Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Hello, ${userProfile.name.split(" ").firstOrNull() ?: "Aspirant"} 👋",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Bell Button with Badge
                    Box {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Senior Live Chat Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48))
                        )
                    )
                    .clickable { onOpenSeniorChat() }
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "💬 SENIOR MENTORSHIP",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF08A)
                        ) {
                            Text(
                                text = "OFFER ₹$liveChatPrice",
                                color = Color(0xFF854D0E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1-on-1 Personal Live Chat with Seniors",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Book real-time slot • Take personal guidance & clear doubts with assigned KCET Senior Mentors!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹100  ₹50 Only",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFEF08A)
                        )

                        Button(
                            onClick = onOpenSeniorChat,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Book Date & Time",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239)
                            )
                        }
                    }
                }
            }
        }

        // Live AI Doubt Solver Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF818CF8))
                        )
                    )
                    .clickable { onNavigateTab(BottomTab.DOUBTS) }
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE AI GURU",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "INSTANT SOLUTIONS",
                                color = Color(0xFF15803D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🤖 AI Doubt Solver (Text & Photo)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Upload question paper photos or type doubts • Immediate step-by-step Gemini solutions!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onNavigateTab(BottomTab.DOUBTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Ask Question / Insert Photo ➔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                        }
                    }
                }
            }
        }

        // Top Header: 3-Line Sidebar Drawer Button, Welcome Greeting & Notification Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 3-Line Sidebar Menu Icon Button
                    IconButton(onClick = onOpenDrawer) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEEF2FF),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Expand Sidebar Drawer",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = "⚡ $userPoints XP • Rank #$userRank",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hi, ${userProfile.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Notification Dashboard Bell Button with Badge Counter
                    Box {
                        IconButton(onClick = onOpenNotifications) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFF1F2),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification Dashboard",
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        if (unreadNotificationCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE11D48),
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = onOpenBookmarks) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved Bookmarks",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Featured Hero Card: KCET Full Mock 01 (Sleek Interface Gradient Hero)
        item {
            val heroGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4F46E5),
                    Color(0xFF4338CA),
                    Color(0xFF6B21A8)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color(0xFF4F46E5).copy(alpha = 0.3f), spotColor = Color(0xFF4F46E5).copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(heroGradient)
                    .padding(24.dp)
            ) {
                Column {
                    // Tag Badge with Pulsing Green Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FREE SAMPLE TEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "KCET Full Mock 01",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Physics • Chem • Math",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC7D2FE)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Simulate the real CBT experience with a 20-minute timed trial and instant analytics.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartFreeTest,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4338CA)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Start Practice Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Leaderboard Badges Showcase Banner (Student Portal Home Page)
        item {
            var selectedHomeBadgeTab by remember {
                mutableStateOf(
                    when (userRank) {
                        1 -> 0
                        in 2..4 -> 1
                        in 5..9 -> 2
                        in 10..15 -> 3
                        else -> 4
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mini scrollable tab switcher to explore all 5 tier Badges
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedHomeBadgeTab == 0) Color(0xFFD97706) else Color.Transparent)
                                .clickable { selectedHomeBadgeTab = 0 }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👑 GrandMaster",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedHomeBadgeTab == 0) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedHomeBadgeTab == 1) Color(0xFF7C3AED) else Color.Transparent)
                                .clickable { selectedHomeBadgeTab = 1 }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏆 Master",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedHomeBadgeTab == 1) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedHomeBadgeTab == 2) Color(0xFF0284C7) else Color.Transparent)
                                .clickable { selectedHomeBadgeTab = 2 }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💎 Diamond",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedHomeBadgeTab == 2) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedHomeBadgeTab == 3) Color(0xFF64748B) else Color.Transparent)
                                .clickable { selectedHomeBadgeTab = 3 }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥈 Silver",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedHomeBadgeTab == 3) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedHomeBadgeTab == 4) Color(0xFFEA580C) else Color.Transparent)
                                .clickable { selectedHomeBadgeTab = 4 }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥉 Bronze",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedHomeBadgeTab == 4) Color.White else Color(0xFF64748B)
                            )
                        }
                    }
                }

                when (selectedHomeBadgeTab) {
                    0 -> {
                        // GrandMaster Badge Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { showBigGrandmasterBadge = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFFD700), Color(0xFFF59E0B)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Color(0xFFF59E0B).copy(alpha = 0.5f))
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7))
                                            .border(2.dp, Color(0xFFF59E0B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_grandmaster_badge),
                                            contentDescription = "GrandMaster Badge",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "👑 GrandMaster Badge",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFF59E0B)
                                            ) {
                                                Text(
                                                    text = "RANK #1",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Statewide Leaderboard #1 Medal • Tap to enlarge",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(onClick = { showBigGrandmasterBadge = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Enlarge Grandmaster Badge",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Master Badge Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { showBigMasterBadge = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFC084FC), Color(0xFF8B5CF6)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Color(0xFF8B5CF6).copy(alpha = 0.5f))
                                            .clip(CircleShape)
                                            .background(Color(0xFFEDE9FE))
                                            .border(2.dp, Color(0xFF8B5CF6), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_master_badge),
                                            contentDescription = "Master Badge",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "🏆 Master Badge",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF581C87)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF7C3AED)
                                            ) {
                                                Text(
                                                    text = if (userRank in 2..4) "RANK #$userRank" else "RANK #2–#4",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Statewide Master Medal • Tap to enlarge",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7E22CE),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(onClick = { showBigMasterBadge = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Enlarge Master Badge",
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Diamond Badge Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { showBigDiamondBadge = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF0284C7)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Color(0xFF0284C7).copy(alpha = 0.5f))
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE))
                                            .border(2.dp, Color(0xFF0284C7), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_diamond_badge),
                                            contentDescription = "Diamond Badge",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "💎 Diamond Badge",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0369A1)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF0284C7)
                                            ) {
                                                Text(
                                                    text = if (userRank in 5..9) "RANK #$userRank" else "RANK #5–#9",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Statewide Diamond Medal • Tap to enlarge",
                                            fontSize = 11.sp,
                                            color = Color(0xFF0284C7),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(onClick = { showBigDiamondBadge = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Enlarge Diamond Badge",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        // Silver Badge Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { showBigSilverBadge = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFF64748B)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Color(0xFF64748B).copy(alpha = 0.5f))
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9))
                                            .border(2.dp, Color(0xFF64748B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_silver_badge),
                                            contentDescription = "Silver Badge",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "🥈 Silver Badge",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF64748B)
                                            ) {
                                                Text(
                                                    text = if (userRank in 10..15) "RANK #$userRank" else "RANK #10–#15",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Statewide Silver Achiever • Tap to enlarge",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(onClick = { showBigSilverBadge = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Enlarge Silver Badge",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Bronze Badge Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { showBigBronzeBadge = true },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFFDBA74), Color(0xFFEA580C)))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Color(0xFFEA580C).copy(alpha = 0.5f))
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEDD5))
                                            .border(2.dp, Color(0xFFEA580C), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_bronze_badge),
                                            contentDescription = "Bronze Badge",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "🥉 Bronze Badge",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFC2410C)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFEA580C)
                                            ) {
                                                Text(
                                                    text = if (userRank > 15) "RANK #$userRank" else "RANK #16+",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Statewide Bronze Contender • Tap to enlarge",
                                            fontSize = 11.sp,
                                            color = Color(0xFFEA580C),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(onClick = { showBigBronzeBadge = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Enlarge Bronze Badge",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid: 3 Columns (State Rank, Total XP Points, Practice Time)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Leaderboard Rank
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(BottomTab.PROFILE) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (userRank) {
                                        1 -> Color(0xFFFEF3C7)
                                        in 2..4 -> Color(0xFFEDE9FE)
                                        else -> Color(0xFFE0F2FE)
                                    }
                                )
                                .clickable {
                                    when (userRank) {
                                        1 -> showBigGrandmasterBadge = true
                                        in 2..4 -> showBigMasterBadge = true
                                        else -> showBigDiamondBadge = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = when (userRank) {
                                        1 -> R.drawable.img_grandmaster_badge
                                        in 2..4 -> R.drawable.img_master_badge
                                        else -> R.drawable.img_diamond_badge
                                    }
                                ),
                                contentDescription = when (userRank) {
                                    1 -> "GrandMaster Badge"
                                    in 2..4 -> "Master Badge"
                                    else -> "Diamond Badge"
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LEADERBOARD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "#$userRank",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (userRank) {
                                1 -> Color(0xFFD97706)
                                in 2..4 -> Color(0xFF7C3AED)
                                else -> Color(0xFF0284C7)
                            }
                        )
                    }
                }

                // Card 2: Total XP Points
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(BottomTab.PROFILE) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TOTAL XP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$userPoints XP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                // Card 3: Practice Time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TEST TIME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = testTimeDisplay,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        // KCET Rank vs Marks Dashboard & Predictor (Live Firebase Data)
        item {
            RankVsMarksDashboard(
                rankVsMarksList = rankVsMarksList,
                isPremiumUser = userProfile.isPremium,
                onUpgradeClick = onUpgradeClick
            )
        }

        // KCET Premium Pass Features Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onUpgradeClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "👑 KCET Premium Pass",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "All-In-One Exam & Counselling Toolkit",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF08A)
                        ) {
                            Text(
                                text = "SPECIAL ₹299",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF854D0E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "✨ What is included in Premium:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val premiumFeaturesList = listOf(
                        "📊 KCET Marks vs Rank Dashboard & Instant Predictor",
                        "📝 15+ Full Length CBT Mock Tests & Real Exam Templates",
                        "📚 10+ Years Official Solved PYQ Papers with Step Solutions",
                        "🌿 Branch Explorer: 200+ Karnataka Colleges & GM/2A/SC/ST Cutoffs",
                        "🏛️ College Recommendation & Rank Range",
                        "📖 Chapter-wise DPP Practice Sets & Formula eBooks",
                        "⚡ Priority AI Doubt Solver & Ad-Free Experience"
                    )

                    premiumFeaturesList.forEach { featureText ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• $featureText",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF451A03)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userProfile.isPremium) "⭐ Premium Unlocked (Manage)" else "⚡ Unlock All Features (₹299)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Featured Modules Grid Shortcuts
        item {
            Text(
                text = "Featured Resources",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(BottomTab.RESOURCES) }
                        .padding(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Syllabus & Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "PDF Study Material",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(BottomTab.COUNSELLING) }
                        .padding(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "KEA Counselling",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Option Entry Guide",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Important Dates & Schedule (Stored in Firebase)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ Important Dates & Schedule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEEF2FF),
                    onClick = { onNavigateTab(BottomTab.COUNSELLING) }
                ) {
                    Text(
                        text = "Full Timeline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        items(importantDates.take(4)) { dateItem ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(
                        1.dp,
                        if (dateItem.isHighlighted || dateItem.status == "Active Now") Color(0xFF6366F1) else Color(0xFFE2E8F0),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (dateItem.status) {
                                "Active Now" -> Color(0xFFDCFCE7)
                                "Crucial" -> Color(0xFFFEE2E2)
                                "Completed" -> Color(0xFFF1F5F9)
                                else -> Color(0xFFEEF2FF)
                            }
                        ) {
                            Text(
                                text = dateItem.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (dateItem.status) {
                                    "Active Now" -> Color(0xFF15803D)
                                    "Crucial" -> Color(0xFFB91C1C)
                                    "Completed" -> Color(0xFF64748B)
                                    else -> Color(0xFF4338CA)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = dateItem.dateOrDeadline,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dateItem.eventTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dateItem.description,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Latest Announcements
        item {
            Text(
                text = "📢 Latest Announcements",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        items(announcements) { ann ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (ann.isImportant) Color(0xFFFEE2E2) else Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (ann.isImportant) Color(0xFFEF4444) else Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = ann.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = ann.date,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ann.message,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

