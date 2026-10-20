package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.PendingExamInfo

@Composable
fun ExamGuidelinesDialog(
    pendingExamInfo: PendingExamInfo,
    onDismiss: () -> Unit,
    onConfirmStartExam: (cetNo: String, versionCode: String, serialNo: String, name: String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Guidelines, 2: Offline OMR Details
    var isAgreedGuidelines by remember { mutableStateOf(false) }

    // Offline Candidate Hall Ticket & OMR Details
    var candidateName by remember { mutableStateOf("KCET ASPIRANT") }
    var cetNumber by remember { mutableStateOf("261KCET892") }
    var selectedVersion by remember { mutableStateOf("A1") }
    var bookletSerial by remember { mutableStateOf("781045") }
    var isAgreedOmrBubble by remember { mutableStateOf(false) }

    val versionCodes = listOf("A1", "A2", "B1", "B2", "C1", "C2", "D1", "D2")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Title with Shield / Hall Ticket Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (step == 1) Color(0xFF1E1B4B) else Color(0xFF065F46), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (step == 1) Icons.Default.Security else Icons.Default.Badge,
                            contentDescription = "Header Icon",
                            tint = if (step == 1) Color(0xFF818CF8) else Color(0xFF34D399),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (step == 1) Color(0xFFEEF2FF) else Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = if (step == 1) "STEP 1/2: GUIDELINES & SECURITY" else "STEP 2/2: OFFLINE KCET OMR FILLING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (step == 1) Color(0xFF4338CA) else Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (step == 1) pendingExamInfo.title else "Fill Offline KCET OMR Details",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    // STEP 1: GUIDELINES & LOCKDOWN RULES
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Key Exam Metrics Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricItem(
                                icon = Icons.Default.Timer,
                                label = "Duration",
                                value = "${pendingExamInfo.durationMins} Mins",
                                tint = Color(0xFF2563EB)
                            )
                            MetricItem(
                                icon = Icons.Default.Assignment,
                                label = "Questions",
                                value = "${pendingExamInfo.questionCount} Qs",
                                tint = Color(0xFF7C3AED)
                            )
                            MetricItem(
                                icon = Icons.Default.CheckCircle,
                                label = "Total Marks",
                                value = "${pendingExamInfo.totalMarks} Marks",
                                tint = Color(0xFF059669)
                            )
                        }

                        // Section 1: Standard Examination Instructions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFF3E8FF))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📋 Official KCET Examination Guidelines",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B21A8)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InstructionRow("1.", "Marking Scheme: +1 Mark for correct answer. 0 Negative marking for incorrect ones.")
                                InstructionRow("2.", "Continuous Timer: Once started, the timer runs NON-STOP. Time CANNOT be paused (same as real hall exam).")
                                InstructionRow("3.", "OMR Filling Step: Next, you will fill your CET Reg No, Booklet Version Code & Serial No.")
                                InstructionRow("4.", "Allowed Actions: Only Next, Prev, Bookmark, Palette jump, and Submit are permitted.")
                            }
                        }

                        // Section 2: Strict Anti-Cheating & Lockdown Security Rules
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🔒 Strict CBT Lock & Anti-Cheating Rules",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                SecurityRuleRow("🚫", "No Text Copying", "Question and options text selection/copying is strictly blocked.")
                                SecurityRuleRow("🔒", "Screen Lock Enabled", "Screenshots and screen recording are disabled during test execution.")
                                SecurityRuleRow("⏱️", "No Time Stopping", "Pause button is disabled in strict real exam mode.")
                                SecurityRuleRow("⚠️", "App Switching Alert", "Leaving the test interface logs a security violation.")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Agreement Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isAgreedGuidelines) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAgreedGuidelines,
                            onCheckedChange = { isAgreedGuidelines = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF059669))
                        )
                        Text(
                            text = "I have read all guidelines and agree to enter strict offline KCET examination mode.",
                            fontSize = 12.sp,
                            color = if (isAgreedGuidelines) Color(0xFF065F46) else Color(0xFF334155),
                            fontWeight = if (isAgreedGuidelines) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Row (Proceed to OMR filling)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }

                        Button(
                            onClick = { step = 2 },
                            enabled = isAgreedGuidelines,
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E1B4B),
                                disabledContainerColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            Text(
                                text = "Fill OMR Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next Step",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                } else {
                    // STEP 2: OFFLINE KCET OMR / CANDIDATE FILLING FORM
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "OMR Filling",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fill your Hall Ticket & OMR Booklet information exactly as in real offline KCET exam center.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Candidate Name Field
                        OutlinedTextField(
                            value = candidateName,
                            onValueChange = { candidateName = it },
                            label = { Text("Candidate Full Name") },
                            placeholder = { Text("e.g. ULLAS SUVARNA") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF4338CA)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4338CA),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // CET Application / Admission Ticket Number
                        OutlinedTextField(
                            value = cetNumber,
                            onValueChange = { cetNumber = it },
                            label = { Text("CET Admission Ticket No.") },
                            placeholder = { Text("e.g. 261KCET892") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF2563EB)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Booklet Version Code Selector
                        Column {
                            Text(
                                text = "Select Question Booklet Version Code",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(versionCodes) { ver ->
                                    val selected = ver == selectedVersion
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selected) Color(0xFF1E1B4B) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (selected) Color(0xFF1E1B4B) else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { selectedVersion = ver }
                                    ) {
                                        Text(
                                            text = "Version $ver",
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Question Booklet Serial Number Field
                        OutlinedTextField(
                            value = bookletSerial,
                            onValueChange = { bookletSerial = it },
                            label = { Text("Question Booklet Serial No.") },
                            placeholder = { Text("6-digit serial (e.g. 781045)") },
                            leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF059669)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF059669),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // OMR Bubble Training Agreement
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isAgreedOmrBubble) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAgreedOmrBubble,
                                onCheckedChange = { isAgreedOmrBubble = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF059669))
                            )
                            Text(
                                text = "I certify that I am shading OMR bubbles carefully. Timer runs NON-STOP and screen will lock into KCET mode.",
                                fontSize = 11.sp,
                                color = if (isAgreedOmrBubble) Color(0xFF065F46) else Color(0xFF475569),
                                fontWeight = if (isAgreedOmrBubble) FontWeight.Bold else FontWeight.Normal,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Row for Step 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back", color = Color(0xFF64748B))
                        }

                        Button(
                            onClick = {
                                onConfirmStartExam(cetNumber, selectedVersion, bookletSerial, candidateName)
                            },
                            enabled = isAgreedOmrBubble && cetNumber.isNotBlank(),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF065F46),
                                disabledContainerColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Strict Exam",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Submit & Begin Exam",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
private fun InstructionRow(num: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = num,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF7E22CE),
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF4C1D95),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun SecurityRuleRow(iconSymbol: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = iconSymbol, fontSize = 13.sp, modifier = Modifier.width(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7F1D1D))
            Text(text = desc, fontSize = 11.sp, color = Color(0xFF991B1B), lineHeight = 15.sp)
        }
    }
}
