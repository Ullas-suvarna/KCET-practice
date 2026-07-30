package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
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

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    testResults: List<TestResultEntity>,
    userPoints: Int = 450,
    leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    badges: List<BadgeItem> = emptyList(),
    analyticsSummary: AnalyticsSummary? = null,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onUpgradeToPremium: () -> Unit,
    onLogout: () -> Unit,
    onOpenAdmin: () -> Unit,
    onNavigateTab: (BottomTab) -> Unit = {}
) {
    val context = LocalContext.current

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Profile, 1: Leaderboard & Badges, 2: Performance Analytics

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(userProfile.name) }
    var editRank by remember { mutableStateOf(userProfile.kcetTargetRank) }
    var editStream by remember { mutableStateOf(userProfile.targetStream) }

    var newPass by remember { mutableStateOf("") }

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
                LeaderboardCard(leaderboardEntries = leaderboardEntries)
            }

            item {
                BadgesSection(badges = badges)
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
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_kcet_icon_1785390517153),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.padding(12.dp)
                            )
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
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (userProfile.isPremium) Color(0xFF9333EA) else Color(0xFF4F46E5)
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showChangePasswordDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Password", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            // App Preferences
            item {
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
                                    tint = Color(0xFF4F46E5)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Dark Theme Mode", fontSize = 14.sp, color = Color(0xFF1E293B))
                            }
                            Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAdmin() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF4F46E5))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Open Admin Portal Screen", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            }
                            Text("Open >", color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Test History Section
            item {
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
                            text = "No practice test attempts recorded yet. Take the free test to build your performance history!",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                items(testResults) { res ->
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
                            Column {
                                Text(text = res.testName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Text(
                                    text = "Score: ${res.score}/${res.totalQuestions} (${String.format("%.1f", res.percentage)}%)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = "${res.correctCount} Correct",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4F46E5),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = onLogout,
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editRank, editStream)
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
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

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        showChangePasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

