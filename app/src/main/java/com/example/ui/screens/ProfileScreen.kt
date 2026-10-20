package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Phone
import com.example.data.HelplineItem
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.FirebaseService
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AnalyticsSummary
import com.example.data.BadgeItem
import com.example.data.LeaderboardEntry
import com.example.data.TestResultEntity
import com.example.data.UserProfile
import com.example.ui.components.BadgesSection
import com.example.ui.components.GlassCard
import com.example.ui.components.ImprovementPlanCard
import com.example.ui.components.LeaderboardCard
import com.example.ui.components.SubjectPerformanceBreakdown
import com.example.ui.components.TopicMasteryCard
import com.example.viewmodel.BottomTab

import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.ContactSupportDialog
import com.example.ui.components.GrandmasterBigBadgeDialog
import com.example.ui.components.MasterBigBadgeDialog
import com.example.ui.components.DiamondBigBadgeDialog
import androidx.compose.foundation.Image
import com.example.ui.components.DisclaimerDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.TermsAndConditionsDialog

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    testResults: List<TestResultEntity>,
    userPoints: Int = 450,
    leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    badges: List<BadgeItem> = emptyList(),
    analyticsSummary: AnalyticsSummary? = null,
    helplines: List<HelplineItem> = emptyList(),
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onUpdateProfile: (String, String, String, String?) -> Unit,
    onUpgradeToPremium: () -> Unit,
    onLogout: () -> Unit,
    onRemovePhoto: () -> Unit = {},
    onUnlockAllBadges: () -> Unit = {},
    onNavigateTab: (BottomTab) -> Unit = {},
    onDeleteTestResult: (Int) -> Unit = {},
    onClearTestHistory: () -> Unit = {},
    onChallengeStudent: ((LeaderboardEntry) -> Unit)? = null
) {
    val context = LocalContext.current

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Profile, 1: Leaderboard & Badges, 2: Performance Analytics

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showClearHistoryConfirmDialog by remember { mutableStateOf(false) }
    var testResultToDeleteId by remember { mutableStateOf<Int?>(null) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showProfileBigGrandmasterBadge by remember { mutableStateOf(false) }
    var showProfileBigMasterBadge by remember { mutableStateOf(false) }
    var showProfileBigDiamondBadge by remember { mutableStateOf(false) }
    var showProfileBigSilverBadge by remember { mutableStateOf(false) }
    var showProfileBigBronzeBadge by remember { mutableStateOf(false) }

    if (showProfileBigGrandmasterBadge) {
        GrandmasterBigBadgeDialog(
            studentName = userProfile.name,
            points = userPoints,
            onDismiss = { showProfileBigGrandmasterBadge = false }
        )
    }

    if (showProfileBigMasterBadge) {
        MasterBigBadgeDialog(
            studentName = userProfile.name,
            rank = leaderboardEntries.find { it.uid == userProfile.uid || it.email.equals(userProfile.email, true) || it.isCurrentUser }?.rank ?: 2,
            points = userPoints,
            onDismiss = { showProfileBigMasterBadge = false }
        )
    }

    if (showProfileBigDiamondBadge) {
        DiamondBigBadgeDialog(
            studentName = userProfile.name,
            rank = leaderboardEntries.find { it.uid == userProfile.uid || it.email.equals(userProfile.email, true) || it.isCurrentUser }?.rank ?: 5,
            points = userPoints,
            onDismiss = { showProfileBigDiamondBadge = false }
        )
    }

    if (showProfileBigSilverBadge) {
        com.example.ui.components.SilverBigBadgeDialog(
            studentName = userProfile.name,
            rank = leaderboardEntries.find { it.uid == userProfile.uid || it.email.equals(userProfile.email, true) || it.isCurrentUser }?.rank ?: 10,
            points = userPoints,
            onDismiss = { showProfileBigSilverBadge = false }
        )
    }

    if (showProfileBigBronzeBadge) {
        com.example.ui.components.BronzeBigBadgeDialog(
            studentName = userProfile.name,
            rank = leaderboardEntries.find { it.uid == userProfile.uid || it.email.equals(userProfile.email, true) || it.isCurrentUser }?.rank ?: 16,
            points = userPoints,
            onDismiss = { showProfileBigBronzeBadge = false }
        )
    }

    var editName by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var editRank by remember(userProfile.kcetTargetRank) { mutableStateOf(userProfile.kcetTargetRank) }
    var editStream by remember(userProfile.targetStream) { mutableStateOf(userProfile.targetStream) }
    var editPhotoUrl by remember(userProfile.photoUrl) { mutableStateOf(userProfile.photoUrl ?: "") }

    var newPass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUpdateProfile(
                userProfile.name,
                userProfile.kcetTargetRank,
                userProfile.targetStream,
                it.toString()
            )
            Toast.makeText(context, "Profile photo updating...", Toast.LENGTH_SHORT).show()
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Top Segmented Tab Switcher (Profile | Leaderboard | Analytics)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Profile", "Leaderboard", "Analytics").forEachIndexed { index, title ->
                        val isSelected = selectedSubTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedSubTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 1) {
            // Leaderboard & Badges Tab Content
            item {
                LeaderboardCard(
                    leaderboardEntries = leaderboardEntries,
                    onChallengeStudent = onChallengeStudent
                )
            }

            item {
                BadgesSection(badges = badges, onUnlockAllBadges = onUnlockAllBadges)
            }
        } else if (selectedSubTab == 2) {
            // Performance Analytics Tab Content
            analyticsSummary?.let { analytics ->
                item {
                    SubjectPerformanceBreakdown(subjectBreakdown = analytics.subjectBreakdown)
                }

                item {
                    TopicMasteryCard(topicAccuracies = analytics.topicAccuracies)
                }

                item {
                    ImprovementPlanCard(
                        weakAreas = analytics.weakAreas,
                        recommendations = analytics.recommendations,
                        onOpenResources = { onNavigateTab(BottomTab.RESOURCES) },
                        onOpenPractice = { onNavigateTab(BottomTab.PRACTICE) }
                    )
                }
            }
        } else {
            // Profile & Settings Tab Content (Default)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFE0F2FE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBAE6FD))
                                    .border(2.5.dp, Color(0xFF0284C7), CircleShape)
                                    .clickable { galleryLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!userProfile.photoUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.kcet_genz_logo_1785569849419),
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7))
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { galleryLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Upload Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (!userProfile.photoUrl.isNullOrBlank()) {
                            TextButton(
                                onClick = { onRemovePhoto() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = userProfile.email,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )

                        val currentStudentEntry = leaderboardEntries.find { it.uid == userProfile.uid || it.email.equals(userProfile.email, true) || it.isCurrentUser }
                        val studentRank = currentStudentEntry?.rank ?: 25
                        val studentBadgeInfo = com.example.ui.components.getLeaderboardBadgeInfo(studentRank)

                        if (studentBadgeInfo != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = studentBadgeInfo.containerColor,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, studentBadgeInfo.borderColor),
                                shadowElevation = 2.dp,
                                modifier = when (studentRank) {
                                    1 -> Modifier.clickable { showProfileBigGrandmasterBadge = true }
                                    in 2..4 -> Modifier.clickable { showProfileBigMasterBadge = true }
                                    in 5..9 -> Modifier.clickable { showProfileBigDiamondBadge = true }
                                    in 10..15 -> Modifier.clickable { showProfileBigSilverBadge = true }
                                    else -> Modifier.clickable { showProfileBigBronzeBadge = true }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (studentRank == 1) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_grandmaster_badge),
                                            contentDescription = "Grandmaster Badge (Tap to enlarge)",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .border(0.5.dp, Color(0xFFD97706), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else if (studentRank in 2..4) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_master_badge),
                                            contentDescription = "Master Badge (Tap to enlarge)",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .border(0.5.dp, Color(0xFF7C3AED), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else if (studentRank in 5..9) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_diamond_badge),
                                            contentDescription = "Diamond Badge (Tap to enlarge)",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .border(0.5.dp, Color(0xFF0284C7), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else if (studentRank in 10..15) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_silver_badge),
                                            contentDescription = "Silver Badge (Tap to enlarge)",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .border(0.5.dp, Color(0xFF64748B), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_bronze_badge),
                                            contentDescription = "Bronze Badge (Tap to enlarge)",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .border(0.5.dp, Color(0xFFEA580C), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Rank #${studentRank} • ${studentBadgeInfo.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = studentBadgeInfo.contentColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFBAE6FD),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC))
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Student ID", userProfile.formattedStudentId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied Student ID: ${userProfile.formattedStudentId}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🆔 Student ID: ${userProfile.formattedStudentId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (userProfile.isPremium) Color(0xFF9333EA) else Color(0xFF0284C7)
                        ) {
                            Text(
                                text = if (userProfile.isPremium) "⭐ PREMIUM PRO CANDIDATE" else "FREE CANDIDATE PLAN",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A), containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC))
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", fontSize = 13.sp, color = Color(0xFF0F172A))
                            }

                            OutlinedButton(
                                onClick = { showChangePasswordDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A), containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC))
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Password", fontSize = 13.sp, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            // App Preferences & Theme Mode
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE0F2FE))
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "App Settings & Modes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Dark Theme Mode", fontSize = 14.sp, color = Color(0xFF1E293B))
                            }
                            Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                        }
                    }
                }
            }

            // Legal, Support & App Information Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE0F2FE))
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "App Info, Legal & Support", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        Spacer(modifier = Modifier.height(12.dp))

                        // About App
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showAboutDialog = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("About KCET Gen Z", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("Version 1.0.0 (Production Build)", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                            Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        // Contact Support
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showSupportDialog = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Need Help? Contact Support", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("KcetGenZservice@gmail.com", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                            Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        // Terms & Conditions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showTermsDialog = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Terms & Conditions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            }
                            Text("Read", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        // Privacy Policy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showPrivacyDialog = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Privacy Policy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            }
                            Text("Read", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        // KEA Disclaimer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showDisclaimerDialog = true }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("KEA Official Disclaimer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            }
                            Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "© 2026 KCET Gen Z. All Rights Reserved.\nDeveloped & Maintained by Gen Z EduTech.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Helplines & Student Support Section
            item {
                val activeHelplines = if (helplines.isNotEmpty()) helplines else com.example.data.SampleData.defaultHelplines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Helpline & Student Support", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text("24/7 HELPLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Have questions about KCET Gen Z, registration, or counseling? Reach out directly:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        activeHelplines.forEach { help ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = help.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (help.type) {
                                                    "EMAIL" -> Color(0xFFEEF2FF)
                                                    "INSTAGRAM" -> Color(0xFFFDF2F8)
                                                    else -> Color(0xFFF0FDF4)
                                                }
                                            ) {
                                                Text(
                                                    text = help.type,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (help.type) {
                                                        "EMAIL" -> Color(0xFF4F46E5)
                                                        "INSTAGRAM" -> Color(0xFFDB2777)
                                                        else -> Color(0xFF16A34A)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        OutlinedButton(
                                            onClick = {
                                                val targetUrl = if (help.actionUrl.isNotBlank()) help.actionUrl
                                                else if (help.type == "EMAIL") "mailto:${help.contactValue}"
                                                else "https://instagram.com/ullassuvarna65"
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Helpline", help.contactValue)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Copied ${help.contactValue} to clipboard!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (help.type == "EMAIL") "📧 Email" else if (help.type == "INSTAGRAM") "📸 Open Insta" else "Contact",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(text = help.contactValue, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4F46E5))
                                    
                                    if (help.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = help.description, fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Test History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your CBT Test History (${testResults.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    if (testResults.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearHistoryConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (testResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "No practice test attempts recorded yet. Take any test to build your performance history and earn XP!",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                items(testResults) { res ->
                    val qCount = res.totalQuestions.coerceAtLeast(1)
                    val attempted = res.correctCount + res.wrongCount
                    val attemptedRatio = (attempted.toFloat() / qCount).coerceIn(0f, 1f)
                    val isQualified = attemptedRatio > 0.5f

                    val baseCorrectXP = maxOf(0, res.correctCount) * 15
                    val completionBonus = (50f * attemptedRatio).toInt()
                    val speedPerQ = if (qCount > 0) (res.timeTakenSeconds / qCount).toInt() else 60
                    val speedBonus = if (speedPerQ < 40) (25f * attemptedRatio).toInt() else 0
                    val earnedXP = if (isQualified) (baseCorrectXP + completionBonus + speedBonus).coerceAtLeast(0) else 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.testName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Score: ${res.score}/${res.totalQuestions} (${String.format("%.1f", res.percentage)}%) • $attempted Attempted",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (earnedXP > 0) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = if (earnedXP > 0) "+$earnedXP XP" else "0 XP (<=50%)",
                                        fontSize = 11.sp,
                                        color = if (earnedXP > 0) Color(0xFF166534) else Color(0xFF92400E),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteTestResult(res.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Test Result",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Details") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRank,
                        onValueChange = { editRank = it },
                        label = { Text("Target Rank") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editStream,
                        onValueChange = { editStream = it },
                        label = { Text("Target Stream") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Gallery", color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (editPhotoUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { editPhotoUrl = "" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove Photo", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhotoUrl,
                        onValueChange = { editPhotoUrl = it },
                        label = { Text("Profile Photo / Avatar Image URL") },
                        placeholder = { Text("https://images.unsplash.com/photo-...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Quick Avatar Samples:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sampleAvatars = listOf(
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150",
                            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"
                        )
                        sampleAvatars.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (editPhotoUrl == url) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                                        CircleShape
                                    )
                                    .clickable { editPhotoUrl = url }
                            ) {
                                coil.compose.AsyncImage(
                                    model = url,
                                    contentDescription = "Avatar $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onUpdateProfile(
                            editName.trim(),
                            editRank.trim(),
                            editStream.trim(),
                            if (editPhotoUrl.isNotBlank()) editPhotoUrl.trim() else null
                        )
                        Toast.makeText(context, "Profile details updated & synced across app!", Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    val profileScope = rememberCoroutineScope()
    var isNewPassVisible by remember { mutableStateOf(false) }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password (min 6 chars)") },
                    trailingIcon = {
                        IconButton(onClick = { isNewPassVisible = !isNewPassVisible }) {
                            Icon(
                                imageVector = if (isNewPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isNewPassVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (isNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val passTrimmed = newPass.trim()
                        if (passTrimmed.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        profileScope.launch {
                            try {
                                FirebaseService.auth.currentUser?.updatePassword(passTrimmed)
                            } catch (_: Exception) {}

                            FirebaseService.updateUserPasswordInFirestore(userProfile.email.ifBlank { userProfile.uid }, passTrimmed)
                            Toast.makeText(context, "Password updated successfully! Please use your new password on next login.", Toast.LENGTH_LONG).show()
                            showChangePasswordDialog = false
                            newPass = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Legal & Support Dialogs
    if (showTermsDialog) {
        TermsAndConditionsDialog(onDismiss = { showTermsDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showSupportDialog) {
        ContactSupportDialog(onDismiss = { showSupportDialog = false })
    }
    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
    if (showDisclaimerDialog) {
        DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out? You will need to sign in again to access your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearHistoryConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirmDialog = false },
            title = { Text("Clear Test History", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all test history records? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearHistoryConfirmDialog = false
                        onClearTestHistory()
                        Toast.makeText(context, "Test history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearHistoryConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (testResultToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { testResultToDeleteId = null },
            title = { Text("Delete Test Result", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this test result record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = testResultToDeleteId!!
                        testResultToDeleteId = null
                        onDeleteTestResult(id)
                        Toast.makeText(context, "Test result deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { testResultToDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

