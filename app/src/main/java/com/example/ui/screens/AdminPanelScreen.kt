package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.ImportantDateItem
import com.example.data.Option
import com.example.data.Question
import com.example.data.RankVsMarksItem
import com.example.data.ResourceItem
import com.example.data.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    questions: List<Question>,
    resourcesList: List<ResourceItem> = emptyList(),
    importantDates: List<ImportantDateItem> = emptyList(),
    rankVsMarksList: List<RankVsMarksItem> = emptyList(),
    onAddQuestion: (Subject, String, String, String, String, String, Option, String) -> Unit,
    onDeleteQuestion: (Int) -> Unit,
    onAddAnnouncement: (String, String, Boolean) -> Unit,
    onAddResource: (title: String, category: String, yearOrSubject: String, downloadUrl: String, fileSize: String, description: String) -> Unit,
    onDeleteResource: (id: String) -> Unit = {},
    onAddImportantDate: (String, String, String, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteImportantDate: (String) -> Unit = {},
    onAddRankVsMarks: (String, String, String, String, Int, Int) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteRankVsMarks: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: PDF Resources, 2: Dates, 3: Rank vs Marks, 4: Announce, 5: Students

    // Add Question Form State
    var selectedSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var qText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOpt by remember { mutableStateOf(Option.A) }
    var explanation by remember { mutableStateOf("") }

    // PDF Resource Form State
    var pdfTitle by remember { mutableStateOf("") }
    var pdfCategory by remember { mutableStateOf("Syllabus") } // Syllabus, PYQ Papers, Formula Sheets, PDF Notes
    var pdfYearOrSubject by remember { mutableStateOf("Physics & Maths") }
    var pdfUrl by remember { mutableStateOf("") }
    var pdfSize by remember { mutableStateOf("2.5 MB") }
    var pdfDesc by remember { mutableStateOf("") }

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
                    text = { Text("📄 PDF Resources (${resourcesList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Dates (${importantDates.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Rank vs Marks (${rankVsMarksList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Announce", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("Students", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
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
                                    listOf("Syllabus", "PYQ Papers", "Formula Sheets", "PDF Notes").forEach { cat ->
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
                } else if (selectedTab == 2) {
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
                } else if (selectedTab == 2) {
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
                } else if (selectedTab == 3) {
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
