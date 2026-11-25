package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminEmailDeliveryStatus
import com.example.data.AdminEmailLogItem
import com.example.data.AdminEmailService
import com.example.data.AdminRegisteredStudent
import com.example.viewmodel.AdminEmailNotificationUiState

@Composable
fun AdminEmailNotificationsSection(
    uiState: AdminEmailNotificationUiState,
    onLoadStudents: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectStudent: (AdminRegisteredStudent?) -> Unit,
    onSubjectChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendEmail: () -> Unit,
    onLoadEmailLogs: (Boolean) -> Unit,
    onDismissFeedback: () -> Unit
) {
    val context = LocalContext.current
    var dropdownExpanded by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Fetch students and email history once when opening the section if not yet loaded.
    LaunchedEffect(Unit) {
        if (!uiState.isStudentsLoaded && !uiState.isLoadingStudents) {
            onLoadStudents(false)
        }
        if (!uiState.isLogsLoaded && !uiState.isLoadingLogs) {
            onLoadEmailLogs(false)
        }
    }

    LaunchedEffect(uiState.sendSuccessMessage) {
        val msg = uiState.sendSuccessMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val selected = uiState.selectedStudent
    val filteredStudents = remember(uiState.students, uiState.searchQuery) {
        AdminEmailService.filterStudents(uiState.students, uiState.searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_email_notifications_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner + Firebase Auth Session Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF38BDF8), Color(0xFF10B981))
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = Color(0xFF6366F1).copy(alpha = 0.25f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Admin Email Notifications",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Email Notifications",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Send verified emails via KCET GenZ backend to registered students",
                                fontSize = 11.5.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                            Color(0xFF10B981).copy(alpha = 0.45f)
                        } else {
                            Color(0xFFF59E0B).copy(alpha = 0.55f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                                Icons.Default.VerifiedUser
                            } else {
                                Icons.Default.Security
                            },
                            contentDescription = "Firebase Admin Session",
                            tint = if (uiState.isFirebaseSignedIn && uiState.isFirebaseEmailVerified) {
                                Color(0xFF34D399)
                            } else {
                                Color(0xFFFBBF24)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isFirebaseSignedIn) {
                                    "Admin Session: ${uiState.firebaseAuthEmail ?: "Signed In"}"
                                } else {
                                    "Firebase Admin Session: Not Signed In"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    !uiState.isFirebaseSignedIn ->
                                        "Sign in with your verified Firebase Admin account to load students and send emails."
                                    uiState.isFirebaseEmailVerified ->
                                        "Fresh Firebase ID Token attached automatically on every API request."
                                    else ->
                                        "Verify admin email before accessing student email APIs."
                                },
                                fontSize = 10.5.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // 2. Section A: Student Selection Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_student_selection_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Select Registered Student",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Search by name, registered Gmail, or student ID (${uiState.students.size} loaded)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    OutlinedButton(
                        onClick = { onLoadStudents(true) },
                        enabled = !uiState.isLoadingStudents,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("refresh_students_button")
                    ) {
                        if (uiState.isLoadingStudents) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("students_loading_indicator"),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4F46E5)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Students",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reload",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                if (uiState.isLoadingStudents) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("students_loading_bar"),
                        color = Color(0xFF4F46E5),
                        trackColor = Color(0xFFE0E7FF)
                    )
                }

                if (!uiState.studentsError.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("students_error_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Students Load Error",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.studentsError,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onLoadStudents(true) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("retry_load_students_button")
                            ) {
                                Text("Retry", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Selected student card with Read-Only Registered Gmail
                if (selected != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.5.dp, Color(0xFF4F46E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("selected_student_summary_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Selected Student",
                                        tint = Color(0xFF4338CA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = selected.name,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1B4B),
                                            modifier = Modifier.testTag("selected_student_name")
                                        )
                                        if (selected.studentId.isNotBlank()) {
                                            Text(
                                                text = "ID: ${selected.studentId}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF4338CA)
                                            )
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("change_selected_student_button")
                                ) {
                                    Text(
                                        text = "Change",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4338CA)
                                    )
                                }
                            }

                            // Strictly Read-Only Registered Gmail Display (No manual email input allowed)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Read-Only Registered Email",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Registered Gmail (Read-Only)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = selected.email.ifBlank { "Resolved by server for UID ${selected.uid}" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.testTag("selected_student_readonly_email")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Input + Dropdown Toggle
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { newValue ->
                        onSearchQueryChange(newValue)
                        dropdownExpanded = true
                    },
                    label = { Text("Search student by name, Gmail, or ID") },
                    placeholder = { Text("Type name, @gmail.com, or KCET ID...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Students",
                            tint = Color(0xFF4F46E5)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        dropdownExpanded = true
                                    },
                                    modifier = Modifier.testTag("clear_student_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { dropdownExpanded = !dropdownExpanded },
                                modifier = Modifier.testTag("admin_email_student_dropdown_trigger")
                            ) {
                                Icon(
                                    imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = if (dropdownExpanded) "Hide Student List" else "Show Student List",
                                    tint = Color(0xFF4F46E5)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_email_student_search_input")
                )

                // Show dropdown picker automatically if opened or while no student is selected yet
                val showStudentList = dropdownExpanded || selected == null
                if (showStudentList) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_student_dropdown_list")
                    ) {
                        when {
                            uiState.isLoadingStudents && uiState.students.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Loading registered students...",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            uiState.students.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                        .testTag("students_empty_state"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "No registered students found",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Tap Reload to fetch registered students from the backend.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            filteredStudents.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                        .testTag("students_search_empty_state"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No registered students match \"${uiState.searchQuery.trim()}\".",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp)
                                        .verticalScroll(rememberScrollState())
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filteredStudents.forEach { student ->
                                        val isSelected = selected?.uid == student.uid
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFFE0E7FF) else Color.White,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .clickable {
                                                    onSelectStudent(student)
                                                    localValidationError = null
                                                    dropdownExpanded = false
                                                }
                                                .testTag("student_option_${student.uid}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = student.name,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = student.email.ifBlank { "UID: ${student.uid}" },
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFF475569),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                if (student.studentId.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFF1F5F9),
                                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                                    ) {
                                                        Text(
                                                            text = student.studentId,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF334155),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
            }
        }

        // 3. Section B: Email Composer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_composer_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. Compose Student Email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                // Read-only Recipient Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Recipient",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recipient (Auto-resolved by Backend)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = if (selected != null) {
                                    "${selected.name} <${selected.email}>"
                                } else {
                                    "Select a registered student above"
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected != null) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                modifier = Modifier.testTag("composer_readonly_recipient")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.subject,
                    onValueChange = {
                        onSubjectChange(it)
                        localValidationError = null
                    },
                    enabled = !uiState.isSending,
                    label = { Text("Email Subject *") },
                    placeholder = { Text("Enter email subject") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_email_subject_input")
                )

                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = {
                        onMessageChange(it)
                        localValidationError = null
                    },
                    enabled = !uiState.isSending,
                    label = { Text("Email Message *") },
                    placeholder = { Text("Write your message for the student...") },
                    minLines = 5,
                    maxLines = 10,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_email_message_input")
                )

                val displayValidationError = localValidationError
                if (!displayValidationError.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_validation_error")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Validation Warning",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayValidationError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }
        }

        // 4. Section C: Live Branded Email Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_preview_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Email Preview",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. KCET GenZ Email Preview",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "LIVE PREVIEW",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE0E7FF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (selected != null) {
                            "To: ${selected.name} (${selected.email})"
                        } else {
                            "To: No student selected"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        modifier = Modifier.testTag("preview_recipient_text")
                    )

                    Text(
                        text = if (uiState.subject.isNotBlank()) {
                            uiState.subject
                        } else {
                            "(Subject preview)"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (uiState.subject.isNotBlank()) Color(0xFF0F172A) else Color(0xFF94A3B8),
                        modifier = Modifier.testTag("preview_subject_text")
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.message.isNotBlank()) {
                                uiState.message
                            } else {
                                "Your formatted email message will preview here before sending."
                            },
                            fontSize = 13.sp,
                            color = if (uiState.message.isNotBlank()) Color(0xFF1E293B) else Color(0xFF94A3B8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .testTag("preview_message_text")
                        )
                    }

                    Text(
                        text = "Sent via KCET GenZ Official Admin Portal • Do not reply to automated headers",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // 5. Section D & E: Status Banners + Send Action
        if (!uiState.sendSuccessMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_email_send_success_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Email Sent",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.sendSuccessMessage,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onDismissFeedback,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("OK", fontSize = 11.sp, color = Color(0xFF065F46))
                    }
                }
            }
        }

        if (!uiState.sendErrorMessage.isNullOrBlank()) {
            val isTimeout = uiState.isSendTimeoutUncertain
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isTimeout) Color(0xFFFFFBEB) else Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, if (isTimeout) Color(0xFFD97706) else Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(
                        if (isTimeout) {
                            "admin_email_timeout_warning_banner"
                        } else {
                            "admin_email_send_error_banner"
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTimeout) Icons.Default.Warning else Icons.Default.Error,
                        contentDescription = "Send Error",
                        tint = if (isTimeout) Color(0xFFD97706) else Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.sendErrorMessage,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTimeout) Color(0xFF92400E) else Color(0xFF991B1B),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissFeedback) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Error",
                            tint = if (isTimeout) Color(0xFF92400E) else Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                when {
                    selected == null || selected.uid.isBlank() -> {
                        localValidationError = "Please select a registered student before sending."
                    }
                    uiState.subject.trim().isBlank() -> {
                        localValidationError = "Please enter an email subject."
                    }
                    uiState.message.trim().isBlank() -> {
                        localValidationError = "Please enter an email message."
                    }
                    else -> {
                        localValidationError = null
                        showConfirmDialog = true
                    }
                }
            },
            enabled = !uiState.isSending,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5),
                disabledContainerColor = Color(0xFF94A3B8)
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("admin_email_send_button")
        ) {
            if (uiState.isSending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("admin_email_sending_indicator"),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sending Email...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Email",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Review & Send Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // 6. Section F: Email History Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_history_section"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Email History",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Email History (${uiState.emailLogs.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Synced from backend admin email logs",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onLoadEmailLogs(true) },
                        enabled = !uiState.isLoadingLogs,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("refresh_email_logs_button")
                    ) {
                        if (uiState.isLoadingLogs) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("email_logs_loading_indicator"),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4F46E5)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Email History",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                if (uiState.isLoadingLogs) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_logs_loading_bar"),
                        color = Color(0xFF4F46E5),
                        trackColor = Color(0xFFE0E7FF)
                    )
                }

                if (!uiState.logsError.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_logs_error_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "History Error",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.logsError,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onLoadEmailLogs(true) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("retry_load_logs_button")
                            ) {
                                Text("Retry", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                if (uiState.emailLogs.isEmpty() && !uiState.isLoadingLogs) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_logs_empty_state")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "No admin emails sent yet",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Submitted, uncertain, and failed delivery records will appear here.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.emailLogs.forEachIndexed { index, item ->
                            AdminEmailHistoryCard(
                                index = index,
                                item = item
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog Before Sending Email
    if (showConfirmDialog && selected != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            modifier = Modifier.testTag("send_confirmation_dialog"),
            title = {
                Text(
                    text = "Confirm Student Email",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Please verify the recipient and subject before sending:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Recipient: ${selected.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.testTag("confirm_dialog_recipient_name")
                            )
                            Text(
                                text = "Registered Gmail: ${selected.email}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4338CA),
                                modifier = Modifier.testTag("confirm_dialog_recipient_email")
                            )
                            Text(
                                text = "Subject: ${uiState.subject.trim()}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.testTag("confirm_dialog_subject")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSendEmail()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("confirm_send_confirm_button")
                ) {
                    Text("Confirm & Send", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("confirm_send_cancel_button")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
private fun AdminEmailHistoryCard(
    index: Int,
    item: AdminEmailLogItem
) {
    val (statusColor, statusBg) = when (item.status) {
        AdminEmailDeliveryStatus.SUBMITTED -> Color(0xFF059669) to Color(0xFFECFDF5)
        AdminEmailDeliveryStatus.UNCERTAIN -> Color(0xFFD97706) to Color(0xFFFFFBEB)
        AdminEmailDeliveryStatus.FAILED -> Color(0xFFDC2626) to Color(0xFFFEF2F2)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("email_log_item_$index")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.recipientName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (item.recipientEmail.isNotBlank()) {
                        Text(
                            text = item.recipientEmail,
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("email_log_status_$index")
                ) {
                    Text(
                        text = item.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Subject: ${item.subject}",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Sent Time",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.timestampFormatted,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (!item.errorDetail.isNullOrBlank() && item.status != AdminEmailDeliveryStatus.SUBMITTED) {
                Text(
                    text = item.errorDetail,
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
