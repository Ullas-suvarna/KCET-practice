package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.StudentNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDashboardScreen(
    notifications: List<StudentNotification> = emptyList(),
    onMarkAsRead: (String) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onDeleteNotification: (String) -> Unit = {},
    onNavigateAction: (String) -> Unit = {},
    onSendTestPushNotification: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var notificationToDeleteId by remember { mutableStateOf<String?>(null) }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "BOOKING" -> notifications.filter { it.type == "BOOKING" }
            "ANNOUNCEMENT" -> notifications.filter { it.type == "ANNOUNCEMENT" }
            "PAYMENT" -> notifications.filter { it.type == "PAYMENT" }
            "SYSTEM" -> notifications.filter { it.type == "SYSTEM" }
            else -> notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notification Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE11D48)
                            ) {
                                Text(
                                    text = "$unreadCount New",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = onMarkAllAsRead) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = Color(0xFF0284C7)
                            )
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearAllConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Daily Push Notification Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Daily Alerts",
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Mobile Push Alerts Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "You will receive daily notifications when new study materials, formulas & mock tests are uploaded.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E40AF),
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSendTestPushNotification,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Test Notification", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Filter Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "🌟 All (${notifications.size})",
                    "BOOKING" to "💬 1-on-1 Sessions",
                    "PAYMENT" to "💳 Payments",
                    "ANNOUNCEMENT" to "📢 Announcements",
                    "SYSTEM" to "⚙️ System"
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF334155)
                        )
                    )
                }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Notifications Here",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All your 1-on-1 meeting updates, admin payment verifications, and KCET announcements will appear right here.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        val cardBg = if (!notif.isRead) Color(0xFFF0F9FF) else Color.White
                        val borderColor = if (!notif.isRead) Color(0xFFBAE6FD) else Color(0xFFE2E8F0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkAsRead(notif.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (!notif.isRead) 2.dp else 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Category Badge Icon
                                        val (icon, iconBg, iconColor) = when (notif.type) {
                                            "BOOKING" -> Triple(Icons.Default.Person, Color(0xFFFEF3C7), Color(0xFFD97706))
                                            "PAYMENT" -> Triple(Icons.Default.Payment, Color(0xFFDCFCE7), Color(0xFF16A34A))
                                            "ANNOUNCEMENT" -> Triple(Icons.Default.School, Color(0xFFE0F2FE), Color(0xFF0284C7))
                                            else -> Triple(Icons.Default.Info, Color(0xFFF1F5F9), Color(0xFF64748B))
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = iconBg,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = iconColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = notif.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0284C7))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = notif.message,
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.timestamp,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (!notif.actionType.isNullOrBlank()) {
                                        Button(
                                            onClick = {
                                                onMarkAsRead(notif.id)
                                                onNavigateAction(notif.actionType ?: "")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (notif.actionType) {
                                                    "NAV_SENIOR_CHAT" -> "View Slot ➔"
                                                    "NAV_PRACTICE" -> "Practice Now ➔"
                                                    "NAV_COUNSELLING" -> "Open Guide ➔"
                                                    else -> "View ➔"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = { Text("Clear All Notifications", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all notifications? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirmDialog = false
                        onClearAll()
                        Toast.makeText(context, "All notifications cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (notificationToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { notificationToDeleteId = null },
            title = { Text("Delete Notification", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this notification? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = notificationToDeleteId!!
                        notificationToDeleteId = null
                        onDeleteNotification(id)
                        Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { notificationToDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun String?.isNull_Blank(): Boolean = this.isNullOrBlank()
