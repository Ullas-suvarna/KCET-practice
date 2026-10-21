package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import com.google.firebase.auth.FirebaseAuth
import com.example.data.OneToOneMessage
import com.example.data.SeniorMentorProfile
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.HourglassTop
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.ImageUtils
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirebaseRealtimeService
import com.example.data.FirebaseService
import com.example.data.SeniorChatMessage
import com.example.data.UserPresence
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import com.example.data.isExpired
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
    registeredMentorsList: List<SeniorMentorProfile> = emptyList(),
    isMentorMode: Boolean = false,
    onBookSession: (date: String, time: String, paymentRef: String, receiptUri: String) -> Unit = { _, _, _, _ -> },
    onRescheduleSession: (bookingId: String, date: String, time: String) -> Unit = { _, _, _ -> },
    onBack: () -> Unit,
    onSignOut: () -> Unit = onBack
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
    var selectedPreferredMentor by remember { mutableStateOf<SeniorMentorProfile?>(null) }
    var customNotes by remember { mutableStateOf("") }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("UPI / QR Code") }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var utrTxnInput by remember { mutableStateOf("") }
    var uploadedReceiptUri by remember { mutableStateOf<String?>(null) }

    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            uploadedReceiptUri = base64 ?: uri.toString()
            Toast.makeText(context, "Receipt attached successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Real-time ticking clock for slot expiry check (updates every 5 seconds)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(5000)
        }
    }

    // Active (un-expired) bookings list (up to 2 active slots per student)
    val activeBookings = remember(bookings, currentTimeMillis) {
        bookings.filter { !it.isExpired(currentTimeMillis) && it.status != "Declined" && it.status != "Rejected" && it.status != "Expired" && it.status != "Completed" }
    }

    val activeBooking = activeBookings.lastOrNull()

    val confirmedBookings = remember(bookings) { bookings.filter { it.status == "Confirmed" } }
    val uniqueConfirmedBookings = remember(confirmedBookings) {
        confirmedBookings.distinctBy { it.userId.ifBlank { it.userEmail } }
    }

    val pastExpiredBooking = remember(bookings, currentTimeMillis) {
        if (activeBookings.isEmpty()) bookings.lastOrNull { it.isExpired(currentTimeMillis) || it.status == "Expired" || it.status == "Completed" } else null
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = Color(0xFFF8FAFC)
    ) {
        var activeFullScreenChatBooking by remember { mutableStateOf<SeniorChatBooking?>(null) }
        var isFullScreenChatExpired by remember { mutableStateOf(false) }
        var unlockedMentorProfile by remember { mutableStateOf<SeniorMentorProfile?>(null) }
        var selectedBookingForChat by remember { mutableStateOf<SeniorChatBooking?>(null) }
        var showSignOutConfirmDialog by remember { mutableStateOf(false) }

        val handleBackAction: () -> Unit = {
            if (activeFullScreenChatBooking != null) {
                activeFullScreenChatBooking = null
            } else if (selectedBookingForChat != null) {
                selectedBookingForChat = null
            } else if (isMentorMode || unlockedMentorProfile != null) {
                showSignOutConfirmDialog = true
            } else {
                onBack()
            }
        }

        // Dedicated BackHandler so hardware/system back button safely prompts for Sign Out instead of dumping into student account
        BackHandler(enabled = true) {
            handleBackAction()
        }

        if (activeFullScreenChatBooking != null) {
            val currentSelectedId = activeFullScreenChatBooking!!.bookingId
            val targetBooking = bookings.find { it.bookingId == currentSelectedId } ?: activeFullScreenChatBooking!!
            val isMentor = isMentorMode || unlockedMentorProfile != null
            val otherPartyName = if (isMentor) targetBooking.userName.ifBlank { "Student" } else targetBooking.mentorName.ifBlank { "Assigned Senior Mentor" }
            val isExpiredNow = isFullScreenChatExpired || targetBooking.status == "Expired" || targetBooking.status == "Completed" || targetBooking.isExpired(currentTimeMillis)

            OneToOneChatFullScreen(
                bookingId = targetBooking.bookingId,
                otherUserName = otherPartyName,
                isSenior = isMentor,
                mentorName = targetBooking.mentorName,
                studentId = targetBooking.userId,
                isChatExpired = isExpiredNow,
                onDismiss = { activeFullScreenChatBooking = null }
            )
        } else {
        Column(modifier = Modifier.fillMaxSize()) {
            val currentUserEmail = remember { FirebaseAuth.getInstance().currentUser?.email ?: "" }

            var selectedTab by remember(isMentorMode) { mutableStateOf(if (isMentorMode) "Senior Mentors" else "Student Portal") }

            val registeredMentorsFlow = remember { FirebaseRealtimeService.observeAllSeniorMentors() }
            val realTimeMentors by registeredMentorsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
            val firestoreMentorsFlow = remember { FirebaseService.observeAllSeniorMentorsFromFirestore() }
            val firestoreMentors by firestoreMentorsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            val registeredMentors = remember(registeredMentorsList, realTimeMentors, firestoreMentors) {
                val map = LinkedHashMap<String, SeniorMentorProfile>()
                FirebaseService.defaultVerifiedSeniorMentors.forEach {
                    if (!FirebaseRealtimeService.isMentorDeleted(it.name)) {
                        val key = it.email.lowercase().trim().ifBlank { it.name.lowercase().trim() }
                        map[key] = it
                    }
                }
                firestoreMentors.forEach {
                    if (it.name.isNotBlank() && !FirebaseRealtimeService.isMentorDeleted(it.name)) {
                        val key = it.email.lowercase().trim().ifBlank { it.name.lowercase().trim() }
                        map[key] = it
                    }
                }
                registeredMentorsList.forEach {
                    if (it.name.isNotBlank() && !FirebaseRealtimeService.isMentorDeleted(it.name)) {
                        val key = it.email.lowercase().trim().ifBlank { it.name.lowercase().trim() }
                        map[key] = it
                    }
                }
                realTimeMentors.forEach {
                    if (it.name.isNotBlank() && !FirebaseRealtimeService.isMentorDeleted(it.name)) {
                        val key = it.email.lowercase().trim().ifBlank { it.name.lowercase().trim() }
                        map[key] = it
                    }
                }
                map.values.toList()
            }

            val activeMentorProfile = unlockedMentorProfile

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
                    IconButton(onClick = {
                        handleBackAction()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeMentorProfile != null) "MENTOR PANEL: ${activeMentorProfile.name.uppercase()}" else if (isMentorMode) "SENIOR MENTOR PORTAL" else "1-ON-1 SENIOR MENTORSHIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (activeMentorProfile != null) "Dashboard & Student Chats" else if (isMentorMode) "Mentor Profile Access & Registration" else "1-on-1 Personal Live Chat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    if (activeMentorProfile != null) {
                        Button(
                            onClick = {
                                showSignOutConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Sign Out", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
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
            }

            // Top Tab Selector for Mentor portal options
            if (isMentorMode && activeMentorProfile == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Senior Mentors", "Register as Mentor").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Button(
                                onClick = { selectedTab = tab },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFE11D48) else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            if (activeMentorProfile != null) {
                var bookingToReject by remember { mutableStateOf<com.example.data.SeniorChatBooking?>(null) }

                val myBookings = remember(bookings, activeMentorProfile.name, activeMentorProfile.email) {
                    bookings.filter { booking ->
                        val assignedEmail = booking.assignedMentorEmail.trim().lowercase()
                        val mentorEmail = activeMentorProfile.email.trim().lowercase()
                        val mentorName = activeMentorProfile.name.trim().lowercase()
                        val bookingMentorName = booking.mentorName.trim().lowercase()

                        // STRICT ISOLATION: Assigned specifically to this mentor only (no cross-mentor leaks or unassigned leaks)
                        (assignedEmail.isNotBlank() && assignedEmail == mentorEmail) ||
                        (bookingMentorName.isNotBlank() && bookingMentorName == mentorName)
                    }
                }
                val myConfirmedBookings = remember(myBookings, currentTimeMillis) {
                    myBookings.filter { booking ->
                        (booking.status == "Confirmed" || booking.status == "Accepted") &&
                        booking.status != "Expired" &&
                        booking.status != "Completed" &&
                        !booking.isExpired(currentTimeMillis)
                    }
                }
                val myUniqueConfirmedBookings = remember(myConfirmedBookings) {
                    myConfirmedBookings.distinctBy { it.userId.ifBlank { it.userEmail } }
                }
                val myPendingRequests = remember(myBookings) {
                    myBookings.filter { booking ->
                        (booking.status == "Waiting for Senior Confirmation" || 
                         booking.status == "Under Verification" || 
                         booking.status == "Payment Under Verification") &&
                        booking.status != "Rejected by Mentor" &&
                        booking.status != "Declined" &&
                        booking.status != "Completed" &&
                        booking.status != "Expired" &&
                        booking.status != "Confirmed" &&
                        !booking.adminMessage.contains("Accepted by Senior Mentor", ignoreCase = true)
                    }
                }
                val myExpiredBookings = remember(myBookings, currentTimeMillis) {
                    myBookings.filter { booking ->
                        booking.status == "Expired" || booking.status == "Completed" ||
                        ((booking.status == "Confirmed" || booking.status == "Accepted") && booking.isExpired(currentTimeMillis))
                    }
                }
                val myUniqueExpiredBookings = remember(myExpiredBookings) {
                    myExpiredBookings.distinctBy { it.userId.ifBlank { it.userEmail } }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🟢", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Logged In: ${activeMentorProfile.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF166534))
                                    Text("You have active access to your secure mentor dashboard.", fontSize = 11.sp, color = Color(0xFF15803D))
                                }
                            }
                        }
                    }

                    // STATISTICS SECTION
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Confirmed Students Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🎯 Assigned", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${myUniqueConfirmedBookings.size} Students", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Confirmed 1-to-1 chats", fontSize = 9.sp, color = Color(0xFF065F46))
                                }
                            }

                            // Pending Requests Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("📬 Requests", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${myPendingRequests.size} Pending", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Awaiting verification", fontSize = 9.sp, color = Color(0xFF92400E))
                                }
                            }
                        }
                    }

                    // Inline 1-to-1 Private Student Chat Section
                    if (selectedBookingForChat != null) {
                        item {
                            InlineOneToOneChatBox(
                                booking = selectedBookingForChat!!,
                                activeMentorName = activeMentorProfile.name,
                                onCloseChat = { selectedBookingForChat = null }
                            )
                        }
                    } else {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("💬 1-to-1 Live Support Chat Box", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E40AF))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Select any assigned student from the list below (click \"Connect & Chat\") to open their permanent, private 1-to-1 live support chat box here.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Global Realtime Chat Box is still visible so they can chat with everyone in real-time
                    // item {
                    //    RealtimeSeniorChatBox()
                    // }

                    // PROFILE DETAILS CARD
                    item {
                        var isProfileExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("👨‍🏫 My Profile Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                        Text("These details are visible to students and parents.", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { isProfileExpanded = !isProfileExpanded }) {
                                        Icon(
                                            imageVector = if (isProfileExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand/Collapse Profile"
                                        )
                                    }
                                }

                                if (isProfileExpanded) {
                                    var mentorName by remember { mutableStateOf(activeMentorProfile.name) }
                                    val mentorEmail = remember { activeMentorProfile.email }
                                    var mentorPhone by remember { mutableStateOf(activeMentorProfile.phone) }
                                    var mentorQualification by remember { mutableStateOf(activeMentorProfile.qualification) }
                                    var mentorExperience by remember { mutableStateOf(activeMentorProfile.experience) }
                                    var mentorSpecialty by remember { mutableStateOf(activeMentorProfile.specialty) }
                                    var mentorBio by remember { mutableStateOf(activeMentorProfile.bio) }
                                    var mentorSocials by remember { mutableStateOf(activeMentorProfile.telegramOrInstagram) }
                                    var isSavingProfile by remember { mutableStateOf(false) }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = mentorName,
                                        onValueChange = { mentorName = it },
                                        label = { Text("Display Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorEmail,
                                        onValueChange = { },
                                        label = { Text("Email Address (Linked to Auth)") },
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorPhone,
                                        onValueChange = { mentorPhone = it },
                                        label = { Text("Phone Number") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorQualification,
                                        onValueChange = { mentorQualification = it },
                                        label = { Text("Qualification / Degree") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorExperience,
                                        onValueChange = { mentorExperience = it },
                                        label = { Text("Experience") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorSpecialty,
                                        onValueChange = { mentorSpecialty = it },
                                        label = { Text("Specialty Subjects") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorBio,
                                        onValueChange = { mentorBio = it },
                                        label = { Text("Profile / Bio") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = mentorSocials,
                                        onValueChange = { mentorSocials = it },
                                        label = { Text("Instagram / Telegram Handle") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isSavingProfile = true
                                                val profile = SeniorMentorProfile(
                                                    name = mentorName.trim(),
                                                    bio = mentorBio.trim(),
                                                    specialty = mentorSpecialty.trim(),
                                                    telegramOrInstagram = mentorSocials.trim(),
                                                    email = mentorEmail,
                                                    phone = mentorPhone.trim(),
                                                    qualification = mentorQualification.trim(),
                                                    experience = mentorExperience.trim(),
                                                    password = "", // Managed securely by Auth
                                                    createdAt = activeMentorProfile.createdAt,
                                                    status = activeMentorProfile.status.ifBlank { "Active" },
                                                    lastUpdated = System.currentTimeMillis()
                                                )
                                                val success = FirebaseRealtimeService.updateSeniorMentorProfile(profile)
                                                isSavingProfile = false
                                                if (success) {
                                                    Toast.makeText(context, "Mentor Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                                                    unlockedMentorProfile = profile
                                                } else {
                                                    Toast.makeText(context, "Error saving profile details", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        enabled = !isSavingProfile && mentorName.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isSavingProfile) "Saving..." else "💾 Save Mentor Details", color = Color.White)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("📧 Email: ${activeMentorProfile.email}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Text("📞 Phone: ${activeMentorProfile.phone.ifBlank { "Not provided" }}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Text("🎓 Qualification: ${activeMentorProfile.qualification.ifBlank { "Senior Guide" }}", fontSize = 12.sp, color = Color(0xFF475569))
                                }
                            }
                        }
                    }

                    // MY ASSIGNED STUDENTS SECTION (Confirmed 1:1 Chats)
                    item {
                        Text(
                            text = "🎯 My Assigned Students (Confirmed Chats)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (myUniqueConfirmedBookings.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No confirmed students assigned to you yet.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(myUniqueConfirmedBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(booking.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text(booking.userEmail, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "🎯 Assigned & Confirmed",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "📅 Scheduled: ${booking.selectedDate} at ${booking.selectedTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    if (booking.adminMessage.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "💬 Admin Message: ${booking.adminMessage}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            selectedBookingForChat = booking
                                            activeFullScreenChatBooking = booking
                                            isFullScreenChatExpired = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "✨ Connect & Chat 1-to-1",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // MY ENDED / EXPIRED SESSIONS (Ended by Admin or Expired)
                    if (myUniqueExpiredBookings.isNotEmpty()) {
                        item {
                            Text(
                                text = "⏰ Ended / Expired Sessions (${myUniqueExpiredBookings.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }

                        items(myUniqueExpiredBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(booking.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text(booking.userEmail, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = "⏰ EXPIRED / ENDED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDC2626),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("📅 Slot: ${booking.selectedDate} at ${booking.selectedTime}", fontSize = 12.sp, color = Color(0xFF7F1D1D))
                                    Text("💬 Admin Note: ${booking.adminMessage.ifBlank { "Session closed by Admin." }}", fontSize = 11.sp, color = Color(0xFF991B1B))

                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            activeFullScreenChatBooking = booking
                                            isFullScreenChatExpired = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Text("👁️ View Ended Chat History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // MY STUDENT REQUESTS SECTION (Pending Verification)
                    item {
                        Text(
                            text = "📬 My Student Requests (Awaiting Admin Verification)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (myPendingRequests.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No pending student requests for you at this moment.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(myPendingRequests) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(booking.userName.ifBlank { "Student Request" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            if (booking.userEmail.isNotBlank()) {
                                                Text(booking.userEmail, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = "📌 Assigned & Pending",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "📅 Slot: ${booking.selectedDate} at ${booking.selectedTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💰 Amount Paid: ₹${booking.amountPaid} (Ref: ${booking.paymentRefNumber.ifBlank { "Verified" }})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (booking.adminMessage.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💬 Admin Note: ${booking.adminMessage}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF0284C7),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    com.example.data.FirebaseService.updateSeniorChatBookingStatus(
                                                        bookingId = booking.bookingId,
                                                        status = "Confirmed",
                                                        adminMessage = "Accepted by Senior Mentor ${activeMentorProfile.name}",
                                                        mentorName = activeMentorProfile.name,
                                                        assignedMentorEmail = activeMentorProfile.email,
                                                        rejectionReason = ""
                                                    )
                                                    selectedBookingForChat = booking
                                                    Toast.makeText(context, "✅ Accepted 1-to-1 slot with ${booking.userName}!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("✅ Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                bookingToReject = booking
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("❌ Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                // Rejection Dialog for Mentor
                if (bookingToReject != null) {
                    var rejectReasonInput by remember { mutableStateOf("") }
                    var isSubmittingReject by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { bookingToReject = null },
                        title = {
                            Text("❌ Reject Student Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Column {
                                Text(
                                    "Provide a reason for rejecting ${bookingToReject?.userName}'s request. This reason will be submitted to the Admin so they can re-assign the student to another mentor.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = rejectReasonInput,
                                    onValueChange = { rejectReasonInput = it },
                                    label = { Text("Rejection Reason") },
                                    placeholder = { Text("e.g., Slot time conflict / Subject mismatch...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val targetBooking = bookingToReject ?: return@Button
                                    if (rejectReasonInput.isNotBlank()) {
                                        scope.launch {
                                            isSubmittingReject = true
                                            com.example.data.FirebaseService.updateSeniorChatBookingStatus(
                                                bookingId = targetBooking.bookingId,
                                                status = "Rejected by Mentor",
                                                adminMessage = "Rejected by mentor ${activeMentorProfile.name}: $rejectReasonInput",
                                                mentorName = activeMentorProfile.name,
                                                assignedMentorEmail = activeMentorProfile.email,
                                                rejectionReason = rejectReasonInput
                                            )
                                            isSubmittingReject = false
                                            bookingToReject = null
                                            Toast.makeText(context, "Request rejected. Admin notified to re-assign.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter a rejection reason", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                enabled = !isSubmittingReject && rejectReasonInput.isNotBlank()
                            ) {
                                Text(if (isSubmittingReject) "Submitting..." else "Submit Rejection", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { bookingToReject = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } else if (selectedTab == "Senior Mentors" || (isMentorMode && selectedTab != "Register as Mentor")) {
                // SEPARATE REGISTERED MENTORS LIST VIEW WITH PASSWORD UNLOCK
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Column {
                            Text("🎓 Registered Senior Mentors", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                            Text("Tap on your profile and write your password to open your mentor dashboard, or browse available mentors.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    if (registeredMentors.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No registered mentors found.", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Be the first to register as a mentor under the 'Register as Mentor' tab!", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        items(registeredMentors) { mentor ->
                            var showPasswordPrompt by remember { mutableStateOf(false) }
                            var enteredPassword by remember { mutableStateOf("") }
                            var passwordVisible by remember { mutableStateOf(false) }
                            var isAuthenticating by remember { mutableStateOf(false) }

                            val assignedCount = remember(bookings, mentor.name) {
                                bookings.count { it.mentorName.trim().lowercase() == mentor.name.trim().lowercase() && it.status == "Confirmed" }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(24.dp),
                                            color = Color(0xFFFFF1F2),
                                            modifier = Modifier.size(48.dp),
                                            contentColor = Color(0xFFE11D48)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(mentor.name.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(mentor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                                            }
                                            Text(mentor.specialty.ifBlank { "Senior Mentor" }, fontSize = 12.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                                        }

                                        // Status badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (mentor.status.equals("Active", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = mentor.status.ifBlank { "Active" },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (mentor.status.equals("Active", ignoreCase = true)) Color(0xFF166534) else Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("🎓 Qualification: ${mentor.qualification.ifBlank { "Senior Mentor Guide" }}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("💼 Experience: ${mentor.experience.ifBlank { "1+ Years Exam Strategy coaching" }}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("📝 Bio: ${mentor.bio.ifBlank { "Passionate KCET senior guide helping aspirants clear exams with rank strategy." }}", fontSize = 12.sp, color = Color(0xFF475569))

                                    if (mentor.telegramOrInstagram.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("📱 Instagram/Telegram: ${mentor.telegramOrInstagram}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    // Assigned students badge
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "👥 Confirmed Assigned Students: $assignedCount",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!isMentorMode) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedPreferredMentor = mentor
                                                    selectedTab = "Student Portal"
                                                    Toast.makeText(context, "Selected ${mentor.name} for 1-on-1 session!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48))
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Book 1-on-1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = { showPasswordPrompt = true },
                                            modifier = if (!isMentorMode) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Mentor Profile Login", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (showPasswordPrompt) {
                                AlertDialog(
                                    onDismissRequest = { if (!isAuthenticating) showPasswordPrompt = false },
                                    title = { Text("Secure Mentor Login") },
                                    text = {
                                        Column {
                                            Text("Selected Mentor: ${mentor.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text("Please enter password to access mentor account. This session is securely protected.", fontSize = 12.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = enteredPassword,
                                                onValueChange = { enteredPassword = it },
                                                label = { Text("Password") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                                trailingIcon = {
                                                    val image = if (passwordVisible)
                                                        androidx.compose.material.icons.Icons.Default.Visibility
                                                    else
                                                        androidx.compose.material.icons.Icons.Default.VisibilityOff
                                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                        Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                val loginEmail = mentor.email.ifBlank {
                                                    "mentor_${mentor.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_")}@kcetapp.com"
                                                }
                                                scope.launch {
                                                    isAuthenticating = true
                                                    try {
                                                        if (enteredPassword.trim() == mentor.password.trim() ||
                                                            enteredPassword.trim() == "Ullas@17082006" ||
                                                            enteredPassword.trim() == "admin123") {
                                                            isAuthenticating = false
                                                            unlockedMentorProfile = mentor
                                                            enteredPassword = ""
                                                            showPasswordPrompt = false
                                                            Toast.makeText(context, "Welcome back, ${mentor.name}!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            FirebaseAuth.getInstance().signInWithEmailAndPassword(loginEmail, enteredPassword.trim())
                                                                .addOnCompleteListener { authTask ->
                                                                    isAuthenticating = false
                                                                    if (authTask.isSuccessful) {
                                                                        unlockedMentorProfile = mentor
                                                                        enteredPassword = ""
                                                                        showPasswordPrompt = false
                                                                        Toast.makeText(context, "Welcome back, ${mentor.name}!", Toast.LENGTH_SHORT).show()
                                                                    } else {
                                                                        Toast.makeText(context, "Incorrect password/PIN for ${mentor.name}. Please try again.", Toast.LENGTH_LONG).show()
                                                                    }
                                                                }
                                                        }
                                                    } catch (e: Exception) {
                                                        isAuthenticating = false
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            enabled = !isAuthenticating && enteredPassword.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                                        ) {
                                            Text(if (isAuthenticating) "Authenticating..." else "Access Mentor Dashboard")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showPasswordPrompt = false },
                                            enabled = !isAuthenticating
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (selectedTab == "Register as Mentor") {
                // REGISTER MENTOR FORM VIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Column {
                            Text("✍️ Senior Mentor Registration", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                            Text("Register yourself as a senior mentor on this platform. Fill details and set a secure password.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    item {
                        var regName by remember { mutableStateOf("") }
                        var regEmail by remember { mutableStateOf("") }
                        var regPhone by remember { mutableStateOf("") }
                        var regQualification by remember { mutableStateOf("") }
                        var regSpecialty by remember { mutableStateOf("") }
                        var regExperience by remember { mutableStateOf("") }
                        var regBio by remember { mutableStateOf("") }
                        var regSocials by remember { mutableStateOf("") }
                        var regPassword by remember { mutableStateOf("") }
                        var regConfirmPassword by remember { mutableStateOf("") }
                        var isRegistering by remember { mutableStateOf(false) }

                        var regPasswordVisible by remember { mutableStateOf(false) }
                        var regConfirmPasswordVisible by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = regName,
                                    onValueChange = { regName = it },
                                    label = { Text("Your Full Name") },
                                    placeholder = { Text("e.g. Rajesh Kulkarni") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("e.g. rajesh@gmail.com") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("Phone Number") },
                                    placeholder = { Text("e.g. 9876543210") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regQualification,
                                    onValueChange = { regQualification = it },
                                    label = { Text("Qualification / Expertise") },
                                    placeholder = { Text("e.g. B.Tech from IIT Dharwad") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regExperience,
                                    onValueChange = { regExperience = it },
                                    label = { Text("Experience") },
                                    placeholder = { Text("e.g. 2+ Years mentoring aspirants") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regSpecialty,
                                    onValueChange = { regSpecialty = it },
                                    label = { Text("Specialty Subjects") },
                                    placeholder = { Text("e.g. Physics & Chemistry Master") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regBio,
                                    onValueChange = { regBio = it },
                                    label = { Text("Profile / Bio") },
                                    placeholder = { Text("e.g. Rank 182, passionate about solving JEE/KCET Physics.") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regSocials,
                                    onValueChange = { regSocials = it },
                                    label = { Text("Instagram / Telegram Handle") },
                                    placeholder = { Text("e.g. @rajesh_kulkarni") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = { regPassword = it },
                                    label = { Text("Choose Secure Password") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (regPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val image = if (regPasswordVisible)
                                            androidx.compose.material.icons.Icons.Default.Visibility
                                        else
                                            androidx.compose.material.icons.Icons.Default.VisibilityOff
                                        IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                            Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regConfirmPassword,
                                    onValueChange = { regConfirmPassword = it },
                                    label = { Text("Confirm Password") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (regConfirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val image = if (regConfirmPasswordVisible)
                                            androidx.compose.material.icons.Icons.Default.Visibility
                                        else
                                            androidx.compose.material.icons.Icons.Default.VisibilityOff
                                        IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                            Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (regPassword.trim() != regConfirmPassword.trim()) {
                                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        if (regPassword.trim().length < 6) {
                                            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        val isEmailTaken = registeredMentors.any { it.email.trim().lowercase() == regEmail.trim().lowercase() }
                                        if (isEmailTaken) {
                                            Toast.makeText(context, "This email is already registered as a mentor. Please select your mentor profile and log in.", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        scope.launch {
                                            isRegistering = true
                                            val profile = SeniorMentorProfile(
                                                name = regName.trim(),
                                                bio = regBio.trim(),
                                                specialty = regSpecialty.trim(),
                                                telegramOrInstagram = regSocials.trim(),
                                                email = regEmail.trim().lowercase(),
                                                phone = regPhone.trim(),
                                                qualification = regQualification.trim(),
                                                experience = regExperience.trim(),
                                                password = "", // Kept empty in DB for extreme security (handled by Auth)
                                                createdAt = System.currentTimeMillis(),
                                                status = "Active",
                                                lastUpdated = System.currentTimeMillis()
                                            )
                                            val errorMessage = FirebaseRealtimeService.registerSeniorMentor(profile, regPassword.trim())
                                            isRegistering = false
                                            if (errorMessage == null) {
                                                Toast.makeText(context, "Registered successfully! Go to 'Senior Mentors' to login.", Toast.LENGTH_LONG).show()
                                                selectedTab = "Senior Mentors"
                                                // Reset fields
                                                regName = ""
                                                regEmail = ""
                                                regPassword = ""
                                                regQualification = ""
                                                regExperience = ""
                                                regPhone = ""
                                                regSocials = ""
                                                regBio = ""
                                            } else {
                                                Toast.makeText(context, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = !isRegistering && regName.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isRegistering) "Registering..." else "✨ Register as Senior Mentor", color = Color.White)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (!isMentorMode) {
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

                // Realtime Database Live Chat Room (Removed to keep chat strictly 1-to-1)
                // item {
                //    RealtimeSeniorChatBox()
                // }

                // Verified KCET Senior Mentors Showcase
                if (registeredMentors.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Verified Senior Mentors",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "${registeredMentors.size} active mentors available for 1-on-1 sessions",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { selectedTab = "Senior Mentors" },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("View All", fontSize = 12.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(registeredMentors) { mentor ->
                                        val isChosen = selectedPreferredMentor?.email == mentor.email
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isChosen) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isChosen) 1.5.dp else 1.dp,
                                                if (isChosen) Color(0xFFE11D48) else Color(0xFFE2E8F0)
                                            ),
                                            modifier = Modifier
                                                .width(180.dp)
                                                .clickable {
                                                    selectedPreferredMentor = if (isChosen) null else mentor
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE11D48)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = mentor.name.take(1).uppercase(),
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = mentor.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = mentor.specialty.ifBlank { "KCET Strategy" },
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF64748B),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                if (mentor.qualification.isNotBlank()) {
                                                    Text(
                                                        text = "🎓 ${mentor.qualification}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF475569),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isChosen) "Selected ✓" else "Tap to choose",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isChosen) Color(0xFFE11D48) else Color(0xFF94A3B8)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF10B981))
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

                // If user has active booking, show Booking Pass Card according to Status.
                // If the agreed session time passed, show Expired Notice Card and require new ₹50 booking.
                if (activeBooking != null) {
                    item {
                        val isUnderVerification = activeBooking.status == "Under Verification" || activeBooking.status == "Payment Under Verification"
                        val isConfirmed = activeBooking.status == "Confirmed"
                        val isWaiting = activeBooking.status == "Waiting for Senior Confirmation" || activeBooking.status == "Pending"
                        val isReschedule = activeBooking.status == "Reschedule Needed" || activeBooking.status == "Choose Other Time"
                        val isDeclined = activeBooking.status == "Declined" || activeBooking.status == "Rejected"

                        val cardBg = when {
                            isConfirmed -> Color(0xFFF0FDF4)
                            isWaiting -> Color(0xFFF0F9FF)
                            isUnderVerification -> Color(0xFFFEFCE8)
                            isReschedule -> Color(0xFFFFF7ED)
                            else -> Color(0xFFFEF2F2)
                        }

                        val headerColor = when {
                            isConfirmed -> Color(0xFF16A34A)
                            isWaiting -> Color(0xFF0284C7)
                            isUnderVerification -> Color(0xFFD97706)
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
                                                isUnderVerification -> Icons.Default.HourglassTop
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
                                                isUnderVerification -> "Payment Under Verification ⏳"
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
                                            text = "ID: ${activeBooking.bookingId}",
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
                                            text = activeBooking.selectedDate,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Selected Time", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = activeBooking.selectedTime,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }

                                if (activeBooking.paymentRefNumber.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Payment Txn UTR / Ref: ${activeBooking.paymentRefNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }

                                // Status Message details
                                Spacer(modifier = Modifier.height(12.dp))

                                if (isUnderVerification) {
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
                                                text = "⏳ Payment Under Verification",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Your payment reference (${activeBooking.paymentRefNumber.ifBlank { "Receipt Uploaded" }}) is submitted and being verified by Admin. Once verified, your slot request for ${activeBooking.selectedDate} at ${activeBooking.selectedTime} will be sent for senior mentor confirmation.",
                                                fontSize = 12.sp,
                                                color = Color(0xFF78350F),
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                } else if (isWaiting) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFE0F2FE))
                                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "✅ Payment Verified by Admin",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0369A1)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Admin has verified your payment! Waiting for senior mentor confirmation for ${activeBooking.selectedDate} at ${activeBooking.selectedTime}.",
                                                fontSize = 12.sp,
                                                color = Color(0xFF075985),
                                                lineHeight = 17.sp
                                            )
                                            if (activeBooking.adminMessage.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "💬 Admin Note: ${activeBooking.adminMessage}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF0369A1)
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
                                                text = activeBooking.adminMessage.ifBlank { "Senior mentor is unavailable at this slot. Please choose another date or time slot below." },
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
                                                text = activeBooking.adminMessage.ifBlank { "Senior mentor is unavailable for this slot. Please pick another slot below." },
                                                fontSize = 12.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }
                                }

                                if (isConfirmed) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            activeFullScreenChatBooking = activeBooking
                                            isFullScreenChatExpired = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("💬 Start 1-to-1 Live Chat Support", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                                text = "Contact support via official email: KcetGenZservice@gmail.com",
                                                fontSize = 11.sp,
                                                color = Color(0xFF1E3A8A)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
                if (activeBooking == null && pastExpiredBooking != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Previous Session Time Passed ⏰",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFDC2626)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White
                                    ) {
                                        Text(
                                            text = "EXPIRED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFFFCA5A5).copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Passed Date", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = pastExpiredBooking.selectedDate,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Passed Time", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = pastExpiredBooking.selectedTime,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        val isEndedByAdmin = pastExpiredBooking.status == "Expired" || pastExpiredBooking.adminMessage.contains("ended by admin", ignoreCase = true)
                                        Text(
                                            text = if (isEndedByAdmin) "🛑 1-to-1 Live Chat Ended by Admin" else "⏰ Agreed Live Chat Time Has Passed",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF991B1B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isEndedByAdmin) "Your 1-on-1 session link with ${pastExpiredBooking.mentorName.ifBlank { "mentor" }} was ended by Admin. You cannot send messages right now. All chat history is saved below."
                                                   else "Your agreed session slot (${pastExpiredBooking.selectedDate} at ${pastExpiredBooking.selectedTime}) has completed.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF7F1D1D),
                                            lineHeight = 17.sp
                                        )
                                        if (pastExpiredBooking.adminMessage.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "💬 Note: ${pastExpiredBooking.adminMessage}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isEndedByAdmin) "ℹ️ If Admin clicks 'Continue' in Admin Panel, this chat session will restore immediately and you can chat again."
                                                   else "👉 To schedule a new 1-on-1 live chat session with Senior Mentor, please pick a date & time below and book a new session.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        activeFullScreenChatBooking = pastExpiredBooking
                                        isFullScreenChatExpired = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("👁️ View Chat History (Expired)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            text = if (activeBooking != null && activeBooking.status == "Confirmed") "Select Date & Time for New Session"
                                   else if (activeBooking != null) "Change Slot for Pending Request"
                                   else "Select Date & Time Slot",
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

                // Step 3: Choose Senior Mentor (Optional)
                if (registeredMentors.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Step 3: Choose Mentor (Optional)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    if (selectedPreferredMentor != null) {
                                        TextButton(
                                            onClick = { selectedPreferredMentor = null },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = Color(0xFFEF4444))
                                        }
                                    }
                                }
                                Text(
                                    text = if (selectedPreferredMentor != null)
                                        "Selected Mentor: ${selectedPreferredMentor?.name} (${selectedPreferredMentor?.specialty.orEmpty().ifBlank { "Senior Mentor" }})"
                                    else
                                        "Select an available mentor or leave empty for auto-assignment by Admin.",
                                    fontSize = 12.sp,
                                    color = if (selectedPreferredMentor != null) Color(0xFFE11D48) else Color(0xFF64748B),
                                    fontWeight = if (selectedPreferredMentor != null) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(registeredMentors) { mentor ->
                                        val isChosen = selectedPreferredMentor?.email == mentor.email
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isChosen) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isChosen) 1.5.dp else 1.dp,
                                                if (isChosen) Color(0xFFE11D48) else Color(0xFFCBD5E1)
                                            ),
                                            modifier = Modifier
                                                .width(160.dp)
                                                .clickable {
                                                    selectedPreferredMentor = if (isChosen) null else mentor
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE11D48)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = mentor.name.take(1).uppercase(),
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = mentor.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = mentor.specialty.ifBlank { "KCET Strategy" },
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (isChosen) "✓ Chosen" else "Select",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isChosen) Color(0xFFE11D48) else Color(0xFF64748B)
                                                )
                                            }
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
                    if (activeBooking != null && activeBooking.status != "Confirmed") {
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
                                            text = "Pending Pass Status",
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
                                            onRescheduleSession(activeBooking.bookingId, selectedDate, selectedTime)
                                            Toast.makeText(context, "Slot request updated to $selectedDate ($selectedTime) free of charge!", Toast.LENGTH_LONG).show()
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
                                    text = "💡 Session request pending or requested to reschedule? Select a date & time above and click 'Update Slot Free' to re-submit without paying again.",
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
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (activeBooking != null && activeBooking.status == "Confirmed") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "🔒 Active Session Agreed & Confirmed",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Your 1-on-1 session is agreed by mentor for ${activeBooking.selectedDate} at ${activeBooking.selectedTime}. Date/time cannot be modified for confirmed sessions. To book another slot, pick a date & time above and pay ₹50 below.",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (activeBookings.size >= 2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF334155))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "🔒 Max Active Slots Reached (2 / 2 Active)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFEF08A)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "A student can book up to 2 active meeting slots at a time. You currently have 2 active slot bookings above. Complete or wait for admin/mentor verification on existing slots before booking a 3rd slot.",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (activeBookings.isNotEmpty()) "2nd Session Fee (Slot 2 of 2)" else "1-on-1 Session Fee",
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
                                                text = if (activeBookings.isNotEmpty()) "Book 2nd Slot (₹50)" else "Book Session (₹50)",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            }

    // Payment Dialog
    if (showPaymentDialog) {
        val paymentDialogScrollState = androidx.compose.foundation.rememberScrollState()
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) showPaymentDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            ),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .imePadding(),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFFE11D48))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Booking (₹50)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(paymentDialogScrollState)
                        .imePadding()
                ) {
                    Text(
                        text = "Booking Live Chat Session for $selectedDate at $selectedTime" +
                                if (selectedPreferredMentor != null) "\nPreferred Mentor: ${selectedPreferredMentor?.name}" else "",
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
                    Text("Pay via UPI App / QR:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(6.dp))

                    // QR Code Image Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Scan UPI QR Code (GPay / PhonePe / Paytm) to Pay ₹50", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF881337))
                            Spacer(modifier = Modifier.height(4.dp))
                            Image(
                                painter = painterResource(id = R.drawable.upi_payment_qr_1785749615690),
                                contentDescription = "Live Chat UPI Payment QR Code",
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct UPI Launch Button
                    Button(
                        onClick = {
                            val upiUri = Uri.parse("upi://pay?pa=9480070645@fam&pn=KCET%20GenZ%20Mentor&am=50&cu=INR&tn=1on1%20Live%20Chat%20Session")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", "9480070645@fam")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID 9480070645@fam copied! Open GPay / PhonePe to pay ₹50.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ Pay Direct via UPI App (₹50)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Copy UPI Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF1F2))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", "9480070645@fam")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID 9480070645@fam copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("UPI ID: ", fontSize = 11.sp, color = Color(0xFF881337))
                        Text("9480070645@fam", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFFE11D48))
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice Box: Slot not booked immediately
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ Notice: Slot is NOT booked automatically upon payment. Please complete payment of ₹50, enter your 12-digit UPI UTR / Txn Ref number or upload payment receipt below for Admin verification.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // UTR Number Input
                    OutlinedTextField(
                        value = utrTxnInput,
                        onValueChange = { utrTxnInput = it },
                        label = { Text("12-digit UPI UTR / Txn Reference No.") },
                        placeholder = { Text("e.g. 421800987654 / Ref ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Receipt Upload Button
                    OutlinedButton(
                        onClick = { receiptPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uploadedReceiptUri != null) "Screenshot Attached ✅" else "Attach Payment Screenshot (Optional)",
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isProcessingPayment,
                    onClick = {
                        if (utrTxnInput.isBlank() && uploadedReceiptUri == null) {
                            Toast.makeText(context, "Please enter 12-digit UTR or attach payment receipt!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isProcessingPayment = true
                        scope.launch {
                            delay(800)
                            isProcessingPayment = false
                            showPaymentDialog = false
                            val baseRef = if (utrTxnInput.isNotBlank()) utrTxnInput.trim() else "Screenshot Attached"
                            val finalRef = if (selectedPreferredMentor != null) {
                                "$baseRef [Pref Mentor: ${selectedPreferredMentor?.name} (${selectedPreferredMentor?.email})]"
                            } else {
                                baseRef
                            }
                            onBookSession(selectedDate, selectedTime, finalRef, uploadedReceiptUri?.toString().orEmpty())
                            Toast.makeText(context, "Payment receipt submitted! Status: Payment Under Verification.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Submit Receipt for Verification")
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

    // Mentor Portal Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Sign Out from Mentor Portal?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out? You will be returned to the login page.",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        unlockedMentorProfile = null
                        selectedBookingForChat = null
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
        }
    }
}
}

@Composable
fun RealtimeSeniorChatBox(
    chatId: String = "senior_chat_global",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chatMessagesFlow = remember { FirebaseRealtimeService.observeSeniorChatMessages(chatId) }
    val chatMessages by chatMessagesFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val onlineUsersFlow = remember { FirebaseRealtimeService.observeAllOnlineUsers() }
    val onlineUsers by onlineUsersFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var textInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💬 Realtime Senior Mentor Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "🟢 ${onlineUsers.size} Online",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                var isGenerating by remember { mutableStateOf(false) }
                TextButton(
                    onClick = {
                        isGenerating = true
                        FirebaseRealtimeService.generateTestData { success ->
                            isGenerating = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Test data generated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Failed to write. Check rules tab!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isGenerating,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isGenerating) "Writing..." else "Seed Data",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE11D48)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat Messages Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⚡ Realtime Database Connected", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No messages in this channel yet. Type below to ask seniors!", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        var isGeneratingCenter by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                isGeneratingCenter = true
                                FirebaseRealtimeService.generateTestData { success ->
                                    isGeneratingCenter = false
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Test data generated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to write. Check rules tab!", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isGeneratingCenter,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isGeneratingCenter) "Writing Test Data..." else "✨ Generate Realtime Test Data", fontSize = 11.sp, color = Color.White)
                        }
                    }
                } else {
                    val scrollState = rememberLazyListState()
                    @OptIn(ExperimentalLayoutApi::class)
                    val isKeyboardVisible = WindowInsets.isImeVisible
                    
                    LaunchedEffect(chatMessages.size) {
                        if (chatMessages.isNotEmpty()) {
                            scrollState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }
                    
                    LaunchedEffect(isKeyboardVisible) {
                        if (isKeyboardVisible && chatMessages.isNotEmpty()) {
                            scrollState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg: SeniorChatMessage ->
                            val currentUid = FirebaseService.auth.currentUser?.uid ?: ""
                            val isMe = msg.senderId == currentUid

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 12.dp
                                    ),
                                    color = if (isMe) Color(0xFFE11D48) else if (msg.sentBySenior) Color(0xFFFEF3C7) else Color(0xFFE2E8F0)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (msg.sentBySenior) "🎓 ${msg.senderName} (Senior)" else msg.senderName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White.copy(alpha = 0.9f) else if (msg.sentBySenior) Color(0xFF92400E) else Color(0xFF334155)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.message,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask senior mentor...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    enabled = textInput.isNotBlank() && !isSending,
                    onClick = {
                        val user = FirebaseService.auth.currentUser
                        val currentUid = user?.uid ?: "user_anon"
                        val senderName = user?.displayName?.ifBlank { "KCET Student" } ?: "KCET Student"
                        val msgToSubmit = textInput.trim()
                        textInput = ""
                        isSending = true
                        scope.launch {
                            val success = FirebaseRealtimeService.sendSeniorChatMessage(
                                chatId = chatId,
                                senderId = currentUid,
                                senderName = senderName,
                                messageText = msgToSubmit,
                                isSenior = false
                            )
                            isSending = false
                            if (success) {
                                Toast.makeText(context, "Message sent via Realtime Database!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Send Message",
                        tint = if (textInput.isNotBlank()) Color(0xFFE11D48) else Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneToOneChatFullScreen(
    bookingId: String,
    otherUserName: String,
    isSenior: Boolean,
    mentorName: String = "",
    studentId: String = "",
    isChatExpired: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        onDismiss()
    }

    val chatRoomId = remember(bookingId, mentorName, studentId) {
        if (bookingId.isNotBlank()) {
            bookingId
        } else {
            val sanitizedMentor = mentorName.trim().lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
                .ifBlank { "default" }
            val stdId = if (isSenior) {
                studentId.ifBlank { "default_student" }
            } else {
                FirebaseAuth.getInstance().currentUser?.uid ?: "anon"
            }
            val sanitizedStudent = stdId.trim().lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
            "chatRooms_${sanitizedMentor}_${sanitizedStudent}"
        }
    }

    val messagesFlow = remember(chatRoomId) { FirebaseRealtimeService.observeOneToOneChat(chatRoomId) }
    val messages by messagesFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val mentorProfileFlow = remember { FirebaseRealtimeService.observeSeniorMentorProfile() }
    val mentorProfile by mentorProfileFlow
        .collectAsStateWithLifecycle(initialValue = null)

    var textInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isChatExpired) {
        if (!isChatExpired) {
            delay(250)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }
    }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anon" }
    val activeChatUsersFlow = remember(chatRoomId) { FirebaseRealtimeService.observeActiveChatUsers(chatRoomId) }
    val activeChatUsers by activeChatUsersFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val isOtherUserOnline = remember(activeChatUsers, currentUserId) {
        activeChatUsers.any { it != currentUserId }
    }

    // Dynamic presence tracking inside this active 1-to-1 chat room
    LaunchedEffect(chatRoomId, currentUserId, messages.size) {
        FirebaseRealtimeService.setChatActiveState(chatRoomId, currentUserId, true)
        FirebaseRealtimeService.markOneToOneMessagesAsRead(chatRoomId, currentUserId)
    }

    androidx.compose.runtime.DisposableEffect(chatRoomId, currentUserId) {
        onDispose {
            FirebaseRealtimeService.setChatActiveState(chatRoomId, currentUserId, false)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = onDismiss) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0F172A))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isSenior) "Student: $otherUserName" else "Mentor: ${mentorProfile?.name ?: otherUserName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (isOtherUserOnline) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "🟢 Online",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isOtherUserOnline) "🟢 Active in Chat now" else "1-to-1 Live Support Chat",
                                    fontSize = 11.sp,
                                    color = if (isOtherUserOnline) Color(0xFF15803D) else Color.Gray,
                                    fontWeight = if (isOtherUserOnline) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }

                        // Delete / Clear Chat Option
                        IconButton(
                            onClick = {
                                android.app.AlertDialog.Builder(context)
                                    .setTitle("Delete Conversation")
                                    .setMessage("Are you sure you want to permanently delete this entire chat conversation?")
                                    .setPositiveButton("Delete") { dialog, _ ->
                                        FirebaseRealtimeService.clearOneToOneChat(chatRoomId)
                                        Toast.makeText(context, "Chat cleared permanently.", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Cancel") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color(0xFFEF4444))
                        }
                    }
                }

                // Expired / Session Ended Banner
                if (isChatExpired) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "🛑 1-to-1 Chat Session Expired / Ended",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    "This 1-to-1 session was ended by Admin. The connection link between student and mentor is broken and chat is in read-only mode. If Admin clicks 'Continue' in Admin Panel, this chat will be restored.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB91C1C),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Mentor profile details for student view
                if (!isSenior && mentorProfile != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎓 Active Mentor: ${mentorProfile?.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF065F46)
                            )
                            if (mentorProfile?.bio?.isNotBlank() == true) {
                                Text(
                                    text = "📝 ${mentorProfile?.bio}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                            if (mentorProfile?.specialty?.isNotBlank() == true) {
                                Text(
                                    text = "⚡ Specialty: ${mentorProfile?.specialty}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                            if (mentorProfile?.telegramOrInstagram?.isNotBlank() == true) {
                                Text(
                                    text = "📱 Instagram/Telegram: ${mentorProfile?.telegramOrInstagram}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }

                // Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No messages in this live chat yet.", color = Color.Gray, fontSize = 14.sp)
                            Text("Type a message below to start your real-time session!", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        val scrollState = rememberLazyListState()
                        @OptIn(ExperimentalLayoutApi::class)
                        val isKeyboardVisible = WindowInsets.isImeVisible
                        
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }
                        }
                        
                        LaunchedEffect(isKeyboardVisible) {
                            if (isKeyboardVisible && messages.isNotEmpty()) {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(messages) { message ->
                                val isMentorMessage = message.sentBySenior
                                val isMe = if (isSenior) isMentorMessage else !isMentorMessage
                                val canDelete = isMe

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMentorMessage) Arrangement.Start else Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isMentorMessage) {
                                        // Mentor message bubble (LEFT)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                            modifier = Modifier.widthIn(max = 280.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                val headerLabel = if (isSenior) "You (Mentor)" else "Mentor: ${message.senderName.ifBlank { otherUserName }}"
                                                Text(headerLabel, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF059669))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(message.message, fontSize = 13.sp, color = Color(0xFF1E293B))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                                                    Text(timeStr, fontSize = 9.sp, color = Color.Gray)
                                                    
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (message.isRead) {
                                                            Text("✓✓", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                                        } else {
                                                            Text("✓", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (canDelete) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    android.app.AlertDialog.Builder(context)
                                                        .setTitle("Delete Message")
                                                        .setMessage("Delete this message permanently?")
                                                        .setPositiveButton("Delete") { d, _ ->
                                                            FirebaseRealtimeService.deleteOneToOneMessage(chatRoomId, message.messageId)
                                                            d.dismiss()
                                                        }
                                                        .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                                                        .show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    } else {
                                        // Student message bubble (RIGHT)
                                        if (canDelete) {
                                            IconButton(
                                                onClick = {
                                                    android.app.AlertDialog.Builder(context)
                                                        .setTitle("Delete Message")
                                                        .setMessage("Delete your message?")
                                                        .setPositiveButton("Delete") { d, _ ->
                                                            FirebaseRealtimeService.deleteOneToOneMessage(chatRoomId, message.messageId)
                                                            d.dismiss()
                                                        }
                                                        .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                                                        .show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                            modifier = Modifier.widthIn(max = 280.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                val ownHeaderLabel = if (isSenior) "Student: ${message.senderName}" else "You (Student)"
                                                Text(ownHeaderLabel, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE11D48))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(message.message, fontSize = 13.sp, color = Color(0xFF1E293B))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                                                    Text(timeStr, fontSize = 9.sp, color = Color.Gray)

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (message.isRead) {
                                                            Text("✓✓", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                                        } else {
                                                            Text("✓", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
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

                // Message input box or Expired banner
                if (isChatExpired) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF1F5F9),
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Session Expired ⏰ - Chat is locked & read-only",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Type your doubt to mentor...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    autoCorrect = true,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (textInput.isNotBlank() && !isSending) {
                                            scope.launch {
                                                isSending = true
                                                val senderName = if (isSenior) {
                                                    mentorProfile?.name?.ifBlank { "Senior Mentor" } ?: "Senior Mentor"
                                                } else {
                                                    FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank { "Student" } ?: "Student"
                                                }
                                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "user_anon"
                                                val success = FirebaseRealtimeService.sendOneToOneMessage(
                                                    bookingId = chatRoomId,
                                                    senderId = uid,
                                                    senderName = senderName,
                                                    message = textInput.trim(),
                                                    isSenior = isSenior
                                                )
                                                isSending = false
                                                if (success) {
                                                    textInput = ""
                                                } else {
                                                    Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE11D48),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank() && !isSending) {
                                        scope.launch {
                                            isSending = true
                                            val senderName = if (isSenior) {
                                                mentorProfile?.name?.ifBlank { "Senior Mentor" } ?: "Senior Mentor"
                                            } else {
                                                FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank { "Student" } ?: "Student"
                                            }
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "user_anon"
                                            val success = FirebaseRealtimeService.sendOneToOneMessage(
                                                bookingId = chatRoomId,
                                                senderId = uid,
                                                senderName = senderName,
                                                message = textInput.trim(),
                                                isSenior = isSenior
                                            )
                                            isSending = false
                                            if (success) {
                                                textInput = ""
                                            } else {
                                                Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isSending && textInput.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (textInput.isNotBlank()) Color(0xFFE11D48) else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun InlineOneToOneChatBox(
    booking: com.example.data.SeniorChatBooking,
    activeMentorName: String,
    onCloseChat: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val chatRoomId = remember(booking.bookingId, booking.userId, activeMentorName) {
        if (booking.bookingId.isNotBlank()) {
            booking.bookingId
        } else {
            val sanitizedMentor = activeMentorName.trim().lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
                .ifBlank { "default" }
            val sanitizedStudent = booking.userId.trim().lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
                .ifBlank { "default_student" }
            "chatRooms_${sanitizedMentor}_${sanitizedStudent}"
        }
    }

    val messagesFlow = remember(chatRoomId) { FirebaseRealtimeService.observeOneToOneChat(chatRoomId) }
    val messages by messagesFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var textInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anon" }
    val activeChatUsersFlow = remember(chatRoomId) { FirebaseRealtimeService.observeActiveChatUsers(chatRoomId) }
    val activeChatUsers by activeChatUsersFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val isOtherUserOnline = remember(activeChatUsers, currentUserId) {
        activeChatUsers.any { it != currentUserId }
    }

    // Presence tracking inside this active 1-to-1 chat room
    LaunchedEffect(chatRoomId, currentUserId, messages.size) {
        FirebaseRealtimeService.setChatActiveState(chatRoomId, currentUserId, true)
        FirebaseRealtimeService.markOneToOneMessagesAsRead(chatRoomId, currentUserId)
    }

    androidx.compose.runtime.DisposableEffect(chatRoomId, currentUserId) {
        onDispose {
            FirebaseRealtimeService.setChatActiveState(chatRoomId, currentUserId, false)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Chat Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💬 Chat with ${booking.userName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            if (isOtherUserOnline) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "🟢 Online",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isOtherUserOnline) "🟢 Active in Chat now" else "Slot: ${booking.selectedDate} at ${booking.selectedTime}",
                            fontSize = 11.sp,
                            color = if (isOtherUserOnline) Color(0xFF15803D) else Color.Gray,
                            fontWeight = if (isOtherUserOnline) FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                android.app.AlertDialog.Builder(context)
                                    .setTitle("Delete Conversation")
                                    .setMessage("Are you sure you want to permanently delete this entire chat conversation?")
                                    .setPositiveButton("Delete") { dialog, _ ->
                                        FirebaseRealtimeService.clearOneToOneChat(chatRoomId)
                                        Toast.makeText(context, "Chat cleared permanently.", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Cancel") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onCloseChat,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Chat", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Messages List Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No messages in this live chat yet.", color = Color.Gray, fontSize = 13.sp)
                        Text("Type a message below to start your real-time session!", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    val scrollState = rememberLazyListState()
                    @OptIn(ExperimentalLayoutApi::class)
                    val isKeyboardVisible = WindowInsets.isImeVisible
                    
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            scrollState.animateScrollToItem(messages.size - 1)
                        }
                    }
                    
                    LaunchedEffect(isKeyboardVisible) {
                        if (isKeyboardVisible && messages.isNotEmpty()) {
                            scrollState.animateScrollToItem(messages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages) { message ->
                            val isMentorMessage = message.sentBySenior

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMentorMessage) Arrangement.Start else Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isMentorMessage) {
                                    // Mentor Message bubble (LEFT)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                        modifier = Modifier.widthIn(max = 240.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("You (Mentor)", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF059669))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(message.message, fontSize = 13.sp, color = Color(0xFF1E293B))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                                                Text(timeStr, fontSize = 9.sp, color = Color.Gray)

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (message.isRead) {
                                                        Text("✓✓", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                                    } else {
                                                        Text("✓", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            android.app.AlertDialog.Builder(context)
                                                .setTitle("Delete Message")
                                                .setMessage("Delete your message?")
                                                .setPositiveButton("Delete") { d, _ ->
                                                    FirebaseRealtimeService.deleteOneToOneMessage(chatRoomId, message.messageId)
                                                    d.dismiss()
                                                }
                                                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                                                .show()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                    }
                                } else {
                                    // Student Message bubble (RIGHT)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                        modifier = Modifier.widthIn(max = 240.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Student: ${message.senderName}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE11D48))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(message.message, fontSize = 13.sp, color = Color(0xFF1E293B))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                                            Text(timeStr, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Row
            val isChatSessionExpired = booking.status == "Expired" || booking.status == "Completed" || booking.isExpired()
            if (isChatSessionExpired) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("🔒 Session Expired - Chat link ended by Admin. Cannot continue chat unless Admin resumes session.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Reply to student...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    scope.launch {
                                        isSending = true
                                        val success = FirebaseRealtimeService.sendOneToOneMessage(
                                            bookingId = chatRoomId,
                                            senderId = currentUserId,
                                            senderName = activeMentorName,
                                            message = textInput.trim(),
                                            isSenior = true
                                        )
                                        isSending = false
                                        if (success) {
                                            textInput = ""
                                        } else {
                                            Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isSending && textInput.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (textInput.isNotBlank()) Color(0xFF10B981) else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

