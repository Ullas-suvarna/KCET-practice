package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DoubtMessage
import com.example.data.GeminiDoubtService
import com.example.data.RecentDoubtChatManager
import com.example.data.SavedDoubtChat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AiDoubtSolverScreen(userEmail: String = "guest_user") {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var textInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var presetImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var copiedMessageId by remember { mutableStateOf<String?>(null) }
    var showRecentChatsSheet by remember { mutableStateOf(false) }
    var showAllSavedChats by remember { mutableStateOf(false) }
    var activeChatId by remember { mutableStateOf<String?>(null) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    @OptIn(ExperimentalLayoutApi::class)
    val isImeVisible = WindowInsets.isImeVisible

    // Permanently stored recent chats (strictly max 5, auto-purges oldest when 6th is added)
    var recentChats by remember(userEmail) {
        mutableStateOf(RecentDoubtChatManager.getRecentChats(context, userEmail))
    }

    // Default welcome message builder
    val defaultGreeting = remember {
        DoubtMessage(
            text = """
👋 **Hello Aspirant! I am your KCET Live AI Guru.**

Send me any doubt in **Physics, Chemistry, Mathematics, or Biology**:
• 📷 **Attach or take a photo** of a textbook question, circuit diagram, or equation.
• ✍️ **Type your question** directly in the input box.

I will analyze it immediately and provide step-by-step solutions with standard shortcut tricks!
""".trimIndent(),
            isUser = false,
            timestamp = "Online"
        )
    }

    // Chat messages stream initialized with permanently restored active messages if available
    val messages = remember(userEmail) {
        val savedActive = RecentDoubtChatManager.getActiveChatMessages(context, userEmail)
        if (savedActive.isNotEmpty()) {
            mutableStateListOf<DoubtMessage>().apply { addAll(savedActive) }
        } else {
            mutableStateListOf(defaultGreeting)
        }
    }

    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            bringIntoViewRequester.bringIntoView()
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(messages.size, isAnalyzing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(messages.size, isAnalyzing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            presetImageBitmap = null
        }
    }

    val onSendDoubt: (String, Uri?, Bitmap?) -> Unit = { prompt, uri, bitmap ->
        val promptClean = prompt.trim()
        if (promptClean.isNotBlank() || uri != null || bitmap != null) {
            val userMsg = DoubtMessage(
                text = promptClean.ifBlank { "📷 Attached Question Image" },
                isUser = true,
                imageUri = uri,
                imageBitmap = bitmap
            )
            messages.add(userMsg)
            RecentDoubtChatManager.saveActiveChatMessages(context, messages, userEmail)

            // Clear input states
            textInput = ""
            selectedImageUri = null
            presetImageBitmap = null
            isAnalyzing = true

            coroutineScope.launch {
                val aiResponseText = GeminiDoubtService.askDoubt(
                    context = context,
                    userPrompt = promptClean,
                    imageUri = uri,
                    presetBitmap = bitmap
                )

                val aiMsg = DoubtMessage(
                    text = aiResponseText,
                    isUser = false,
                    timestamp = "Just now"
                )
                messages.add(aiMsg)
                isAnalyzing = false

                // Save active conversation state so refreshing the app never loses messages
                RecentDoubtChatManager.saveActiveChatMessages(context, messages, userEmail)

                // Permanently save chat session to Recent Doubt list
                val updatedChats = RecentDoubtChatManager.saveNewChat(
                    context = context,
                    question = promptClean.ifBlank { "📷 Attached Question Image for AI analysis" },
                    answer = aiResponseText,
                    userId = userEmail,
                    hasImage = (uri != null || bitmap != null)
                )
                recentChats = updatedChats
                activeChatId = updatedChats.firstOrNull()?.id
            }
        }
    }

    // Restore / load a previous saved chat into view
    val onLoadSavedChat: (SavedDoubtChat) -> Unit = { chat ->
        activeChatId = chat.id
        messages.clear()
        val qMsg = DoubtMessage(
            text = chat.questionText,
            isUser = true,
            timestamp = chat.formattedTime
        )
        val aMsg = DoubtMessage(
            text = chat.aiAnswerText,
            isUser = false,
            timestamp = chat.formattedTime,
            subjectTag = chat.subjectTag
        )
        messages.add(qMsg)
        messages.add(aMsg)
        RecentDoubtChatManager.saveActiveChatMessages(context, messages, userEmail)
        showRecentChatsSheet = false
        Toast.makeText(context, "📖 Restored saved doubt from ${chat.formattedTime}", Toast.LENGTH_SHORT).show()
    }

    // Start a fresh new session
    val onStartNewDoubt: () -> Unit = {
        activeChatId = null
        messages.clear()
        messages.add(defaultGreeting)
        RecentDoubtChatManager.saveActiveChatMessages(context, messages, userEmail)
        textInput = ""
        selectedImageUri = null
        presetImageBitmap = null
        Toast.makeText(context, "✨ Started fresh chat. Previous chats remain saved in Recent!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("ai_doubt_solver_screen")
    ) {
        // --- Top Header Card ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "AI Icon",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KCET AI Doubt Solver",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Text(
                                text = "Multimodal Gemini AI • PCMB Tutor",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Action buttons in top bar: Start New Doubt & 3-dot Menu (More Options & Recent Chats)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onStartNewDoubt() }
                                .testTag("new_doubt_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "New Doubt",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (showRecentChatsSheet || recentChats.isNotEmpty()) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (showRecentChatsSheet || recentChats.isNotEmpty()) Color(0xFFC7D2FE) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    if (showRecentChatsSheet) {
                                        showRecentChatsSheet = false
                                    } else {
                                        showAllSavedChats = false
                                        showRecentChatsSheet = true
                                    }
                                }
                                .testTag("recent_chats_3dot_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Recent Chats",
                                    tint = if (showRecentChatsSheet || recentChats.isNotEmpty()) Color(0xFF4F46E5) else Color(0xFF475569),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Quick Questions & Sample Photo Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickPromptChip(
                            icon = Icons.Filled.AddPhotoAlternate,
                            label = "📷 Sample Physics Circuit Photo",
                            onClick = {
                                val sampleBitmap = createSamplePhysicsCircuitBitmap()
                                presetImageBitmap = sampleBitmap
                                selectedImageUri = null
                                onSendDoubt("Solve the circuit diagram and calculate equivalent resistance and current.", null, sampleBitmap)
                            }
                        )
                    }
                    item {
                        QuickPromptChip(
                            icon = Icons.Filled.Lightbulb,
                            label = "⚛️ Ray Optics Snell's Law",
                            onClick = {
                                textInput = "Explain Snell's Law in optics with formula and KCET sample problem."
                            }
                        )
                    }
                    item {
                        QuickPromptChip(
                            icon = Icons.Filled.Lightbulb,
                            label = "📐 Calculus Integration Shortcut",
                            onClick = {
                                textInput = "Solve integral e^x (sin x + cos x) dx with KCET shortcut formula."
                            }
                        )
                    }
                    item {
                        QuickPromptChip(
                            icon = Icons.Filled.Lightbulb,
                            label = "🧪 Markovnikov Addition",
                            onClick = {
                                textInput = "Explain Markovnikov rule in organic chemistry with KCET example."
                            }
                        )
                    }
                }
            }
        }

        // --- Chat Messages Stream ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                DoubtMessageItem(
                    message = msg,
                    onCopySolution = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        copiedMessageId = msg.id
                        Toast.makeText(context, "📋 Solution copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    isCopied = copiedMessageId == msg.id
                )
            }

            if (isAnalyzing) {
                item {
                    AnalyzingIndicatorCard()
                }
            }
        }

        // --- Bottom Attachment & Input Bar ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Attached Image Preview Container
                AnimatedVisibility(
                    visible = selectedImageUri != null || presetImageBitmap != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (presetImageBitmap != null) {
                                Image(
                                    bitmap = presetImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Preset Photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    presetImageBitmap = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Image attached for Gemini analysis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                            Text(
                                text = "AI will extract text/diagrams automatically",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Image Picker Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF))
                            .testTag("attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Attach Photo",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = "Ask KCET doubt or paste question...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 140.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                        if (messages.isNotEmpty()) {
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                    }
                                }
                            }
                            .testTag("doubt_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            cursorColor = Color(0xFF4F46E5)
                        ),
                        minLines = 1,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Action Button
                    val canSend = textInput.isNotBlank() || selectedImageUri != null || presetImageBitmap != null
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend && !isAnalyzing) {
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
                                    )
                                }
                            )
                            .clickable(enabled = canSend && !isAnalyzing) {
                                onSendDoubt(textInput, selectedImageUri, presetImageBitmap)
                            }
                            .testTag("send_doubt_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send Question",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // --- Modal Bottom Sheet for Recent Saved Chats ---
    if (showRecentChatsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showRecentChatsSheet = false
                showAllSavedChats = false
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header with "Recent Chats" and "✕" close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Recent Chats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        if (recentChats.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${recentChats.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            showRecentChatsSheet = false
                            showAllSavedChats = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                if (recentChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No saved doubts yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Ask your first doubt to automatically save it here!",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    val displayedChats = if (showAllSavedChats || recentChats.size <= 3) recentChats else recentChats.take(3)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        itemsIndexed(displayedChats, key = { _, chat -> chat.id }) { index, chat ->
                            val isActive = activeChatId == chat.id
                            Card(
                                onClick = {
                                    onLoadSavedChat(chat)
                                    showRecentChatsSheet = false
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) Color(0xFFEEF2FF) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isActive) 1.5.dp else 1.dp,
                                    color = if (isActive) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("saved_chat_item_${index + 1}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color(0xFF4F46E5) else Color(0xFFEEF2FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color.White else Color(0xFF4F46E5)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = chat.questionText.ifBlank { "Photo Doubt Solution" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFDCFCE7))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF15803D)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${chat.subjectTag} • ${chat.formattedTime}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (activeChatId == chat.id) {
                                                activeChatId = null
                                            }
                                            recentChats = RecentDoubtChatManager.deleteChat(context, chat.id, userEmail)
                                            Toast.makeText(context, "Deleted chat", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Delete",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (recentChats.size > 3) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (!showAllSavedChats) {
                            Button(
                                onClick = { showAllSavedChats = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("view_all_chats_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEEF2FF),
                                    contentColor = Color(0xFF4F46E5)
                                )
                            ) {
                                Text(
                                    text = "View All Chats (${recentChats.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            TextButton(
                                onClick = { showAllSavedChats = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("show_latest_chats_button")
                            ) {
                                Text("Show Latest 3 Only", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onStartNewDoubt()
                            showRecentChatsSheet = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (recentChats.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                recentChats = RecentDoubtChatManager.clearAllChats(context, userEmail)
                                messages.clear()
                                messages.add(defaultGreeting)
                                RecentDoubtChatManager.saveActiveChatMessages(context, messages, userEmail)
                                showRecentChatsSheet = false
                                Toast.makeText(context, "🗑️ Cleared all recent chats", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickPromptChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
            )
        }
    }
}

@Composable
fun DoubtMessageItem(
    message: DoubtMessage,
    onCopySolution: (String) -> Unit,
    isCopied: Boolean
) {
    if (message.isUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4F46E5)
                ),
                shape = RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.imageUri != null) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Question Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(bottom = 6.dp)
                        )
                    } else if (message.imageBitmap != null) {
                        Image(
                            bitmap = message.imageBitmap.asImageBitmap(),
                            contentDescription = "Question Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Guru",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(2.dp, 18.dp, 18.dp, 18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KCET AI Guru",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Verified Solution",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onCopySolution(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                contentDescription = "Copy Solution",
                                tint = if (isCopied) Color(0xFF16A34A) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FormattedSolutionView(text = message.text)
                }
            }
        }
    }
}

@Composable
fun FormattedSolutionView(text: String) {
    val cleanedText = GeminiDoubtService.cleanMathText(text)
    val lines = cleanedText.lines()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                return@forEach
            }

            when {
                // Correct Option or Final Answer Badge Card
                trimmed.contains("🎯") || trimmed.startsWith("Option", ignoreCase = true) ||
                        trimmed.contains("Correct Option", ignoreCase = true) ||
                        trimmed.contains("Correct Answer", ignoreCase = true) ||
                        trimmed.contains("Major Product:", ignoreCase = true) -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎯", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = parseRichMarkdown(trimmed.replace("🎯", "").trim()),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                lineHeight = 21.sp
                            )
                        }
                    }
                }

                // Section Headers (### or emojis like 💡, ⚡, 📸, ⚛️, 📐, 🧪, 🤖)
                trimmed.startsWith("###") || trimmed.startsWith("💡") || trimmed.startsWith("⚡") ||
                        trimmed.startsWith("📸") || trimmed.startsWith("⚛️") || trimmed.startsWith("📐") ||
                        trimmed.startsWith("🧪") || trimmed.startsWith("🤖") -> {
                    val cleanHeader = trimmed.replace("###", "").trim()
                    Text(
                        text = parseRichMarkdown(cleanHeader),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3730A3),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                // Step headers like "Step 1:", "Given:", "Formula:", "Calculation:", "Shortcut Trick:"
                isStepHeader(trimmed) -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4F46E5).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
                        ) {
                            Text(
                                text = extractStepLabel(trimmed),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        val remaining = extractStepBody(trimmed)
                        if (remaining.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = parseRichMarkdown(remaining),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                // Pure Equation / Formula Card
                isFormulaLine(trimmed) -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📐",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = parseRichMarkdown(trimmed),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Bullet point lines
                trimmed.startsWith("•") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val bulletText = trimmed.removePrefix("•").removePrefix("- ").removePrefix("* ").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseRichMarkdown(bulletText),
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Standard text line
                else -> {
                    Text(
                        text = parseRichMarkdown(trimmed),
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

private fun isStepHeader(line: String): Boolean {
    val lower = line.lowercase().trim()
    return lower.startsWith("step 1") || lower.startsWith("step 2") || lower.startsWith("step 3") ||
            lower.startsWith("step 4") || lower.startsWith("step 5") || lower.startsWith("given:") ||
            lower.startsWith("to find:") || lower.startsWith("formula used:") || lower.startsWith("shortcut:") ||
            lower.startsWith("trick:") || lower.startsWith("key concept:") || lower.startsWith("calculation:") ||
            lower.startsWith("explanation:")
}

private fun extractStepLabel(line: String): String {
    val colonIdx = line.indexOf(':')
    return if (colonIdx != -1) {
        line.substring(0, colonIdx).replace("*", "").trim()
    } else {
        line.replace("*", "").trim()
    }
}

private fun extractStepBody(line: String): String {
    val colonIdx = line.indexOf(':')
    return if (colonIdx != -1 && colonIdx < line.length - 1) {
        line.substring(colonIdx + 1).trim()
    } else {
        ""
    }
}

private fun isFormulaLine(line: String): Boolean {
    val mathSymbols = listOf("=", "∫", "√", "±", "÷", "×", "Δ", "θ", "α", "β", "μ", "Ω", "λ", "π", "°", "→", "≈", "≤", "≥", "Req", "sin(", "cos(", "tan(", "d/dx", "HBr", "CH3", "C =", "V =", "I =", "R =", "F =", "a =", "k =")
    val hasSymbol = mathSymbols.any { line.contains(it) }
    val isNumberedFormula = (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.")) && line.contains("=")
    val isShortMath = hasSymbol && line.length < 90 && !line.contains(" because ") && !line.contains(" therefore ")
    return isShortMath || isNumberedFormula
}

private fun parseRichMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        // First clean up inline backticks and split by bold **
        val clean = text.replace("`", "")
        val parts = clean.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                ) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

@Composable
fun AnalyzingIndicatorCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖 Gemini 3.5 Flash is solving your doubt...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4338CA).copy(alpha = alpha)
                )
            }
        }
    }
}

