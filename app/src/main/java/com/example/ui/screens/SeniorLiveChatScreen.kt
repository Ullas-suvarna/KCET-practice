package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SeniorChatBooking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorLiveChatScreen(
    bookings: List<SeniorChatBooking> = emptyList(),
    onBookSession: (date: String, time: String) -> Unit,
    onRescheduleSession: (bookingId: String, date: String, time: String) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Calculate real-time available dates starting from tomorrow with 1-day gaps as requested
    val availableDates = remember {
        val dateList = mutableListOf<String>()
        val cal = Calendar.getInstance()

        // Starting from tomorrow (e.g., if today is 31/07/2026 -> 01/08/2026, 02/08/2026, 03/08/2026...)
        cal.add(Calendar.DAY_OF_YEAR, 1)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

        for (i in 0..4) {
            val formatted = dateFormat.format(cal.time)
            val dayLabel = dayNameFormat.format(cal.time)
            dateList.add(formatted)
            cal.add(Calendar.DAY_OF_YEAR, 1) // 1 day gap between slots
        }
        dateList
    }

    val availableTimes = listOf(
        "10:00 AM - 11:00 AM",
        "02:00 PM - 03:00 PM",
        "05:00 PM - 06:00 PM",
        "08:00 PM - 09:00 PM",
        "09:30 PM - 10:30 PM"
    )

    var selectedDate by remember { mutableStateOf(availableDates.firstOrNull() ?: "01/08/2026") }
    var selectedTime by remember { mutableStateOf(availableTimes[2]) } // Default 05:00 PM - 06:00 PM
    var customNotes by remember { mutableStateOf("") }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("UPI / QR Code") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Active confirmed booking view
    val latestBooking = bookings.lastOrNull()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SENIOR MENTORSHIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "1-on-1 Personal Live Chat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF1F2)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFER ₹50",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE11D48)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Top Banner: Senior Advice & Unlimited Doubt Clearing
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
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "KCET Senior Mentor",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF08A)
                                ) {
                                    Text(
                                        text = "50% OFF",
                                        color = Color(0xFF854D0E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Personal Live Chat with Top KCET Seniors 💬",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Take personal advice on KCET preparation, college option entry, strategy, and get UNLIMITED doubt clearing directly on Instagram!",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pricing Tag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "₹100",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textDecoration = TextDecoration.LineThrough,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "₹50 Only",
                                    fontSize = 24.sp,
                                    color = Color(0xFFFEF08A),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "(Limited Time Offer)",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // If user has bookings, show Booking Pass Card according to Status
                if (latestBooking != null) {
                    item {
                        val isConfirmed = latestBooking.status == "Confirmed"
                        val isWaiting = latestBooking.status == "Waiting for Senior Confirmation" || latestBooking.status == "Pending"
                        val isReschedule = latestBooking.status == "Reschedule Needed" || latestBooking.status == "Choose Other Time"
                        val isDeclined = latestBooking.status == "Declined"

                        val cardBg = when {
                            isConfirmed -> Color(0xFFF0FDF4)
                            isWaiting -> Color(0xFFFEFCE8)
                            isReschedule -> Color(0xFFFFF7ED)
                            else -> Color(0xFFFEF2F2)
                        }

                        val headerColor = when {
                            isConfirmed -> Color(0xFF16A34A)
                            isWaiting -> Color(0xFFD97706)
                            isReschedule -> Color(0xFFEA580C)
                            else -> Color(0xFFDC2626)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, headerColor.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when {
                                                isConfirmed -> Icons.Default.CheckCircle
                                                isWaiting -> Icons.Default.Schedule
                                                isReschedule -> Icons.Default.HelpOutline
                                                else -> Icons.Default.Lock
                                            },
                                            contentDescription = null,
                                            tint = headerColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when {
                                                isConfirmed -> "Booking Confirmed ✅"
                                                isWaiting -> "Waiting for Senior Confirmation ⏳"
                                                isReschedule -> "Senior Suggested Reschedule 💬"
                                                else -> "Booking Declined ❌"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = headerColor
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White
                                    ) {
                                        Text(
                                            text = "ID: ${latestBooking.bookingId}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = headerColor.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Selected Date", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = latestBooking.selectedDate,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Selected Time", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = latestBooking.selectedTime,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }

                                // Status Message details
                                Spacer(modifier = Modifier.height(12.dp))

                                if (isWaiting) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "⏳ Request Submitted to Senior Mentor",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Your date (${latestBooking.selectedDate}) and time (${latestBooking.selectedTime}) request is sent to Senior Mentor (@ULLAS_U_SUVARNA). The mentor is reviewing availability and will confirm or send a message shortly!",
                                                fontSize = 12.sp,
                                                color = Color(0xFF78350F),
                                                lineHeight = 17.sp
                                            )
                                            if (latestBooking.adminMessage.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "💬 Senior Note: ${latestBooking.adminMessage}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        }
                                    }
                                } else if (isReschedule) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFEDD5))
                                            .border(1.dp, Color(0xFFFDBA74), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "💬 Message from Senior Mentor:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFC2410C)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = latestBooking.adminMessage.ifBlank { "Senior mentor is unavailable at this slot. Please choose another date or time slot below." },
                                                fontSize = 12.sp,
                                                color = Color(0xFF9A3412),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 17.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "👉 Please select a new date & time slot below to re-submit your request.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC2410C)
                                            )
                                        }
                                    }
                                } else if (isDeclined) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "❌ Booking Request Not Confirmed",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFB91C1C)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = latestBooking.adminMessage.ifBlank { "Senior mentor is unavailable for this slot. Please pick another slot below." },
                                                fontSize = 12.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }
                                }

                                if (isConfirmed) {
                                    // QR Code Display from User's uploaded screenshot
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Instagram Mentor QR Code",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Image(
                                                painter = painterResource(id = R.drawable.img_insta_qr_1785438753192),
                                                contentDescription = "Instagram QR Code @ULLAS_U_SUVARNA",
                                                modifier = Modifier
                                                    .size(210.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Fit
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "@ULLAS_U_SUVARNA",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp,
                                                color = Color(0xFFD97706)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Instruction Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "📌 Next Steps for Live Chat:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Follow Instagram handle @ULLAS_U_SUVARNA and send a direct message (DM) at your selected time (${latestBooking.selectedTime} on ${latestBooking.selectedDate}) to start your unlimited doubt clearing live chat session!",
                                                fontSize = 12.sp,
                                                color = Color(0xFF78350F),
                                                lineHeight = 17.sp
                                            )
                                            if (latestBooking.adminMessage.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "💬 Senior Note: ${latestBooking.adminMessage}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Support Contact Info Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Need Help or Have Questions?",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF)
                                            )
                                            Text(
                                                text = "Contact support via email: KcetGenZservice@gmail.com | Insta: @ULLAS U SUVARNA",
                                                fontSize = 11.sp,
                                                color = Color(0xFF1E3A8A)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://instagram.com/ULLAS_U_SUVARNA")
                                            )
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Open Instagram", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Instagram Handle", "@ULLAS_U_SUVARNA")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied @ULLAS_U_SUVARNA to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Handle", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Booking Form Title
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (latestBooking != null) "Book Another Slot" else "Select Date & Time Slot",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Step 1: Real-Time Date Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "1. Choose Available Date (1-Day Gap Real-Time Slots)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(availableDates) { dateStr ->
                                    val isSelected = selectedDate == dateStr

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFE11D48) else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) Color(0xFFBE123C) else Color(0xFFCBD5E1)
                                        ),
                                        modifier = Modifier
                                            .clickable { selectedDate = dateStr }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = dateStr,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Available",
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF16A34A),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 2: Time Slot Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "2. Select Time Slot",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                availableTimes.forEach { timeStr ->
                                    val isSelected = selectedTime == timeStr

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedTime = timeStr },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) Color(0xFFE11D48) else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color(0xFFE11D48) else Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = timeStr,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color(0xFF9F1239) else Color(0xFF334155)
                                                )
                                            }

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedTime = timeStr }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Mentorship Benefits & Features
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ What You Get in Live Chat Session:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val benefits = listOf(
                                "🔥 Unlimited Doubt Clearing (Physics, Chem, Math, Biology)",
                                "🎯 Personalized KCET Strategy & Chapter Weightage Guide",
                                "🎓 KEA Counselling Option Entry Order & Cutoff Advice",
                                "💬 Direct 1-on-1 Instagram DM Access with Senior Mentor",
                                "⚡ Fast response at your selected time slot"
                            )

                            benefits.forEach { b ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = b,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Booking Button & Price Banner
                item {
                    val hasExistingBooking = latestBooking != null
                    if (hasExistingBooking && latestBooking != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Pass Status",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "PAID ₹50",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF4ADE80)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF166534)
                                            ) {
                                                Text(
                                                    text = "NO EXTRA FEE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onRescheduleSession(latestBooking.bookingId, selectedDate, selectedTime)
                                            Toast.makeText(context, "Slot updated to $selectedDate ($selectedTime) free of charge! Firebase updated.", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "🔄 Update Slot Free",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "💡 Admin timing declined or changed? Pick a new slot above and click 'Update Slot Free'. You will NOT be charged money again! Data is stored permanently in Firebase.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Session Fee",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹100",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.5f),
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "₹50",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFEF08A)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Book Session (₹50)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) showPaymentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFFE11D48))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Booking (₹50)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Booking Live Chat Session for $selectedDate at $selectedTime",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Regular Fee:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("₹100", fontSize = 12.sp, textDecoration = TextDecoration.LineThrough, color = Color(0xFF64748B))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Special Offer Discount:", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Text("-₹50", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹50", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE11D48))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val methods = listOf("UPI / Google Pay / PhonePe / Paytm", "Credit / Debit Card", "Net Banking")
                    methods.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = m }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = paymentMethod == m, onClick = { paymentMethod = m })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(m, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isProcessingPayment,
                    onClick = {
                        isProcessingPayment = true
                        scope.launch {
                            delay(1200) // Simulate fast secure payment
                            isProcessingPayment = false
                            showPaymentDialog = false
                            onBookSession(selectedDate, selectedTime)
                            Toast.makeText(context, "Payment Successful! Session booked for $selectedDate.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Pay ₹50 & Confirm")
                    }
                }
            },
            dismissButton = {
                if (!isProcessingPayment) {
                    TextButton(onClick = { showPaymentDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
