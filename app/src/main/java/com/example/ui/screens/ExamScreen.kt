package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.QuestionPaletteStatus
import com.example.data.UserAnswer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<Int, UserAnswer>,
    timerSeconds: Int,
    isTimerPaused: Boolean,
    bookmarkedQuestionIds: Set<Int> = emptySet(),
    onSelectOption: (Int, Option) -> Unit,
    onToggleMarkForReview: (Int) -> Unit,
    onToggleBookmark: (Question) -> Unit = {},
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onTogglePauseTimer: () -> Unit,
    getQuestionStatus: (Int) -> QuestionPaletteStatus,
    onSubmitExam: () -> Unit
) {
    val context = LocalContext.current
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val currentQuestion = questions.getOrNull(currentIndex)
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    if (currentQuestion == null) return

    val currentAns = userAnswers[currentQuestion.id]
    val selectedOpt = currentAns?.selectedOption
    val isMarked = currentAns?.isMarkedForReview == true

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1B4B)
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (timerSeconds < 300) Color(0xFFEF4444) else Color(0xFF6366F1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formattedTime,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        IconButton(onClick = onTogglePauseTimer) {
                            Icon(
                                imageVector = if (isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause Timer",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Q ${currentIndex + 1}/${questions.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showPaletteSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Question Palette",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = null)
                        Text("Prev")
                    }

                    OutlinedButton(
                        onClick = { onToggleMarkForReview(currentQuestion.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = if (isMarked) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                contentColor = Color(0xFFF59E0B)
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        Icon(
                            imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isMarked) "Reviewing" else "Review")
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = onNext,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("Next")
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Submit Exam", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Subject Badge & Bookmark Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF818CF8).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = currentQuestion.subject.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                val isBookmarked = bookmarkedQuestionIds.contains(currentQuestion.id)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBookmarked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                    border = if (isBookmarked) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                    modifier = Modifier.clickable {
                        onToggleBookmark(currentQuestion)
                        Toast.makeText(
                            context,
                            if (isBookmarked) "Removed from Saved Bookmarks" else "Question Saved to Bookmarks!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Question",
                            tint = if (isBookmarked) Color(0xFFD97706) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isBookmarked) "Bookmarked" else "Bookmark",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBookmarked) Color(0xFFD97706) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Text
            Text(
                text = "Q${currentIndex + 1}. ${currentQuestion.questionText}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options List
            val options = listOf(
                Pair(Option.A, currentQuestion.optionA),
                Pair(Option.B, currentQuestion.optionB),
                Pair(Option.C, currentQuestion.optionC),
                Pair(Option.D, currentQuestion.optionD)
            )

            options.forEach { (opt, text) ->
                val isSelected = selectedOpt == opt

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSelectOption(currentQuestion.id, opt) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6366F1)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = opt.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF6366F1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { showSubmitConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text(text = "Finish & Submit Exam", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Question Palette Bottom Sheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Question Palette",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = Color(0xFF10B981), label = "Answered")
                    LegendItem(color = Color(0xFFEF4444), label = "Not Answered")
                    LegendItem(color = Color(0xFFF59E0B), label = "Review")
                    LegendItem(color = Color.Gray, label = "Not Visited")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        val status = getQuestionStatus(q.id)
                        val statusColor = when (status) {
                            QuestionPaletteStatus.ANSWERED -> Color(0xFF10B981)
                            QuestionPaletteStatus.NOT_ANSWERED -> Color(0xFFEF4444)
                            QuestionPaletteStatus.MARKED_FOR_REVIEW -> Color(0xFFF59E0B)
                            QuestionPaletteStatus.ANSWERED_AND_MARKED -> Color(0xFF8B5CF6)
                            QuestionPaletteStatus.NOT_VISITED -> Color.Gray.copy(alpha = 0.4f)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusColor,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    onJumpToQuestion(index)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showPaletteSheet = false
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showPaletteSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Palette")
                }
            }
        }
    }

    // Submit Confirm Dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = userAnswers.values.count { it.selectedOption != null }
        val unansweredCount = questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit Exam Confirmation") },
            text = {
                Column {
                    Text("Are you sure you want to finish and submit your KCET test?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Answered Questions: $answeredCount")
                    Text("• Unanswered Questions: $unansweredCount")
                    Text("• Remaining Time: $formattedTime")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Submit Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        ) { }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
