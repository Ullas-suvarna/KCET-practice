package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.FilterChip
import com.example.data.HelplineItem
import com.example.data.ImportantDateItem
import com.example.data.Option
import com.example.data.Question
import com.example.data.RankVsMarksItem
import com.example.data.ResourceItem
import com.example.data.SeniorChatBooking
import com.example.data.Subject
import com.example.data.TestSeriesSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    questions: List<Question>,
    resourcesList: List<ResourceItem> = emptyList(),
    importantDates: List<ImportantDateItem> = emptyList(),
    rankVsMarksList: List<RankVsMarksItem> = emptyList(),
    testSeriesList: List<TestSeriesSet> = emptyList(),
    seniorChatBookings: List<SeniorChatBooking> = emptyList(),
    helplines: List<HelplineItem> = emptyList(),
    onAddQuestion: (Subject, String, String, String, String, String, Option, String) -> Unit,
    onGenerate180QuestionsForSet: (testSetId: String) -> Unit = {},
    onDeleteQuestion: (Int) -> Unit,
    onAddAnnouncement: (String, String, Boolean) -> Unit,
    onAddResource: (title: String, category: String, yearOrSubject: String, downloadUrl: String, fileSize: String, description: String) -> Unit,
    onDeleteResource: (id: String) -> Unit = {},
    onAddImportantDate: (String, String, String, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteImportantDate: (String) -> Unit = {},
    onAddRankVsMarks: (String, String, String, String, Int, Int) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteRankVsMarks: (String) -> Unit = {},
    onAddTestSeriesSet: (title: String, category: String, questionCount: Int, durationMins: Int, isPremium: Boolean, description: String) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteTestSeriesSet: (id: String) -> Unit = {},
    onUpdateBookingStatus: (bookingId: String, status: String, adminMessage: String) -> Unit = { _, _, _ -> },
    onDeleteBooking: (bookingId: String) -> Unit = {},
    onAddHelpline: (title: String, contactValue: String, type: String, description: String, actionUrl: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteHelpline: (id: String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Test Series, 2: Senior Chat, 3: PDF Resources, 4: Dates, 5: Rank vs Marks, 6: Announce, 7: Students, 8: Helplines

    // Helpline Form State
    var helpTitle by remember { mutableStateOf("") }
    var helpContactValue by remember { mutableStateOf("") }
    var helpType by remember { mutableStateOf("EMAIL") }
    var helpDesc by remember { mutableStateOf("") }
    var helpActionUrl by remember { mutableStateOf("") }

    // Add Question Form State
    var selectedSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var qText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOpt by remember { mutableStateOf(Option.A) }
    var explanation by remember { mutableStateOf("") }

    // Test Series Form State
    var tsTitle by remember { mutableStateOf("") }
    var tsCategory by remember { mutableStateOf("Full Mock Tests") }
    var tsCountStr by remember { mutableStateOf("20") }
    var tsDurationStr by remember { mutableStateOf("20") }
    var tsIsPremium by remember { mutableStateOf(true) }
    var tsDescription by remember { mutableStateOf("") }

    // PDF Resource Form State
    var pdfTitle by remember { mutableStateOf("") }
    var pdfCategory by remember { mutableStateOf("Counselling Roadmap") } // Counselling Roadmap, Syllabus, PYQ Papers, Formula Sheets, PDF Notes
    var pdfYearOrSubject by remember { mutableStateOf("KEA Counselling 2026") }
    var pdfUrl by remember { mutableStateOf("") }
    var pdfSize by remember { mutableStateOf("3.8 MB") }
    var pdfDesc by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUrl = uri.toString()
            if (pdfTitle.isBlank()) {
                val pathName = uri.lastPathSegment?.substringAfterLast("/")?.substringAfterLast(":") ?: "Uploaded_KCET_Document.pdf"
                pdfTitle = "KCET Official Counselling Events Roadmap - $pathName"
            }
            pdfCategory = "Counselling Roadmap"
            pdfYearOrSubject = "KEA Counselling 2026"
            pdfSize = "3.5 MB"
            pdfDesc = "Official uploaded KCET Counselling Events Roadmap PDF document for document verification, option entry, and seat reporting."
            Toast.makeText(context, "Local PDF File Selected Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Important Date Form State
    var dateTitle by remember { mutableStateOf("") }
    var dateValue by remember { mutableStateOf("") }
    var dateStatus by remember { mutableStateOf("Upcoming") } // Active Now, Upcoming, Crucial, Completed
    var dateCategory by remember { mutableStateOf("Counselling") } // Exam, Counselling, Application, Results, Admit Card
    var dateDesc by remember { mutableStateOf("") }
    var isHighlighted by remember { mutableStateOf(true) }

    // Rank vs Marks Form State
    var marksRange by remember { mutableStateOf("") }
    var expectedRankRange by remember { mutableStateOf("") }
    var rankYear by remember { mutableStateOf("2025") }
    var rankDesc by remember { mutableStateOf("") }
    var minMarksStr by remember { mutableStateOf("100") }
    var maxMarksStr by remember { mutableStateOf("109") }

    // Announcement Form State
    var annTitle by remember { mutableStateOf("") }
    var annMsg by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1B4B)),
                title = {
                    Text("KCET Admin Management Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF312E81),
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Questions (${questions.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🏆 Test Series (${testSeriesList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("💬 Senior Chat (${seniorChatBookings.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("📄 PDF Resources (${resourcesList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Dates (${importantDates.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("Rank vs Marks (${rankVsMarksList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = { Text("Announce", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 7,
                    onClick = { selectedTab = 7 },
                    text = { Text("Students", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 8,
                    onClick = { selectedTab = 8 },
                    text = { Text("📞 Helplines (${helplines.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // Add Question Form
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("➕ Add New KCET Question", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Subject Radio Row
                                Text("Subject Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Subject.values().forEach { sub ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedSubject == sub,
                                                onClick = { selectedSubject = sub }
                                            )
                                            Text(text = sub.name.take(3), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = qText,
                                    onValueChange = { qText = it },
                                    label = { Text("Question Text") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Correct Answer Option:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Option.values().forEach { opt ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = correctOpt == opt, onClick = { correctOpt = opt })
                                            Text(text = opt.name, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = explanation,
                                    onValueChange = { explanation = it },
                                    label = { Text("Solution Explanation") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (qText.isBlank() || optA.isBlank() || optB.isBlank()) {
                                            Toast.makeText(context, "Please fill question and options", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        onAddQuestion(selectedSubject, qText, optA, optB, optC, optD, correctOpt, explanation)
                                        Toast.makeText(context, "Question Added to CBT Exam Bank!", Toast.LENGTH_SHORT).show()
                                        qText = ""
                                        optA = ""
                                        optB = ""
                                        optC = ""
                                        optD = ""
                                        explanation = ""
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Question to CBT Exam", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Existing Question Bank (${questions.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(questions) { q ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF818CF8).copy(0.2f)
                                    ) {
                                        Text(
                                            text = q.subject.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF6366F1),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Q${q.id}. ${q.questionText}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Ans: ${q.correctOption.name}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(onClick = {
                                    onDeleteQuestion(q.id)
                                    Toast.makeText(context, "Question Deleted", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // Test Series Sets Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🏆 Add Premium / Free Test Series Question Set", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = tsTitle,
                                    onValueChange = { tsTitle = it },
                                    label = { Text("Test Series Title (e.g. KCET Grand Mock Test #1)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tsCategory,
                                        onValueChange = { tsCategory = it },
                                        label = { Text("Category (Full Mock / Subject / Chapter)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = tsCountStr,
                                        onValueChange = { tsCountStr = it },
                                        label = { Text("Questions Count") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tsDurationStr,
                                        onValueChange = { tsDurationStr = it },
                                        label = { Text("Duration (Mins)") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { tsIsPremium = !tsIsPremium },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (tsIsPremium) Color(0xFFFEF3C7) else Color(0xFFE2E8F0),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (tsIsPremium) Color(0xFFD97706) else Color(0xFF94A3B8))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Checkbox(checked = tsIsPremium, onCheckedChange = { tsIsPremium = it })
                                            Text(
                                                text = if (tsIsPremium) "👑 Premium Set" else "🆓 Free Set",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (tsIsPremium) Color(0xFF92400E) else Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = tsDescription,
                                    onValueChange = { tsDescription = it },
                                    label = { Text("Description & Instructions") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (tsTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter Test Series Title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val qCount = tsCountStr.toIntOrNull() ?: 20
                                        val duration = tsDurationStr.toIntOrNull() ?: 20
                                        onAddTestSeriesSet(tsTitle, tsCategory, qCount, duration, tsIsPremium, tsDescription)
                                        Toast.makeText(context, "Test Series Set Added to Firebase!", Toast.LENGTH_SHORT).show()
                                        tsTitle = ""
                                        tsDescription = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Text("Save Test Series Set to Firebase")
                                }
                            }
                        }
                    }

                    item {
                        Text("Live Test Series Question Sets in Firebase (${testSeriesList.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(testSeriesList) { set ->
                        val linkedQuestions = questions.filter { it.testSetId == set.id }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = set.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (set.isPremium) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = if (set.isPremium) "👑 Premium" else "🆓 Free",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (set.isPremium) Color(0xFF92400E) else Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${set.category} • Target ${set.questionCount} Questions • ${set.durationMins} mins",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF4F46E5)
                                        )
                                        Text(
                                            text = "🔥 Stored in Firebase: ${linkedQuestions.size} Questions (Options & Explanations)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (linkedQuestions.isNotEmpty()) Color(0xFF059669) else Color(0xFFD97706)
                                        )
                                        if (set.description.isNotBlank()) {
                                            Text(text = set.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }

                                    IconButton(onClick = {
                                        onDeleteTestSeriesSet(set.id)
                                        Toast.makeText(context, "Test Series Set Removed from Firebase", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        onGenerate180QuestionsForSet(set.id)
                                        Toast.makeText(context, "⚡ 180 Questions (Physics, Chemistry, Maths) auto-generated & saved to Firebase for ${set.title}!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ Auto-Generate & Save 180 Qs (PCM) to Firebase", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // Senior Live Chat Requests Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF312E81))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("💬 1-on-1 Senior Mentorship Live Chat Bookings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Approve student time slots, send messages, suggest alternate times, or cancel bookings.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }

                    if (seniorChatBookings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No live chat booking requests found yet.", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(seniorChatBookings) { booking ->
                            var adminNoteText by remember { mutableStateOf(booking.adminMessage) }

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
                                            Text(
                                                text = booking.userName.ifBlank { "Student Request" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (booking.userEmail.isNotBlank()) {
                                                Text(text = booking.userEmail, fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        // Status Chip
                                        val statusColor = when (booking.status) {
                                            "Confirmed" -> Color(0xFF16A34A)
                                            "Waiting for Senior Confirmation", "Pending" -> Color(0xFFD97706)
                                            "Reschedule Needed", "Choose Other Time" -> Color(0xFFEA580C)
                                            else -> Color(0xFFDC2626)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = statusColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = booking.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Requested Date", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(booking.selectedDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Requested Time", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(booking.selectedTime, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Amount Paid", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text("₹${booking.amountPaid}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Admin Response Message Input
                                    OutlinedTextField(
                                        value = adminNoteText,
                                        onValueChange = { adminNoteText = it },
                                        label = { Text("Admin Message / Time Availability Note") },
                                        placeholder = { Text("e.g. Confirmed! DM @ULLAS_U_SUVARNA at scheduled time. / Slot busy, please pick 8 PM slot.") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action Buttons Row
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    onUpdateBookingStatus(booking.bookingId, "Confirmed", adminNoteText)
                                                    Toast.makeText(context, "Slot Confirmed for Student!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("✅ Confirm Slot", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    onUpdateBookingStatus(booking.bookingId, "Reschedule Needed", adminNoteText)
                                                    Toast.makeText(context, "Reschedule Note Sent to Student!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("💬 Suggest Other Time", fontSize = 11.sp)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    onUpdateBookingStatus(booking.bookingId, "Declined", adminNoteText)
                                                    Toast.makeText(context, "Booking Declined", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("❌ Decline", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onDeleteBooking(booking.bookingId)
                                                    Toast.makeText(context, "Booking Removed", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("🗑️ Delete Request", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 3) {
                    // PDF Resources Management Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48)
                                    )
                                    Text("Add New PDF Resource to Database", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = pdfTitle,
                                    onValueChange = { pdfTitle = it },
                                    label = { Text("PDF Resource Title") },
                                    placeholder = { Text("e.g. KCET Physics Solved Question Bank") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Counselling Roadmap", "Syllabus", "PYQ Papers", "Formula Sheets", "PDF Notes").forEach { cat ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (pdfCategory == cat) Color(0xFFE11D48) else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable { pdfCategory = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pdfCategory == cat) Color.White else Color(0xFF334155),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48))
                                    ) {
                                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("📁 Upload / Pick PDF File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            pdfTitle = "KCET Official Counselling Events Roadmap & Guidelines 2026"
                                            pdfCategory = "Counselling Roadmap"
                                            pdfYearOrSubject = "KEA Counselling 2026"
                                            pdfUrl = "https://cetonline.karnataka.gov.in/kea/kcet2026_counselling_roadmap.pdf"
                                            pdfSize = "3.8 MB"
                                            pdfDesc = "Complete KEA step-by-step PDF roadmap covering Document Verification, Secret Key, Mock Option Entry, Seat Matrix, Choice 1-4 Rules, and Final College Reporting."
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                                    ) {
                                        Text("🗺️ Preset Official Roadmap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = pdfYearOrSubject,
                                        onValueChange = { pdfYearOrSubject = it },
                                        label = { Text("Subject / Year") },
                                        placeholder = { Text("Physics & Math") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pdfSize,
                                        onValueChange = { pdfSize = it },
                                        label = { Text("File Size") },
                                        placeholder = { Text("3.2 MB") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pdfUrl,
                                    onValueChange = { pdfUrl = it },
                                    label = { Text("PDF Download Link / URL") },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("https://cetonline.karnataka.gov.in/kea/kcet2026.pdf") }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { pdfUrl = "https://cetonline.karnataka.gov.in/kea/kcet2026_syllabus.pdf" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Use Sample KCET PDF URL", fontSize = 10.sp, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pdfDesc,
                                    onValueChange = { pdfDesc = it },
                                    label = { Text("Description & Notes for Students") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (pdfTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter PDF resource title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        onAddResource(pdfTitle, pdfCategory, pdfYearOrSubject, pdfUrl, pdfSize, pdfDesc)
                                        Toast.makeText(context, "PDF Resource Added & Saved to Database!", Toast.LENGTH_SHORT).show()
                                        pdfTitle = ""
                                        pdfUrl = ""
                                        pdfDesc = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                                ) {
                                    Text("➕ Save PDF Resource to Database", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Existing PDF Resources in Database (${resourcesList.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(resourcesList) { res ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFE4E6),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = res.category.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "• ${res.fileSize}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = res.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Subject/Year: ${res.yearOrSubject}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                                    if (res.description.isNotBlank()) {
                                        Text(text = res.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }

                                IconButton(onClick = {
                                    onDeleteResource(res.id)
                                    Toast.makeText(context, "PDF Removed from Database", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete PDF", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                } else if (selectedTab == 4) {
                    // Schedule & Important Dates Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🗓️ Add / Update Firebase Schedule Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(value = dateTitle, onValueChange = { dateTitle = it }, label = { Text("Event Title (e.g. Document Verification)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateValue, onValueChange = { dateValue = it }, label = { Text("Date / Timeline (e.g. June 05 - June 20, 2026)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateStatus, onValueChange = { dateStatus = it }, label = { Text("Status (Active Now, Upcoming, Crucial, Completed)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateDesc, onValueChange = { dateDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (dateTitle.isBlank() || dateValue.isBlank()) {
                                            Toast.makeText(context, "Please fill title and date string", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        onAddImportantDate(dateTitle, dateValue, dateStatus, dateDesc, dateCategory, isHighlighted)
                                        Toast.makeText(context, "Saved to Firebase Firestore!", Toast.LENGTH_SHORT).show()
                                        dateTitle = ""
                                        dateValue = ""
                                        dateDesc = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Save Date to Firebase")
                                }
                            }
                        }
                    }

                    item {
                        Text("Live Schedule Items in Firebase (${importantDates.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(importantDates) { dateItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (dateItem.status) {
                                            "Active Now" -> Color(0xFFDCFCE7)
                                            "Completed" -> Color(0xFFF1F5F9)
                                            else -> Color(0xFFEEF2FF)
                                        }
                                    ) {
                                        Text(
                                            text = dateItem.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (dateItem.status == "Active Now") Color(0xFF15803D) else Color(0xFF4338CA),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = dateItem.eventTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Date: ${dateItem.dateOrDeadline}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                                    Text(text = dateItem.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                IconButton(onClick = {
                                    onDeleteImportantDate(dateItem.id)
                                    Toast.makeText(context, "Date Item Removed from Firebase", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                } else if (selectedTab == 5) {
                    // KCET Rank vs Marks Tab (Firebase)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📊 Add / Edit KCET Marks vs Rank Range (Firebase)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = marksRange,
                                        onValueChange = { marksRange = it },
                                        label = { Text("Marks Range (e.g. 150 - 159)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = expectedRankRange,
                                        onValueChange = { expectedRankRange = it },
                                        label = { Text("Expected Rank (e.g. 251 - 800)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = minMarksStr,
                                        onValueChange = { minMarksStr = it },
                                        label = { Text("Min Marks (e.g. 150)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = maxMarksStr,
                                        onValueChange = { maxMarksStr = it },
                                        label = { Text("Max Marks (e.g. 159)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = rankYear,
                                    onValueChange = { rankYear = it },
                                    label = { Text("Data Year (e.g. 2025)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = rankDesc,
                                    onValueChange = { rankDesc = it },
                                    label = { Text("College Scope / Recommendation Advice") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (marksRange.isBlank() || expectedRankRange.isBlank()) {
                                            Toast.makeText(context, "Please enter marks range & expected rank", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val minM = minMarksStr.toIntOrNull() ?: 0
                                        val maxM = maxMarksStr.toIntOrNull() ?: 180
                                        onAddRankVsMarks(marksRange, expectedRankRange, rankYear, rankDesc, minM, maxM)
                                        Toast.makeText(context, "Rank vs Marks Range Saved to Firebase!", Toast.LENGTH_SHORT).show()
                                        marksRange = ""
                                        expectedRankRange = ""
                                        rankDesc = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Save Rank Range to Firebase")
                                }
                            }
                        }
                    }

                    item {
                        Text("Live Rank vs Marks Ranges in Firebase (${rankVsMarksList.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(rankVsMarksList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Marks: ${item.marksRange}  ➔  Rank: ${item.expectedRankRange}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF1E1B4B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Year ${item.year} • Min: ${item.minMarks} Max: ${item.maxMarks}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF4F46E5),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (item.description.isNotBlank()) {
                                        Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }

                                IconButton(onClick = {
                                    onDeleteRankVsMarks(item.id)
                                    Toast.makeText(context, "Item Removed from Firebase", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                } else if (selectedTab == 6) {
                    // Announcements Tab

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📢 Post Announcement Notification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(value = annTitle, onValueChange = { annTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = annMsg, onValueChange = { annMsg = it }, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (annTitle.isBlank()) return@Button
                                        onAddAnnouncement(annTitle, annMsg, true)
                                        Toast.makeText(context, "Announcement Broadcasted!", Toast.LENGTH_SHORT).show()
                                        annTitle = ""
                                        annMsg = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Broadcast Announcement")
                                }
                            }
                        }
                    }
                } else if (selectedTab == 8) {
                    // Helplines Management Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📞 Add New Helpline Contact",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add student support emails, Instagram handles (@ULLAS U SUVARNA), phone numbers, or WhatsApp lines.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Presets Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            helpTitle = "Official Helpline Email"
                                            helpContactValue = "KcetGenZservice@gmail.com"
                                            helpType = "EMAIL"
                                            helpDesc = "Contact official KCET Gen Z support for queries & account help."
                                            helpActionUrl = "mailto:KcetGenZservice@gmail.com"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("📧 Email Preset", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            helpTitle = "Official Instagram"
                                            helpContactValue = "@ULLAS U SUVARNA"
                                            helpType = "INSTAGRAM"
                                            helpDesc = "Follow & DM @ULLAS U SUVARNA on Instagram for live KCET updates."
                                            helpActionUrl = "https://instagram.com/ullassuvarna65"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("📸 Insta Preset", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = helpTitle,
                                    onValueChange = { helpTitle = it },
                                    label = { Text("Helpline Title (e.g. Email Helpline / Instagram)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = helpContactValue,
                                    onValueChange = { helpContactValue = it },
                                    label = { Text("Contact Value (e.g. KcetGenZservice@gmail.com / @ULLAS U SUVARNA)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Helpline Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("EMAIL", "INSTAGRAM", "PHONE", "WHATSAPP").forEach { type ->
                                        FilterChip(
                                            selected = helpType == type,
                                            onClick = {
                                                helpType = type
                                                if (type == "EMAIL" && helpActionUrl.isBlank()) {
                                                    helpActionUrl = "mailto:${helpContactValue.ifBlank { "KcetGenZservice@gmail.com" }}"
                                                }
                                            },
                                            label = { Text(type, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = helpDesc,
                                    onValueChange = { helpDesc = it },
                                    label = { Text("Short Description / Instructions") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = helpActionUrl,
                                    onValueChange = { helpActionUrl = it },
                                    label = { Text("Action Link / URL (e.g. mailto: or https://instagram.com/...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (helpTitle.isNotBlank() && helpContactValue.isNotBlank()) {
                                            onAddHelpline(
                                                helpTitle.trim(),
                                                helpContactValue.trim(),
                                                helpType,
                                                helpDesc.trim(),
                                                helpActionUrl.trim()
                                            )
                                            Toast.makeText(context, "Helpline added & synced to Firebase!", Toast.LENGTH_SHORT).show()
                                            helpTitle = ""
                                            helpContactValue = ""
                                            helpDesc = ""
                                            helpActionUrl = ""
                                        } else {
                                            Toast.makeText(context, "Please fill Title and Contact Value!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Helpline Contact", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Active Helplines (${helplines.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    if (helplines.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Text("No custom helplines active.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                            }
                        }
                    } else {
                        items(helplines, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (item.type) {
                                                    "EMAIL" -> Color(0xFFEEF2FF)
                                                    "INSTAGRAM" -> Color(0xFFFDF2F8)
                                                    "WHATSAPP" -> Color(0xFFF0FDF4)
                                                    else -> Color(0xFFFEF3C7)
                                                }
                                            ) {
                                                Text(
                                                    text = item.type,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (item.type) {
                                                        "EMAIL" -> Color(0xFF4F46E5)
                                                        "INSTAGRAM" -> Color(0xFFDB2777)
                                                        "WHATSAPP" -> Color(0xFF16A34A)
                                                        else -> Color(0xFFD97706)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Contact: ${item.contactValue}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        if (item.description.isNotBlank()) {
                                            Text(text = item.description, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteHelpline(item.id)
                                            Toast.makeText(context, "Helpline deleted permanently!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Helpline", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Registered Students Tab
                    item {
                        Text("Registered Students & Test Analytics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    val dummyStudents = listOf(
                        Pair("Karthik Gowda", "karthik.kcet2026@gmail.com | Rank Goal: Under 500"),
                        Pair("Ananya Rao", "ananya.rao@gmail.com | Rank Goal: Under 1000"),
                        Pair("Praveen Kumar", "praveen.k@gmail.com | Rank Goal: Under 2000"),
                        Pair("Sneha Patil", "sneha.patil@gmail.com | Rank Goal: Under 150")
                    )

                    items(dummyStudents) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = student.first, fontWeight = FontWeight.Bold)
                                    Text(text = student.second, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
