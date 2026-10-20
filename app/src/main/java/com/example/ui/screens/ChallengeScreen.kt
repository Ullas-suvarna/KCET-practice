package com.example.ui.screens

import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChallengeData
import com.example.data.ChallengeService
import com.example.data.LeaderboardEntry
import com.example.data.UserProfile
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    userProfile: UserProfile,
    challenges: List<ChallengeData>,
    leaderboard: List<LeaderboardEntry>,
    onBack: () -> Unit,
    onStartQuiz: (ChallengeData) -> Unit,
    onAcceptChallenge: (String) -> Unit,
    onRejectChallenge: (String) -> Unit,
    onJoinChallenge: (String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onDeleteHistory: (String) -> Unit = {},
    onChallengeStudent: (LeaderboardEntry) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Incoming, 2: Sent, 3: Active/Upcoming, 4: History
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedStudentForDetails by remember { mutableStateOf<LeaderboardEntry?>(null) }

    // Realtime timer tick trigger
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val currentUid = if (userProfile.uid.isNotBlank()) userProfile.uid else "user_" + userProfile.email.replace(".", "_")

    val incoming = challenges.filter { it.challengedId == currentUid && it.status == "PENDING" && it.historyDeletedBy[currentUid] != true }
    val sent = challenges.filter { it.challengerId == currentUid && it.status == "PENDING" && it.historyDeletedBy[currentUid] != true }
    val activeUpcoming = challenges.filter {
        (it.challengerId == currentUid || it.challengedId == currentUid) &&
                it.historyDeletedBy[currentUid] != true &&
                (it.status == "ACCEPTED" || it.status == "READY" || it.status == "IN_PROGRESS" || it.status == "PREPARING" || it.status == "JOINING" || it.status == "READY_TO_START")
    }
    val history = challenges.filter {
        (it.challengerId == currentUid || it.challengedId == currentUid) &&
                it.historyDeletedBy[currentUid] != true &&
                (it.status == "COMPLETED" || it.status == "REJECTED" || it.status == "EXPIRED" || it.status == "TERMINATED" || it.status == "DRAW")
    }

    val filteredList = when (selectedTab) {
        1 -> incoming
        2 -> sent
        3 -> activeUpcoming
        4 -> history
        else -> challenges.filter { it.historyDeletedBy[currentUid] != true }
    }

    var showQuitTestConfirm by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val joinedActiveChallenge = activeUpcoming.find { ch ->
        val hasJoined = if (ch.challengerId == currentUid) ch.challengerJoined else ch.challengedJoined
        val myStat = if (ch.challengerId == currentUid) ch.challengerStatus else ch.challengedStatus
        hasJoined && (ch.status == "JOINING" || ch.status == "READY_TO_START" || ch.status == "IN_PROGRESS") && myStat == "IN_PROGRESS"
    }

    BackHandler(enabled = joinedActiveChallenge != null) {
        showQuitTestConfirm = true
    }

    if (showQuitTestConfirm && joinedActiveChallenge != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuitTestConfirm = false },
            title = { Text("Are you ready to quit test?", fontWeight = FontWeight.Bold) },
            text = { Text("If you quit now, you will forfeit the challenge and return to home page. Your opponent will automatically win the challenge.", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitTestConfirm = false
                        val challengeId = joinedActiveChallenge.challengeId
                        coroutineScope.launch {
                            ChallengeService.quitChallengeQuiz(challengeId, currentUid, emptyMap(), 0L)
                        }
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Quit & Forfeit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitTestConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("⚔️ 1 VS 1 CHALLENGE", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Compete in 20-Q PCM Head-to-Head Tests", fontSize = 11.sp, color = Color(0xFF93C5FD))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Summary Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("Incoming", "${incoming.size}", Color(0xFFE11D48), modifier = Modifier.weight(1f))
                StatChip("Sent", "${sent.size}", Color(0xFFD97706), modifier = Modifier.weight(1f))
                StatChip("Active", "${activeUpcoming.size}", Color(0xFF10B981), modifier = Modifier.weight(1f))
                StatChip("History", "${history.size}", Color(0xFF6366F1), modifier = Modifier.weight(1f))
            }

            // Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("All", "Incoming", "Sent", "Active", "History", "Challenge")
                items(tabs.size) { idx ->
                    val isSel = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF4F46E5) else Color.Transparent)
                            .clickable {
                                if (joinedActiveChallenge != null) {
                                    showQuitTestConfirm = true
                                } else {
                                    if (idx == 5) {
                                        onNavigateToLeaderboard()
                                    } else {
                                        selectedTab = idx
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tabs[idx],
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Dropdown Selector for Challenging
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF6366F1))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = !isDropdownExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedStudentForDetails?.let { "Selected: ${it.name} (Rank #${it.rank})" } ?: "⚔️ Select Student to Challenge...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (isDropdownExpanded) "▲ Close" else "▼ Select",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    if (isDropdownExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))

                        val studentsList = leaderboard.filter { entry ->
                            val entryUid = if (entry.uid.isNotBlank()) entry.uid else "user_" + entry.email.replace(".", "_")
                            entryUid != currentUid && !entry.isCurrentUser
                        }.sortedBy { it.rank }

                        if (studentsList.isEmpty()) {
                            Text("No other students available on leaderboard yet.", fontSize = 11.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(8.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(studentsList, key = { it.uid.ifBlank { it.email } }) { student ->
                                        val badgeInfo = com.example.ui.components.getLeaderboardBadgeInfo(student.rank)
                                        val badgeTitle = badgeInfo?.title ?: "🏅 Student"
                                        val isSelected = selectedStudentForDetails?.uid == student.uid

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFF4F46E5).copy(alpha = 0.3f) else Color(0xFF0F172A),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF6366F1) else Color(0xFF334155)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedStudentForDetails = student
                                                    isDropdownExpanded = false
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFF6366F1),
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(student.avatarInitials, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(student.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("Badge: $badgeTitle • Rank #${student.rank}", fontSize = 10.sp, color = Color(0xFFF59E0B))
                                                    }
                                                }
                                                Text("XP: ${student.points}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Student Details & Challenge Option
            selectedStudentForDetails?.let { student ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(student.avatarInitials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(student.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    val badgeInfo = com.example.ui.components.getLeaderboardBadgeInfo(student.rank)
                                    Text(badgeInfo?.title ?: "🏅 Student", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            IconButton(onClick = { selectedStudentForDetails = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rank: #${student.rank}", fontSize = 11.sp, color = Color(0xFF93C5FD))
                            Text("Total XP: ${student.points}", fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            Text("Accuracy: ${String.format(java.util.Locale.US, "%.1f", student.accuracy)}%", fontSize = 11.sp, color = Color(0xFFFDE68A))
                            Text("Tests: ${student.testsCount}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onChallengeStudent(student)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("⚔️ CHALLENGE ${student.name.uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚔️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No challenges in this category", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap any student profile in the Leaderboard to send a 1v1 challenge!", fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.challengeId }) { ch ->
                        StudentChallengeCard(
                            challenge = ch,
                            currentUid = currentUid,
                            now = currentTime,
                            onAccept = { onAcceptChallenge(ch.challengeId) },
                            onReject = { onRejectChallenge(ch.challengeId) },
                            onStartQuiz = { onStartQuiz(ch) },
                            onJoin = { onJoinChallenge(ch.challengeId) },
                            onDeleteHistory = { onDeleteHistory(ch.challengeId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
        }
    }
}

@Composable
fun StudentChallengeCard(
    challenge: ChallengeData,
    currentUid: String,
    now: Long,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStartQuiz: () -> Unit,
    onJoin: () -> Unit,
    onDeleteHistory: () -> Unit = {}
) {
    val isChallenger = (challenge.challengerId == currentUid)
    val opponentName = if (isChallenger) challenge.challengedName else challenge.challengerName
    val opponentBadge = if (isChallenger) challenge.challengedBadge else challenge.challengedBadge
    val opponentRank = if (isChallenger) challenge.challengedRank else challenge.challengedRank

    val isPendingIncoming = (!isChallenger && challenge.status == "PENDING")
    val isPendingSent = (isChallenger && challenge.status == "PENDING")

    val remainingMs = challenge.getEffectiveRemainingMs(now)
    val formattedTime = formatMsToClock(remainingMs)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF4F46E5).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (challenge.status) {
                "IN_PROGRESS", "READY_TO_START" -> Color(0xFF10B981)
                "ACCEPTED", "READY" -> Color(0xFF38BDF8)
                "PREPARING", "JOINING" -> Color(0xFFF59E0B)
                "PENDING" -> Color(0xFFF59E0B)
                "COMPLETED" -> Color(0xFF6366F1)
                else -> Color(0xFF475569)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Type & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (challenge.isXpChallenge) Color(0xFFD97706).copy(alpha = 0.2f) else Color(0xFF0D9488).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, if (challenge.isXpChallenge) Color(0xFFF59E0B) else Color(0xFF2DD4BF))
                ) {
                    Text(
                        text = if (challenge.isXpChallenge) "🔥 200 XP Challenge" else "🤝 Friendly Challenge",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (challenge.isXpChallenge) Color(0xFFFDE68A) else Color(0xFF99F6E4),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (challenge.status) {
                        "PENDING" -> Color(0xFFD97706)
                        "PREPARING" -> Color(0xFF92400E)
                        "JOINING" -> Color(0xFFB45309)
                        "ACCEPTED", "READY", "READY_TO_START" -> Color(0xFF0284C7)
                        "IN_PROGRESS" -> Color(0xFF059669)
                        "COMPLETED" -> Color(0xFF4F46E5)
                        "EXPIRED" -> Color(0xFF64748B)
                        "REJECTED" -> Color(0xFFE11D48)
                        "TERMINATED" -> Color(0xFF991B1B)
                        else -> Color(0xFF475569)
                    }
                ) {
                    Text(
                        text = challenge.status,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opponent vs Player Visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("YOU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(if (isChallenger) challenge.challengerName else challenge.challengedName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Rank #${if (isChallenger) challenge.challengerRank else challenge.challengedRank}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }

                Text("VS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFEC4899), modifier = Modifier.padding(horizontal = 8.dp))

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("OPPONENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(opponentName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End)
                    Text("Rank #$opponentRank • $opponentBadge", fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Paused Banner
            if (challenge.paused) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDC2626).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⏸ CHALLENGE PAUSED BY ADMIN (Remaining: $formattedTime)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }

            // Timers & State Actions
            if (!challenge.paused && challenge.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Response Time: $formattedTime", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }

                    if (isPendingIncoming) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onReject,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5))
                            ) {
                                Text("REJECT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onAccept,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("ACCEPT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text("Waiting for opponent...", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (challenge.status == "PREPARING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preparation Time Left: $formattedTime", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
            
            if (challenge.status == "JOINING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join Deadline: $formattedTime", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }

                    val hasJoined = if (isChallenger) challenge.challengerJoined else challenge.challengedJoined
                    if (!hasJoined) {
                        Button(
                            onClick = onJoin,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("JOIN TEST", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Waiting for opponent...", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (!challenge.paused && (challenge.status == "ACCEPTED" || challenge.status == "READY")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⏳ Quiz Starts In: $formattedTime", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBAE6FD))
                    }

                    Button(
                        onClick = onStartQuiz,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("🚀 Ready / Launch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val myStatus = if (isChallenger) challenge.challengerStatus else challenge.challengedStatus
            val oppStatus = if (isChallenger) challenge.challengedStatus else challenge.challengerStatus

            if ((challenge.status == "IN_PROGRESS" || challenge.status == "READY_TO_START") && myStatus != "SUBMITTED" && myStatus != "QUIT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (challenge.status == "IN_PROGRESS") "🚀 Quiz is LIVE now!" else "⏳ Starting soon...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    Button(
                        onClick = onStartQuiz,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("▶ START 20-Q QUIZ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (challenge.status == "COMPLETED" || challenge.status == "DRAW" || challenge.status == "EXPIRED" || challenge.status == "REJECTED" || challenge.status == "TERMINATED") {
                var showResultDialog by remember { mutableStateOf(false) }
                var showDeleteConfirm by remember { mutableStateOf(false) }
                val myScore = if (isChallenger) challenge.challengerScore else challenge.challengedScore
                val oppScore = if (isChallenger) challenge.challengedScore else challenge.challengerScore
                val iWon = (challenge.winnerId == currentUid)

                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (challenge.isDraw) "🤝 DRAW: $myScore / 20 vs $oppScore / 20"
                        else if (iWon) "🏆 YOU WON! $myScore vs $oppScore"
                        else "❌ RESULT: $myScore vs $oppScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (challenge.isDraw) Color(0xFFFDE68A) else if (iWon) Color(0xFF34D399) else Color(0xFFFCA5A5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5))
                        ) {
                            Text("Delete History", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        if (challenge.status == "COMPLETED" || challenge.status == "DRAW") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showResultDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("View Result", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showResultDialog) {
                    ChallengeResultDialog(
                        challenge = challenge,
                        currentUid = currentUid,
                        onDismiss = { showResultDialog = false }
                    )
                }

                if (showDeleteConfirm) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete this challenge from your history?", fontWeight = FontWeight.Bold) },
                        text = { Text("This will remove it from your challenge history. Authoritative audit records and opponent history remain intact.", fontSize = 12.sp) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDeleteHistory()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            } else if (myStatus == "SUBMITTED" || myStatus == "QUIT") {
                var showResultDialog by remember { mutableStateOf(false) }
                val bothSubmitted = (oppStatus == "SUBMITTED" || oppStatus == "QUIT")

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (myStatus == "SUBMITTED") Color(0xFF065F46).copy(alpha = 0.3f) else Color(0xFF991B1B).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, if (myStatus == "SUBMITTED") Color(0xFF10B981) else Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (bothSubmitted) "✅ Both Tests Submitted" else if (myStatus == "SUBMITTED") "✅ Test Submitted Successfully" else "⚠️ Test Exited",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (myStatus == "SUBMITTED") Color(0xFF34D399) else Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (bothSubmitted) "Your result is ready."
                            else "⏳ Waiting for another student to finish their test...",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                        if (myStatus == "SUBMITTED") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your Score: ${if (isChallenger) challenge.challengerScore ?: "?" else challenge.challengedScore ?: "?"} / 20",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (bothSubmitted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showResultDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("View Result", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showResultDialog) {
                    ChallengeResultDialog(
                        challenge = challenge,
                        currentUid = currentUid,
                        onDismiss = { showResultDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeResultDialog(
    challenge: ChallengeData,
    currentUid: String,
    onDismiss: () -> Unit
) {
    val isChallenger = (challenge.challengerId == currentUid)
    val myName = if (isChallenger) challenge.challengerName else challenge.challengedName
    val oppName = if (isChallenger) challenge.challengedName else challenge.challengerName
    val myScore = if (isChallenger) challenge.challengerScore else challenge.challengedScore
    val oppScore = if (isChallenger) challenge.challengedScore else challenge.challengerScore
    val myCorrect = if (isChallenger) challenge.challengerCorrectCount else challenge.challengedCorrectCount
    val oppCorrect = if (isChallenger) challenge.challengedCorrectCount else challenge.challengerCorrectCount
    val myWrong = if (isChallenger) challenge.challengerWrongCount else challenge.challengedWrongCount
    val oppWrong = if (isChallenger) challenge.challengedWrongCount else challenge.challengerWrongCount
    val myUnanswered = if (isChallenger) challenge.challengerUnansweredCount else challenge.challengedUnansweredCount
    val oppUnanswered = if (isChallenger) challenge.challengedUnansweredCount else challenge.challengerUnansweredCount
    val myAccuracy = if (isChallenger) challenge.challengerAccuracy else challenge.challengedAccuracy
    val oppAccuracy = if (isChallenger) challenge.challengedAccuracy else challenge.challengerAccuracy

    val myAnswers = if (isChallenger) challenge.challengerAnswers else challenge.challengedAnswers
    val oppAnswers = if (isChallenger) challenge.challengedAnswers else challenge.challengerAnswers

    val qSet = ChallengeService.getQuestionSetById(challenge.questionSetId)
    val questions = qSet?.questions ?: emptyList()

    var expandedQuestionId by remember { mutableStateOf<Int?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚔️ 1 vs 1 Challenge Result", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("YOU ($myName)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Text("Score: ${myScore ?: 0} / ${questions.size}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text("Correct: $myCorrect | Wrong: $myWrong | Unanswered: $myUnanswered", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("Accuracy: ${String.format(java.util.Locale.US, "%.1f", myAccuracy)}%", fontSize = 10.sp, color = Color(0xFF34D399))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("OPPONENT ($oppName)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF472B6))
                                Text("Score: ${oppScore ?: 0} / ${questions.size}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text("Correct: $oppCorrect | Wrong: $oppWrong | Unanswered: $oppUnanswered", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("Accuracy: ${String.format(java.util.Locale.US, "%.1f", oppAccuracy)}%", fontSize = 10.sp, color = Color(0xFF34D399))
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        Text(
                            text = if (challenge.isDraw) "🤝 Draw!" else if (challenge.winnerId == currentUid) "🏆 Winner: YOU!" else "❌ Winner: Opponent",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (challenge.isDraw) Color(0xFFFDE68A) else if (challenge.winnerId == currentUid) Color(0xFF34D399) else Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Question-by-Question Comparison", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                questions.forEachIndexed { index, q ->
                    val qNum = index + 1
                    val isExpanded = expandedQuestionId == q.id
                    val correctOptStr = q.correctOption.name
                    val myAns = myAnswers[q.id.toString()] ?: "-"
                    val oppAns = oppAnswers[q.id.toString()] ?: "-"

                    val myIsCorrect = myAns == correctOptStr
                    val oppIsCorrect = oppAns == correctOptStr

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Question $qNum (${q.subject})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                                TextButton(onClick = { expandedQuestionId = if (isExpanded) null else q.id }) {
                                    Text(if (isExpanded) "▲ Hide Explanation" else "▼ View Explanation", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(q.questionText, fontSize = 12.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Your Answer: $myAns", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                Text("Opponent Answer: $oppAns", fontSize = 11.sp, color = Color(0xFFF472B6))
                                Text("Correct Answer: $correctOptStr", fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "You: " + if (myAns == "-") "⚠️ Unanswered" else if (myIsCorrect) "✅ Correct" else "❌ Wrong",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (myAns == "-") Color(0xFFF59E0B) else if (myIsCorrect) Color(0xFF34D399) else Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "Opponent: " + if (oppAns == "-") "⚠️ Unanswered" else if (oppIsCorrect) "✅ Correct" else "❌ Wrong",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (oppAns == "-") Color(0xFFF59E0B) else if (oppIsCorrect) Color(0xFF34D399) else Color(0xFFFCA5A5)
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Options:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text("A. ${q.optionA}", fontSize = 11.sp, color = Color.White)
                                Text("B. ${q.optionB}", fontSize = 11.sp, color = Color.White)
                                Text("C. ${q.optionC}", fontSize = 11.sp, color = Color.White)
                                Text("D. ${q.optionD}", fontSize = 11.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Explanation:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Text(q.explanation.ifBlank { "No explanation provided." }, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }
        }
    }
}



fun formatMsToClock(ms: Long): String {
    if (ms <= 0L) return "00:00:00"
    val totalSec = ms / 1000L
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
