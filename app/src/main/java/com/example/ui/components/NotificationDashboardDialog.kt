package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Announcement
import com.example.data.ImportantDateItem
import com.example.data.PaymentReceipt
import com.example.data.SeniorChatBooking

data class StudentNotification(
    val id: String,
    val category: String, // "CHAT", "PAYMENT", "ANNOUNCEMENT"
    val title: String,
    val message: String,
    val timestampFormatted: String,
    val statusTag: String, // "CONFIRMED", "WAITING", "VERIFYING", "RESCHEDULE", "DECLINED", "INFO"
    val adminNote: String = "",
    val bookingId: String = ""
)

@Composable
fun NotificationDashboardDialog(
    seniorBookings: List<SeniorChatBooking> = emptyList(),
    paymentReceipt: PaymentReceipt? = null,
    announcements: List<Announcement> = emptyList(),
    importantDates: List<ImportantDateItem> = emptyList(),
    onOpenSeniorChat: () -> Unit = {},
    onOpenProPass: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "CHAT", "PAYMENT", "ANNOUNCEMENTS"

    // Consolidate real notifications list from senior bookings, payment receipts & announcements
    val allNotifications = remember(seniorBookings, paymentReceipt, announcements, importantDates) {
        val list = mutableListOf<StudentNotification>()

        // 1. Senior Chat Bookings Notifications
        seniorBookings.forEach { booking ->
            val isUnderVerification = booking.status == "Under Verification" || booking.status == "Payment Under Verification"
            val isConfirmed = booking.status == "Confirmed"
            val isWaiting = booking.status == "Waiting for Senior Confirmation" || booking.status == "Pending"
            val isReschedule = booking.status == "Reschedule Needed" || booking.status == "Choose Other Time"
            val isDeclined = booking.status == "Declined" || booking.status == "Rejected"

            val tag = when {
                isConfirmed -> "CONFIRMED"
                isWaiting -> "WAITING"
                isUnderVerification -> "VERIFYING"
                isReschedule -> "RESCHEDULE"
                isDeclined -> "DECLINED"
                else -> "INFO"
            }

            val title = when {
                isConfirmed -> "✅ Senior Chat Session Confirmed!"
                isWaiting -> "⏳ Payment Verified - Awaiting Senior Confirmation"
                isUnderVerification -> "⏳ Senior Chat Payment Under Verification"
                isReschedule -> "💬 Senior Suggested Reschedule"
                isDeclined -> "❌ Senior Chat Booking Declined"
                else -> "💬 Senior Chat Update"
            }

            val message = when {
                isConfirmed -> "Your 1-on-1 session for ${booking.selectedDate} at ${booking.selectedTime} is confirmed! Open chat to connect with your assigned senior mentor."
                isWaiting -> "Payment ₹50 was verified by Admin! Waiting for senior mentor confirmation for slot ${booking.selectedDate} at ${booking.selectedTime}."
                isUnderVerification -> "Payment receipt / UTR submitted for slot ${booking.selectedDate} at ${booking.selectedTime}. Admin is verifying your payment."
                isReschedule -> "Senior mentor requested a time change for session on ${booking.selectedDate}. Please re-pick a date and time slot."
                isDeclined -> "Your 1-on-1 session booking for ${booking.selectedDate} at ${booking.selectedTime} was declined."
                else -> "Status: ${booking.status}"
            }

            val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
            val dateFormatted = try { sdf.format(java.util.Date(booking.bookedAtTimestamp)) } catch (e: Exception) { "Recently" }

            list.add(
                StudentNotification(
                    id = booking.bookingId,
                    category = "CHAT",
                    title = title,
                    message = message,
                    timestampFormatted = dateFormatted,
                    statusTag = tag,
                    adminNote = booking.adminMessage,
                    bookingId = booking.bookingId
                )
            )
        }

        // 2. Pro Membership Payment Receipt Notifications
        if (paymentReceipt != null) {
            val status = paymentReceipt.status
            val isVerified = status == "VERIFIED"
            val isRejected = status == "REJECTED"
            val isVerifying = status == "UNDER_VERIFICATION"

            val title = when {
                isVerified -> "⭐ Pro Pass Verified & Unlocked!"
                isRejected -> "❌ Pro Pass Payment Receipt Rejected"
                else -> "⏳ Pro Pass Payment Under Review"
            }

            val message = when {
                isVerified -> "Your payment of ₹${paymentReceipt.amountPaid} has been verified by Admin! Lifetime access to all mock tests & PYQs is unlocked."
                isRejected -> "Your payment receipt submission (Ref: ${paymentReceipt.transactionRef}) was rejected."
                else -> "Your Pro Pass receipt (Ref: ${paymentReceipt.transactionRef}) is currently under Admin review."
            }

            val tag = when {
                isVerified -> "CONFIRMED"
                isRejected -> "DECLINED"
                else -> "VERIFYING"
            }

            list.add(
                StudentNotification(
                    id = paymentReceipt.receiptId.ifBlank { "PR_${paymentReceipt.submittedAtTimestamp}" },
                    category = "PAYMENT",
                    title = title,
                    message = message,
                    timestampFormatted = paymentReceipt.submittedAtFormatted,
                    statusTag = tag,
                    adminNote = paymentReceipt.rejectionReason
                )
            )
        }

        // 3. System Announcements & Important Dates
        announcements.forEach { ann ->
            list.add(
                StudentNotification(
                    id = "ANN_${ann.id}",
                    category = "ANNOUNCEMENT",
                    title = "📢 " + ann.title,
                    message = ann.message,
                    timestampFormatted = ann.date,
                    statusTag = "INFO"
                )
            )
        }

        importantDates.filter { it.isHighlighted }.forEach { imp ->
            list.add(
                StudentNotification(
                    id = "IMP_${imp.id}",
                    category = "ANNOUNCEMENT",
                    title = "📅 " + imp.eventTitle,
                    message = "${imp.description} (Deadline/Date: ${imp.dateOrDeadline})",
                    timestampFormatted = imp.status,
                    statusTag = "INFO"
                )
            )
        }

        // Welcome notification only show 1st time of opening app and then no need to show
        val prefs = context.getSharedPreferences("kcet_prefs", android.content.Context.MODE_PRIVATE)
        val hasShownWelcome = prefs.getBoolean("has_shown_welcome_notification", false)
        if (list.isEmpty() && !hasShownWelcome) {
            list.add(
                StudentNotification(
                    id = "WELCOME_01",
                    category = "ANNOUNCEMENT",
                    title = "🎉 Welcome to KCET GenZ Mentor!",
                    message = "All notifications for your 1-on-1 Senior Live Chat bookings, payment receipts, and exam alerts will appear here in real-time.",
                    timestampFormatted = "Today",
                    statusTag = "INFO"
                )
            )
            prefs.edit().putBoolean("has_shown_welcome_notification", true).apply()
        }

        list
    }

    val filteredNotifications = remember(allNotifications, selectedFilter) {
        when (selectedFilter) {
            "CHAT" -> allNotifications.filter { it.category == "CHAT" }
            "PAYMENT" -> allNotifications.filter { it.category == "PAYMENT" }
            "ANNOUNCEMENTS" -> allNotifications.filter { it.category == "ANNOUNCEMENT" }
            else -> allNotifications
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Notification Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${allNotifications.size} Total Alerts • Real-time Updates",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Filter Category Chips
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == "ALL",
                                onClick = { selectedFilter = "ALL" },
                                label = { Text("All (${allNotifications.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4F46E5),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val chatCount = allNotifications.count { it.category == "CHAT" }
                            FilterChip(
                                selected = selectedFilter == "CHAT",
                                onClick = { selectedFilter = "CHAT" },
                                label = { Text("Senior Chat 💬 ($chatCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE11D48),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val payCount = allNotifications.count { it.category == "PAYMENT" }
                            FilterChip(
                                selected = selectedFilter == "PAYMENT",
                                onClick = { selectedFilter = "PAYMENT" },
                                label = { Text("Payments 💳 ($payCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF059669),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            val annCount = allNotifications.count { it.category == "ANNOUNCEMENT" }
                            FilterChip(
                                selected = selectedFilter == "ANNOUNCEMENTS",
                                onClick = { selectedFilter = "ANNOUNCEMENTS" },
                                label = { Text("Exam Alerts 📢 ($annCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD97706),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Divider(color = Color(0xFFE2E8F0))

                // Notifications List
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No notifications found in this category.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredNotifications) { item ->
                            val borderAndHeaderColor = when (item.statusTag) {
                                "CONFIRMED" -> Color(0xFF16A34A)
                                "WAITING" -> Color(0xFF0284C7)
                                "VERIFYING" -> Color(0xFFD97706)
                                "RESCHEDULE" -> Color(0xFFEA580C)
                                "DECLINED" -> Color(0xFFDC2626)
                                else -> Color(0xFF4F46E5)
                            }

                            val cardBgColor = when (item.statusTag) {
                                "CONFIRMED" -> Color(0xFFF0FDF4)
                                "WAITING" -> Color(0xFFF0F9FF)
                                "VERIFYING" -> Color(0xFFFEFCE8)
                                "RESCHEDULE" -> Color(0xFFFFF7ED)
                                "DECLINED" -> Color(0xFFFEF2F2)
                                else -> Color.White
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderAndHeaderColor.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = borderAndHeaderColor,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = borderAndHeaderColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = item.timestampFormatted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = borderAndHeaderColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = item.message,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 17.sp
                                    )

                                    if (item.adminNote.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(borderAndHeaderColor.copy(alpha = 0.1f))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "💬 Note: ${item.adminNote}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = borderAndHeaderColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Button based on category
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (item.category == "CHAT") {
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onOpenSeniorChat()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = borderAndHeaderColor),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (item.statusTag == "CONFIRMED") "Open Senior Chat ➔" else "Manage Booking",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        } else if (item.category == "PAYMENT") {
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onOpenProPass()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = borderAndHeaderColor),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View Pro Pass", fontSize = 11.sp)
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
    }
}
