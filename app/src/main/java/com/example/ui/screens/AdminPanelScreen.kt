package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.example.data.Option
import com.example.data.Question
import com.example.data.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    questions: List<Question>,
    onAddQuestion: (Subject, String, String, String, String, String, Option, String) -> Unit,
    onDeleteQuestion: (Int) -> Unit,
    onAddAnnouncement: (String, String, Boolean) -> Unit,
    onAddResource: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Announcements & Resources, 2: Users

    // Add Question Form State
    var selectedSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var qText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOpt by remember { mutableStateOf(Option.A) }
    var explanation by remember { mutableStateOf("") }

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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF312E81),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Questions (${questions.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Posts & PDFs", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Students", fontWeight = FontWeight.Bold) }
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
                    // Posts & Resources Tab
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
