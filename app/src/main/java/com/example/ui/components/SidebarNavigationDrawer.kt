package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.viewmodel.BottomTab
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.ContactSupportDialog
import com.example.ui.components.DisclaimerDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.TermsAndConditionsDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SidebarNavigationDrawer(
    drawerState: DrawerState,
    userProfile: UserProfile,
    unreadNotificationCount: Int = 0,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSeniorChat: () -> Unit = {},
    onNavigateAiRecommendation: () -> Unit = {},
    onNavigateBranchExplorer: () -> Unit = {},
    onNavigateBookmarks: () -> Unit = {},
    onNavigateChallenge: () -> Unit = {},
    onNavigateTab: (BottomTab) -> Unit = {},
    onNavigateAdmin: () -> Unit = {},
    onOpenPremiumDialog: () -> Unit = {},
    liveChatPrice: Int = 50,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Banner with Profile & KCET GenZ Logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = userProfile.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = userProfile.name,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = userProfile.email,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Drawer",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                                ) {
                                    Text(
                                        text = "🎯 ${userProfile.kcetTargetRank}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (userProfile.isPremium) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF08A),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCA8A04))
                                    ) {
                                        Text(
                                            text = "👑 Premium Student",
                                            color = Color(0xFF854D0E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "QUICK NAVIGATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    // 0. ⚔️ 1 vs 1 Challenge System (EVERY Student Account)
                    DrawerMenuItem(
                        icon = Icons.Default.Star,
                        label = "⚔️ 1 vs 1 Challenge",
                        badgeText = "20-Q PCM",
                        badgeBg = Color(0xFF6366F1),
                        iconTint = Color(0xFF6366F1),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateChallenge()
                        }
                    )

                    // 1. Notification Dashboard (Primary Highlight)
                    DrawerMenuItem(
                        icon = Icons.Default.Notifications,
                        label = "Notifications Dashboard",
                        badgeText = if (unreadNotificationCount > 0) "$unreadNotificationCount New" else null,
                        badgeBg = Color(0xFFE11D48),
                        iconTint = Color(0xFF0284C7),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateNotifications()
                        }
                    )

                    // 2. 1-on-1 Personal Senior Chat
                    DrawerMenuItem(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        label = "1-on-1 Live Senior Chat",
                        badgeText = "₹${liveChatPrice} Slot",
                        badgeBg = Color(0xFFE11D48),
                        iconTint = Color(0xFFE11D48),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateSeniorChat()
                        }
                    )

                    // 2.1 🏛️ College Recommendation (Premium)
                    DrawerMenuItem(
                        icon = Icons.Default.Star,
                        label = "🏛️ College Recommendation",
                        badgeText = if (userProfile.isPremium) "AI SMART" else "PRO 🔒",
                        badgeBg = if (userProfile.isPremium) Color(0xFF0D9488) else Color(0xFFD97706),
                        iconTint = if (userProfile.isPremium) Color(0xFF0D9488) else Color(0xFFD97706),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateAiRecommendation()
                        }
                    )

                    // 2.2 🌿 Branch Explorer (Premium)
                    DrawerMenuItem(
                        icon = Icons.Default.School,
                        label = "🌿 Branch Explorer",
                        badgeText = if (userProfile.isPremium) "CUTOFFS" else "PRO 🔒",
                        badgeBg = if (userProfile.isPremium) Color(0xFF16A34A) else Color(0xFFD97706),
                        iconTint = if (userProfile.isPremium) Color(0xFF16A34A) else Color(0xFFD97706),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateBranchExplorer()
                        }
                    )

                    // 3. Saved Question Bookmarks
                    DrawerMenuItem(
                        icon = Icons.Default.Bookmark,
                        label = "Saved Question Bookmarks",
                        iconTint = Color(0xFFD97706),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateBookmarks()
                        }
                    )

                    // 4. Counselling & Cutoff Guide
                    DrawerMenuItem(
                        icon = Icons.Default.School,
                        label = "KCET Counselling Guide",
                        iconTint = Color(0xFF059669),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateTab(BottomTab.COUNSELLING)
                        }
                    )

                    // 5. Rank vs Marks Dashboard
                    DrawerMenuItem(
                        icon = Icons.Default.BarChart,
                        label = "Rank vs Marks Analysis",
                        iconTint = Color(0xFF7C3AED),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateTab(BottomTab.HOME)
                        }
                    )

                    // 6. Free PYQs & Study Material
                    DrawerMenuItem(
                        icon = Icons.Default.Download,
                        label = "Free PYQs & PDF Resources",
                        iconTint = Color(0xFF2563EB),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateTab(BottomTab.RESOURCES)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    Text(
                        text = "ACCOUNT & CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    // 7. Upgrade to Premium Pass
                    if (!userProfile.isPremium) {
                        DrawerMenuItem(
                            icon = Icons.Default.Star,
                            label = "Upgrade to Premium Pass",
                            badgeText = "OFFER",
                            badgeBg = Color(0xFFCA8A04),
                            iconTint = Color(0xFFD97706),
                            onClick = {
                                scope.launch { drawerState.close() }
                                onOpenPremiumDialog()
                            }
                        )
                    }

                    // Admin 1v1 Challenge Control Sidebar Item
                    val isAdminAccount = userProfile.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) || userProfile.uid == "admin_ullas"
                    if (isAdminAccount) {
                        DrawerMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "⚔️ 1 vs 1 Challenges (Admin)",
                            badgeText = "CONTROL",
                            badgeBg = Color(0xFF4F46E5),
                            iconTint = Color(0xFFEC4899),
                            onClick = {
                                scope.launch { drawerState.close() }
                                onNavigateAdmin()
                            }
                        )
                    }

                    // 8. Dark Mode Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = if (isDarkMode) "Dark Theme Enabled" else "Light Theme Enabled",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0284C7)
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    Text(
                        text = "SUPPORT & LEGAL INFORMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    // About App
                    DrawerMenuItem(
                        icon = Icons.Default.Info,
                        label = "About KCET Gen Z",
                        iconTint = Color(0xFF8B5CF6),
                        onClick = {
                            scope.launch { drawerState.close() }
                            showAboutDialog = true
                        }
                    )

                    // Contact Support
                    DrawerMenuItem(
                        icon = Icons.Default.SupportAgent,
                        label = "Need Help? Contact Support",
                        iconTint = Color(0xFF0284C7),
                        onClick = {
                            scope.launch { drawerState.close() }
                            showSupportDialog = true
                        }
                    )

                    // Terms & Conditions
                    DrawerMenuItem(
                        icon = Icons.Default.Gavel,
                        label = "Terms & Conditions",
                        iconTint = Color(0xFF6366F1),
                        onClick = {
                            scope.launch { drawerState.close() }
                            showTermsDialog = true
                        }
                    )

                    // Privacy Policy
                    DrawerMenuItem(
                        icon = Icons.Default.PrivacyTip,
                        label = "Privacy Policy",
                        iconTint = Color(0xFF10B981),
                        onClick = {
                            scope.launch { drawerState.close() }
                            showPrivacyDialog = true
                        }
                    )

                    // Disclaimer
                    DrawerMenuItem(
                        icon = Icons.Default.Security,
                        label = "KEA Official Disclaimer",
                        iconTint = Color(0xFFD97706),
                        onClick = {
                            scope.launch { drawerState.close() }
                            showDisclaimerDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // App Version & Copyright Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© 2026 KCET Gen Z. All Rights Reserved.",
                            fontSize = 10.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Developed & Maintained by Gen Z EduTech.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 1.0.0",
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showAboutDialog = true }
                        )
                    }
                }
            }

            // Dialogs
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
        },
        content = content
    )
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    badgeText: String? = null,
    badgeBg: Color = Color(0xFF0284C7),
    iconTint: Color = Color(0xFF0284C7),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A),
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }

        if (badgeText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
