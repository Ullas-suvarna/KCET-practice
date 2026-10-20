package com.example.ui.screens

import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Close
import com.example.data.Option
import com.example.data.Question
import com.example.data.QuestionPaletteStatus
import com.example.data.Subject
import com.example.data.UserAnswer
import kotlinx.coroutines.launch

enum class ExamViewMode {
    PAPER, OMR, DUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<Int, UserAnswer>,
    timerSeconds: Int,
    isTimerPaused: Boolean,
    bookmarkedQuestionIds: Set<Int> = emptySet(),
    candidateCetNumber: String = "261KCET892",
    candidateBookletVersion: String = "A1",
    candidateBookletSerial: String = "781045",
    candidateName: String = "KCET ASPIRANT",
    isStrictRealExam: Boolean = true,
    onSelectOption: (Int, Option) -> Boolean,
    onToggleMarkForReview: (Int) -> Unit,
    onToggleBookmark: (Question) -> Unit = {},
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onTogglePauseTimer: () -> Unit,
    getQuestionStatus: (Int) -> QuestionPaletteStatus,
    onUpdateCandidateDetails: (name: String, cetNo: String, version: String, serial: String) -> Unit = { _, _, _, _ -> },
    onSubmitExam: () -> Unit,
    onQuitExam: () -> Unit = {},
    isSubmitting: Boolean = false,
    is1v1Challenge: Boolean = false
) {
    val context = LocalContext.current
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var paletteJumpInputText by remember { mutableStateOf("") }
    var selectedPaletteSubjectFilter by remember { mutableStateOf<Subject?>(null) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showCheatingWarningDialog by remember { mutableStateOf(false) }
    var showEditOmrHeaderDialog by remember { mutableStateOf(false) }
    var lockedOmrQuestionInfo by remember { mutableStateOf<Pair<Int, Option>?>(null) }
    var cheatingViolationsCount by remember { mutableStateOf(0) }

    val availableSubjects = remember(questions) { questions.map { it.subject }.distinct() }

    // Default to DUAL View (Question Paper + Authentic KCET OMR Sheet) so OMR is front & center
    var examViewMode by remember { mutableStateOf(ExamViewMode.DUAL) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Anti-Cheating: Monitor app backgrounding / switching (Only active for Full Mock Tests)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (isStrictRealExam && (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP)) {
                cheatingViolationsCount++
                showCheatingWarningDialog = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back button press during active exam
    BackHandler {
        showSubmitConfirmDialog = true
    }

    val currentQuestion = questions.getOrNull(currentIndex)
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    if (currentQuestion == null) return

    val selectedOpt = userAnswers[currentQuestion.id]?.selectedOption
    val isMarked = userAnswers[currentQuestion.id]?.isMarkedForReview == true

    // Safe option selection handler enforcing Real KCET OMR pen-shading rules for Full Mock, or normal tick for Practice
    val handleOptionSelect: (Int, Option) -> Unit = { qId, opt ->
        val success = onSelectOption(qId, opt)
        if (!success && isStrictRealExam) {
            val existingOpt = userAnswers[qId]?.selectedOption
            if (existingOpt != null) {
                lockedOmrQuestionInfo = Pair(qId, existingOpt)
            }
        } else if (success) {
            val isSelectedNow = userAnswers[qId]?.selectedOption == opt
            if (isStrictRealExam) {
                Toast.makeText(context, "✒️ Option ${opt.name} Shaded on KCET OMR Answer Sheet!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    if (isSelectedNow) "✓ Option ${opt.name} Selected" else "Option ${opt.name} Unticked",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
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

                            IconButton(
                                onClick = {
                                    if (isStrictRealExam) {
                                        Toast.makeText(context, "⏱️ Non-Stop Timer Active! Time cannot be paused in Real KCET Exam Mode.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onTogglePauseTimer()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isStrictRealExam) Icons.Default.Lock else if (isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = "Timer Control",
                                    tint = if (isStrictRealExam) Color(0xFFFBBF24) else Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF312E81)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isStrictRealExam) Icons.Default.Lock else Icons.Default.CheckCircle,
                                        contentDescription = "Mode",
                                        tint = if (isStrictRealExam) Color(0xFFF87171) else Color(0xFF34D399),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isStrictRealExam) "STRICT MOCK" else "PRACTICE",
                                        color = if (isStrictRealExam) Color(0xFFF87171) else Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF312E81),
                                modifier = Modifier.clickable { showJumpDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Jump",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Q ${currentIndex + 1}/${questions.size}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showJumpDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Jump Question or Subject",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        IconButton(onClick = { showPaletteSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Question Palette",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showSubmitConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quit Exam",
                                tint = Color(0xFFF87171)
                            )
                        }
                    }
                )

                // Candidate OMR Hall Ticket Info Bar (Clickable to Edit)
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditOmrHeaderDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = candidateName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Text(
                                    text = "Ticket: $candidateCetNumber • Serial: $candidateBookletSerial",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF4338CA),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "VER: $candidateBookletVersion",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF059669),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Edit OMR Info",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // View Mode Switcher Bar (Paper View | Real OMR Sheet | Dual View)
                Surface(
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isPaper = examViewMode == ExamViewMode.PAPER
                        val isOmr = examViewMode == ExamViewMode.OMR
                        val isDual = examViewMode == ExamViewMode.DUAL

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPaper) Color(0xFF38BDF8) else Color(0xFF334155),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { examViewMode = ExamViewMode.PAPER }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📄 Question Paper",
                                    fontSize = 11.sp,
                                    fontWeight = if (isPaper) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPaper) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOmr) Color(0xFF10B981) else Color(0xFF334155),
                            modifier = Modifier
                                .weight(1.1f)
                                .clickable { examViewMode = ExamViewMode.OMR }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📝 KCET OMR Sheet",
                                    fontSize = 11.sp,
                                    fontWeight = if (isOmr) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isOmr) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDual) Color(0xFF8B5CF6) else Color(0xFF334155),
                            modifier = Modifier
                                .weight(0.9f)
                                .clickable { examViewMode = ExamViewMode.DUAL }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📑 Split View",
                                    fontSize = 11.sp,
                                    fontWeight = if (isDual) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDual) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                // Fast Subject Navigation Bar (Physics | Chemistry | Math | Biology)
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text(
                                text = "SUBJECTS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                        items(availableSubjects) { subj ->
                            val isCurrentSubject = currentQuestion.subject == subj
                            val firstIdx = questions.indexOfFirst { it.subject == subj }
                            val subCount = questions.count { it.subject == subj }
                            val solvedCount = questions.filter { it.subject == subj }.count { userAnswers[it.id]?.selectedOption != null }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrentSubject) Color(0xFF6366F1) else Color(0xFF1E293B),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrentSubject) Color(0xFF818CF8) else Color(0xFF334155)
                                ),
                                modifier = Modifier.clickable {
                                    if (firstIdx >= 0) {
                                        onJumpToQuestion(firstIdx)
                                        Toast.makeText(context, "🎯 Jumped to ${subj.name} (Q #${firstIdx + 1})", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = subj.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrentSubject) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrentSubject) Color.White else Color(0xFFCBD5E1)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCurrentSubject) Color(0xFF312E81) else Color(0xFF0F172A)
                                    ) {
                                        Text(
                                            text = "$solvedCount/$subCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentSubject) Color(0xFFA5B4FC) else Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                        onClick = {
                            onToggleMarkForReview(currentQuestion.id)
                            if (isMarked) {
                                Toast.makeText(context, "Question #${currentIndex + 1} Unmarked for Review", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "🔖 Question #${currentIndex + 1} Marked for Review! Check Question Palette.", Toast.LENGTH_SHORT).show()
                            }
                        },
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (is1v1Challenge) {
                            OutlinedButton(
                                onClick = { showSubmitConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                            ) {
                                Text("Exit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { showSubmitConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Submit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
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
                        } else if (!is1v1Challenge) {
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTimerPaused && !isStrictRealExam) {
                Surface(
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Paused",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "⏸️ TEST PAUSED — Timer is stopped",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }

                        Button(
                            onClick = onTogglePauseTimer,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (examViewMode) {
                    ExamViewMode.PAPER -> {
                        SingleQuestionPaperView(
                            currentQuestion = currentQuestion,
                            currentIndex = currentIndex,
                            totalQuestions = questions.size,
                            selectedOpt = selectedOpt,
                            isBookmarked = bookmarkedQuestionIds.contains(currentQuestion.id),
                            isStrictRealExam = isStrictRealExam,
                            onSelectOption = { opt -> handleOptionSelect(currentQuestion.id, opt) },
                            onToggleBookmark = { onToggleBookmark(currentQuestion) },
                            onSubmitExamClick = { showSubmitConfirmDialog = true }
                        )
                    }

                    ExamViewMode.OMR -> {
                        RealKcetOmrSheetView(
                            questions = questions,
                            currentIndex = currentIndex,
                            userAnswers = userAnswers,
                            candidateName = candidateName,
                            candidateCetNumber = candidateCetNumber,
                            candidateBookletVersion = candidateBookletVersion,
                            candidateBookletSerial = candidateBookletSerial,
                            onSelectOption = handleOptionSelect,
                            onJumpToQuestion = onJumpToQuestion,
                            onEditHeaderClick = { showEditOmrHeaderDialog = true }
                        )
                    }

                    ExamViewMode.DUAL -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1.1f)) {
                                SingleQuestionPaperView(
                                    currentQuestion = currentQuestion,
                                    currentIndex = currentIndex,
                                    totalQuestions = questions.size,
                                    selectedOpt = selectedOpt,
                                    isBookmarked = bookmarkedQuestionIds.contains(currentQuestion.id),
                                    isStrictRealExam = isStrictRealExam,
                                    onSelectOption = { opt -> handleOptionSelect(currentQuestion.id, opt) },
                                    onToggleBookmark = { onToggleBookmark(currentQuestion) },
                                    onSubmitExamClick = { showSubmitConfirmDialog = true }
                                )
                            }

                        HorizontalDivider(thickness = 2.dp, color = Color(0xFF334155))

                        Box(modifier = Modifier.weight(1.0f)) {
                            RealKcetOmrSheetView(
                                questions = questions,
                                currentIndex = currentIndex,
                                userAnswers = userAnswers,
                                candidateName = candidateName,
                                candidateCetNumber = candidateCetNumber,
                                candidateBookletVersion = candidateBookletVersion,
                                candidateBookletSerial = candidateBookletSerial,
                                onSelectOption = handleOptionSelect,
                                onJumpToQuestion = onJumpToQuestion,
                                onEditHeaderClick = { showEditOmrHeaderDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Edit OMR Candidate Details Dialog
    if (showEditOmrHeaderDialog) {
        EditOmrHeaderDialog(
            currentName = candidateName,
            currentCetNo = candidateCetNumber,
            currentVersion = candidateBookletVersion,
            currentSerial = candidateBookletSerial,
            onDismiss = { showEditOmrHeaderDialog = false },
            onSave = { name, cet, ver, serial ->
                showEditOmrHeaderDialog = false
                onUpdateCandidateDetails(name, cet, ver, serial)
                Toast.makeText(context, "✅ Candidate Hall Ticket & OMR Details Updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Direct Question Number & Subject Jump Dialog
    if (showJumpDialog) {
        var inputNumText by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Jump",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Jump to Question or Subject",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter any question number (1 to ${questions.size}):",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputNumText,
                            onValueChange = {
                                inputNumText = it.filter { char -> char.isDigit() }
                                errorMessage = null
                            },
                            placeholder = { Text("e.g. 25") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    val num = inputNumText.toIntOrNull()
                                    if (num != null && num in 1..questions.size) {
                                        val targetIndex = num - 1
                                        val qSubject = questions[targetIndex].subject.name
                                        onJumpToQuestion(targetIndex)
                                        showJumpDialog = false
                                        Toast.makeText(context, "🚀 Jumped to Question #$num [$qSubject]", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = "Please enter a valid number between 1 and ${questions.size}"
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val num = inputNumText.toIntOrNull()
                                if (num != null && num in 1..questions.size) {
                                    val targetIndex = num - 1
                                    val qSubject = questions[targetIndex].subject.name
                                    onJumpToQuestion(targetIndex)
                                    showJumpDialog = false
                                    Toast.makeText(context, "🚀 Jumped to Question #$num [$qSubject]", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = "Invalid Q# (1 - ${questions.size})"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("Jump", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Jump Directly by Subject Section:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableSubjects.forEach { subj ->
                            val firstIdx = questions.indexOfFirst { it.subject == subj }
                            val lastIdx = questions.indexOfLast { it.subject == subj }
                            val subCount = questions.count { it.subject == subj }
                            val ansSubCount = questions.filter { it.subject == subj }.count { userAnswers[it.id]?.selectedOption != null }

                            if (firstIdx >= 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (currentQuestion.subject == subj) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, if (currentQuestion.subject == subj) Color(0xFF6366F1) else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onJumpToQuestion(firstIdx)
                                            showJumpDialog = false
                                            Toast.makeText(context, "🎯 Jumped to ${subj.name} Section (Q #${firstIdx + 1})", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = subj.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (currentQuestion.subject == subj) Color(0xFF4338CA) else Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "Q #${firstIdx + 1} to #${lastIdx + 1} ($subCount questions)",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (ansSubCount == subCount) Color(0xFFDCFCE7) else Color(0xFFE0E7FF)
                                        ) {
                                            Text(
                                                text = "$ansSubCount/$subCount Solved",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (ansSubCount == subCount) Color(0xFF15803D) else Color(0xFF3730A3),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // KCET OMR Locked Rule Alert Dialog (Triggered when attempting to re-shade or change option)
    if (lockedOmrQuestionInfo != null) {
        val (qId, alreadyShadedOpt) = lockedOmrQuestionInfo!!
        val qIndex = questions.indexOfFirst { it.id == qId }
        val qNumber = if (qIndex >= 0) qIndex + 1 else qId

        AlertDialog(
            onDismissRequest = { lockedOmrQuestionInfo = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "🚫 Real KCET OMR Rule: Shading Locked!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Option ${alreadyShadedOpt.name} has already been shaded using dark pen ink for Question #$qNumber on your KCET OMR Answer Sheet.",
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📋 Official KCET OMR Examination Regulations:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Shading done using blue or black ballpoint pen cannot be erased, un-filled, or modified.\n2. Multiple option shading for a single question is strictly prohibited and invalidates the evaluation.",
                                fontSize = 10.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { lockedOmrQuestionInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                ) {
                    Text("I Understand KCET OMR Rules", fontWeight = FontWeight.Bold)
                }
            }
        )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question Palette",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total: ${questions.size} Qs",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Enter Question Number Directly
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paletteJumpInputText,
                        onValueChange = { paletteJumpInputText = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Enter Q# (1 - ${questions.size})", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val num = paletteJumpInputText.toIntOrNull()
                                if (num != null && num in 1..questions.size) {
                                    val targetIdx = num - 1
                                    onJumpToQuestion(targetIdx)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showPaletteSheet = false
                                    }
                                    Toast.makeText(context, "🚀 Jumped to Question #$num", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "⚠️ Please enter valid Q# between 1 & ${questions.size}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val num = paletteJumpInputText.toIntOrNull()
                            if (num != null && num in 1..questions.size) {
                                val targetIdx = num - 1
                                onJumpToQuestion(targetIdx)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showPaletteSheet = false
                                }
                                Toast.makeText(context, "🚀 Jumped to Question #$num", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "⚠️ Enter Q# from 1 to ${questions.size}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("Jump", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Filter Bar inside Palette
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedPaletteSubjectFilter == null) Color(0xFF6366F1) else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { selectedPaletteSubjectFilter = null }
                        ) {
                            Text(
                                text = "ALL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPaletteSubjectFilter == null) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    items(availableSubjects) { subj ->
                        val isSelected = selectedPaletteSubjectFilter == subj
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6366F1) else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable {
                                selectedPaletteSubjectFilter = subj
                                val firstIdx = questions.indexOfFirst { it.subject == subj }
                                if (firstIdx >= 0) {
                                    onJumpToQuestion(firstIdx)
                                    Toast.makeText(context, "🎯 Jumped to first question of ${subj.name} (#${firstIdx + 1})", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(
                                text = subj.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

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

                Spacer(modifier = Modifier.height(14.dp))

                val filteredQuestions = remember(questions, selectedPaletteSubjectFilter) {
                    if (selectedPaletteSubjectFilter == null) {
                        questions.mapIndexed { idx, q -> Pair(idx, q) }
                    } else {
                        questions.mapIndexed { idx, q -> Pair(idx, q) }.filter { it.second.subject == selectedPaletteSubjectFilter }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    itemsIndexed(filteredQuestions) { _, item ->
                        val index = item.first
                        val q = item.second
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

    // Anti-Cheating Violation Warning Dialog
    if (showCheatingWarningDialog) {
        AlertDialog(
            onDismissRequest = { showCheatingWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "⚠️ Exam Security Alert! (#$cheatingViolationsCount)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Column {
                    Text("You have switched away or minimized the application during active exam execution.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Text(
                            text = "Please remain inside the KCET test interface. Moving away or switching apps again may result in exam auto-submission.",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCheatingWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B))
                ) {
                    Text("I Understand & Resume Test", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Exit / Submit Confirm Dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = userAnswers.values.count { it.selectedOption != null }
        val unansweredCount = questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = if (is1v1Challenge) Icons.Default.Info else Icons.Default.Warning, contentDescription = null, tint = if (is1v1Challenge) Color(0xFF3B82F6) else Color(0xFFEF4444))
                    Text(if (is1v1Challenge) "1v1 Challenge: Exit or Submit" else "Exit or Submit Test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Session Summary:", fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 13.sp)
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Answered: $answeredCount / ${questions.size}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF16A34A))
                            Text("• Unanswered: $unansweredCount", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFDC2626))
                            Text("• Remaining Time: $formattedTime", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF2563EB))
                        }
                    }

                    if (!is1v1Challenge) {
                        val isBelowHalf = answeredCount <= (questions.size / 2)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isBelowHalf) Color(0xFFFEF2F2) else Color(0xFFFFFBEB))
                                .border(1.dp, if (isBelowHalf) Color(0xFFFCA5A5) else Color(0xFFFCD34D), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBelowHalf) {
                                        "⚠️ Gamification Warning: You attempted $answeredCount / ${questions.size} questions (<= 50%). Submitting/Quitting now earns 0 XP! Quitting early also applies a -50 XP penalty."
                                    } else {
                                        "⚡ Partial XP Awarded: You attempted $answeredCount / ${questions.size} questions (> 50%). XP is calculated based on correct answers provided and your completion ratio. Quitting early applies a -50 XP penalty."
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBelowHalf) Color(0xFF991B1B) else Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF6FF))
                                .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⚔️ 1 vs 1 Challenge Mode: Submitting or exiting will finalize your attempt and wait for your opponent. No additional XP penalty will be deducted.",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isSubmitting) {
                                showSubmitConfirmDialog = false
                                onSubmitExam()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submitting...", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (is1v1Challenge) "Submit 1v1 Test" else "Submit Exam & Calculate Score", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isSubmitting) {
                                showSubmitConfirmDialog = false
                                onQuitExam()
                                if (!is1v1Challenge) {
                                    Toast.makeText(context, "⚠️ Exam Abandonded! -50 XP penalty applied & Leaderboard rank updated.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFEF4444))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (is1v1Challenge) "Exit Test" else "Quit Test (-50 XP Penalty)", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 12.5.sp)
                    }

                    TextButton(
                        onClick = { showSubmitConfirmDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue Test", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = null
        )
    }
}

// Single Question Paper Card View (Read-Only Question Booklet in Strict Real Exam, Interactive in Practice)
@Composable
private fun SingleQuestionPaperView(
    currentQuestion: Question,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOpt: Option?,
    isBookmarked: Boolean,
    isStrictRealExam: Boolean = false,
    onSelectOption: (Option) -> Unit = {},
    onToggleBookmark: () -> Unit,
    onSubmitExamClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
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

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isBookmarked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                border = if (isBookmarked) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                modifier = Modifier.clickable {
                    onToggleBookmark()
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

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Specific Banner
        if (isStrictRealExam) {
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF93C5FD))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📄 FULL MOCK EXAM: Read questions & options here. Shade answers on KCET OMR Sheet below!",
                        fontSize = 10.sp,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Surface(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✍️ PRACTICE MODE: Tap options to select, change, or un-tick your answer freely.",
                        fontSize = 10.sp,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.SemiBold
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

        if (!currentQuestion.imageUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            coil.compose.AsyncImage(
                model = currentQuestion.imageUrl,
                contentDescription = "Question Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    .padding(vertical = 5.dp)
                    .clickable {
                        if (isStrictRealExam) {
                            Toast.makeText(
                                context,
                                "📝 Please shade option (${opt.name}) on the KCET OMR Answer Sheet!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onSelectOption(opt)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        if (isStrictRealExam) Color(0xFF0F172A).copy(alpha = 0.08f) else Color(0xFF2563EB).copy(alpha = 0.1f)
                    } else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        if (isStrictRealExam) Color(0xFF0F172A) else Color(0xFF2563EB)
                    } else Color(0xFFCBD5E1)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Option Badge Circle
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) {
                            if (isStrictRealExam) Color(0xFF0F172A) else Color(0xFF2563EB)
                        } else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                if (isStrictRealExam) Color(0xFF0F172A) else Color(0xFF2563EB)
                            } else Color(0xFF64748B)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = opt.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            if (isStrictRealExam) Color(0xFF0F172A) else Color(0xFF1E40AF)
                        } else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Surface(
                            color = if (isStrictRealExam) Color(0xFF0F172A) else Color(0xFF16A34A),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isStrictRealExam) "✒️ Shaded on OMR" else "✓ Ticked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = onSubmitExamClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Text(text = "Finish & Submit Exam", fontWeight = FontWeight.Bold)
        }
    }
}

// REAL AUTHENTIC KCET OPTICAL MARK READER (OMR) ANSWER SHEET VIEW
@Composable
private fun RealKcetOmrSheetView(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<Int, UserAnswer>,
    candidateName: String,
    candidateCetNumber: String,
    candidateBookletVersion: String,
    candidateBookletSerial: String,
    onSelectOption: (Int, Option) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onEditHeaderClick: () -> Unit
) {
    val context = LocalContext.current

    // Authentic Off-White OMR Paper Sheet Canvas
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5)) // Light cream OMR scanner paper look
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Outer Burgundy/Maroon OMR Sheet Border Frame
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(2.dp, Color(0xFF991B1B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Official KEA Header Title Banner
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KARNATAKA EXAMINATIONS AUTHORITY - BANGALORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF881337),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "COMMON ENTRANCE TEST (KCET) - OMR ANSWER SHEET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        thickness = 1.dp,
                        color = Color(0xFF991B1B)
                    )
                }

                // Clickable Candidate Hall Ticket Header Block
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditHeaderClick() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 CANDIDATE ADMISSION TICKET DETAILS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Surface(
                                color = Color(0xFF065F46),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Tap to Edit Info",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Candidate Name Box
                            Surface(
                                modifier = Modifier.weight(1.3f),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF991B1B)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("CANDIDATE NAME", fontSize = 8.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                    Text(candidateName, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                }
                            }

                            // CET Registration No Box
                            Surface(
                                modifier = Modifier.weight(1.1f),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF991B1B)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("CET REGISTRATION NO.", fontSize = 8.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                    Text(candidateCetNumber, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Version Code Box with shaded bubble
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF991B1B)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("BOOKLET VERSION", fontSize = 8.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                        Text(candidateBookletVersion, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF4338CA))
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1E1B4B),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(candidateBookletVersion.take(1), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Booklet Serial No Box
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF991B1B)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("BOOKLET SERIAL NO.", fontSize = 8.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                    Text(candidateBookletSerial, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF047857))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // OMR Bubble Shading Guidance Bar
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SHADING INSTRUCTIONS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Text("Tap bubble to fill with pen ink. Once shaded, answers are LOCKED permanently.", fontSize = 9.sp, color = Color(0xFF78350F))
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sample:", fontSize = 9.sp, color = Color(0xFF78350F))
                            OmrSampleBubble("A", shaded = false)
                            OmrSampleBubble("B", shaded = true)
                            OmrSampleBubble("C", shaded = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official OMR Answer Sheet Questions Grid (1 to N)
                Text(
                    text = "ANSWERS SECTION (${questions.size} QUESTIONS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF881337),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val chunks = questions.chunked(2) // Display in 2 parallel columns for authentic OMR sheet balance
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    chunks.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { question ->
                                val qIdx = questions.indexOf(question)
                                val userAns = userAnswers[question.id]?.selectedOption
                                val isCurrent = qIdx == currentIndex

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = if (isCurrent) Color(0xFFFEF9C3) else Color.White,
                                    border = BorderStroke(1.dp, if (isCurrent) Color(0xFFCA8A04) else Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Clickable Question Number Box (Jumps to question paper)
                                        Surface(
                                            color = if (isCurrent) Color(0xFF1E1B4B) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable { onJumpToQuestion(qIdx) }
                                        ) {
                                            Text(
                                                text = String.format("%02d", qIdx + 1),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) Color.White else Color(0xFF334155),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                            )
                                        }

                                        // OMR Bubbles A, B, C, D
                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            listOf(Option.A, Option.B, Option.C, Option.D).forEach { opt ->
                                                val isSelected = userAns == opt
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFFFFFDF5),
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                                    ),
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clickable {
                                                            onSelectOption(question.id, opt)
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = opt.name,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (pair.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OmrSampleBubble(label: String, shaded: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (shaded) Color(0xFF0F172A) else Color.White,
        border = BorderStroke(1.dp, if (shaded) Color(0xFF0F172A) else Color(0xFF94A3B8)),
        modifier = Modifier.size(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (shaded) Color.White else Color(0xFF475569)
            )
        }
    }
}

// Edit Candidate Details Dialog
@Composable
private fun EditOmrHeaderDialog(
    currentName: String,
    currentCetNo: String,
    currentVersion: String,
    currentSerial: String,
    onDismiss: () -> Unit,
    onSave: (name: String, cetNo: String, version: String, serial: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var cetNo by remember { mutableStateOf(currentCetNo) }
    var version by remember { mutableStateOf(currentVersion) }
    var serial by remember { mutableStateOf(currentSerial) }

    val versionCodes = listOf("A1", "A2", "B1", "B2", "C1", "C2", "D1", "D2")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF065F46))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit KCET OMR Hall Ticket Info", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Candidate Full Name") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cetNo,
                    onValueChange = { cetNo = it },
                    label = { Text("CET Admission Ticket No.") },
                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Select Booklet Version Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(versionCodes) { ver ->
                            val isSel = ver == version
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF1E1B4B) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF1E1B4B) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { version = ver }
                            ) {
                                Text(
                                    text = ver,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = serial,
                    onValueChange = { serial = it },
                    label = { Text("Booklet Serial No.") },
                    leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, cetNo, version, serial) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46))
            ) {
                Text("Save OMR Hall Ticket Info")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