// Programmatically creates a clean sample physics circuit image bitmap for 1-tap testing
fun createSamplePhysicsCircuitBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(600, 320, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
    canvas.drawRect(0f, 0f, 600f, 320f, bgPaint)

    // Border
    val borderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#4F46E5")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawRect(10f, 10f, 590f, 310f, borderPaint)

    // Header Text
    val textPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 28f
        isFakeBoldText = true
    }
    canvas.drawText("KCET PHYSICS - CIRCUIT QUESTION", 30f, 50f, textPaint)

    // Diagram Box
    val boxPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#EEF2FF")
        style = Paint.Style.FILL
    }
    canvas.drawRect(30f, 80f, 570f, 280f, boxPaint)

    val diagText = Paint().apply {
        color = android.graphics.Color.parseColor("#3730A3")
        textSize = 22f
        isFakeBoldText = true
    }
    canvas.drawText("Voltage Source V = 220V", 50f, 130f, diagText)
    canvas.drawText("Resistor R1 = 10 Ohm (Parallel)", 50f, 170f, diagText)
    canvas.drawText("Resistor R2 = 20 Ohm (Parallel)", 50f, 210f, diagText)
    canvas.drawText("Calculate Req and Total Current I ?", 50f, 250f, diagText)

    return bitmap
}
