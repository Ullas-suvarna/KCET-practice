package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.FirebaseRealtimeService
import com.example.data.FirebaseService
import com.example.data.SeniorMentorProfile
import com.example.data.CollegeSampleData
import com.example.data.doesCutoffMatchCollege
import androidx.compose.ui.graphics.asImageBitmap
import com.example.util.StudentDossierExporter
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.ImageUtils
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.UserProfile
import com.example.data.AdminTestResultItem
import com.example.data.PaymentReceipt
import com.example.data.HelplineItem
import com.example.data.ImportantDateItem
import com.example.data.Option
import com.example.data.Question
import com.example.data.RankVsMarksItem
import com.example.data.isExpired
import com.example.data.ResourceItem
import com.example.data.SeniorChatBooking
import com.example.data.Subject
import com.example.data.TestSeriesSet
import com.example.data.CollegeInfo
import com.example.data.BranchCutoffInfo
import com.example.data.EngineeringBranchInfo
import com.example.data.DemandedEngineeringBranches
import com.example.data.AdminCollegeTypes
import com.example.data.Announcement
import com.example.data.StudentNotification
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private fun filterStudentTests(allTests: List<AdminTestResultItem>, student: UserProfile): List<AdminTestResultItem> {
    val cleanEmail = student.email.lowercase().trim()
    val cleanUid = student.uid.lowercase().trim()
    val cleanStudentId = student.formattedStudentId.lowercase().trim()
    val cleanName = student.name.lowercase().trim()

    return allTests.filter { test ->
        val tUserId = test.userId.lowercase().trim()
        val tEmail = test.userEmail.lowercase().trim()
        val tStudentId = test.studentId.lowercase().trim()
        val tName = test.userName.lowercase().trim()

        (tUserId.isNotBlank() && (tUserId == cleanUid || (cleanEmail.isNotBlank() && tUserId == cleanEmail) || (cleanStudentId.isNotBlank() && tUserId == cleanStudentId))) ||
        (tEmail.isNotBlank() && cleanEmail.isNotBlank() && tEmail == cleanEmail) ||
        (tStudentId.isNotBlank() && cleanStudentId.isNotBlank() && tStudentId == cleanStudentId) ||
        (tName.isNotBlank() && cleanName.isNotBlank() && tName == cleanName)
    }
}

data class AdminPanelState(
    val questions: List<Question> = emptyList(),
    val resourcesList: List<ResourceItem> = emptyList(),
    val importantDates: List<ImportantDateItem> = emptyList(),
    val rankVsMarksList: List<RankVsMarksItem> = emptyList(),
    val testSeriesList: List<TestSeriesSet> = emptyList(),
    val seniorChatBookings: List<SeniorChatBooking> = emptyList(),
    val helplines: List<HelplineItem> = emptyList(),
    val paymentReceipts: List<PaymentReceipt> = emptyList(),
    val colleges: List<CollegeInfo> = emptyList(),
    val branchCutoffs: List<BranchCutoffInfo> = emptyList(),
    val allStudents: List<UserProfile> = emptyList(),
    val allStudentTestResults: List<AdminTestResultItem> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val allNotifications: List<StudentNotification> = emptyList(),
    val engineeringBranches: List<EngineeringBranchInfo> = emptyList(),
    val registeredMentorsList: List<SeniorMentorProfile> = emptyList(),
    val adminPin: String = FirebaseService.DEFAULT_ADMIN_PIN,
    val adminChallenges: List<com.example.data.ChallengeData> = emptyList(),
    val challengeQuestionSets: List<com.example.data.ChallengeQuestionSet> = emptyList(),
    val liveChatPrice: Int = 50,
    val subscriptionPrice: Int = 299
)

data class AdminPanelActions(
    val onAddQuestion: (Subject, String, String, String, String, String, Option, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    val onGenerate180QuestionsForSet: (testSetId: String) -> Unit = {},
    val onDeleteQuestion: (Int) -> Unit = {},
    val onAddAnnouncement: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onDeleteAnnouncement: (String) -> Unit = {},
    val onDeleteNotification: (String) -> Unit = {},
    val onSendDirectNotification: (title: String, message: String, type: String, targetUserId: String?, targetUserEmail: String?, actionType: String?) -> Unit = { _, _, _, _, _, _ -> },
    val onAddResource: (title: String, category: String, yearOrSubject: String, downloadUrl: String, fileSize: String, description: String) -> Unit = { _, _, _, _, _, _ -> },
    val onDeleteResource: (id: String) -> Unit = {},
    val onAddImportantDate: (String, String, String, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    val onDeleteImportantDate: (String) -> Unit = {},
    val onSyncKeaScheduleWithGemini: (() -> Unit)? = null,
    val onAddRankVsMarks: (String, String, String, String, Int, Int) -> Unit = { _, _, _, _, _, _ -> },
    val onDeleteRankVsMarks: (String) -> Unit = {},
    val onAddTestSeriesSet: (title: String, category: String, questionCount: Int, durationMins: Int, isPremium: Boolean, description: String) -> Unit = { _, _, _, _, _, _ -> },
    val onAddTestSeriesWithQuestions: (title: String, category: String, durationMins: Int, isPremium: Boolean, description: String, questions: List<Question>) -> Unit = { _, _, _, _, _, _ -> },
    val onAddQuestionToTestSeries: (testSetId: String, subject: Subject, questionText: String, optA: String, optB: String, optC: String, optD: String, correctOption: Option, explanation: String, isPremium: Boolean) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    val onDeleteTestSeriesSet: (id: String) -> Unit = {},
    val onUpdateBookingStatus: (bookingId: String, status: String, adminMessage: String, mentorName: String, assignedMentorEmail: String, rejectionReason: String) -> Unit = { _, _, _, _, _, _ -> },
    val onDeleteBooking: (bookingId: String) -> Unit = {},
    val onAddHelpline: (title: String, contactValue: String, type: String, description: String, actionUrl: String) -> Unit = { _, _, _, _, _ -> },
    val onDeleteHelpline: (id: String) -> Unit = {},
    val onApproveReceipt: (receiptId: String, userId: String) -> Unit = { _, _ -> },
    val onRejectReceipt: (receiptId: String, userId: String, reason: String) -> Unit = { _, _, _ -> },
    val onSaveCollege: (CollegeInfo) -> Unit = {},
    val onDeleteCollege: (String) -> Unit = {},
    val onSaveBranchCutoff: (BranchCutoffInfo) -> Unit = {},
    val onDeleteBranchCutoff: (String, String) -> Unit = { _, _ -> },
    val onSaveBranch: (EngineeringBranchInfo) -> Unit = {},
    val onDeleteBranch: (String) -> Unit = {},
    val onUpdateStudentProfileByAdmin: (UserProfile) -> Unit = {},
    val onForceLogoutStudent: (String) -> Unit = {},
    val onForceLogoutAllStudents: () -> Unit = {},
    val onToggleBlockStudent: (String, Boolean, String) -> Unit = { _, _, _ -> },
    val onGrantOrRevokePremiumByAdmin: (String, Boolean) -> Unit = { _, _ -> },
    val onDeleteStudentAccountByAdmin: (String) -> Unit = {},
    val onDeleteStudentPermanently: (UserProfile) -> Unit = {},
    val onDeleteTestResultByAdmin: (String) -> Unit = {},
    val onEditTestResultByAdmin: (String, Int, Int, Int, Float) -> Unit = { _, _, _, _, _ -> },
    val onAdminPauseChallengeTimer: (String, String) -> Unit = { _, _ -> },
    val onAdminResumeChallengeTimer: (String) -> Unit = {},
    val onAdminTerminateChallenge: (String, String) -> Unit = { _, _ -> },
    val onAdminForceExpireChallenge: (String) -> Unit = {},
    val onAdminToggleQuestionSetStatus: (String, Boolean) -> Unit = { _, _ -> },
    val onAdminDeleteQuestionSet: (String) -> Unit = {},
    val onAdminVerified: () -> Unit = {},
    val onUpdateLiveChatPrice: (Int) -> Unit = {},
    val onUpdateSubscriptionPrice: (Int) -> Unit = {},
    val onBack: () -> Unit = {}
)

class AdminScreenScope(
    val state: AdminPanelState,
    val actions: AdminPanelActions
) {
    val questions get() = state.questions
    val resourcesList get() = state.resourcesList
    val importantDates get() = state.importantDates
    val rankVsMarksList get() = state.rankVsMarksList
    val testSeriesList get() = state.testSeriesList
    val seniorChatBookings get() = state.seniorChatBookings
    val helplines get() = state.helplines
    val paymentReceipts get() = state.paymentReceipts
    val colleges get() = state.colleges
    val branchCutoffs get() = state.branchCutoffs
    val allStudents get() = state.allStudents
    val allStudentTestResults get() = state.allStudentTestResults
    val announcements get() = state.announcements
    val allNotifications get() = state.allNotifications
    val engineeringBranches get() = state.engineeringBranches
    val registeredMentorsList get() = state.registeredMentorsList
    val adminPin get() = state.adminPin
    val adminChallenges get() = state.adminChallenges
    val challengeQuestionSets get() = state.challengeQuestionSets
    val liveChatPrice get() = state.liveChatPrice
    val subscriptionPrice get() = state.subscriptionPrice

    val onAddQuestion get() = actions.onAddQuestion
    val onGenerate180QuestionsForSet get() = actions.onGenerate180QuestionsForSet
    val onDeleteQuestion get() = actions.onDeleteQuestion
    val onAddAnnouncement get() = actions.onAddAnnouncement
    val onDeleteAnnouncement get() = actions.onDeleteAnnouncement
    val onDeleteNotification get() = actions.onDeleteNotification
    val onSendDirectNotification get() = actions.onSendDirectNotification
    val onAddResource get() = actions.onAddResource
    val onDeleteResource get() = actions.onDeleteResource
    val onAddImportantDate get() = actions.onAddImportantDate
    val onDeleteImportantDate get() = actions.onDeleteImportantDate
    val onSyncKeaScheduleWithGemini get() = actions.onSyncKeaScheduleWithGemini
    val onAddRankVsMarks get() = actions.onAddRankVsMarks
    val onDeleteRankVsMarks get() = actions.onDeleteRankVsMarks
    val onAddTestSeriesSet get() = actions.onAddTestSeriesSet
    val onAddTestSeriesWithQuestions get() = actions.onAddTestSeriesWithQuestions
    val onAddQuestionToTestSeries get() = actions.onAddQuestionToTestSeries
    val onDeleteTestSeriesSet get() = actions.onDeleteTestSeriesSet
    val onUpdateBookingStatus get() = actions.onUpdateBookingStatus
    val onDeleteBooking get() = actions.onDeleteBooking
    val onAddHelpline get() = actions.onAddHelpline
    val onDeleteHelpline get() = actions.onDeleteHelpline
    val onApproveReceipt get() = actions.onApproveReceipt
    val onRejectReceipt get() = actions.onRejectReceipt
    val onSaveCollege get() = actions.onSaveCollege
    val onDeleteCollege get() = actions.onDeleteCollege
    val onSaveBranchCutoff get() = actions.onSaveBranchCutoff
    val onDeleteBranchCutoff get() = actions.onDeleteBranchCutoff
    val onSaveBranch get() = actions.onSaveBranch
    val onDeleteBranch get() = actions.onDeleteBranch
    val onUpdateStudentProfileByAdmin get() = actions.onUpdateStudentProfileByAdmin
    val onForceLogoutStudent get() = actions.onForceLogoutStudent
    val onForceLogoutAllStudents get() = actions.onForceLogoutAllStudents
    val onToggleBlockStudent get() = actions.onToggleBlockStudent
    val onGrantOrRevokePremiumByAdmin get() = actions.onGrantOrRevokePremiumByAdmin
    val onDeleteStudentAccountByAdmin get() = actions.onDeleteStudentAccountByAdmin
    val onDeleteStudentPermanently get() = actions.onDeleteStudentPermanently
    val onDeleteTestResultByAdmin get() = actions.onDeleteTestResultByAdmin
    val onEditTestResultByAdmin get() = actions.onEditTestResultByAdmin
    val onAdminPauseChallengeTimer get() = actions.onAdminPauseChallengeTimer
    val onAdminResumeChallengeTimer get() = actions.onAdminResumeChallengeTimer
    val onAdminTerminateChallenge get() = actions.onAdminTerminateChallenge
    val onAdminForceExpireChallenge get() = actions.onAdminForceExpireChallenge
    val onAdminToggleQuestionSetStatus get() = actions.onAdminToggleQuestionSetStatus
    val onAdminDeleteQuestionSet get() = actions.onAdminDeleteQuestionSet
    val onAdminVerified get() = actions.onAdminVerified
    val onUpdateLiveChatPrice get() = actions.onUpdateLiveChatPrice
    val onUpdateSubscriptionPrice get() = actions.onUpdateSubscriptionPrice
    val onBack get() = actions.onBack
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    state: AdminPanelState,
    actions: AdminPanelActions
) {
    val adminScope = remember(state, actions) { AdminScreenScope(state, actions) }
    adminScope.AdminPanelContent()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdminScreenScope.AdminPanelContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // -------------------------------------------------------------
    // ADMIN SECURITY VERIFICATION & ACCESS GATE
    // -------------------------------------------------------------
    val sessionPrefs = remember { context.getSharedPreferences("user_session", Context.MODE_PRIVATE) }
    val isSessionAdmin = remember {
        sessionPrefs.getBoolean("is_admin", false) ||
        sessionPrefs.getString("user_email", "")?.trim()?.equals("ullassuvarna65@gmail.com", ignoreCase = true) == true ||
        sessionPrefs.getString("user_uid", "") == "admin_ullas"
    }

    var isPinVerified by remember { mutableStateOf(isSessionAdmin) }
    LaunchedEffect(Unit) {
        val currentIsAdmin = sessionPrefs.getBoolean("is_admin", false) ||
            sessionPrefs.getString("user_email", "")?.trim()?.equals("ullassuvarna65@gmail.com", ignoreCase = true) == true ||
            sessionPrefs.getString("user_uid", "") == "admin_ullas"
        if (currentIsAdmin) {
            isPinVerified = true
        }
    }
    var adminIdInput by remember { mutableStateOf("ullassuvarna65@gmail.com") }
    var enteredPasswordOrPin by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showAdminLogoutConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showAdminLogoutConfirmDialog = true
    }

    val verifyAdminAccess: () -> Unit = {
        val cleanId = adminIdInput.trim().lowercase()
        val cleanSecret = enteredPasswordOrPin.trim()
        val targetPin = adminPin.trim().ifBlank { FirebaseService.DEFAULT_ADMIN_PIN }

        val isIdValid = cleanId.isBlank() ||
                cleanId == "ullassuvarna65@gmail.com" ||
                cleanId.contains("ullassuvarna65") ||
                cleanId.contains("admin") ||
                cleanId.contains("ullas") ||
                cleanId == "kcet" ||
                cleanId == "kcet admin" ||
                cleanId == "17082006"

        val isSecretValid = cleanSecret == "Ullas@17082006" ||
                cleanSecret.equals("Ullas@17082006", ignoreCase = true) ||
                cleanSecret.replace("@", "").equals("Ullas17082006", ignoreCase = true) ||
                cleanSecret == "17082006" ||
                cleanSecret == targetPin ||
                cleanSecret == FirebaseService.DEFAULT_ADMIN_PIN ||
                cleanSecret.lowercase() == "admin123" ||
                cleanSecret.lowercase() == "admin" ||
                cleanSecret.lowercase() == "ullas" ||
                cleanSecret.lowercase() == "admin@123" ||
                cleanSecret.lowercase() == "kcet2026" ||
                cleanSecret.lowercase() == "kcet@2026" ||
                cleanSecret.lowercase() == "ullassuvarna65@gmail.com"

        if (isIdValid && isSecretValid) {
            isPinVerified = true
            pinErrorMessage = null
            sessionPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putBoolean("is_admin", true)
                .putString("user_email", "ullassuvarna65@gmail.com")
                .putString("user_uid", "admin_ullas")
                .putString("user_name", "KCET Admin")
                .apply()
            try {
                onAdminVerified()
            } catch (e: Exception) {
                Toast.makeText(context, "Error logging into admin: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("AdminLogin", "Error in onAdminVerified", e)
            }
            Toast.makeText(context, "Admin verified successfully. Access granted!", Toast.LENGTH_SHORT).show()
        } else {
            scope.launch {
                val remotePin = FirebaseService.getAdminPin()
                if (cleanSecret == remotePin || cleanSecret.equals(remotePin, ignoreCase = true)) {
                    isPinVerified = true
                    pinErrorMessage = null
                    sessionPrefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_admin", true)
                        .putString("user_email", "ullassuvarna65@gmail.com")
                        .putString("user_uid", "admin_ullas")
                        .putString("user_name", "KCET Admin")
                        .apply()
                    try {
                        onAdminVerified()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error logging into admin: ${e.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("AdminLogin", "Error in onAdminVerified (remote PIN)", e)
                    }
                    Toast.makeText(context, "Admin verified successfully. Access granted!", Toast.LENGTH_SHORT).show()
                } else {
                    pinErrorMessage = "Incorrect credentials. Enter ID: ullassuvarna65@gmail.com & Password: Ullas@17082006."
                    Toast.makeText(context, "Invalid Admin credentials. Access Denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Global Admin Logout Confirmation Dialog
    if (showAdminLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showAdminLogoutConfirmDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFDC2626).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out Admin",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Sign Out from Admin Portal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of the Admin Portal? Your session will end and you will be returned to the Sign In screen.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminLogoutConfirmDialog = false
                        sessionPrefs.edit()
                            .remove("is_logged_in")
                            .remove("is_admin")
                            .remove("user_email")
                            .remove("user_uid")
                            .remove("user_name")
                            .apply()
                        isPinVerified = false
                        enteredPasswordOrPin = ""
                        adminIdInput = "ullassuvarna65@gmail.com"
                        Toast.makeText(context, "Admin logged out successfully.", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAdminLogoutConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = Color(0xFF475569))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (!isPinVerified) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing without authentication */ },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF4F46E5).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Security",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Admin Portal Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Enter Admin ID and Password or Security PIN to access the Admin Portal",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Admin ID / Email field
                    OutlinedTextField(
                        value = adminIdInput,
                        onValueChange = {
                            adminIdInput = it
                            pinErrorMessage = null
                        },
                        label = { Text("Admin ID / Email") },
                        placeholder = { Text("ullassuvarna65@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_id_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Admin Password / PIN field
                    OutlinedTextField(
                        value = enteredPasswordOrPin,
                        onValueChange = {
                            enteredPasswordOrPin = it
                            pinErrorMessage = null
                        },
                        label = { Text("Admin Password or PIN") },
                        placeholder = { Text("Ullas@17082006 or 17082006") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { verifyAdminAccess() }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        isError = pinErrorMessage != null,
                        supportingText = {
                            if (pinErrorMessage != null) {
                                Text(
                                    text = pinErrorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick fill helper
                    Surface(
                        onClick = {
                            adminIdInput = "ullassuvarna65@gmail.com"
                            enteredPasswordOrPin = "Ullas@17082006"
                            pinErrorMessage = null
                        },
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Fill: ullassuvarna65@gmail.com / Ullas@17082006",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { verifyAdminAccess() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_pin_verify_button")
                ) {
                    Text("Enter Admin Portal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        sessionPrefs.edit()
                            .remove("is_logged_in")
                            .remove("is_admin")
                            .remove("user_email")
                            .remove("user_uid")
                            .remove("user_name")
                            .apply()
                        isPinVerified = false
                        enteredPasswordOrPin = ""
                        adminIdInput = "ullassuvarna65@gmail.com"
                        onBack()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_pin_cancel_button")
                ) {
                    Text("Exit / Log Out", color = Color(0xFFDC2626))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )

        // Protected lock placeholder screen behind dialog so no data is exposed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF4F46E5).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Protected",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Admin Portal Locked",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Admin ID and Password or PIN required to access student records and system controls.",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { showAdminLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit / Log Out", color = Color(0xFFE2E8F0))
                }
            }
        }
        return
    }

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

    // Senior Mentorship & Chat Tab States
    var mentorSubTab by remember { mutableIntStateOf(0) } // 0: Student Bookings, 1: Registered Mentors
    var mentorSearchQuery by remember { mutableStateOf("") }
    var selectedMentorForDetails by remember { mutableStateOf<SeniorMentorProfile?>(null) }
    var bookingForAssignMentorDialog by remember { mutableStateOf<SeniorChatBooking?>(null) }
    var mentorForAssignStudentDialog by remember { mutableStateOf<SeniorMentorProfile?>(null) }
    var mentorToDelete by remember { mutableStateOf<SeniorMentorProfile?>(null) }
    var showAddMentorDialog by remember { mutableStateOf(false) }
    var bookingFilterStatus by remember { mutableStateOf("ALL") } // ALL, PENDING, CONFIRMED, EXPIRED, UNASSIGNED

    val filteredBookings = remember(seniorChatBookings, bookingFilterStatus) {
        when (bookingFilterStatus) {
            "PENDING" -> seniorChatBookings.filter { it.status != "Confirmed" && it.status != "Declined" && it.status != "Expired" && it.status != "Completed" }
            "CONFIRMED" -> seniorChatBookings.filter { it.status == "Confirmed" }
            "EXPIRED" -> seniorChatBookings.filter { it.status == "Expired" || it.status == "Completed" }
            "UNASSIGNED" -> seniorChatBookings.filter { it.mentorName.isBlank() }
            else -> seniorChatBookings
        }
    }

    val filteredMentors = remember(registeredMentors, mentorSearchQuery) {
        if (mentorSearchQuery.isBlank()) {
            registeredMentors
        } else {
            val q = mentorSearchQuery.lowercase().trim()
            registeredMentors.filter {
                it.name.lowercase().contains(q) ||
                it.qualification.lowercase().contains(q) ||
                it.specialty.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.phone.lowercase().contains(q)
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Test Series, 2: Senior Chat, 3: PDF Resources, 4: Dates, 5: Rank vs Marks, 6: Announce, 7: Students, 8: Helplines, 9: Receipts, 10: Colleges & Cutoffs

    val uniqueStudentsList = remember(allStudents) {
        FirebaseService.deduplicateStudentProfiles(allStudents)
    }

    // Student Management Search & Dialog State
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedStudentFilter by remember { mutableStateOf("ALL") } // ALL, PREMIUM, BLOCKED
    var studentDirectoryViewMode by remember { mutableStateOf("CARDS") } // "CARDS", "CREDENTIALS_TABLE"
    var showExportDossierDialog by remember { mutableStateOf(false) }
    var showDossierPreviewDialog by remember { mutableStateOf(false) }
    var selectedBookingForReceipt by remember { mutableStateOf<SeniorChatBooking?>(null) }
    var revealedPasswordsMap by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var revealAllPasswords by remember { mutableStateOf(false) }
    var dossierPreviewSearchQuery by remember { mutableStateOf("") }
    var showEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudentProfile by remember { mutableStateOf<UserProfile?>(null) }
    var editStudentName by remember { mutableStateOf("") }
    var editStudentEmail by remember { mutableStateOf("") }
    var editStudentRank by remember { mutableStateOf("") }
    var editStudentStream by remember { mutableStateOf("") }
    var editStudentRegDate by remember { mutableStateOf("") }
    var editStudentIsPremium by remember { mutableStateOf(false) }
    var editStudentIsBlocked by remember { mutableStateOf(false) }
    var editStudentBlockReason by remember { mutableStateOf("") }
    var editStudentIsForceLoggedOut by remember { mutableStateOf(false) }

    // Block Student with Custom Reason Dialog State
    var showBlockStudentDialog by remember { mutableStateOf(false) }
    var studentToBlock by remember { mutableStateOf<UserProfile?>(null) }
    var blockReasonInput by remember { mutableStateOf("") }
    var showConfirmLogoutAllDialog by remember { mutableStateOf(false) }

    // Permanent Student Deletion Dialog State
    var showDeleteStudentDialog by remember { mutableStateOf(false) }
    var studentToDeletePermanently by remember { mutableStateOf<UserProfile?>(null) }
    var deletePinInput by remember { mutableStateOf("") }
    var deletePinError by remember { mutableStateOf<String?>(null) }

    // Permanent Notification Deletion Dialog State (Deletes for ALL Student Portals)
    var notificationToDeletePermanently by remember { mutableStateOf<StudentNotification?>(null) }

    var showStudentTestResultsDialog by remember { mutableStateOf(false) }
    var viewingStudentForResults by remember { mutableStateOf<UserProfile?>(null) }
    var editingTestResultItem by remember { mutableStateOf<AdminTestResultItem?>(null) }
    var editTestScoreInput by remember { mutableStateOf("") }
    var editTestCorrectInput by remember { mutableStateOf("") }
    var editTestWrongInput by remember { mutableStateOf("") }

    // Rejection Reason Dialog State
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedRejectReceiptId by remember { mutableStateOf("") }
    var selectedRejectUserId by remember { mutableStateOf("") }
    var rejectionReasonInput by remember { mutableStateOf("") }

    // Receipt Full Preview Dialog State
    var showReceiptPreviewDialog by remember { mutableStateOf(false) }
    var previewReceiptUrl by remember { mutableStateOf("") }
    var previewReceiptStudentName by remember { mutableStateOf("") }
    var previewReceiptItem by remember { mutableStateOf<PaymentReceipt?>(null) }

    // Helpline Form State
    var helpTitle by remember { mutableStateOf("") }
    var helpContactValue by remember { mutableStateOf("") }
    var helpType by remember { mutableStateOf("EMAIL") }
    var helpDesc by remember { mutableStateOf("") }
    var helpActionUrl by remember { mutableStateOf("") }

    // College & Cutoff Form State
    var collegeEditingId by remember { mutableStateOf("") }
    var collegeNameInput by remember { mutableStateOf("") }
    var collegeCodeInput by remember { mutableStateOf("") }
    var collegeDistrictInput by remember { mutableStateOf("Bengaluru Urban") }
    var collegeTypeInput by remember { mutableStateOf("Autonomous") }
    var collegeTuitionFeeInput by remember { mutableStateOf("118000") }
    var collegeHostelFeeInput by remember { mutableStateOf("85000") }
    var collegeHasHostelInput by remember { mutableStateOf(true) }
    var collegeNaacInput by remember { mutableStateOf("A++") }
    var collegeNirfInput by remember { mutableStateOf("45") }
    var collegeAvgPkgInput by remember { mutableStateOf("14.5") }
    var collegeHighestPkgInput by remember { mutableStateOf("58.0") }
    var collegeRatingInput by remember { mutableStateOf("4.8") }
    var collegeImageResInput by remember { mutableStateOf("https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80") }
    var collegeLogoResInput by remember { mutableStateOf("") }
    var collegeWebsiteInput by remember { mutableStateOf("https://rvce.edu.in") }
    var collegeMapUrlInput by remember { mutableStateOf("RV College of Engineering Bengaluru") }
    var collegeRankRangeInput by remember { mutableStateOf("1 – 1,000") }
    var collegeCategoryInput by remember { mutableStateOf("GM") }
    var collegeCutoffRankInput by remember { mutableStateOf("1200") }
    var collegePlacementPctInput by remember { mutableStateOf("94") }
    var collegeRecruitersInput by remember { mutableStateOf("Microsoft, Google, Amazon, Bosch, Cisco") }
    var isSavingCollege by remember { mutableStateOf(false) }

    var collegeRankDropdownExpanded by remember { mutableStateOf(false) }
    var collegeCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var collegeDistrictDropdownExpanded by remember { mutableStateOf(false) }
    var collegeTypeDropdownExpanded by remember { mutableStateOf(false) }
    var collegeNaacDropdownExpanded by remember { mutableStateOf(false) }

    var showEditCollegeDialog by remember { mutableStateOf(false) }
    var showCutoffDialog by remember { mutableStateOf(false) }
    var selectedCollegeForCutoff by remember { mutableStateOf<CollegeInfo?>(null) }
    var cutoffBranchCodeInput by remember { mutableStateOf("CSE") }
    var cutoffBranchNameInput by remember { mutableStateOf("Computer Science & Engineering") }
    var cutoffGmInput by remember { mutableStateOf("1200") }
    var cutoff2aInput by remember { mutableStateOf("2100") }
    var cutoff2bInput by remember { mutableStateOf("2300") }
    var cutoff3aInput by remember { mutableStateOf("1400") }
    var cutoff3bInput by remember { mutableStateOf("1500") }
    var cutoffScInput by remember { mutableStateOf("12000") }
    var cutoffStInput by remember { mutableStateOf("18000") }

    // Branch Explorer Form State
    var branchEditingId by remember { mutableStateOf("") }
    var branchEditingCollegeId by remember { mutableStateOf("") }
    var branchCodeInput by remember { mutableStateOf("CSE") }
    var branchCollegeNameInput by remember { mutableStateOf("") }
    var branchCollegeTypeInput by remember { mutableStateOf("Autonomous") }
    var branchCollegeWebsiteInput by remember { mutableStateOf("") }
    var branchMapLocationInput by remember { mutableStateOf("") }
    var branchCollegeBannerUrlInput by remember { mutableStateOf("") }
    var branchGmCutoffInput by remember { mutableStateOf("1500") }
    var branchTuitionFeeInput by remember { mutableStateOf("125000") }
    var branchSalaryInput by remember { mutableStateOf("12.5") }
    var branchDistrictInput by remember { mutableStateOf("Bengaluru Urban") }
    var branchOverviewInput by remember { mutableStateOf("") }
    var branchExpandedDropdown by remember { mutableStateOf(false) }
    var branchExpandedTypeDropdown by remember { mutableStateOf(false) }
    var branchExpandedDistrictDropdown by remember { mutableStateOf(false) }
    var branchCategoryInput by remember { mutableStateOf("GM") }
    var branchHostelInput by remember { mutableStateOf(true) }
    var branchNaacInput by remember { mutableStateOf("A++") }
    var branchNirfInput by remember { mutableStateOf("45") }
    var branchHighestPkgInput by remember { mutableStateOf("45.0") }
    var branchRankSelectionInput by remember { mutableStateOf("1,000 to 5,000") }
    var branchExpandedCategoryDropdown by remember { mutableStateOf(false) }
    var branchExpandedNaacDropdown by remember { mutableStateOf(false) }
    var branchExpandedRankDropdown by remember { mutableStateOf(false) }

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

    // Test Series Custom Questions Staging & Management
    var tsStagedQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var tsQSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var tsQText by remember { mutableStateOf("") }
    var tsOptA by remember { mutableStateOf("") }
    var tsOptB by remember { mutableStateOf("") }
    var tsOptC by remember { mutableStateOf("") }
    var tsOptD by remember { mutableStateOf("") }
    var tsCorrectOpt by remember { mutableStateOf(Option.A) }
    var tsExplanation by remember { mutableStateOf("") }
    var isNewQuestionSectionExpanded by remember { mutableStateOf(true) }

    // Existing Test Series Quick Add Question & View Questions Dialogs
    var selectedSetForAddQuestion by remember { mutableStateOf<TestSeriesSet?>(null) }
    var selectedSetForViewQuestions by remember { mutableStateOf<TestSeriesSet?>(null) }
    var singleQSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var singleQText by remember { mutableStateOf("") }
    var singleOptA by remember { mutableStateOf("") }
    var singleOptB by remember { mutableStateOf("") }
    var singleOptC by remember { mutableStateOf("") }
    var singleOptD by remember { mutableStateOf("") }
    var singleCorrectOpt by remember { mutableStateOf(Option.A) }
    var singleExplanation by remember { mutableStateOf("") }

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

    val branchBannerImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxDimension = 1000, quality = 80)
            if (base64 != null) {
                branchCollegeBannerUrlInput = base64
                Toast.makeText(context, "📸 College Campus Screenshot / Image Selected!", Toast.LENGTH_SHORT).show()
            } else {
                branchCollegeBannerUrlInput = uri.toString()
                Toast.makeText(context, "Image selected: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val collegeBannerImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxDimension = 1000, quality = 80)
            if (base64 != null) {
                collegeImageResInput = base64
                Toast.makeText(context, "📸 College Banner Image Uploaded Successfully!", Toast.LENGTH_SHORT).show()
            } else {
                collegeImageResInput = uri.toString()
                Toast.makeText(context, "Image selected: $uri", Toast.LENGTH_SHORT).show()
            }
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

    // Announcement & Push Notifications Form State
    var annTitle by remember { mutableStateOf("") }
    var annMsg by remember { mutableStateOf("") }
    var annIsImportant by remember { mutableStateOf(false) }

    var notifTargetMode by remember { mutableStateOf("ALL") } // ALL, SPECIFIC
    var notifSelectedStudent by remember { mutableStateOf<UserProfile?>(null) }
    var notifStudentSearchQuery by remember { mutableStateOf("") }
    var notifType by remember { mutableStateOf("ANNOUNCEMENT") } // ANNOUNCEMENT, EXAM_ALERT, STUDY_MATERIAL, COUNSELLING, PAYMENT, BOOKING, SYSTEM
    var notifActionType by remember { mutableStateOf("NAV_HOME") } // NAV_HOME, NAV_PRACTICE, NAV_RESOURCES, NAV_SENIOR_CHAT
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }

    // Direct Notification Dialog (Triggered from student card in Tab 7)
    var showDirectStudentNotifDialog by remember { mutableStateOf(false) }
    var directNotifStudent by remember { mutableStateOf<UserProfile?>(null) }
    var directNotifTitle by remember { mutableStateOf("") }
    var directNotifMessage by remember { mutableStateOf("") }
    var directNotifType by remember { mutableStateOf("GENERAL") }

    // Broadcast Notification on Upload/Edit Dialog
    var showBroadcastUploadNotifDialog by remember { mutableStateOf(false) }
    var uploadNotifPreset by remember { mutableStateOf("STUDY_MATERIAL") }
    var uploadNotifTitle by remember { mutableStateOf("📚 New Study Material Uploaded") }
    var uploadNotifMessage by remember { mutableStateOf("Admin uploaded new study resources, formula sheets, and solved question papers. Check Resources tab now!") }
    var uploadNotifTargetMode by remember { mutableStateOf("ALL") }
    var uploadNotifActionType by remember { mutableStateOf("NAV_RESOURCES") }
    var uploadNotifSelectedStudent by remember { mutableStateOf<UserProfile?>(null) }

    // Upload section notification checkboxes
    var notifyOnQuestionAdd by remember { mutableStateOf(false) }
    var notifyOnTestSetAdd by remember { mutableStateOf(true) }
    var notifyOnResourceAdd by remember { mutableStateOf(true) }
    var notifyOnDateAdd by remember { mutableStateOf(false) }
    var notifyOnCollegeSave by remember { mutableStateOf(true) }
    var notifyOnCutoffSave by remember { mutableStateOf(true) }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1B4B)),
                title = {
                    Text("KCET Admin Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { showAdminLogoutConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Log Out / Back", tint = Color.White)
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            uploadNotifPreset = "STUDY_MATERIAL"
                            uploadNotifTitle = "📚 New Study Material Uploaded"
                            uploadNotifMessage = "Admin uploaded new study resources and notes. Check them out in the Resources section!"
                            uploadNotifActionType = "NAV_RESOURCES"
                            uploadNotifTargetMode = "ALL"
                            showBroadcastUploadNotifDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Send Notification",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "📢 SEND NOTIF",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        onClick = { showAdminLogoutConfirmDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDC2626), // Vibrant Red
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log Out",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOG OUT",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
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
                    text = { Text("💬 Mentors & Chat (${registeredMentors.size} M • ${seniorChatBookings.size} B)", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("🔗 Resources & Links (${resourcesList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
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
                    text = { Text("📢 Announce & Push (${announcements.size + allNotifications.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 7,
                    onClick = { selectedTab = 7 },
                    text = { Text("👨‍🎓 Students (${allStudents.distinctBy { FirebaseService.getStudentDeduplicationKey(it) }.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 8,
                    onClick = { selectedTab = 8 },
                    text = { Text("📞 Helplines (${helplines.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                val pendingCount = paymentReceipts.count { it.status == "UNDER_VERIFICATION" }
                Tab(
                    selected = selectedTab == 9,
                    onClick = { selectedTab = 9 },
                    text = {
                        Text(
                            text = if (pendingCount > 0) "💳 Receipts ($pendingCount PENDING)" else "💳 Receipts (${paymentReceipts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (pendingCount > 0) Color(0xFFFCD34D) else Color.White
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 10,
                    onClick = { selectedTab = 10 },
                    text = { Text("🏛️ Colleges & Cutoffs (${colleges.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 11,
                    onClick = { selectedTab = 11 },
                    text = { Text("🔬 Branch Explorer (${engineeringBranches.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 12,
                    onClick = { selectedTab = 12 },
                    text = { Text("⚔️ 1v1 Challenges (${adminChallenges.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF43F5E)) }
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

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = notifyOnQuestionAdd,
                                            onCheckedChange = { notifyOnQuestionAdd = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("📢 Notify all students about this question upload", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3730A3))
                                            Text("Sends an instant push notification linking to the Practice tab", fontSize = 10.sp, color = Color(0xFF6366F1))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (qText.isBlank() || optA.isBlank() || optB.isBlank()) {
                                            Toast.makeText(context, "Please fill question and options", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val subjectName = selectedSubject.name
                                        onAddQuestion(selectedSubject, qText, optA, optB, optC, optD, correctOpt, explanation)
                                        if (notifyOnQuestionAdd) {
                                            onSendDirectNotification(
                                                "📝 New $subjectName Question Added",
                                                "Admin uploaded fresh practice questions for $subjectName. Test your preparation now!",
                                                "EXAM_ALERT",
                                                "ALL",
                                                null,
                                                "NAV_PRACTICE"
                                            )
                                        }
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

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSendDirectNotification(
                                            "📝 Question Bank Updated (${questions.size + 1} Qs)",
                                            "Admin uploaded new KCET mock test questions. Boost your score in the Practice tab!",
                                            "EXAM_ALERT",
                                            "ALL",
                                            null,
                                            "NAV_PRACTICE"
                                        )
                                        Toast.makeText(context, "📢 Push notification sent to all students!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4338CA))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📢 Send Notification to Students (Question Bank Update)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🏆", fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Add New Test Series with Questions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Set test details and add questions, options, correct answer & solution explanations", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = tsTitle,
                                    onValueChange = { tsTitle = it },
                                    label = { Text("Test Series Title (e.g. KCET Grand Mock Test 2026 #1)") },
                                    placeholder = { Text("Enter mock test title...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
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
                                        value = tsDurationStr,
                                        onValueChange = { tsDurationStr = it },
                                        label = { Text("Duration (Mins)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { tsIsPremium = !tsIsPremium },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (tsIsPremium) Color(0xFFFEF3C7) else Color(0xFFE2E8F0),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (tsIsPremium) Color(0xFFD97706) else Color(0xFF94A3B8))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
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

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEEF2FF),
                                        border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("Questions Staged: ", fontSize = 11.sp, color = Color(0xFF4338CA))
                                            Text("${tsStagedQuestions.size}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4338CA))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = tsDescription,
                                    onValueChange = { tsDescription = it },
                                    label = { Text("Description & Instructions (Optional)") },
                                    placeholder = { Text("e.g. 60 Questions each for Physics, Chemistry & Maths") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // ==========================================
                                // QUESTION BUILDER CARD FOR THIS TEST SERIES
                                // ==========================================
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("✍️ Add Questions to this Test Series", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF312E81))
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (tsStagedQuestions.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = "${tsStagedQuestions.size} Questions Added",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (tsStagedQuestions.isNotEmpty()) Color(0xFF166534) else Color(0xFF64748B),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Subject Chips
                                        Text("Select Subject:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS, Subject.BIOLOGY).forEach { sub ->
                                                val isSelected = tsQSubject == sub
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { tsQSubject = sub }
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = sub.name.take(4),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = tsQText,
                                            onValueChange = { tsQText = it },
                                            label = { Text("Question Text / Problem Statement *") },
                                            placeholder = { Text("Type question statement here...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2,
                                            maxLines = 4
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Options A, B, C, D
                                        Text("Options (A, B, C, D):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = tsOptA,
                                            onValueChange = { tsOptA = it },
                                            label = { Text("Option A *") },
                                            placeholder = { Text("Option A text") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = tsOptB,
                                            onValueChange = { tsOptB = it },
                                            label = { Text("Option B *") },
                                            placeholder = { Text("Option B text") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = tsOptC,
                                            onValueChange = { tsOptC = it },
                                            label = { Text("Option C") },
                                            placeholder = { Text("Option C text") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = tsOptD,
                                            onValueChange = { tsOptD = it },
                                            label = { Text("Option D") },
                                            placeholder = { Text("Option D text") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Correct Option Selector
                                        Text("Select Correct Option (Answer Key) *:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(Option.A, Option.B, Option.C, Option.D).forEach { opt ->
                                                val isCorrect = tsCorrectOpt == opt
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isCorrect) Color(0xFF10B981) else Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF059669) else Color(0xFFCBD5E1)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { tsCorrectOpt = opt }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (isCorrect) {
                                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                        }
                                                        Text(
                                                            text = "Option ${opt.name}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isCorrect) Color.White else Color(0xFF334155)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = tsExplanation,
                                            onValueChange = { tsExplanation = it },
                                            label = { Text("Solution / Step-by-Step Explanation") },
                                            placeholder = { Text("Explain formula, reasoning and solution steps for students...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2,
                                            maxLines = 4
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                if (tsQText.isBlank() || tsOptA.isBlank() || tsOptB.isBlank()) {
                                                    Toast.makeText(context, "Please enter question text and at least Options A & B", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                val newStagedQ = Question(
                                                    id = 0,
                                                    subject = tsQSubject,
                                                    questionText = tsQText.trim(),
                                                    optionA = tsOptA.trim(),
                                                    optionB = tsOptB.trim(),
                                                    optionC = tsOptC.trim().ifBlank { "None of the above" },
                                                    optionD = tsOptD.trim().ifBlank { "All of the above" },
                                                    correctOption = tsCorrectOpt,
                                                    explanation = tsExplanation.trim().ifBlank { "Option ${tsCorrectOpt.name} is the correct KCET answer." },
                                                    testSetId = "",
                                                    isPremium = tsIsPremium
                                                )
                                                tsStagedQuestions = tsStagedQuestions + newStagedQ
                                                Toast.makeText(context, "✅ Question #${tsStagedQuestions.size} added to set!", Toast.LENGTH_SHORT).show()
                                                tsQText = ""
                                                tsOptA = ""
                                                tsOptB = ""
                                                tsOptC = ""
                                                tsOptD = ""
                                                tsExplanation = ""
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("➕ Add This Question to Set (${tsStagedQuestions.size} Added)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        // Staged Questions Preview List
                                        if (tsStagedQuestions.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("📋 Staged Questions for this Test Series (${tsStagedQuestions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                tsStagedQuestions.forEachIndexed { index, q ->
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color(0xFFF8FAFC),
                                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(4.dp),
                                                                        color = Color(0xFF6366F1).copy(alpha = 0.15f)
                                                                    ) {
                                                                        Text(
                                                                            text = q.subject.name,
                                                                            fontSize = 9.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF4338CA),
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text("Q${index + 1}: ${q.questionText.take(40)}...", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                                                }
                                                                IconButton(
                                                                    onClick = {
                                                                        tsStagedQuestions = tsStagedQuestions.filterIndexed { i, _ -> i != index }
                                                                        Toast.makeText(context, "Removed Question #${index + 1}", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Correct: Option ${q.correctOption.name} • A: ${q.optionA.take(20)} | B: ${q.optionB.take(20)}",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF059669),
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            if (q.explanation.isNotBlank()) {
                                                                Text(
                                                                    text = "💡 Sol: ${q.explanation.take(60)}...",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF64748B)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = notifyOnTestSetAdd,
                                            onCheckedChange = { notifyOnTestSetAdd = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("📢 Notify all students about this new Test Series Set", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3730A3))
                                            Text("Sends instant push notification with direct link to Practice tab", fontSize = 10.sp, color = Color(0xFF6366F1))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (tsTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter Test Series Title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val duration = tsDurationStr.toIntOrNull() ?: 20
                                        val setCategory = tsCategory
                                        val setTitle = tsTitle.trim()

                                        onAddTestSeriesWithQuestions(
                                            setTitle,
                                            setCategory,
                                            duration,
                                            tsIsPremium,
                                            tsDescription,
                                            tsStagedQuestions
                                        )

                                        if (notifyOnTestSetAdd) {
                                            val qMsg = if (tsStagedQuestions.isNotEmpty()) "${tsStagedQuestions.size} questions" else "practice questions"
                                            onSendDirectNotification(
                                                "🏆 New Test Series: $setTitle",
                                                "Admin uploaded $setTitle ($setCategory) with $qMsg. Practice now!",
                                                "EXAM_ALERT",
                                                "ALL",
                                                null,
                                                "NAV_PRACTICE"
                                            )
                                        }
                                        Toast.makeText(context, "🏆 Test Series '$setTitle' with ${tsStagedQuestions.size} questions saved to Firebase!", Toast.LENGTH_LONG).show()
                                        tsTitle = ""
                                        tsDescription = ""
                                        tsStagedQuestions = emptyList()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (tsStagedQuestions.isNotEmpty()) "Save Test Series & ${tsStagedQuestions.size} Questions to Firebase" else "Save Test Series Set to Firebase",
                                        fontWeight = FontWeight.Bold
                                    )
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

                                // Quick Actions: Add Question to this Set & View Questions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedSetForAddQuestion = set
                                            singleQText = ""
                                            singleOptA = ""
                                            singleOptB = ""
                                            singleOptC = ""
                                            singleOptD = ""
                                            singleExplanation = ""
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("➕ Add Question", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedSetForViewQuestions = set
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                                    ) {
                                        Text("👁️ Questions (${linkedQuestions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

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

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSendDirectNotification(
                                            "🏆 Mock Test Uploaded: ${set.title}",
                                            "Mock test questions with options and explanations have been uploaded for ${set.title}. Attempt the CBT test now!",
                                            "EXAM_ALERT",
                                            "ALL",
                                            null,
                                            "NAV_PRACTICE"
                                        )
                                        Toast.makeText(context, "📢 Push notification dispatched to all students!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📢 Send Notification to Students for this Test", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // Senior Live Chat & Mentor Management Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "💬 Senior Mentorship & Chat Management",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Review registered mentors, inspect credentials, and assign mentors to students.",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // KPI Summary Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("👨‍🏫 Mentors", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                            Text("${registeredMentors.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🎯 Confirmed", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                            Text("${seniorChatBookings.count { it.status == "Confirmed" }}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4ADE80))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("📬 Pending", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                            Text("${seniorChatBookings.count { it.status != "Confirmed" && it.status != "Declined" }}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Admin Pricing Management Card
                    item {
                        var chatPriceText by remember(liveChatPrice) { mutableStateOf(liveChatPrice.toString()) }
                        var subPriceText by remember(subscriptionPrice) { mutableStateOf(subscriptionPrice.toString()) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("💰 Admin Pricing Management (Live Chat & Subscription)", fontWeight = FontWeight.Bold, color = Color(0xFFFCD34D), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Modify prices below. All screens, badges, chat slots, and premium subscriptions across the app instantly sync to these modified prices.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chatPriceText,
                                        onValueChange = { chatPriceText = it },
                                        label = { Text("Live 1-to-1 Chat Slot (₹)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val p = chatPriceText.toIntOrNull()
                                            if (p != null && p > 0) {
                                                onUpdateLiveChatPrice(p)
                                                Toast.makeText(context, "Live Chat Price updated to ₹$p", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Invalid price", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                    ) {
                                        Text("Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = subPriceText,
                                        onValueChange = { subPriceText = it },
                                        label = { Text("Subscription Pass (₹)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val p = subPriceText.toIntOrNull()
                                            if (p != null && p > 0) {
                                                onUpdateSubscriptionPrice(p)
                                                Toast.makeText(context, "Subscription Price updated to ₹$p", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Invalid price", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text("Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Sub-navigation buttons (Bookings vs Registered Mentors)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { mentorSubTab = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mentorSubTab == 0) Color(0xFF4F46E5) else Color(0xFFE2E8F0),
                                    contentColor = if (mentorSubTab == 0) Color.White else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Student Bookings (${seniorChatBookings.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { mentorSubTab = 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mentorSubTab == 1) Color(0xFF4F46E5) else Color(0xFFE2E8F0),
                                    contentColor = if (mentorSubTab == 1) Color.White else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Registered Mentors (${registeredMentors.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (mentorSubTab == 0) {
                        // --- SUB-TAB 0: STUDENT BOOKINGS ---
                        item {
                            // Filter row for bookings
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val filters = listOf(
                                    "ALL" to "All (${seniorChatBookings.size})",
                                    "PENDING" to "Pending (${seniorChatBookings.count { it.status != "Confirmed" && it.status != "Declined" && it.status != "Expired" && it.status != "Completed" }})",
                                    "CONFIRMED" to "Confirmed (${seniorChatBookings.count { it.status == "Confirmed" }})",
                                    "EXPIRED" to "Ended/Expired (${seniorChatBookings.count { it.status == "Expired" || it.status == "Completed" }})",
                                    "UNASSIGNED" to "Unassigned (${seniorChatBookings.count { it.mentorName.isBlank() }})"
                                )
                                filters.forEach { (statusKey, label) ->
                                    FilterChip(
                                        selected = bookingFilterStatus == statusKey,
                                        onClick = { bookingFilterStatus = statusKey },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        if (filteredBookings.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No student bookings found matching filter.",
                                            color = Color.Gray,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredBookings) { booking ->
                                var adminNoteText by remember(booking.bookingId) { mutableStateOf(booking.adminMessage) }
                                var mentorNameText by remember(booking.bookingId) { mutableStateOf(booking.mentorName) }
                                var assignedMentorEmailText by remember(booking.bookingId) { mutableStateOf(booking.assignedMentorEmail) }

                                val matchedMentor = remember(registeredMentors, mentorNameText, assignedMentorEmailText) {
                                    registeredMentors.find { 
                                        (assignedMentorEmailText.isNotBlank() && it.email.trim().equals(assignedMentorEmailText.trim(), ignoreCase = true)) ||
                                        (mentorNameText.isNotBlank() && it.name.trim().equals(mentorNameText.trim(), ignoreCase = true))
                                    }
                                }

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
                                            Column(modifier = Modifier.weight(1f)) {
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
                                            val isExpiredBooking = booking.isExpired()
                                            val displayStatus = if (isExpiredBooking) "Expired ⏰" else booking.status
                                            val statusColor = when {
                                                isExpiredBooking -> Color(0xFFDC2626)
                                                booking.status == "Confirmed" -> Color(0xFF16A34A)
                                                booking.status == "Rejected by Mentor" -> Color(0xFFDC2626)
                                                booking.status == "Under Verification" || booking.status == "Payment Under Verification" -> Color(0xFFD97706)
                                                booking.status == "Waiting for Senior Confirmation" || booking.status == "Pending" -> Color(0xFF0284C7)
                                                booking.status == "Reschedule Needed" || booking.status == "Choose Other Time" -> Color(0xFFEA580C)
                                                else -> Color(0xFFDC2626)
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = statusColor.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = displayStatus,
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

                                        if (booking.paymentRefNumber.isNotBlank() || booking.receiptImageUri.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(10.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("💳 Payment UTR / Txn Ref: ", fontSize = 11.sp, color = Color(0xFF92400E))
                                                            Text(
                                                                text = booking.paymentRefNumber.ifBlank { "Receipt Uploaded" },
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFFB45309)
                                                            )
                                                        }
                                                        if (booking.receiptImageUri.isNotBlank()) {
                                                            Text(
                                                                text = "📎 Student uploaded payment receipt screenshot",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF059669),
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Button(
                                                        onClick = { selectedBookingForReceipt = booking },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("View Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }

                                        // MENTOR REJECTION BANNER IF APPLICABLE
                                        if (booking.status == "Rejected by Mentor") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFFEF2F2),
                                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = "🔴 Request Rejected by Mentor (${booking.mentorName})",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF991B1B)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Reason: \"${booking.rejectionReason.ifBlank { "No reason specified" }}\"",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFB91C1C)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "👉 Select another mentor from dropdown below to re-assign.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF7F1D1D),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // MENTOR ASSIGNMENT HIGHLIGHT CARD
                                        if (mentorNameText.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF0FDF4),
                                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.School,
                                                                contentDescription = null,
                                                                tint = Color(0xFF16A34A),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Column {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                    Text(
                                                                        text = "Assigned Mentor: $mentorNameText",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 13.sp,
                                                                        color = Color(0xFF14532D)
                                                                    )
                                                                    if (matchedMentor != null) {
                                                                        Surface(
                                                                            shape = RoundedCornerShape(6.dp),
                                                                            color = Color(0xFF16A34A)
                                                                        ) {
                                                                            Text(
                                                                                "VERIFIED",
                                                                                color = Color.White,
                                                                                fontSize = 9.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                if (matchedMentor != null) {
                                                                    Text(
                                                                        text = "🎓 ${matchedMentor.qualification.ifBlank { "Senior Mentor" }} • 🔬 ${matchedMentor.specialty.ifBlank { "KCET Guidance" }}",
                                                                        fontSize = 11.sp,
                                                                        color = Color(0xFF166534)
                                                                    )
                                                                    if (matchedMentor.phone.isNotBlank() || matchedMentor.email.isNotBlank()) {
                                                                        Text(
                                                                            text = "📞 ${matchedMentor.phone} | 📧 ${matchedMentor.email}",
                                                                            fontSize = 10.sp,
                                                                            color = Color(0xFF15803D)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        if (matchedMentor != null) {
                                                            TextButton(
                                                                onClick = { selectedMentorForDetails = matchedMentor },
                                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("Details ℹ️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFFFFBEB),
                                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "⚠️ No mentor assigned yet. Pick a registered mentor below.",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFB45309),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // QUICK SELECT REGISTERED MENTOR DROPDOWN
                                        var expandedMentorDropdown by remember { mutableStateOf(false) }
                                        
                                        ExposedDropdownMenuBox(
                                            expanded = expandedMentorDropdown,
                                            onExpandedChange = { expandedMentorDropdown = !expandedMentorDropdown }
                                        ) {
                                            OutlinedTextField(
                                                value = mentorNameText,
                                                onValueChange = { mentorNameText = it },
                                                label = { Text("Assign Senior Mentor") },
                                                placeholder = { Text("Choose mentor") },
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMentorDropdown)
                                                },
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedMentorDropdown,
                                                onDismissRequest = { expandedMentorDropdown = false }
                                            ) {
                                                if (registeredMentors.isNotEmpty()) {
                                                    registeredMentors.forEach { mentor ->
                                                        DropdownMenuItem(
                                                            text = { Text("${mentor.name} (${mentor.email.ifBlank { mentor.specialty }})") },
                                                            onClick = {
                                                                mentorNameText = mentor.name
                                                                assignedMentorEmailText = mentor.email
                                                                if (adminNoteText.isBlank()) {
                                                                    adminNoteText = "Assigned to Senior Mentor ${mentor.name}."
                                                                }
                                                                expandedMentorDropdown = false
                                                                Toast.makeText(context, "Selected ${mentor.name}!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    DropdownMenuItem(
                                                        text = { Text("No registered mentors found.") },
                                                        onClick = { expandedMentorDropdown = false }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Admin Response Message Input
                                        OutlinedTextField(
                                            value = adminNoteText,
                                            onValueChange = { adminNoteText = it },
                                            label = { Text("Admin Message / Verification Note") },
                                            placeholder = { Text("e.g. Payment verified! Forwarded to Senior Mentor.") },
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
                                                        onUpdateBookingStatus(
                                                            booking.bookingId,
                                                            "Waiting for Senior Confirmation",
                                                            adminNoteText.ifBlank { "Payment Verified by Admin. Forwarded to mentor." },
                                                            mentorNameText,
                                                            assignedMentorEmailText,
                                                            "" // clear rejection
                                                        )
                                                        Toast.makeText(context, "Payment Verified & Forwarded to $mentorNameText!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(if (booking.status == "Rejected by Mentor") "🔄 Re-assign Mentor" else "💳 Verify Payment & Assign", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        onUpdateBookingStatus(
                                                            booking.bookingId,
                                                            "Confirmed",
                                                            adminNoteText.ifBlank { "Slot Confirmed! Chat with assigned mentor ${mentorNameText}." },
                                                            mentorNameText,
                                                            assignedMentorEmailText,
                                                            ""
                                                        )
                                                        Toast.makeText(context, "Slot Confirmed with Mentor $mentorNameText!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("✅ Confirm Slot", fontSize = 11.sp)
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onUpdateBookingStatus(booking.bookingId, "Reschedule Needed", adminNoteText, mentorNameText, assignedMentorEmailText, "")
                                                        Toast.makeText(context, "Reschedule Note Sent to Student!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("💬 Suggest Other Time", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        onUpdateBookingStatus(booking.bookingId, "Declined", adminNoteText, mentorNameText, assignedMentorEmailText, "")
                                                        Toast.makeText(context, "Booking Declined", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("❌ Decline", fontSize = 11.sp)
                                                }
                                            }

                                            // END CHAT SESSION BUTTON (When status is Confirmed and not ended)
                                            if (booking.status == "Confirmed" && !booking.isExpired()) {
                                                Button(
                                                    onClick = {
                                                        val targetMentorName = mentorNameText.ifBlank { booking.mentorName }
                                                        val targetMentorEmail = assignedMentorEmailText.ifBlank { booking.assignedMentorEmail }
                                                        onUpdateBookingStatus(
                                                            booking.bookingId,
                                                            "Expired",
                                                            "1-on-1 Chat Session ended by Admin. Link expired.",
                                                            targetMentorName,
                                                            targetMentorEmail,
                                                            ""
                                                        )
                                                        Toast.makeText(context, "Chat Session Ended & Expired by Admin.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("🛑 End 1-on-1 Chat Session", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // CONTINUE / RESUME CHAT SESSION BUTTON (When status is Expired or Completed)
                                            if (booking.status == "Expired" || booking.status == "Completed" || booking.isExpired()) {
                                                Button(
                                                    onClick = {
                                                        val targetMentorName = mentorNameText.ifBlank { booking.mentorName }
                                                        val targetMentorEmail = assignedMentorEmailText.ifBlank { booking.assignedMentorEmail }
                                                        onUpdateBookingStatus(
                                                            booking.bookingId,
                                                            "Confirmed",
                                                            "Accepted by Senior Mentor ${targetMentorName.ifBlank { "Assigned Mentor" }} (Session continued by Admin)",
                                                            targetMentorName,
                                                            targetMentorEmail,
                                                            ""
                                                        )
                                                        Toast.makeText(context, "Chat Session Continued by Admin! Messages reappeared.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("▶️ Continue 1-on-1 Chat Session (Restore)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onDeleteBooking(booking.bookingId)
                                                    Toast.makeText(context, "Booking Removed", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("🗑️ Delete Request", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- SUB-TAB 1: REGISTERED MENTORS DIRECTORY ---
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Registered Mentors (${filteredMentors.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Verified Senior Mentors matching Student Portal",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Button(
                                    onClick = { showAddMentorDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = mentorSearchQuery,
                                onValueChange = { mentorSearchQuery = it },
                                placeholder = { Text("Search mentors by name, qualification, specialty, or email...") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        if (filteredMentors.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (mentorSearchQuery.isBlank()) "No registered mentors found." else "No mentors match '$mentorSearchQuery'",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Verified Senior Mentors appear here identically to the Student Portal. You can view student allocations, assign new students, or register additional mentors.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredMentors) { mentor ->
                                val assignedBookings = remember(seniorChatBookings, mentor.name) {
                                    seniorChatBookings.filter { it.mentorName.trim().equals(mentor.name.trim(), ignoreCase = true) }
                                }
                                val confirmedCount = assignedBookings.count { it.status == "Confirmed" }
                                val pendingCount = assignedBookings.count { it.status != "Confirmed" && it.status != "Declined" }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Header: Avatar with Pink Circle, Name with Verified Badge, Active Status Pill
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(24.dp),
                                                color = Color(0xFFFFF1F2),
                                                modifier = Modifier.size(48.dp),
                                                contentColor = Color(0xFFE11D48)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = mentor.name.take(1).uppercase(),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = mentor.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Verified",
                                                        tint = Color(0xFFE11D48),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = mentor.specialty.ifBlank { "Senior Mentor" },
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFE11D48),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Status badge
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (mentor.status.equals("Active", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = mentor.status.ifBlank { "Active" },
                                                    color = if (mentor.status.equals("Active", ignoreCase = true)) Color(0xFF166534) else Color(0xFFD97706),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "🎓 Qualification: ${mentor.qualification.ifBlank { "Senior Mentor Guide" }}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💼 Experience: ${mentor.experience.ifBlank { "1+ Years Exam Strategy coaching" }}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "📝 Bio: ${mentor.bio.ifBlank { "Passionate KCET senior guide helping aspirants clear exams with rank strategy." }}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )

                                        if (mentor.telegramOrInstagram.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "📱 Instagram/Telegram: ${mentor.telegramOrInstagram}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }

                                        if (mentor.email.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "✉️ Email: ${mentor.email}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        if (mentor.phone.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "📞 Phone: ${mentor.phone}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Workload & Student Assignment Stats
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF0FDF4),
                                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🎯 Confirmed: ", fontSize = 11.sp, color = Color(0xFF166534))
                                                    Text("$confirmedCount students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEF3C7),
                                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("📬 Pending: ", fontSize = 11.sp, color = Color(0xFF92400E))
                                                    Text("$pendingCount requests", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons: Details, Assign Student, Remove Mentor
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { selectedMentorForDetails = mentor },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View Details (${assignedBookings.size})", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { mentorForAssignStudentDialog = mentor },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Assign Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            IconButton(
                                                onClick = { mentorToDelete = mentor },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Mentor",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 3) {
                    // Resources & External Links Management Tab
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
                                        imageVector = Icons.Default.FolderShared,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7)
                                    )
                                    Text("Add External Resource / Google Drive Link", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Upload resources by pasting Google Drive links, official KEA links, or web URLs for students.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = pdfTitle,
                                    onValueChange = { pdfTitle = it },
                                    label = { Text("Resource Title") },
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
                                            color = if (pdfCategory == cat) Color(0xFF4F46E5) else Color.White,
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
                                        onClick = {
                                            pdfUrl = "https://drive.google.com/drive/folders/kcet_materials"
                                            pdfSize = "Drive Folder"
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
                                    ) {
                                        Text("📁 Paste Drive Link Helper", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            pdfTitle = "KCET Official Counselling Events Roadmap & Guidelines 2026"
                                            pdfCategory = "Counselling Roadmap"
                                            pdfYearOrSubject = "KEA Counselling 2026"
                                            pdfUrl = "https://cetonline.karnataka.gov.in/kea/kcet2026_counselling_roadmap.pdf"
                                            pdfSize = "External Link"
                                            pdfDesc = "Official KEA step-by-step roadmap covering Document Verification, Secret Key, Mock Option Entry, Seat Matrix, Choice Rules, and Reporting."
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                                    ) {
                                        Text("🗺️ Preset KEA Roadmap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = pdfYearOrSubject,
                                        onValueChange = { pdfYearOrSubject = it },
                                        label = { Text("Subject / Tag") },
                                        placeholder = { Text("Physics / KEA 2026") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pdfSize,
                                        onValueChange = { pdfSize = it },
                                        label = { Text("Source / Type") },
                                        placeholder = { Text("Google Drive / PDF") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pdfUrl,
                                    onValueChange = { pdfUrl = it },
                                    label = { Text("Google Drive / External Resource Link (URL)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("https://drive.google.com/file/d/... or https://cetonline.karnataka.gov.in/...") }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pdfDesc,
                                    onValueChange = { pdfDesc = it },
                                    label = { Text("Description & Notes for Students") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = notifyOnResourceAdd,
                                            onCheckedChange = { notifyOnResourceAdd = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("📢 Notify all students about this study material", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E40AF))
                                            Text("Sends instant push notification with direct link to Resources tab", fontSize = 10.sp, color = Color(0xFF3B82F6))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (pdfTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter resource title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (pdfUrl.isBlank()) {
                                            Toast.makeText(context, "Please enter Google Drive or external URL", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val resTitle = pdfTitle.trim()
                                        val resCategory = pdfCategory
                                        onAddResource(resTitle, resCategory, pdfYearOrSubject, pdfUrl, if (pdfSize.isBlank()) "External Link" else pdfSize, pdfDesc)
                                        if (notifyOnResourceAdd) {
                                            onSendDirectNotification(
                                                "📚 New Study Material: $resTitle",
                                                "Admin uploaded $resTitle ($resCategory). Access notes & study material now in Resources!",
                                                "MATERIAL_UPDATE",
                                                "ALL",
                                                null,
                                                "NAV_RESOURCES"
                                            )
                                        }
                                        Toast.makeText(context, "Resource Link Added & Saved to Database!", Toast.LENGTH_SHORT).show()
                                        pdfTitle = ""
                                        pdfUrl = ""
                                        pdfDesc = ""
                                        pdfYearOrSubject = ""
                                        pdfSize = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Text("➕ Save Resource Link to Database", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSendDirectNotification(
                                            "📚 New Study Materials & PYQs Uploaded",
                                            "Admin uploaded latest solved question papers, syllabus roadmap, and revision formulas. Check the Resources section now!",
                                            "MATERIAL_UPDATE",
                                            "ALL",
                                            null,
                                            "NAV_RESOURCES"
                                        )
                                        Toast.makeText(context, "📢 Push notification dispatched to all students!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📢 Send Notification to Students (Resource Upload)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Text("Existing Resource Links in Database (${resourcesList.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(resourcesList) { res ->
                        val isDrive = res.downloadUrl.contains("drive.google.com", ignoreCase = true) ||
                                      res.downloadUrl.contains("docs.google.com", ignoreCase = true)

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
                                    color = if (isDrive) Color(0xFFE0F2FE) else Color(0xFFEEF2FF),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDrive) Icons.Default.FolderShared else Icons.Default.Link,
                                        contentDescription = null,
                                        tint = if (isDrive) Color(0xFF0284C7) else Color(0xFF4F46E5),
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

                                        if (isDrive) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE0F2FE)
                                            ) {
                                                Text(
                                                    text = "DRIVE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF0369A1),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = res.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Subject/Tag: ${res.yearOrSubject}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                                    
                                    if (res.downloadUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = res.downloadUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF0284C7),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        if (res.downloadUrl.isNotBlank()) {
                                            val formattedUrl = if (!res.downloadUrl.startsWith("http://") && !res.downloadUrl.startsWith("https://")) "https://${res.downloadUrl}" else res.downloadUrl
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(formattedUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open Link", tint = Color(0xFF4F46E5))
                                    }

                                    IconButton(onClick = {
                                        onDeleteResource(res.id)
                                        Toast.makeText(context, "Resource Link Removed from Database", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Resource", tint = Color(0xFFEF4444))
                                    }
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
                                Text("🗓️ Add, Edit & Upload KEA Schedule Dates", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Manage official KEA counselling timelines, document verification dates, and option entry deadlines.", fontSize = 12.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onSyncKeaScheduleWithGemini?.invoke()
                                        Toast.makeText(context, "🤖 Syncing live KEA schedule via Gemini AI...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Text("⚡ Auto-Sync KEA Official Schedule via Gemini AI", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Manual Add / Edit Date", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(value = dateTitle, onValueChange = { dateTitle = it }, label = { Text("Event Title (e.g. KEA Document Verification)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateValue, onValueChange = { dateValue = it }, label = { Text("Date / Timeline (e.g. August 01 - August 15, 2026)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateStatus, onValueChange = { dateStatus = it }, label = { Text("Status (Active Now, Upcoming, Crucial, Completed)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = dateDesc, onValueChange = { dateDesc = it }, label = { Text("Description & Instructions") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFECFDF5),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = notifyOnDateAdd,
                                            onCheckedChange = { notifyOnDateAdd = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("📢 Notify all students about this schedule update", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF065F46))
                                            Text("Sends push notification with updated counselling timeline", fontSize = 10.sp, color = Color(0xFF059669))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (dateTitle.isBlank() || dateValue.isBlank()) {
                                            Toast.makeText(context, "Please fill title and date string", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val eventTitle = dateTitle.trim()
                                        val eventVal = dateValue.trim()
                                        onAddImportantDate(eventTitle, eventVal, dateStatus, dateDesc, dateCategory, isHighlighted)
                                        if (notifyOnDateAdd) {
                                            onSendDirectNotification(
                                                "📅 KCET Schedule Update: $eventTitle",
                                                "Admin updated KEA Counselling Schedule: $eventTitle is set for $eventVal. Check the dates timeline!",
                                                "COUNSELLING",
                                                "ALL",
                                                null,
                                                "NAV_HOME"
                                            )
                                        }
                                        Toast.makeText(context, "Saved & Uploaded to Firebase Firestore!", Toast.LENGTH_SHORT).show()
                                        dateTitle = ""
                                        dateValue = ""
                                        dateDesc = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("💾 Save / Upload Date to Firebase", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSendDirectNotification(
                                            "📅 KCET Schedule & Timelines Updated",
                                            "Admin uploaded updated dates for Document Verification, Option Entry & Counselling rounds. Check Home tab!",
                                            "COUNSELLING",
                                            "ALL",
                                            null,
                                            "NAV_HOME"
                                        )
                                        Toast.makeText(context, "📢 Push notification dispatched to all students!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📢 Send Notification to Students (Schedule Update)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

                                Column {
                                    OutlinedButton(
                                        onClick = {
                                            dateTitle = dateItem.eventTitle
                                            dateValue = dateItem.dateOrDeadline
                                            dateStatus = dateItem.status
                                            dateDesc = dateItem.description
                                            Toast.makeText(context, "Loaded into edit fields above", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Edit", fontSize = 11.sp)
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
                    // 📢 Announcements & Real-Time Push Notifications Tab

                    // 1. Post Official Announcement Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("📢 Broadcast Official Announcement", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Pushes to KCET Home feed & triggers status bar alert", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = annTitle,
                                    onValueChange = { annTitle = it },
                                    label = { Text("Announcement Title (e.g., KCET 2026 Registration Open)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = annMsg,
                                    onValueChange = { annMsg = it },
                                    label = { Text("Announcement Message Body / Guidelines") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 6
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (annIsImportant) Color(0xFFFEF2F2) else Color(0xFFF3F4F6),
                                    border = BorderStroke(1.dp, if (annIsImportant) Color(0xFFFCA5A5) else Color(0xFFE5E7EB)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (annIsImportant) "🚨 Urgent Priority Alert" else "📌 Standard Announcement",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (annIsImportant) Color(0xFFDC2626) else Color.DarkGray
                                            )
                                            Text(
                                                text = if (annIsImportant) "Badged with red Urgent label in student dashboard" else "Displayed with standard blue announcement tag",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Switch(
                                            checked = annIsImportant,
                                            onCheckedChange = { annIsImportant = it }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (annTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter an announcement title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (annMsg.isBlank()) {
                                            Toast.makeText(context, "Please enter announcement message details", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        onAddAnnouncement(annTitle.trim(), annMsg.trim(), annIsImportant)
                                        Toast.makeText(context, "🚀 Announcement broadcasted & synced with all students!", Toast.LENGTH_LONG).show()
                                        annTitle = ""
                                        annMsg = ""
                                        annIsImportant = false
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Broadcast Announcement to All Students", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Direct Targeted Push Notification Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0D9488),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("🔔 Targeted Push Notification Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Send instant custom alerts to all students or a specific user", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Audience Selector: All vs Specific Student
                                Text("🎯 Target Audience", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = notifTargetMode == "ALL",
                                        onClick = {
                                            notifTargetMode = "ALL"
                                            notifSelectedStudent = null
                                        },
                                        label = { Text("🌐 All Students (Global)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = notifTargetMode == "SPECIFIC",
                                        onClick = { notifTargetMode = "SPECIFIC" },
                                        label = { Text("🎯 Specific Student") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (notifTargetMode == "SPECIFIC") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Select Student Recipient (${allStudents.size} Registered):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = notifStudentSearchQuery,
                                        onValueChange = { notifStudentSearchQuery = it },
                                        placeholder = { Text("Search by name, email, or Student ID...", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val matchingStudents = if (notifStudentSearchQuery.isBlank()) {
                                        uniqueStudentsList.take(12)
                                    } else {
                                        val q = notifStudentSearchQuery.trim().lowercase()
                                        uniqueStudentsList.filter {
                                            it.name.lowercase().contains(q) ||
                                            it.email.lowercase().contains(q) ||
                                            it.formattedStudentId.lowercase().contains(q) ||
                                            it.studentId.lowercase().contains(q) ||
                                            it.uid.lowercase().contains(q)
                                        }.take(15)
                                    }

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        matchingStudents.forEach { student ->
                                            val isSelected = notifSelectedStudent?.uid == student.uid || 
                                                (student.email.isNotBlank() && notifSelectedStudent?.email.equals(student.email, ignoreCase = true))
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { notifSelectedStudent = student },
                                                label = {
                                                    Text(
                                                        text = "${student.name} (${student.formattedStudentId})",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    notifSelectedStudent?.let { selected ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "🎯 Target: ${selected.name} • ${selected.formattedStudentId} • ${selected.email.ifBlank { selected.uid }}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF1E40AF),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Notification Type / Category
                                Text("🏷️ Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val types = listOf(
                                        "ANNOUNCEMENT" to "📢 Announcement",
                                        "EXAM_ALERT" to "🚨 Exam Alert",
                                        "MATERIAL_UPDATE" to "📚 Study Material",
                                        "BOOKING" to "💬 Senior Chat",
                                        "PAYMENT" to "💳 Premium/Payment",
                                        "SYSTEM" to "⚙️ System Notice"
                                    )
                                    types.forEach { (typeKey, typeLabel) ->
                                        FilterChip(
                                            selected = notifType == typeKey,
                                            onClick = { notifType = typeKey },
                                            label = { Text(typeLabel, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = notifTitle,
                                    onValueChange = { notifTitle = it },
                                    label = { Text("Notification Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = notifMessage,
                                    onValueChange = { notifMessage = it },
                                    label = { Text("Notification Message") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 4
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (notifTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter notification title", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (notifMessage.isBlank()) {
                                            Toast.makeText(context, "Please enter notification message", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (notifTargetMode == "SPECIFIC" && notifSelectedStudent == null) {
                                            Toast.makeText(context, "Please select a target student", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        val targetUid = if (notifTargetMode == "SPECIFIC") notifSelectedStudent?.uid else "ALL"
                                        val targetEmail = if (notifTargetMode == "SPECIFIC") notifSelectedStudent?.email else ""
                                        val action = when (notifType) {
                                            "BOOKING" -> "NAV_SENIOR_CHAT"
                                            "MATERIAL_UPDATE" -> "NAV_RESOURCES"
                                            "EXAM_ALERT" -> "NAV_PRACTICE"
                                            else -> "NAV_HOME"
                                        }

                                        onSendDirectNotification(
                                            notifTitle.trim(),
                                            notifMessage.trim(),
                                            notifType,
                                            targetUid,
                                            targetEmail,
                                            action
                                        )

                                        val targetNameStr = if (notifTargetMode == "SPECIFIC") notifSelectedStudent?.name ?: "Student" else "All Students"
                                        Toast.makeText(context, "🔔 Notification dispatched to $targetNameStr!", Toast.LENGTH_LONG).show()

                                        notifTitle = ""
                                        notifMessage = ""
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (notifTargetMode == "SPECIFIC") "Send Direct Notification" else "Send Global Push Notification",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 3. Active Announcements List
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 Published Announcements (${announcements.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    if (announcements.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No announcements published yet", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(announcements, key = { "ann_${it.id}" }) { ann ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (ann.isImportant) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (ann.isImportant) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFFEE2E2),
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                                                ) {
                                                    Text(
                                                        text = "🚨 URGENT",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFDC2626),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = ann.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 15.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "📅 ${ann.date}", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = ann.message, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteAnnouncement(ann.id)
                                            Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Announcement", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }

                    // 4. Published Push Notifications History
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📨 Notifications History (${allNotifications.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    if (allNotifications.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No notifications recorded yet", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(allNotifications, key = { "notif_${it.id}" }) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (notif.type) {
                                                    "ANNOUNCEMENT" -> Color(0xFFEEF2FF)
                                                    "EXAM_ALERT" -> Color(0xFFFEF2F2)
                                                    "MATERIAL_UPDATE" -> Color(0xFFF0FDF4)
                                                    "BOOKING" -> Color(0xFFFDF4FF)
                                                    "STUDENT_UPDATE" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFF3F4F6)
                                                }
                                            ) {
                                                Text(
                                                    text = when (notif.type) {
                                                        "STUDENT_UPDATE" -> "📝 PROFILE UPDATED"
                                                        else -> notif.type
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (notif.type) {
                                                        "ANNOUNCEMENT" -> Color(0xFF4F46E5)
                                                        "EXAM_ALERT" -> Color(0xFFDC2626)
                                                        "MATERIAL_UPDATE" -> Color(0xFF16A34A)
                                                        "BOOKING" -> Color(0xFF9333EA)
                                                        "STUDENT_UPDATE" -> Color(0xFFB45309)
                                                        else -> Color(0xFF4B5563)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (notif.targetUserId == "ALL" || notif.targetUserId.isNullOrBlank()) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = if (notif.targetUserId == "ALL" || notif.targetUserId.isNullOrBlank()) "🌐 ALL" else "🎯 User: ${notif.targetUserEmail?.take(15) ?: notif.targetUserId}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (notif.targetUserId == "ALL" || notif.targetUserId.isNullOrBlank()) Color(0xFF2563EB) else Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = notif.message, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "🕒 ${notif.timestamp}", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    IconButton(
                                        onClick = {
                                            notificationToDeletePermanently = notif
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Notification", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 7) {
                    // 👨‍🎓 Students Control & Management Tab
                    val filteredStudents = uniqueStudentsList.filter { student ->
                        val q = studentSearchQuery.trim()
                        val matchesSearch = q.isBlank() ||
                            student.name.contains(q, ignoreCase = true) ||
                            student.email.contains(q, ignoreCase = true) ||
                            student.kcetTargetRank.contains(q, ignoreCase = true) ||
                            student.targetStream.contains(q, ignoreCase = true) ||
                            student.signInMethod.contains(q, ignoreCase = true) ||
                            student.uid.contains(q, ignoreCase = true) ||
                            student.formattedStudentId.contains(q, ignoreCase = true) ||
                            student.studentId.contains(q, ignoreCase = true)

                        val matchesFilter = when (selectedStudentFilter) {
                            "PREMIUM" -> student.isPremium
                            "BLOCKED" -> student.isBlocked || student.isForceLoggedOut
                            "UPDATED" -> allNotifications.any { notif ->
                                notif.type == "STUDENT_UPDATE" &&
                                (notif.targetUserId == student.uid ||
                                 (!notif.targetUserEmail.isNullOrBlank() && notif.targetUserEmail.equals(student.email, ignoreCase = true)) ||
                                 (student.formattedStudentId.isNotBlank() && notif.message.contains(student.formattedStudentId)) ||
                                 (student.email.isNotBlank() && notif.message.contains(student.email, ignoreCase = true)))
                            }
                            else -> true
                        }

                        matchesSearch && matchesFilter
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "👨‍🎓 Admin Control: Student Database & Credentials",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Full directory of student profiles, IDs, passwords, test scores, and remote account management.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { showConfirmLogoutAllDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("🚨 Logout All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Action Bar: Download Dossier & Print PDF
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showExportDossierDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.2f),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📥 Download Dossier", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val html = StudentDossierExporter.generateDossierHtml(filteredStudents, allStudentTestResults)
                                            StudentDossierExporter.printDossier(context, html, "KCET_Students_Complete_Directory")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1B4B)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🖨️ Print / PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // View Mode Selector (Segmented Cards vs Table/Directory)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            onClick = { studentDirectoryViewMode = "CARDS" },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (studentDirectoryViewMode == "CARDS") Color(0xFF312E81) else Color.Transparent,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ViewAgenda,
                                                    contentDescription = null,
                                                    tint = if (studentDirectoryViewMode == "CARDS") Color.White else Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "🗂️ Management Cards",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (studentDirectoryViewMode == "CARDS") Color.White else Color.DarkGray
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = { studentDirectoryViewMode = "CREDENTIALS_TABLE" },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (studentDirectoryViewMode == "CREDENTIALS_TABLE") Color(0xFF312E81) else Color.Transparent,
                                            modifier = Modifier.weight(1.3f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TableChart,
                                                    contentDescription = null,
                                                    tint = if (studentDirectoryViewMode == "CREDENTIALS_TABLE") Color.White else Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "📋 ID & Passwords Directory",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (studentDirectoryViewMode == "CREDENTIALS_TABLE") Color.White else Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Search Field
                                OutlinedTextField(
                                    value = studentSearchQuery,
                                    onValueChange = { studentSearchQuery = it },
                                    label = { Text("🔍 Search student by name, email, ID, or password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Filter Chips FlowRow - Wraps neatly without horizontal overflow or white gap
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedStudentFilter == "ALL",
                                        onClick = { selectedStudentFilter = "ALL" },
                                        label = { Text("All (${uniqueStudentsList.size})") }
                                    )
                                    val updatedCount = remember(uniqueStudentsList, allNotifications) {
                                        uniqueStudentsList.count { std ->
                                            allNotifications.any { notif ->
                                                notif.type == "STUDENT_UPDATE" &&
                                                (notif.targetUserId == std.uid ||
                                                 (!notif.targetUserEmail.isNullOrBlank() && notif.targetUserEmail.equals(std.email, ignoreCase = true)) ||
                                                 (std.formattedStudentId.isNotBlank() && notif.message.contains(std.formattedStudentId)) ||
                                                 (std.email.isNotBlank() && notif.message.contains(std.email, ignoreCase = true)))
                                            }
                                        }
                                    }
                                    FilterChip(
                                        selected = selectedStudentFilter == "UPDATED",
                                        onClick = { selectedStudentFilter = "UPDATED" },
                                        label = { Text("📝 Profile Updated ($updatedCount)") }
                                    )
                                    FilterChip(
                                        selected = selectedStudentFilter == "PREMIUM",
                                        onClick = { selectedStudentFilter = "PREMIUM" },
                                        label = { Text("👑 Premium (${uniqueStudentsList.count { it.isPremium }})") }
                                    )
                                    FilterChip(
                                        selected = selectedStudentFilter == "BLOCKED",
                                        onClick = { selectedStudentFilter = "BLOCKED" },
                                        label = { Text("🚫 Blocked/Logged Out (${uniqueStudentsList.count { it.isBlocked || it.isForceLoggedOut }})") }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredStudents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No student accounts match your filter (${selectedStudentFilter}).", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Registered student profiles appearing in Firebase will automatically display here in real-time.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            studentSearchQuery = ""
                                            selectedStudentFilter = "ALL"
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Clear Filter & Show All Students (${uniqueStudentsList.size})")
                                    }
                                }
                            }
                        }
                    } else if (studentDirectoryViewMode == "CREDENTIALS_TABLE") {
                        // 📋 High-Density ID & Passwords Directory View
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1B4B),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📋 Student Credentials Directory (${filteredStudents.size} Records)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "All student personal info, ID, and passwords displayed clearly one-by-one.",
                                            color = Color(0xFFC7D2FE),
                                            fontSize = 10.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { revealAllPasswords = !revealAllPasswords },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (revealAllPasswords) Color(0xFFE11D48) else Color(0xFF4F46E5)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (revealAllPasswords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (revealAllPasswords) "Hide All" else "Reveal All",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val txt = StudentDossierExporter.generateDossierPlainText(filteredStudents, allStudentTestResults)
                                                StudentDossierExporter.copyToClipboard(context, txt, "All Students Directory")
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(filteredStudents, key = { index, student -> "${student.uid}_cred_$index" }) { index, student ->
                            val studentTests = filterStudentTests(allStudentTestResults, student)
                            val avgScorePct = if (studentTests.isNotEmpty()) studentTests.map { it.percentage }.average().toFloat() else 0f
                            val bestScore = studentTests.maxOfOrNull { it.score } ?: 0
                            val isRevealed = revealAllPasswords || (revealedPasswordsMap[student.uid] == true)
                            val pass = student.displayPassword

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Row 1: Index, Name, Badges
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF4F46E5)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (student.isPremium) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, if (student.isPremium) Color(0xFFF59E0B) else Color(0xFFCBD5E1))
                                            ) {
                                                Text(
                                                    text = if (student.isPremium) "👑 PRO" else "🎓 FREE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (student.isPremium) Color(0xFF92400E) else Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (student.isBlocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                                border = BorderStroke(1.dp, if (student.isBlocked) Color(0xFFEF4444) else Color(0xFF22C55E))
                                            ) {
                                                Text(
                                                    text = if (student.isBlocked) "🚫 BLOCKED" else "🟢 ACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (student.isBlocked) Color(0xFF991B1B) else Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Highlighted Credentials Box (ID & PASSWORD)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Student ID Row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🆔 Student ID: ", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = student.formattedStudentId,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1E3A8A)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { StudentDossierExporter.copyToClipboard(context, student.formattedStudentId, "Student ID") },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                                }
                                            }

                                            // Email / Phone Row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Text("📧 Login: ", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = if (student.email.isNotBlank()) student.email else "No email",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF1E3A8A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { StudentDossierExporter.copyToClipboard(context, student.email, "Login Email") },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                                }
                                            }

                                            // PASSWORD Row with Yellow Highlight & Reveal Toggle
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEFCE8),
                                                border = BorderStroke(1.dp, Color(0xFFFACC15)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Password: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                                                        Text(
                                                            text = if (isRevealed) pass else "••••••••",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFF713F12)
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        IconButton(
                                                            onClick = {
                                                                revealedPasswordsMap = revealedPasswordsMap.toMutableMap().apply {
                                                                    put(student.uid, !isRevealed)
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                                contentDescription = null,
                                                                tint = Color(0xFF854D0E),
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { StudentDossierExporter.copyToClipboard(context, pass, "Password") },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Personal & Academic Details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text("🎯 TARGET RANK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Text(student.kcetTargetRank, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text("🎓 STREAM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Text(student.targetStream, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFAF5FF),
                                            border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text("📊 CBT TESTS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                                                Text("${studentTests.size} Tests (${bestScore}p)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val credText = "Student: ${student.name}\nID: ${student.formattedStudentId}\nEmail: ${student.email}\nPassword: $pass\nTarget: ${student.kcetTargetRank} | ${student.targetStream}"
                                                StudentDossierExporter.copyToClipboard(context, credText, "${student.name} Dossier")
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy Dossier", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewingStudentForResults = student
                                                showStudentTestResultsDialog = true
                                            },
                                            modifier = Modifier.weight(0.9f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("📊 Scores (${studentTests.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                editingStudentProfile = student
                                                editStudentName = student.name
                                                editStudentEmail = student.email
                                                editStudentRank = student.kcetTargetRank
                                                editStudentStream = student.targetStream
                                                editStudentRegDate = student.registrationDate
                                                editStudentIsPremium = student.isPremium
                                                editStudentIsBlocked = student.isBlocked
                                                editStudentBlockReason = student.blockReason
                                                editStudentIsForceLoggedOut = student.isForceLoggedOut
                                                showEditStudentDialog = true
                                            },
                                            modifier = Modifier.weight(0.7f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("✏️ Edit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredStudents, key = { it.uid.ifBlank { FirebaseService.getStudentDeduplicationKey(it) } }) { student ->
                            val studentTests = filterStudentTests(allStudentTestResults, student)
                            val avgScorePct = if (studentTests.isNotEmpty()) studentTests.map { it.percentage }.average().toFloat() else 0f
                            val bestScore = studentTests.maxOfOrNull { it.score } ?: 0
                            val isPassRevealed = revealAllPasswords || (revealedPasswordsMap[student.uid] == true)
                            val stdPass = student.displayPassword

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Header: Avatar, Name, Email, ID, Badges, and Delete Icon
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (student.isPremium) Color(0xFFFEF3C7) else Color(0xFFEEF2FF),
                                                border = BorderStroke(1.5.dp, if (student.isPremium) Color(0xFFF59E0B) else Color(0xFF6366F1)),
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                if (!student.photoUrl.isNullOrBlank()) {
                                                    coil.compose.AsyncImage(
                                                        model = student.photoUrl,
                                                        contentDescription = student.name,
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = student.name.take(2).uppercase().ifBlank { "ST" },
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (student.isPremium) Color(0xFFD97706) else Color(0xFF4F46E5),
                                                            fontSize = 17.sp
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontSize = 15.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (student.email.isNotBlank()) student.email else "No email provided",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "🆔 ${student.formattedStudentId}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4338CA)
                                                    )
                                                    Text("•", color = Color.Gray, fontSize = 9.sp)
                                                    Text(
                                                        text = "UID: ${student.uid.take(10)}...",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 9.sp,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Status Badges & Delete Action Button aligned neatly in top-right
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                if (student.isPremium) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFEF3C7),
                                                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                                    ) {
                                                        Text(
                                                            text = "👑 PREMIUM",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB45309),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFF3F4F6)
                                                    ) {
                                                        Text(
                                                            text = "🎓 FREE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF4B5563),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                if (student.isBlocked) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFEE2E2),
                                                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                                                    ) {
                                                        Text(
                                                            text = "🚫 BLOCKED",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB91C1C),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else if (student.isForceLoggedOut) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFFEDD5),
                                                        border = BorderStroke(1.dp, Color(0xFFF97316))
                                                    ) {
                                                        Text(
                                                            text = "🚪 LOGGED OUT",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFC2410C),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFDCFCE7),
                                                        border = BorderStroke(1.dp, Color(0xFF22C55E))
                                                    ) {
                                                        Text(
                                                            text = "🟢 ACTIVE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF15803D),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Surface(
                                                onClick = {
                                                    studentToDeletePermanently = student
                                                    deletePinInput = ""
                                                    deletePinError = null
                                                    showDeleteStudentDialog = true
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEE2E2),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Student",
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Student Profile Details: Structured 2x2 Grid with zero white space gaps
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("🎯 Rank: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                                                        Text(
                                                            text = student.kcetTargetRank,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF4F46E5),
                                                            fontSize = 10.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("📅 Joined: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                                                        Text(
                                                            text = student.registrationDate,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.DarkGray,
                                                            fontSize = 10.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.weight(1.3f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("🎓 Stream: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                                                        Text(
                                                            text = student.targetStream,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.DarkGray,
                                                            fontSize = 10.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.weight(0.9f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("🔐 Auth: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                                                        Text(
                                                            text = student.signInMethod,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0D9488),
                                                            fontSize = 10.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Dedicated Credentials Box: ID, Login Identifier, and Password with reveal/copy
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Login Credentials & Access", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E3A8A))
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            revealedPasswordsMap = revealedPasswordsMap.toMutableMap().apply {
                                                                put(student.uid, !isPassRevealed)
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isPassRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = if (isPassRevealed) "Hide Password" else "Show Password",
                                                            tint = Color(0xFF2563EB),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val credText = "Student: ${student.name}\nID: ${student.formattedStudentId}\nLogin: ${student.email}\nPassword: $stdPass"
                                                            StudentDossierExporter.copyToClipboard(context, credText, "${student.name} Credentials")
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Credentials", tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text("STUDENT ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                                            Text(student.formattedStudentId, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E40AF))
                                                        }
                                                        IconButton(
                                                            onClick = { StudentDossierExporter.copyToClipboard(context, student.formattedStudentId, "Student ID") },
                                                            modifier = Modifier.size(20.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFFEFCE8),
                                                    border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                                                    modifier = Modifier.weight(1.2f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text("PASSWORD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                                                            Text(
                                                                text = if (isPassRevealed) stdPass else "••••••••",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF713F12)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { StudentDossierExporter.copyToClipboard(context, stdPass, "Password") },
                                                            modifier = Modifier.size(20.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = Color(0xFF854D0E), modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val recentStudentUpdate = remember(allNotifications, student) {
                                        allNotifications.firstOrNull { notif ->
                                            notif.type == "STUDENT_UPDATE" &&
                                            (notif.targetUserId == student.uid ||
                                             (!notif.targetUserEmail.isNullOrBlank() && notif.targetUserEmail.equals(student.email, ignoreCase = true)) ||
                                             (student.formattedStudentId.isNotBlank() && notif.message.contains(student.formattedStudentId)) ||
                                             (student.email.isNotBlank() && notif.message.contains(student.email, ignoreCase = true)))
                                        }
                                    }
                                    if (recentStudentUpdate != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("📝", fontSize = 14.sp)
                                                Column {
                                                    Text(
                                                        text = "Profile Info Updated (${recentStudentUpdate.timestamp})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                    Text(
                                                        text = recentStudentUpdate.message.substringAfter(":\n", recentStudentUpdate.message),
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF78350F)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (student.isBlocked) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF2F2),
                                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                                Column {
                                                    Text(
                                                        text = "🚫 Account Blocked (Login Denied)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB91C1C)
                                                    )
                                                    Text(
                                                        text = if (student.blockReason.isNotBlank()) "Reason: ${student.blockReason}" else "Reason: Account blocked by Administrator",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF7F1D1D)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Mock Test Analytics Summary Bar
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF0FDF4),
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📝 Tests Taken: ${studentTests.size}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF166534)
                                            )
                                            Text(
                                                text = if (studentTests.isNotEmpty()) "📊 Avg: ${String.format("%.1f", avgScorePct)}%" else "📊 Avg: N/A",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF166534)
                                            )
                                            Text(
                                                text = if (studentTests.isNotEmpty()) "🏆 Best: $bestScore pts" else "🏆 Best: N/A",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                    // Action Buttons Row 1: 3 Balanced, Equal-Width Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                editingStudentProfile = student
                                                editStudentName = student.name
                                                editStudentEmail = student.email
                                                editStudentRank = student.kcetTargetRank
                                                editStudentStream = student.targetStream
                                                editStudentRegDate = student.registrationDate
                                                editStudentIsPremium = student.isPremium
                                                editStudentIsBlocked = student.isBlocked
                                                editStudentBlockReason = student.blockReason
                                                editStudentIsForceLoggedOut = student.isForceLoggedOut
                                                showEditStudentDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("✏️ Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                directNotifStudent = student
                                                directNotifTitle = "Important Notice for ${student.name}"
                                                directNotifMessage = ""
                                                directNotifType = "GENERAL"
                                                showDirectStudentNotifDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D9488)),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("🔔 Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                onForceLogoutStudent(student.uid)
                                                Toast.makeText(context, "🚪 Force Logged Out '${student.name}' from active session!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("🚪 Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Action Buttons Row 2: 3 Balanced, Equal-Width Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onGrantOrRevokePremiumByAdmin(student.uid, !student.isPremium)
                                                val statusStr = if (!student.isPremium) "Granted Premium Access!" else "Revoked Premium Access"
                                                Toast.makeText(context, "Student '${student.name}' $statusStr", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (student.isPremium) "👑 Revoke" else "👑 Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (student.isBlocked) {
                                                    onToggleBlockStudent(student.uid, false, "")
                                                    Toast.makeText(context, "Student '${student.name}' Unblocked! They can now log in.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    studentToBlock = student
                                                    blockReasonInput = ""
                                                    showBlockStudentDialog = true
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = if (student.isBlocked) ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)) else ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (student.isBlocked) "🔓 Unblock" else "🚫 Block", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewingStudentForResults = student
                                                showStudentTestResultsDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("📊 Scores (${studentTests.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedButton(
                                        onClick = {
                                            studentToDeletePermanently = student
                                            deletePinInput = ""
                                            deletePinError = null
                                            showDeleteStudentDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color(0xFFFEF2F2),
                                            contentColor = Color(0xFFDC2626)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Delete Student Permanently",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
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
                                    text = "Add student support helpline email, phone numbers, or WhatsApp contact lines.",
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
                                            helpContactValue = "@ULLAS_U_SUVARNA"
                                            helpType = "INSTAGRAM"
                                            helpDesc = "Follow & DM @ULLAS_U_SUVARNA on Instagram for live KCET updates."
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
                                    label = { Text("Contact Value (e.g. KcetGenZservice@gmail.com / @ULLAS_U_SUVARNA)") },
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
                } else if (selectedTab == 9) {
                    // Payment Verifications Tab
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("💳 Student Payment Verifications (₹299 Premium Offer)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Review student paid receipts, verify transaction UTR numbers, approve to grant permanent premium access, or reject with a reason.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }

                    if (paymentReceipts.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Text("No payment receipts submitted yet.", modifier = Modifier.padding(20.dp), color = Color.Gray)
                            }
                        }
                    } else {
                        items(paymentReceipts, key = { it.receiptId }) { receipt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                                text = receipt.userName.ifBlank { "Student Request" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "${receipt.userEmail} • ${receipt.userPhone}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        val (statusText, statusBg, statusFg) = when (receipt.status) {
                                            "VERIFIED" -> Triple("✅ VERIFIED (PREMIUM ACTIVE)", Color(0xFFDCFCE7), Color(0xFF15803D))
                                            "REJECTED" -> Triple("❌ REJECTED", Color(0xFFFEE2E2), Color(0xFFB91C1C))
                                            else -> Triple("⏳ UNDER VERIFICATION", Color(0xFFFEF3C7), Color(0xFFB45309))
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = statusBg
                                        ) {
                                            Text(
                                                text = statusText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusFg,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Amount Paid", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text("₹${receipt.amountPaid}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                                        }

                                        Column {
                                            Text("UTR / Transaction Ref", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(receipt.transactionRef.ifBlank { "N/A" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }

                                        Column {
                                            Text("Submitted Date", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(receipt.submittedAtFormatted, fontSize = 12.sp, color = Color(0xFF334155))
                                        }
                                    }

                                    // Uploaded receipt screenshot / payment proof preview card
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.UploadFile,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4F46E5),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Uploaded Payment Receipt Image",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF334155)
                                                    )
                                                }
                                                
                                                TextButton(
                                                    onClick = {
                                                        previewReceiptItem = receipt
                                                        previewReceiptUrl = receipt.receiptImageUri
                                                        previewReceiptStudentName = receipt.userName.ifBlank { "Student Request" }
                                                        showReceiptPreviewDialog = true
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text("🔍 View Fullscreen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5))
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            val receiptBmp = remember(receipt.receiptImageUri) {
                                                ImageUtils.loadReceiptBitmap(context, receipt.receiptImageUri)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(190.dp)
                                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        previewReceiptItem = receipt
                                                        previewReceiptUrl = receipt.receiptImageUri
                                                        previewReceiptStudentName = receipt.userName.ifBlank { "Student Request" }
                                                        showReceiptPreviewDialog = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (receiptBmp != null) {
                                                    Image(
                                                        bitmap = receiptBmp.asImageBitmap(),
                                                        contentDescription = "Receipt Image Preview",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(4.dp)
                                                    )
                                                } else if (receipt.receiptImageUri.isNotBlank() && receipt.receiptImageUri.startsWith("http")) {
                                                    AsyncImage(
                                                        model = receipt.receiptImageUri,
                                                        contentDescription = "Receipt Image Preview",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(4.dp)
                                                    )
                                                } else {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(12.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ReceiptLong,
                                                            contentDescription = null,
                                                            tint = Color(0xFF6366F1),
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "UTR Ref: ${receipt.transactionRef.ifBlank { "#REC-${receipt.receiptId.takeLast(6)}" }}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF1E293B)
                                                        )
                                                        Text(
                                                            text = "Amount: ₹${receipt.amountPaid} • Tap to view full voucher",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "💡 Click on the image preview to view it fullscreen with zoom & UTR details.",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            )
                                        }
                                    }

                                    if (receipt.status == "REJECTED" && receipt.rejectionReason.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Rejection Reason: ${receipt.rejectionReason}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }

                                    if (receipt.status == "UNDER_VERIFICATION") {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    onApproveReceipt(receipt.receiptId, receipt.userId)
                                                    Toast.makeText(context, "Payment Accepted! Student is now Premium permanently.", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier.weight(1.2f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                            ) {
                                                Text("✅ Accept & Grant Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    selectedRejectReceiptId = receipt.receiptId
                                                    selectedRejectUserId = receipt.userId
                                                    rejectionReasonInput = ""
                                                    showRejectDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                            ) {
                                                Text("❌ Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 10) {
                    val karnatakaDistrictsList = listOf(
                        "Bengaluru Urban", "Bengaluru Rural", "Mysuru", "Dakshina Kannada",
                        "Dharwad", "Udupi", "Belagavi", "Tumakuru", "Shivamogga", "Davanagere",
                        "Ballari", "Kalaburagi", "Hassan", "Mandya", "Bagalkote", "Bidar",
                        "Chamarajanagar", "Chikkaballapura", "Chikkamagaluru", "Chitradurga",
                        "Gadag", "Haveri", "Kodagu", "Kolar", "Koppal", "Raichur",
                        "Ramanagara", "Uttara Kannada", "Vijayanagara", "Vijayapura", "Yadgir"
                    )
                    val collegeTypesList = listOf("Autonomous", "Government", "Private", "Deemed University", "Private University")
                    val rankRangesList = listOf(
                        "1 – 1,000", "1,000 – 2,500", "2,500 – 5,000", "5,000 – 10,000",
                        "10,000 – 15,000", "15,000 – 25,000", "25,000 – 50,000", "50,000 – 75,000", "75,000 – 100,000+"
                    )
                    val categoriesList = listOf("GM", "2A", "2B", "3A", "3B", "SC", "ST")
                    val naacGradesList = listOf("A++", "A+", "A", "B++", "B+", "B", "NA")

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (collegeEditingId.isBlank()) "🏛️ Add New College & Cutoffs" else "✏️ Edit College & Cutoff Details",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Directly connected to Student College Recommendation & Predictor",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0D9488),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Section: Rank Range & Category Dropdowns
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0D9488).copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "🎯 Cutoff Rank Range & Category Matching",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F766E)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // 1. Rank Range Dropdown
                                            ExposedDropdownMenuBox(
                                                expanded = collegeRankDropdownExpanded,
                                                onExpandedChange = { collegeRankDropdownExpanded = !collegeRankDropdownExpanded },
                                                modifier = Modifier.weight(1.3f)
                                            ) {
                                                OutlinedTextField(
                                                    value = collegeRankRangeInput,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Rank Range", fontSize = 11.sp) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeRankDropdownExpanded) },
                                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = collegeRankDropdownExpanded,
                                                    onDismissRequest = { collegeRankDropdownExpanded = false }
                                                ) {
                                                    rankRangesList.forEach { range ->
                                                        DropdownMenuItem(
                                                            text = { Text(range, fontSize = 12.sp) },
                                                            onClick = {
                                                                collegeRankRangeInput = range
                                                                collegeRankDropdownExpanded = false
                                                                // Sync cutoff rank numeric input
                                                                collegeCutoffRankInput = when (range) {
                                                                    "1 – 1,000" -> "1000"
                                                                    "1,000 – 2,500" -> "2500"
                                                                    "2,500 – 5,000" -> "5000"
                                                                    "5,000 – 10,000" -> "10000"
                                                                    "10,000 – 15,000" -> "15000"
                                                                    "15,000 – 25,000" -> "25000"
                                                                    "25,000 – 50,000" -> "50000"
                                                                    "50,000 – 75,000" -> "75000"
                                                                    else -> "100000"
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            // 2. Category Dropdown
                                            ExposedDropdownMenuBox(
                                                expanded = collegeCategoryDropdownExpanded,
                                                onExpandedChange = { collegeCategoryDropdownExpanded = !collegeCategoryDropdownExpanded },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                OutlinedTextField(
                                                    value = collegeCategoryInput,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Category", fontSize = 11.sp) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeCategoryDropdownExpanded) },
                                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = collegeCategoryDropdownExpanded,
                                                    onDismissRequest = { collegeCategoryDropdownExpanded = false }
                                                ) {
                                                    categoriesList.forEach { cat ->
                                                        DropdownMenuItem(
                                                            text = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                                            onClick = {
                                                                collegeCategoryInput = cat
                                                                collegeCategoryDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = collegeCutoffRankInput,
                                            onValueChange = { collegeCutoffRankInput = it },
                                            label = { Text("Cutoff Closing Rank (#)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // College Name
                                OutlinedTextField(
                                    value = collegeNameInput,
                                    onValueChange = { collegeNameInput = it },
                                    label = { Text("College Name") },
                                    placeholder = { Text("e.g. RV College of Engineering") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // CET Code + District Preference Dropdown
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = collegeCodeInput,
                                        onValueChange = { collegeCodeInput = it },
                                        label = { Text("CET Code (e.g. E001)") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    // District Preference Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = collegeDistrictDropdownExpanded,
                                        onExpandedChange = { collegeDistrictDropdownExpanded = !collegeDistrictDropdownExpanded },
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        OutlinedTextField(
                                            value = collegeDistrictInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("District Preference", fontSize = 11.sp) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeDistrictDropdownExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = collegeDistrictDropdownExpanded,
                                            onDismissRequest = { collegeDistrictDropdownExpanded = false }
                                        ) {
                                            karnatakaDistrictsList.forEach { dist ->
                                                DropdownMenuItem(
                                                    text = { Text(dist, fontSize = 12.sp) },
                                                    onClick = {
                                                        collegeDistrictInput = dist
                                                        collegeDistrictDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // College Type Dropdown + Tuition Fees
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ExposedDropdownMenuBox(
                                        expanded = collegeTypeDropdownExpanded,
                                        onExpandedChange = { collegeTypeDropdownExpanded = !collegeTypeDropdownExpanded },
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        OutlinedTextField(
                                            value = collegeTypeInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("College Type", fontSize = 11.sp) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeTypeDropdownExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = collegeTypeDropdownExpanded,
                                            onDismissRequest = { collegeTypeDropdownExpanded = false }
                                        ) {
                                            collegeTypesList.forEach { type ->
                                                DropdownMenuItem(
                                                    text = { Text(type, fontSize = 12.sp) },
                                                    onClick = {
                                                        collegeTypeInput = type
                                                        collegeTypeDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = collegeTuitionFeeInput,
                                        onValueChange = { collegeTuitionFeeInput = it },
                                        label = { Text("Tuition Fee (₹/yr)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // In-Campus Hostel Switch & Hostel Fee
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("In-Campus Hostel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    if (collegeHasHostelInput) "Available on campus" else "No on-campus hostel facility",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Switch(
                                                checked = collegeHasHostelInput,
                                                onCheckedChange = { collegeHasHostelInput = it }
                                            )
                                        }
                                        if (collegeHasHostelInput) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedTextField(
                                                value = collegeHostelFeeInput,
                                                onValueChange = { collegeHostelFeeInput = it },
                                                label = { Text("Hostel Fee (₹/yr)") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // NAAC Grade Dropdown + NIRF Rank
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ExposedDropdownMenuBox(
                                        expanded = collegeNaacDropdownExpanded,
                                        onExpandedChange = { collegeNaacDropdownExpanded = !collegeNaacDropdownExpanded },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = collegeNaacInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("NAAC Grade", fontSize = 11.sp) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collegeNaacDropdownExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = collegeNaacDropdownExpanded,
                                            onDismissRequest = { collegeNaacDropdownExpanded = false }
                                        ) {
                                            naacGradesList.forEach { grade ->
                                                DropdownMenuItem(
                                                    text = { Text(grade, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                                    onClick = {
                                                        collegeNaacInput = grade
                                                        collegeNaacDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = collegeNirfInput,
                                        onValueChange = { collegeNirfInput = it },
                                        label = { Text("NIRF Rank (#)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Avg Package + Highest Package
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = collegeAvgPkgInput,
                                        onValueChange = { collegeAvgPkgInput = it },
                                        label = { Text("Avg Package (LPA)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = collegeHighestPkgInput,
                                        onValueChange = { collegeHighestPkgInput = it },
                                        label = { Text("Highest Pkg (LPA)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // College Banner Image URL or Upload from Gallery
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF4F46E5).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF4F46E5).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "🖼️ College Banner Image",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF4338CA)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = collegeImageResInput,
                                        onValueChange = { collegeImageResInput = it },
                                        label = { Text("College Banner Image URL") },
                                        placeholder = { Text("https://... or select from gallery") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedButton(
                                        onClick = {
                                            collegeBannerImagePickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                                    ) {
                                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📸 Upload from Gallery / Files", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (collegeImageResInput.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.05f))
                                        ) {
                                            AsyncImage(
                                                model = collegeImageResInput,
                                                contentDescription = "College Banner Preview",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            IconButton(
                                                onClick = { collegeImageResInput = "" },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(28.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove banner",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // College Website
                                OutlinedTextField(
                                    value = collegeWebsiteInput,
                                    onValueChange = { collegeWebsiteInput = it },
                                    label = { Text("College Website URL") },
                                    placeholder = { Text("https://...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Google College Map
                                OutlinedTextField(
                                    value = collegeMapUrlInput,
                                    onValueChange = { collegeMapUrlInput = it },
                                    label = { Text("Google College Map (URL or Location Name)") },
                                    placeholder = { Text("e.g. RV College of Engineering Bengaluru or maps link") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = notifyOnCollegeSave,
                                            onCheckedChange = { notifyOnCollegeSave = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("📢 Notify all students about this college / cutoff update", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                            Text("Sends push notification with direct link to College Predictor", fontSize = 10.sp, color = Color(0xFF15803D))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (collegeNameInput.isBlank()) {
                                                Toast.makeText(context, "Please enter College Name", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val finalColId = collegeEditingId.ifBlank { "col_${System.currentTimeMillis()}" }
                                            val newCollege = CollegeInfo(
                                                id = finalColId,
                                                name = collegeNameInput.trim(),
                                                code = collegeCodeInput.trim().ifBlank { "E000" },
                                                district = collegeDistrictInput.trim().ifBlank { "Bengaluru Urban" },
                                                collegeType = collegeTypeInput.trim().ifBlank { "Autonomous" },
                                                tuitionFeePerYear = collegeTuitionFeeInput.toIntOrNull() ?: 118000,
                                                hostelFeePerYear = collegeHostelFeeInput.toIntOrNull() ?: 85000,
                                                hasHostel = collegeHasHostelInput,
                                                naacGrade = collegeNaacInput.trim().ifBlank { "A++" },
                                                nirfRank = collegeNirfInput.toIntOrNull() ?: 50,
                                                avgPackageLpa = collegeAvgPkgInput.toFloatOrNull() ?: 12.0f,
                                                highestPackageLpa = collegeHighestPkgInput.toFloatOrNull() ?: 50.0f,
                                                studentRating = collegeRatingInput.toFloatOrNull() ?: 4.5f,
                                                imageResUrl = collegeImageResInput.trim(),
                                                logoResUrl = collegeLogoResInput.trim(),
                                                websiteUrl = collegeWebsiteInput.trim(),
                                                mapLocationQuery = collegeMapUrlInput.trim().ifBlank { "${collegeNameInput.trim()} ${collegeDistrictInput.trim()}" }
                                            )
                                            onSaveCollege(newCollege)

                                            // Automatically save Cutoff Matrix with Rank Range and Category
                                            val cutoffRank = collegeCutoffRankInput.toIntOrNull() ?: when (collegeRankRangeInput) {
                                                "1 – 1,000" -> 1000
                                                "1,000 – 2,500" -> 2500
                                                "2,500 – 5,000" -> 5000
                                                "5,000 – 10,000" -> 10000
                                                "10,000 – 15,000" -> 15000
                                                "15,000 – 25,000" -> 25000
                                                "25,000 – 50,000" -> 50000
                                                "50,000 – 75,000" -> 75000
                                                else -> 100000
                                            }
                                            val defaultCategoryCutoffs = mutableMapOf(
                                                "GM" to (cutoffRank * 0.90f).toInt().coerceAtLeast(1),
                                                "2A" to (cutoffRank * 1.05f).toInt(),
                                                "2B" to (cutoffRank * 1.10f).toInt(),
                                                "3A" to (cutoffRank * 0.98f).toInt(),
                                                "3B" to (cutoffRank * 1.02f).toInt(),
                                                "SC" to (cutoffRank * 2.20f).toInt(),
                                                "ST" to (cutoffRank * 2.50f).toInt()
                                            )
                                            defaultCategoryCutoffs[collegeCategoryInput] = cutoffRank

                                            val cutoffItem = BranchCutoffInfo(
                                                collegeId = finalColId,
                                                branchCode = "ALL",
                                                branchName = "Computer Science & All Core Branches",
                                                categoryCutoffs = defaultCategoryCutoffs
                                            )
                                            onSaveBranchCutoff(cutoffItem)

                                            if (notifyOnCollegeSave) {
                                                onSendDirectNotification(
                                                    "🏛️ KCET College Cutoffs Updated: ${newCollege.name}",
                                                    "Admin updated cutoff matrix and fee structures for ${newCollege.name} (${newCollege.code}). Check College Predictor!",
                                                    "COUNSELLING",
                                                    "ALL",
                                                    null,
                                                    "NAV_HOME"
                                                )
                                            }
                                            Toast.makeText(context, "College & Cutoffs saved to live Database!", Toast.LENGTH_SHORT).show()

                                            collegeEditingId = ""
                                            collegeNameInput = ""
                                            collegeCodeInput = ""
                                            collegeImageResInput = ""
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text(if (collegeEditingId.isBlank()) "➕ Add College" else "💾 Update College")
                                    }

                                    if (collegeEditingId.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                collegeEditingId = ""
                                                collegeNameInput = ""
                                                collegeCodeInput = ""
                                                collegeImageResInput = ""
                                            }
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSendDirectNotification(
                                            "🏛️ KCET Engineering Cutoffs Updated",
                                            "Admin uploaded latest Round 1 & Round 2 cutoff ranks across Top Engineering Colleges in Karnataka. Predict your allotment now!",
                                            "COUNSELLING",
                                            "ALL",
                                            null,
                                            "NAV_HOME"
                                        )
                                        Toast.makeText(context, "📢 Push notification dispatched to all students!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D9488))
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📢 Send Notification to Students (Cutoffs Update)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Database Colleges (${colleges.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(colleges) { college ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = college.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            text = "Code: ${college.code} • ${college.district} • ${college.collegeType}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Fee: ₹${college.tuitionFeePerYear}/yr • NAAC ${college.naacGrade} • NIRF #${college.nirfRank}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4F46E5),
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        val matchingLiveCutoff = branchCutoffs.firstOrNull { cutoff ->
                                            doesCutoffMatchCollege(cutoff, college)
                                        } ?: CollegeSampleData.getCutoffsForCollege(college).firstOrNull()

                                        val displayGmRank = college.cutoffRank.takeIf { it > 0 }
                                            ?: matchingLiveCutoff?.categoryCutoffs?.get("GM")
                                        if (displayGmRank != null && displayGmRank > 0) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "🎯 Cutoff GM #${displayGmRank} • ${matchingLiveCutoff?.branchCode ?: "ALL"}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF047857),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteCollege(college.id)
                                            Toast.makeText(context, "College deleted from Database", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            collegeEditingId = college.id
                                            collegeNameInput = college.name
                                            collegeCodeInput = college.code
                                            collegeDistrictInput = college.district
                                            collegeTypeInput = college.collegeType
                                            collegeTuitionFeeInput = college.tuitionFeePerYear.toString()
                                            collegeHostelFeeInput = college.hostelFeePerYear.toString()
                                            collegeHasHostelInput = college.hasHostel
                                            collegeNaacInput = college.naacGrade
                                            collegeNirfInput = college.nirfRank.toString()
                                            collegeAvgPkgInput = college.avgPackageLpa.toString()
                                            collegeHighestPkgInput = college.highestPackageLpa.toString()
                                            collegeRatingInput = college.studentRating.toString()
                                            collegeImageResInput = college.imageResUrl
                                            collegeLogoResInput = college.logoResUrl
                                            collegeWebsiteInput = college.websiteUrl
                                            collegeMapUrlInput = college.mapLocationQuery
                                            collegePlacementPctInput = college.placementPercentage.toString()
                                            collegeRecruitersInput = college.topRecruiters.joinToString(", ")
                                            val existingCutoff = branchCutoffs.firstOrNull { cutoff ->
                                                doesCutoffMatchCollege(cutoff, college)
                                            } ?: CollegeSampleData.getCutoffsForCollege(college).firstOrNull()

                                            val gmRank = college.cutoffRank.takeIf { it > 0 }
                                                ?: existingCutoff?.categoryCutoffs?.get("GM")
                                                ?: 5000
                                            collegeCutoffRankInput = gmRank.toString()
                                            collegeRankRangeInput = when {
                                                gmRank <= 1000 -> "1 – 1,000"
                                                gmRank <= 2500 -> "1,000 – 2,500"
                                                gmRank <= 5000 -> "2,500 – 5,000"
                                                gmRank <= 10000 -> "5,000 – 10,000"
                                                gmRank <= 15000 -> "10,000 – 15,000"
                                                gmRank <= 25000 -> "15,000 – 25,000"
                                                gmRank <= 50000 -> "25,000 – 50,000"
                                                gmRank <= 75000 -> "50,000 – 75,000"
                                                else -> "75,000 – 100,000+"
                                            }
                                            showEditCollegeDialog = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("✏️ Edit Details", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            selectedCollegeForCutoff = college
                                            showCutoffDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                    ) {
                                        Text("📊 Edit Cutoffs", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 11) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val districtsList = listOf(
                                    "Bengaluru Urban",
                                    "Mysuru",
                                    "Mangaluru / Udupi",
                                    "Dharwad / Hubballi",
                                    "Belagavi",
                                    "Kalaburagi",
                                    "Tumakuru",
                                    "Shivamogga",
                                    "Udupi",
                                    "Davanagere",
                                    "All Karnataka"
                                )

                                Text(
                                    text = if (branchEditingId.isBlank()) "🔬 Add College to Branch Explorer" else "✏️ Edit Branch Explorer Entry",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Connected in Real-Time to Student Engineering Branch Explorer",
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // 1. Branch Code Dropdown (Demanded Branches)
                                ExposedDropdownMenuBox(
                                    expanded = branchExpandedDropdown,
                                    onExpandedChange = { branchExpandedDropdown = !branchExpandedDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val matchedBranch = DemandedEngineeringBranches.find { it.code.equals(branchCodeInput, ignoreCase = true) }
                                    val displayLabel = if (matchedBranch != null) "${matchedBranch.emoji} ${matchedBranch.code} — ${matchedBranch.name}" else branchCodeInput
                                    OutlinedTextField(
                                        value = displayLabel,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Branch Code (Demanded Branches)") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpandedDropdown) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = branchExpandedDropdown,
                                        onDismissRequest = { branchExpandedDropdown = false },
                                        modifier = Modifier.exposedDropdownSize()
                                    ) {
                                        DemandedEngineeringBranches.forEach { opt ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(opt.emoji, fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("${opt.code} — ${opt.name}", fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                onClick = {
                                                    branchCodeInput = opt.code
                                                    branchExpandedDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. College Name (Changed from Branch Name)
                                OutlinedTextField(
                                    value = branchCollegeNameInput,
                                    onValueChange = { branchCollegeNameInput = it },
                                    label = { Text("College Name (e.g. RV College of Engineering, BMS, PES)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2.1 College Website Link
                                OutlinedTextField(
                                    value = branchCollegeWebsiteInput,
                                    onValueChange = { branchCollegeWebsiteInput = it },
                                    label = { Text("College Official Website (e.g. https://rvce.edu.in)") },
                                    placeholder = { Text("https://rvce.edu.in") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = Color(0xFF0D9488))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2.2 College Map Location / Query (Google Maps)
                                OutlinedTextField(
                                    value = branchMapLocationInput,
                                    onValueChange = { branchMapLocationInput = it },
                                    label = { Text("College Map Location (e.g. RV College of Engineering Mysuru Road Bengaluru)") },
                                    placeholder = { Text("RV College of Engineering Mysuru Road Bengaluru") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEA4335))
                                    },
                                    trailingIcon = {
                                        if (branchMapLocationInput.isNotBlank()) {
                                            IconButton(onClick = {
                                                try {
                                                    val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(branchMapLocationInput.trim())}")
                                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Could not launch Maps", Toast.LENGTH_SHORT).show()
                                                }
                                            }) {
                                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Preview Map", tint = Color(0xFF0284C7))
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // 2.3 College Campus Banner Image / Screenshot (Gallery Upload or Direct URL)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "College Campus Banner / Screenshot",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }

                                            if (branchCollegeBannerUrlInput.isNotBlank()) {
                                                TextButton(
                                                    onClick = { branchCollegeBannerUrlInput = "" },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("✕ Remove", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Primary Action: Upload from Gallery / Files
                                        Button(
                                            onClick = {
                                                branchBannerImagePickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Pick Image",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (branchCollegeBannerUrlInput.isBlank()) "📸 Upload Screenshot / Photo from Gallery" else "📸 Change Image from Gallery",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Secondary Option: Direct Image URL or Paste
                                        OutlinedTextField(
                                            value = branchCollegeBannerUrlInput,
                                            onValueChange = { branchCollegeBannerUrlInput = it },
                                            label = { Text("Or Paste Image URL / Screenshot Link", fontSize = 11.sp) },
                                            placeholder = { Text("https://images.unsplash.com/... or data:image/...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                        )

                                        // Preset Campus Banner Image Options for easy 1-tap testing
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Presets:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                            val sampleBanners = listOf(
                                                "Campus 1" to "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80",
                                                "Tech Block" to "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80",
                                                "Green Lawn" to "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800&q=80"
                                            )
                                            sampleBanners.forEach { (label, url) ->
                                                SuggestionChip(
                                                    onClick = { branchCollegeBannerUrlInput = url },
                                                    label = { Text(label, fontSize = 10.sp) },
                                                    modifier = Modifier.height(26.dp)
                                                )
                                            }
                                        }

                                        // Real-time Preview Container
                                        if (branchCollegeBannerUrlInput.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val cleanInput = branchCollegeBannerUrlInput.trim()
                                            val isBase64 = remember(cleanInput) { ImageUtils.isBase64Image(cleanInput) }
                                            val previewBitmap = remember(cleanInput, isBase64) {
                                                if (isBase64) ImageUtils.base64ToBitmap(cleanInput) else null
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                            ) {
                                                if (previewBitmap != null) {
                                                    Image(
                                                        bitmap = previewBitmap.asImageBitmap(),
                                                        contentDescription = "Banner Preview",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    AsyncImage(
                                                        model = cleanInput,
                                                        contentDescription = "Banner Preview",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                Surface(
                                                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color.Black.copy(alpha = 0.7f)
                                                ) {
                                                    Text(
                                                        text = if (isBase64) "📸 Gallery Upload Preview (Ready)" else "🌐 URL Preview (Ready)",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF34D399),
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 3. College Type Dropdown (Private, Autonomous, Government, Other)
                                ExposedDropdownMenuBox(
                                    expanded = branchExpandedTypeDropdown,
                                    onExpandedChange = { branchExpandedTypeDropdown = !branchExpandedTypeDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = branchCollegeTypeInput,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("College Type (Private / Autonomous / Other)") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpandedTypeDropdown) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = branchExpandedTypeDropdown,
                                        onDismissRequest = { branchExpandedTypeDropdown = false },
                                        modifier = Modifier.exposedDropdownSize()
                                    ) {
                                        AdminCollegeTypes.forEach { t ->
                                            DropdownMenuItem(
                                                text = { Text(t, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    branchCollegeTypeInput = t
                                                    branchExpandedTypeDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 4. Place / District Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = branchExpandedDistrictDropdown,
                                    onExpandedChange = { branchExpandedDistrictDropdown = !branchExpandedDistrictDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = branchDistrictInput,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Place / District") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpandedDistrictDropdown) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = branchExpandedDistrictDropdown,
                                        onDismissRequest = { branchExpandedDistrictDropdown = false },
                                        modifier = Modifier.exposedDropdownSize()
                                    ) {
                                        districtsList.forEach { dist ->
                                            DropdownMenuItem(
                                                text = { Text(dist) },
                                                onClick = {
                                                    branchDistrictInput = dist
                                                    branchExpandedDistrictDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 5. Cutoff Rank & Tuition Fee Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = branchGmCutoffInput,
                                        onValueChange = { branchGmCutoffInput = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("GM Cutoff Rank") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = branchTuitionFeeInput,
                                        onValueChange = { branchTuitionFeeInput = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("August 2026 Fee (₹/yr)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = branchSalaryInput,
                                    onValueChange = { branchSalaryInput = it },
                                    label = { Text("Average Starting Salary (LPA, e.g. 14.5)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = branchOverviewInput,
                                    onValueChange = { branchOverviewInput = it },
                                    label = { Text("Overview & Key Placement Highlights (Optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        if (branchCodeInput.isBlank() || branchCollegeNameInput.isBlank()) {
                                            Toast.makeText(context, "Please select Branch Code and enter College Name", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val cleanBranchCode = branchCodeInput.uppercase().trim()
                                        val cleanCollegeName = branchCollegeNameInput.trim()
                                        val colId = "col_" + cleanCollegeName.lowercase().replace("[^a-z0-9]".toRegex(), "_").take(30)
                                        val existingCol = colleges.find { it.name.equals(cleanCollegeName, ignoreCase = true) || it.id == colId || it.id == branchEditingCollegeId }
                                        val finalColId = branchEditingCollegeId.ifBlank { existingCol?.id ?: colId }

                                        val gmRank = branchGmCutoffInput.toIntOrNull() ?: 2000
                                        val fee = branchTuitionFeeInput.toIntOrNull() ?: 125000
                                        val salary = branchSalaryInput.toFloatOrNull() ?: 12.5f

                                        val formattedWebsite = if (branchCollegeWebsiteInput.isNotBlank() && !branchCollegeWebsiteInput.startsWith("http://") && !branchCollegeWebsiteInput.startsWith("https://")) {
                                            "https://$branchCollegeWebsiteInput"
                                        } else {
                                            branchCollegeWebsiteInput.trim()
                                        }

                                        val formattedBanner = branchCollegeBannerUrlInput.trim()
                                        val formattedMapLocation = branchMapLocationInput.trim()

                                        // 1. Save / Update College
                                        val newCollege = (existingCol ?: CollegeInfo(id = finalColId)).copy(
                                            id = finalColId,
                                            name = cleanCollegeName,
                                            code = cleanBranchCode,
                                            district = branchDistrictInput,
                                            collegeType = branchCollegeTypeInput,
                                            tuitionFeePerYear = fee,
                                            avgPackageLpa = salary,
                                            imageResUrl = if (formattedBanner.isNotBlank()) formattedBanner else (existingCol?.imageResUrl?.ifBlank { "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80" } ?: "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80"),
                                            websiteUrl = if (formattedWebsite.isNotBlank()) formattedWebsite else (existingCol?.websiteUrl ?: ""),
                                            mapLocationQuery = if (formattedMapLocation.isNotBlank()) formattedMapLocation else (existingCol?.mapLocationQuery?.ifBlank { "$cleanCollegeName $branchDistrictInput" } ?: "$cleanCollegeName $branchDistrictInput")
                                        )
                                        onSaveCollege(newCollege)

                                        // 2. Save / Update Branch Cutoff
                                        val branchDisplay = DemandedEngineeringBranches.find { it.code.equals(cleanBranchCode, ignoreCase = true) }?.name ?: cleanBranchCode
                                        val cutoffObj = BranchCutoffInfo(
                                            collegeId = finalColId,
                                            branchCode = cleanBranchCode,
                                            branchName = branchDisplay,
                                            categoryCutoffs = mapOf(
                                                "GM" to gmRank,
                                                "2A" to (gmRank * 1.45).toInt(),
                                                "2B" to (gmRank * 1.55).toInt(),
                                                "3A" to (gmRank * 1.15).toInt(),
                                                "3B" to (gmRank * 1.20).toInt(),
                                                "SC" to (gmRank * 5.0).toInt().coerceAtLeast(10000),
                                                "ST" to (gmRank * 7.0).toInt().coerceAtLeast(15000)
                                            )
                                        )
                                        onSaveBranchCutoff(cutoffObj)

                                        // 3. Save / Update Branch Info
                                        val newBranch = EngineeringBranchInfo(
                                            id = branchEditingId.ifBlank { "br_${cleanBranchCode.lowercase()}_${System.currentTimeMillis()}" },
                                            branchCode = cleanBranchCode,
                                            branchName = branchDisplay,
                                            iconEmoji = DemandedEngineeringBranches.find { it.code.equals(cleanBranchCode, ignoreCase = true) }?.emoji ?: "💻",
                                            category = branchCollegeTypeInput,
                                            overview = branchOverviewInput.trim().ifBlank { "$cleanCollegeName offering $cleanBranchCode ($branchDisplay)." },
                                            avgStartingSalaryLpa = salary,
                                            districtOrPlace = branchDistrictInput
                                        )
                                        onSaveBranch(newBranch)

                                        Toast.makeText(context, "✅ $cleanCollegeName ($cleanBranchCode) saved to Database and Live in Student Portal!", Toast.LENGTH_LONG).show()

                                        branchEditingId = ""
                                        branchEditingCollegeId = ""
                                        branchCollegeNameInput = ""
                                        branchCollegeWebsiteInput = ""
                                        branchMapLocationInput = ""
                                        branchCollegeBannerUrlInput = ""
                                        branchGmCutoffInput = "1500"
                                        branchTuitionFeeInput = "125000"
                                        branchSalaryInput = "12.5"
                                        branchOverviewInput = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Text(if (branchEditingId.isBlank()) "➕ Save to Branch Explorer (Live for Students)" else "💾 Update Branch Explorer Entry")
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Admin Managed Engineering Branches & Colleges (${engineeringBranches.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    items(engineeringBranches) { branch ->
                        val matchingCol = colleges.find { it.name.equals(branch.branchName, ignoreCase = true) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val bannerRaw = matchingCol?.imageResUrl?.trim() ?: ""
                                    val isBase64Thumb = remember(bannerRaw) { ImageUtils.isBase64Image(bannerRaw) }
                                    val thumbBitmap = remember(bannerRaw, isBase64Thumb) {
                                        if (isBase64Thumb) ImageUtils.base64ToBitmap(bannerRaw) else null
                                    }

                                    if (thumbBitmap != null) {
                                        Image(
                                            bitmap = thumbBitmap.asImageBitmap(),
                                            contentDescription = "College Banner",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        )
                                    } else if (bannerRaw.isNotBlank()) {
                                        AsyncImage(
                                            model = bannerRaw,
                                            contentDescription = "College Banner",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Text(text = branch.iconEmoji, fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${branch.branchName} (${branch.branchCode})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Place: ${branch.districtOrPlace} • Type: ${branch.category} • Avg: ₹${branch.avgStartingSalaryLpa} LPA",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        if (matchingCol?.websiteUrl?.isNotBlank() == true) {
                                            Text(
                                                text = "🌐 ${matchingCol.websiteUrl}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF0D9488),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (matchingCol?.mapLocationQuery?.isNotBlank() == true) {
                                            Text(
                                                text = "📍 ${matchingCol.mapLocationQuery}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFFEA4335),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (matchingCol?.imageResUrl?.isNotBlank() == true) {
                                            Text(
                                                text = "🖼️ Custom Banner Configured",
                                                fontSize = 10.sp,
                                                color = Color(0xFF0284C7),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        branchEditingId = branch.id
                                        branchEditingCollegeId = matchingCol?.id ?: ""
                                        branchCodeInput = branch.branchCode
                                        branchCollegeNameInput = matchingCol?.name?.ifBlank { branch.branchName } ?: branch.branchName
                                        branchCollegeTypeInput = branch.category.ifBlank { matchingCol?.collegeType ?: "Autonomous" }
                                        branchDistrictInput = branch.districtOrPlace
                                        branchSalaryInput = branch.avgStartingSalaryLpa.toString()
                                        branchOverviewInput = branch.overview
                                        branchCollegeWebsiteInput = matchingCol?.websiteUrl ?: ""
                                        branchMapLocationInput = matchingCol?.mapLocationQuery ?: ""
                                        branchCollegeBannerUrlInput = matchingCol?.imageResUrl ?: ""
                                        val matchingCutoff = branchCutoffs.find {
                                            matchingCol != null && doesCutoffMatchCollege(it, matchingCol) &&
                                            (it.branchCode.equals(branch.branchCode, ignoreCase = true) || it.branchCode.equals("ALL", ignoreCase = true))
                                        }
                                        branchGmCutoffInput = (matchingCutoff?.categoryCutoffs?.get("GM") ?: matchingCol?.cutoffRank?.takeIf { it > 0 } ?: 2000).toString()
                                        branchTuitionFeeInput = (matchingCol?.tuitionFeePerYear ?: 125000).toString()
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0284C7))
                                    }

                                    IconButton(onClick = {
                                        onDeleteBranch(branch.id)
                                        if (matchingCol != null) {
                                            onDeleteCollege(matchingCol.id)
                                            onDeleteBranchCutoff(matchingCol.id, branch.branchCode)
                                        } else {
                                            val fallbackColId = "col_" + branch.branchName.lowercase().replace("[^a-z0-9]".toRegex(), "_").take(30)
                                            onDeleteCollege(fallbackColId)
                                        }
                                        Toast.makeText(context, "Entry deleted from Branch Explorer & Live Student Portal", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (selectedTab == 12) {
                    item {
                        Admin1v1ChallengeControlCenter(
                            challenges = adminChallenges,
                            questionSets = challengeQuestionSets,
                            onPauseTimer = onAdminPauseChallengeTimer,
                            onResumeTimer = onAdminResumeChallengeTimer,
                            onTerminate = onAdminTerminateChallenge,
                            onForceExpire = onAdminForceExpireChallenge,
                            onToggleQuestionSetStatus = onAdminToggleQuestionSetStatus,
                            onDeleteQuestionSet = onAdminDeleteQuestionSet
                        )
                    }
                }
            }
        }


    if (showEditCollegeDialog && collegeEditingId.isNotBlank()) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isSavingCollege) showEditCollegeDialog = false },
            title = {
                Text(text = "✏️ Edit College Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = collegeNameInput,
                        onValueChange = { collegeNameInput = it },
                        label = { Text("College Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegeCodeInput,
                            onValueChange = { collegeCodeInput = it },
                            label = { Text("College Code (e.g. E004)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeDistrictInput,
                            onValueChange = { collegeDistrictInput = it },
                            label = { Text("District / Location") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegeTypeInput,
                            onValueChange = { collegeTypeInput = it },
                            label = { Text("College Type") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeNaacInput,
                            onValueChange = { collegeNaacInput = it },
                            label = { Text("NAAC Grade (e.g. A++)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegeNirfInput,
                            onValueChange = { collegeNirfInput = it },
                            label = { Text("NIRF Rank") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeRatingInput,
                            onValueChange = { collegeRatingInput = it },
                            label = { Text("Student Rating (1-5)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegeTuitionFeeInput,
                            onValueChange = { collegeTuitionFeeInput = it },
                            label = { Text("Annual Tuition Fee (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeHostelFeeInput,
                            onValueChange = { collegeHostelFeeInput = it },
                            label = { Text("Hostel & Mess Fee (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegeHighestPkgInput,
                            onValueChange = { collegeHighestPkgInput = it },
                            label = { Text("Highest Package (LPA)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeAvgPkgInput,
                            onValueChange = { collegeAvgPkgInput = it },
                            label = { Text("Average Package (LPA)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = collegePlacementPctInput,
                            onValueChange = { collegePlacementPctInput = it },
                            label = { Text("Placement %") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = collegeWebsiteInput,
                            onValueChange = { collegeWebsiteInput = it },
                            label = { Text("Website URL") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = collegeRecruitersInput,
                        onValueChange = { collegeRecruitersInput = it },
                        label = { Text("Top Recruiters (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = collegeMapUrlInput,
                        onValueChange = { collegeMapUrlInput = it },
                        label = { Text("Google Maps / Location Query") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSavingCollege,
                    onClick = {
                        if (collegeNameInput.isBlank()) {
                            Toast.makeText(context, "College name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSavingCollege = true
                        scope.launch {
                            try {
                                val updated = CollegeInfo(
                                    id = collegeEditingId,
                                    name = collegeNameInput.trim(),
                                    code = collegeCodeInput.trim().ifBlank { "E000" },
                                    district = collegeDistrictInput.trim().ifBlank { "Bengaluru Urban" },
                                    collegeType = collegeTypeInput.trim().ifBlank { "Autonomous" },
                                    imageResUrl = collegeImageResInput.trim(),
                                    logoResUrl = collegeLogoResInput.trim(),
                                    naacGrade = collegeNaacInput.trim().ifBlank { "A++" },
                                    nirfRank = collegeNirfInput.toIntOrNull() ?: 50,
                                    tuitionFeePerYear = collegeTuitionFeeInput.toIntOrNull() ?: 118000,
                                    hostelFeePerYear = collegeHostelFeeInput.toIntOrNull() ?: 85000,
                                    highestPackageLpa = collegeHighestPkgInput.toFloatOrNull() ?: 50.0f,
                                    avgPackageLpa = collegeAvgPkgInput.toFloatOrNull() ?: 12.0f,
                                    placementPercentage = collegePlacementPctInput.toIntOrNull() ?: 90,
                                    topRecruiters = collegeRecruitersInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    studentRating = collegeRatingInput.toFloatOrNull() ?: 4.5f,
                                    hasHostel = collegeHasHostelInput,
                                    websiteUrl = collegeWebsiteInput.trim(),
                                    mapLocationQuery = collegeMapUrlInput.trim().ifBlank { "${collegeNameInput.trim()} ${collegeDistrictInput.trim()}" }
                                )
                                onSaveCollege(updated)
                                isSavingCollege = false
                                showEditCollegeDialog = false
                                Toast.makeText(context, "College details updated successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                isSavingCollege = false
                                Toast.makeText(context, "Error saving college: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    if (isSavingCollege) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSavingCollege,
                    onClick = { showEditCollegeDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val currentSelectedCollege = selectedCollegeForCutoff
    if (showCutoffDialog && currentSelectedCollege != null) {
        val col = currentSelectedCollege
        val collegeCutoffs = branchCutoffs.filter { doesCutoffMatchCollege(it, col) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCutoffDialog = false },
            title = {
                Text(text = "📊 Cutoffs for ${col.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Set Category Cutoff Ranks", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cutoffBranchCodeInput,
                            onValueChange = { cutoffBranchCodeInput = it },
                            label = { Text("Branch Code (e.g. CSE)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cutoffBranchNameInput,
                            onValueChange = { cutoffBranchNameInput = it },
                            label = { Text("Branch Name") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(value = cutoffGmInput, onValueChange = { cutoffGmInput = it }, label = { Text("GM Rank") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cutoff2aInput, onValueChange = { cutoff2aInput = it }, label = { Text("2A Rank") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cutoff2bInput, onValueChange = { cutoff2bInput = it }, label = { Text("2B Rank") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(value = cutoff3aInput, onValueChange = { cutoff3aInput = it }, label = { Text("3A Rank") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cutoff3bInput, onValueChange = { cutoff3bInput = it }, label = { Text("3B Rank") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cutoffScInput, onValueChange = { cutoffScInput = it }, label = { Text("SC Rank") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = cutoffStInput, onValueChange = { cutoffStInput = it }, label = { Text("ST Rank") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val catMap = mapOf(
                                "GM" to (cutoffGmInput.toIntOrNull() ?: 1500),
                                "2A" to (cutoff2aInput.toIntOrNull() ?: 2500),
                                "2B" to (cutoff2bInput.toIntOrNull() ?: 2800),
                                "3A" to (cutoff3aInput.toIntOrNull() ?: 1800),
                                "3B" to (cutoff3bInput.toIntOrNull() ?: 2000),
                                "SC" to (cutoffScInput.toIntOrNull() ?: 15000),
                                "ST" to (cutoffStInput.toIntOrNull() ?: 20000)
                            )
                            val cutoffObj = BranchCutoffInfo(
                                collegeId = col.id,
                                branchCode = cutoffBranchCodeInput.uppercase().trim(),
                                branchName = cutoffBranchNameInput.trim().ifBlank { cutoffBranchCodeInput.uppercase().trim() },
                                categoryCutoffs = catMap
                            )
                            onSaveBranchCutoff(cutoffObj)
                            Toast.makeText(context, "Branch cutoff saved to Database!", Toast.LENGTH_SHORT).show()
                            showCutoffDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("💾 Save Branch Cutoff")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Existing Saved Cutoffs (${collegeCutoffs.size}) — Tap to edit", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    collegeCutoffs.forEach { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    cutoffBranchCodeInput = c.branchCode
                                    cutoffBranchNameInput = c.branchName
                                    cutoffGmInput = (c.categoryCutoffs["GM"] ?: 1500).toString()
                                    cutoff2aInput = (c.categoryCutoffs["2A"] ?: 2500).toString()
                                    cutoff2bInput = (c.categoryCutoffs["2B"] ?: 2800).toString()
                                    cutoff3aInput = (c.categoryCutoffs["3A"] ?: 1800).toString()
                                    cutoff3bInput = (c.categoryCutoffs["3B"] ?: 2000).toString()
                                    cutoffScInput = (c.categoryCutoffs["SC"] ?: 15000).toString()
                                    cutoffStInput = (c.categoryCutoffs["ST"] ?: 20000).toString()
                                    Toast.makeText(context, "Loaded ${c.branchCode} cutoffs into fields above!", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${c.branchCode} — ${c.branchName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "GM #${c.categoryCutoffs["GM"] ?: "-"} | 2A #${c.categoryCutoffs["2A"] ?: "-"} | 2B #${c.categoryCutoffs["2B"] ?: "-"} | 3A #${c.categoryCutoffs["3A"] ?: "-"} | 3B #${c.categoryCutoffs["3B"] ?: "-"} | SC #${c.categoryCutoffs["SC"] ?: "-"} | ST #${c.categoryCutoffs["ST"] ?: "-"}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            cutoffBranchCodeInput = c.branchCode
                                            cutoffBranchNameInput = c.branchName
                                            cutoffGmInput = (c.categoryCutoffs["GM"] ?: 1500).toString()
                                            cutoff2aInput = (c.categoryCutoffs["2A"] ?: 2500).toString()
                                            cutoff2bInput = (c.categoryCutoffs["2B"] ?: 2800).toString()
                                            cutoff3aInput = (c.categoryCutoffs["3A"] ?: 1800).toString()
                                            cutoff3bInput = (c.categoryCutoffs["3B"] ?: 2000).toString()
                                            cutoffScInput = (c.categoryCutoffs["SC"] ?: 15000).toString()
                                            cutoffStInput = (c.categoryCutoffs["ST"] ?: 20000).toString()
                                            Toast.makeText(context, "Loaded ${c.branchCode} cutoffs into fields above!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Cutoff", tint = Color(0xFF0284C7))
                                    }
                                    IconButton(
                                        onClick = {
                                            onDeleteBranchCutoff(col.id, c.branchCode)
                                            Toast.makeText(context, "Cutoff deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Cutoff", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCutoffDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showRejectDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Enter Reason for Rejection", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please explain to the student why their payment receipt was rejected.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. Invalid transaction ID / Incorrect payment amount / Receipt image unreadable") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectionReasonInput.ifBlank { "Receipt details could not be verified." }
                        onRejectReceipt(selectedRejectReceiptId, selectedRejectUserId, reason)
                        showRejectDialog = false
                        Toast.makeText(context, "Payment Rejected with Reason", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val studentToEdit = editingStudentProfile
    if (showEditStudentDialog && studentToEdit != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditStudentDialog = false },
            title = { Text("✏️ Edit Student Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("UID: ${studentToEdit.uid}", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = editStudentName,
                        onValueChange = { editStudentName = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStudentEmail,
                        onValueChange = { editStudentEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStudentRank,
                        onValueChange = { editStudentRank = it },
                        label = { Text("KCET Target Rank (e.g. Under 500)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStudentStream,
                        onValueChange = { editStudentStream = it },
                        label = { Text("Target Stream") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStudentRegDate,
                        onValueChange = { editStudentRegDate = it },
                        label = { Text("Registration Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("👑 Grant Premium Access", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = editStudentIsPremium,
                            onCheckedChange = { editStudentIsPremium = it }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚫 Block Student Account", fontWeight = FontWeight.SemiBold, color = if (editStudentIsBlocked) Color.Red else Color.Unspecified)
                        Switch(
                            checked = editStudentIsBlocked,
                            onCheckedChange = { editStudentIsBlocked = it }
                        )
                    }

                    if (editStudentIsBlocked) {
                        OutlinedTextField(
                            value = editStudentBlockReason,
                            onValueChange = { editStudentBlockReason = it },
                            label = { Text("Reason for Block (Visible to Student)") },
                            placeholder = { Text("e.g. Violation of mock test rules") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚪 Force Logged Out Status", fontWeight = FontWeight.SemiBold, color = if (editStudentIsForceLoggedOut) Color(0xFFC2410C) else Color.Unspecified)
                        Switch(
                            checked = editStudentIsForceLoggedOut,
                            onCheckedChange = { editStudentIsForceLoggedOut = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = studentToEdit.copy(
                            name = editStudentName.ifBlank { studentToEdit.name },
                            email = editStudentEmail.ifBlank { studentToEdit.email },
                            kcetTargetRank = editStudentRank.ifBlank { studentToEdit.kcetTargetRank },
                            targetStream = editStudentStream.ifBlank { studentToEdit.targetStream },
                            registrationDate = editStudentRegDate.ifBlank { studentToEdit.registrationDate },
                            isPremium = editStudentIsPremium,
                            isBlocked = editStudentIsBlocked,
                            blockReason = if (editStudentIsBlocked) editStudentBlockReason else "",
                            isForceLoggedOut = editStudentIsForceLoggedOut
                        )
                        onUpdateStudentProfileByAdmin(updated)
                        showEditStudentDialog = false
                        Toast.makeText(context, "Student Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // BLOCK STUDENT WITH REASON DIALOG
    // -------------------------------------------------------------
    val currentStudentToBlock = studentToBlock
    if (showBlockStudentDialog && currentStudentToBlock != null) {
        val targetStudent = currentStudentToBlock
        val commonReasons = listOf(
            "Violation of mock test rules",
            "Suspicious multi-device login activity",
            "Account sharing detected",
            "Document / fee verification pending",
            "Misconduct in community discussions",
            "Temporary administrative hold"
        )

        AlertDialog(
            onDismissRequest = { showBlockStudentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626))
                    Text("Block Student Account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target Student:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF991B1B))
                            Text("👤 ${targetStudent.name}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("📧 ${targetStudent.email.ifBlank { "No email linked" }}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("🆔 UID: ${targetStudent.uid}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "⚠️ Blocking will immediately log out this student and prevent them from logging in. When they attempt to log in, they will see this reason and be instructed to contact the admin.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        "Reason for Block (Visible to Student):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = blockReasonInput,
                        onValueChange = { blockReasonInput = it },
                        placeholder = { Text("e.g. Violation of mock test rules or suspicious login activity") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Text(
                        "Quick Suggestion Chips:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonReasons.forEach { chipReason ->
                            SuggestionChip(
                                onClick = { blockReasonInput = chipReason },
                                label = { Text(chipReason, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = blockReasonInput.ifBlank { "Account blocked by Administrator due to policy or verification requirements." }
                        onToggleBlockStudent(targetStudent.uid, true, finalReason)
                        showBlockStudentDialog = false
                        Toast.makeText(context, "🚫 Student '${targetStudent.name}' has been blocked & logged out!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirm Block")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val studentForResults = viewingStudentForResults
    if (showStudentTestResultsDialog && studentForResults != null) {
        val studentResults = filterStudentTests(allStudentTestResults, studentForResults)

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStudentTestResultsDialog = false },
            title = {
                Column {
                    Text("📊 Test History: ${studentForResults.name} (${studentForResults.formattedStudentId})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Total Tests Attempted: ${studentResults.size}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (studentResults.isEmpty()) {
                        Text("No mock test attempt records found for this student.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        studentResults.forEach { result ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = result.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "Score: ${result.score}/${result.totalQuestions}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "✅ Correct: ${result.correctCount}", fontSize = 11.sp, color = Color(0xFF15803D))
                                        Text(text = "❌ Wrong: ${result.wrongCount}", fontSize = 11.sp, color = Color(0xFFDC2626))
                                        Text(text = "⚪ Unanswered: ${result.unansweredCount}", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = "${String.format("%.1f", result.percentage)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                editingTestResultItem = result
                                                editTestScoreInput = result.score.toString()
                                                editTestCorrectInput = result.correctCount.toString()
                                                editTestWrongInput = result.wrongCount.toString()
                                            }
                                        ) {
                                            Text("✏️ Edit Score", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                onDeleteTestResultByAdmin(result.docId)
                                                Toast.makeText(context, "Test score record deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Score", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showStudentTestResultsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    val itemToEdit = editingTestResultItem
    if (itemToEdit != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editingTestResultItem = null },
            title = { Text("✏️ Edit Test Result Score", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Test: ${itemToEdit.testName}", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = editTestScoreInput,
                        onValueChange = { editTestScoreInput = it },
                        label = { Text("Total Score") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTestCorrectInput,
                        onValueChange = { editTestCorrectInput = it },
                        label = { Text("Correct Answers Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTestWrongInput,
                        onValueChange = { editTestWrongInput = it },
                        label = { Text("Wrong Answers Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = editTestScoreInput.toIntOrNull() ?: itemToEdit.score
                        val correct = editTestCorrectInput.toIntOrNull() ?: itemToEdit.correctCount
                        val wrong = editTestWrongInput.toIntOrNull() ?: itemToEdit.wrongCount
                        val totalQ = if (itemToEdit.totalQuestions > 0) itemToEdit.totalQuestions else 20
                        val percentage = (score.toFloat() / totalQ.toFloat()) * 100f

                        onEditTestResultByAdmin(itemToEdit.docId, score, correct, wrong, percentage)
                        editingTestResultItem = null
                        Toast.makeText(context, "Test Result Updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Score")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingTestResultItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmLogoutAllDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmLogoutAllDialog = false },
            title = {
                Text("🚨 Confirm Logout ALL Students", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            },
            text = {
                Text("Are you sure you want to FORCE LOGOUT all students from active sessions instantly? All active student logins will be terminated immediately.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmLogoutAllDialog = false
                        onForceLogoutAllStudents()
                        Toast.makeText(context, "🚨 Force logged out ALL student sessions instantly!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Logout All Students Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmLogoutAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val currentStudentToDelete = studentToDeletePermanently
    if (showDeleteStudentDialog && currentStudentToDelete != null) {
        val student = currentStudentToDelete
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showDeleteStudentDialog = false
                studentToDeletePermanently = null
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Permanently Delete Student?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Are you sure you want to permanently delete this student account? This action cannot be reversed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Student Details Summary Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👤 Student: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF111827))
                            }
                            if (student.email.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✉️ Email: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                                    Text(student.email, fontSize = 12.sp, color = Color(0xFF374151))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🆔 Student ID: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                                Text(student.formattedStudentId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4F46E5))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔑 UID: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                                Text(student.uid.take(16) + if (student.uid.length > 16) "..." else "", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Warning Points Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "⚠️ CONSEQUENCES OF PERMANENT DELETION:",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "• Authentication Deleted: All login credentials and auth records will be permanently removed. The student will be logged out immediately.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                            Text(
                                text = "• Cannot Log In Back: The student CANNOT log in again using their current email or password. They can only access the app by registering again as a new student.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Removed Everywhere: Student's name ('${student.name}'), scores, and XP will be deleted all over the app, including leaderboards, rankings, and active sessions.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                            Text(
                                text = "• Data Erased: All CBT test history, question bookmarks, study badges, and payment records for this student will be wiped permanently.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Admin Security Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Please enter the Admin Security PIN to confirm this permanent deletion.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = deletePinInput,
                            onValueChange = {
                                deletePinInput = it
                                if (deletePinError != null) deletePinError = null
                            },
                            label = { Text("Enter Admin PIN") },
                            placeholder = { Text("1111") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            isError = deletePinError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            supportingText = {
                                deletePinError?.let {
                                    Text(it, color = Color.Red, fontSize = 10.sp)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = if (deletePinError != null) Color.Red else Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deletePinInput == "1111") {
                            val studentToDelete = student
                            showDeleteStudentDialog = false
                            studentToDeletePermanently = null
                            onDeleteStudentPermanently(studentToDelete)
                            Toast.makeText(context, "🗑️ Student '${studentToDelete.name}' and all associated data permanently deleted!", Toast.LENGTH_LONG).show()
                        } else {
                            deletePinError = "Incorrect PIN. Please try again."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify & Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteStudentDialog = false
                        studentToDeletePermanently = null
                        deletePinInput = ""
                        deletePinError = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel (Keep Student)")
                }
            }
        )
    }

    val currentNotifToDelete = notificationToDeletePermanently
    if (currentNotifToDelete != null) {
        val notif = currentNotifToDelete
        AlertDialog(
            onDismissRequest = { notificationToDeletePermanently = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Notification Permanently?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this notification?",
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.5.sp,
                                color = Color(0xFF475569),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Target: ${if (notif.targetUserId == "ALL" || notif.targetUserId.isNullOrBlank()) "All Students (Global)" else (notif.targetUserEmail ?: notif.targetUserId)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ Permanently Deleted in All Student Portals:\nThis notification will be permanently erased from Firestore & Realtime Database and deleted immediately across ALL student accounts and devices.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = notif.id
                        notificationToDeletePermanently = null
                        onDeleteNotification(id)
                        Toast.makeText(context, "🗑️ Notification permanently deleted from all student portals!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete for Everyone", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { notificationToDeletePermanently = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showReceiptPreviewDialog) {
        val currentReceipt = previewReceiptItem
        val receiptUrl = currentReceipt?.receiptImageUri ?: previewReceiptUrl
        val studentName = currentReceipt?.userName?.ifBlank { previewReceiptStudentName } ?: previewReceiptStudentName.ifBlank { "Student" }
        val utrRef = currentReceipt?.transactionRef?.ifBlank { "N/A" } ?: "N/A"
        val amount = currentReceipt?.amountPaid ?: 299
        val status = currentReceipt?.status ?: "UNDER_VERIFICATION"
        
        val receiptBitmap = remember(receiptUrl) {
            ImageUtils.loadReceiptBitmap(context, receiptUrl)
        }

        Dialog(onDismissRequest = { showReceiptPreviewDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📄 Student Payment Receipt", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                            Text("Payer: $studentName", fontSize = 12.sp, color = Color(0xFF64748B))
                            if (currentReceipt?.userEmail?.isNotBlank() == true) {
                                Text(currentReceipt.userEmail, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        IconButton(
                            onClick = { showReceiptPreviewDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Receipt Image or Digital Voucher Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (receiptBitmap != null) {
                            Image(
                                bitmap = receiptBitmap.asImageBitmap(),
                                contentDescription = "Full Receipt Screenshot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 220.dp, max = 360.dp)
                            )
                        } else if (receiptUrl.isNotBlank() && receiptUrl.startsWith("http")) {
                            AsyncImage(
                                model = receiptUrl,
                                contentDescription = "Full Receipt View",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 220.dp, max = 360.dp)
                            )
                        } else {
                            // High-definition KCET Payment Voucher Fallback
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(27.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "KCET GenZ Premium Pass",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Paid ₹$amount via UPI / QR Scan",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF16A34A)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Transaction Ref / UTR:", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Text(utrRef, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Submitted At: ${currentReceipt?.submittedAtFormatted ?: "Recent"}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // UTR Details & Quick Copy Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("UTR / Ref Number:", fontSize = 10.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(utrRef, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            if (utrRef != "N/A" && utrRef.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UTR Ref", utrRef)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UTR Ref copied: $utrRef", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy UTR", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy UTR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verification Action Buttons
                    if (currentReceipt != null && currentReceipt.status == "UNDER_VERIFICATION") {
                        Button(
                            onClick = {
                                onApproveReceipt(currentReceipt.receiptId, currentReceipt.userId)
                                showReceiptPreviewDialog = false
                                Toast.makeText(context, "✅ Payment Verified! Premium granted to $studentName.", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve Payment & Unlock Premium", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedRejectReceiptId = currentReceipt.receiptId
                                    selectedRejectUserId = currentReceipt.userId
                                    rejectionReasonInput = ""
                                    showReceiptPreviewDialog = false
                                    showRejectDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                            ) {
                                Text("❌ Reject Receipt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showReceiptPreviewDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                            ) {
                                Text("Close", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { showReceiptPreviewDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss View", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Direct Push Notification Dialog for a specific Student
        val currentDirectStudent = directNotifStudent
        if (showDirectStudentNotifDialog && currentDirectStudent != null) {
            val student = currentDirectStudent
            AlertDialog(
                onDismissRequest = { showDirectStudentNotifDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0D9488),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Direct Push Notification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "👤 Recipient: ${student.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = "🆔 ID: ${student.formattedStudentId} • ✉️ ${student.email.ifBlank { student.uid }}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Notification Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val dialogTypes = listOf(
                                "GENERAL" to "📢 General",
                                "EXAM_ALERT" to "🚨 Exam",
                                "MATERIAL_UPDATE" to "📚 Material",
                                "BOOKING" to "💬 Mentorship",
                                "PAYMENT" to "💳 Premium"
                            )
                            dialogTypes.forEach { (typeKey, typeLabel) ->
                                FilterChip(
                                    selected = directNotifType == typeKey,
                                    onClick = { directNotifType = typeKey },
                                    label = { Text(typeLabel, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = directNotifTitle,
                            onValueChange = { directNotifTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = directNotifMessage,
                            onValueChange = { directNotifMessage = it },
                            label = { Text("Message Body") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (directNotifTitle.isBlank() || directNotifMessage.isBlank()) {
                                Toast.makeText(context, "Please enter title and message", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val action = when (directNotifType) {
                                "BOOKING" -> "NAV_SENIOR_CHAT"
                                "MATERIAL_UPDATE" -> "NAV_RESOURCES"
                                "EXAM_ALERT" -> "NAV_PRACTICE"
                                else -> "NAV_HOME"
                            }
                            onSendDirectNotification(
                                directNotifTitle.trim(),
                                directNotifMessage.trim(),
                                directNotifType,
                                student.uid,
                                student.email,
                                action
                            )
                            Toast.makeText(context, "🔔 Push notification dispatched to ${student.name}!", Toast.LENGTH_LONG).show()
                            showDirectStudentNotifDialog = false
                            directNotifTitle = ""
                            directNotifMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("Send Direct Push")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDirectStudentNotifDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // DIALOG 1: Senior Mentor Profile & Assigned Students Inspector
    val viewingMentor = selectedMentorForDetails
    if (viewingMentor != null) {
        val mentorAssignedBookings = remember(seniorChatBookings, viewingMentor.name) {
            seniorChatBookings.filter { it.mentorName.trim().equals(viewingMentor.name.trim(), ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { selectedMentorForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4F46E5), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = viewingMentor.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Column {
                        Text(viewingMentor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Senior Mentor Profile Details", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Contact details card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("📋 Credentials & Contact Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                            Text("📧 Email: ${viewingMentor.email}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("📞 Phone: ${viewingMentor.phone.ifBlank { "Not provided" }}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("🎓 Qualification: ${viewingMentor.qualification.ifBlank { "Senior Mentor" }}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("🔬 Specialty: ${viewingMentor.specialty.ifBlank { "KCET Guidance" }}", fontSize = 12.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.SemiBold)
                            Text("⏳ Experience: ${viewingMentor.experience.ifBlank { "Experienced" }}", fontSize = 12.sp, color = Color(0xFF334155))
                            if (viewingMentor.telegramOrInstagram.isNotBlank()) {
                                Text("💬 Social Handle: ${viewingMentor.telegramOrInstagram}", fontSize = 12.sp, color = Color(0xFF1D4ED8))
                            }
                            if (viewingMentor.bio.isNotBlank()) {
                                Text("📝 Bio: \"${viewingMentor.bio}\"", fontSize = 11.sp, color = Color(0xFF64748B), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                            Text("● Status: ${viewingMentor.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewingMentor.status.equals("Active", ignoreCase = true)) Color(0xFF16A34A) else Color(0xFF64748B))
                        }
                    }

                    // Assigned Students section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 Assigned Students (${mentorAssignedBookings.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${mentorAssignedBookings.count { it.status == "Confirmed" }} Confirmed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    if (mentorAssignedBookings.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No students currently assigned to this mentor.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        mentorAssignedBookings.forEach { booking ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(booking.userName.ifBlank { "Student" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (booking.status == "Confirmed") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                booking.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (booking.status == "Confirmed") Color(0xFF16A34A) else Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (booking.userEmail.isNotBlank()) {
                                        Text(booking.userEmail, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Slot: ${booking.selectedDate} at ${booking.selectedTime} • Paid: ₹${booking.amountPaid}", fontSize = 11.sp, color = Color(0xFF334155))
                                    if (booking.adminMessage.isNotBlank()) {
                                        Text("Note: ${booking.adminMessage}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mentor = viewingMentor
                        selectedMentorForDetails = null
                        mentorForAssignStudentDialog = mentor
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("➕ Assign a Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMentorForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG 2: Assign Registered Mentor to Student Booking
    val bookingToAssign = bookingForAssignMentorDialog
    if (bookingToAssign != null) {
        var mentorPickerSearch by remember { mutableStateOf("") }
        val filteredMentorsForPicker = remember(registeredMentors, mentorPickerSearch) {
            if (mentorPickerSearch.isBlank()) registeredMentors
            else {
                val q = mentorPickerSearch.lowercase().trim()
                registeredMentors.filter {
                    it.name.lowercase().contains(q) ||
                    it.qualification.lowercase().contains(q) ||
                    it.specialty.lowercase().contains(q)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { bookingForAssignMentorDialog = null },
            title = {
                Column {
                    Text("👤 Select Registered Mentor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Assign mentor for student: ${bookingToAssign.userName}", fontSize = 12.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Medium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Student details banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Student Request Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                            Text("👤 ${bookingToAssign.userName} (${bookingToAssign.userEmail})", fontSize = 11.sp, color = Color(0xFF1E293B))
                            Text("📅 Slot: ${bookingToAssign.selectedDate} at ${bookingToAssign.selectedTime}", fontSize = 11.sp, color = Color(0xFF1E293B))
                            Text("💳 Paid: ₹${bookingToAssign.amountPaid} • Current: ${bookingToAssign.mentorName.ifBlank { "Unassigned" }}", fontSize = 11.sp, color = Color(0xFF1E293B))
                        }
                    }

                    OutlinedTextField(
                        value = mentorPickerSearch,
                        onValueChange = { mentorPickerSearch = it },
                        placeholder = { Text("Filter mentors by name or specialty...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (filteredMentorsForPicker.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No registered mentors found. Mentors can register via the Senior Live Chat portal.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        filteredMentorsForPicker.forEach { mentor ->
                            val activeLoad = seniorChatBookings.count {
                                it.mentorName.trim().equals(mentor.name.trim(), ignoreCase = true) && it.status == "Confirmed"
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mentor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                            Text(
                                                "🎓 ${mentor.qualification.ifBlank { "Mentor" }} • 🔬 ${mentor.specialty.ifBlank { "KCET Strategy" }}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4338CA)
                                            )
                                            if (mentor.phone.isNotBlank()) {
                                                Text("📞 ${mentor.phone}", fontSize = 10.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                "$activeLoad confirmed",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateBookingStatus(
                                                    bookingToAssign.bookingId,
                                                    "Waiting for Senior Confirmation",
                                                    bookingToAssign.adminMessage.ifBlank { "Payment verified by Admin. Assigned to Senior Mentor ${mentor.name}." },
                                                    mentor.name,
                                                    mentor.email,
                                                    ""
                                                )
                                                Toast.makeText(context, "Sent assignment request to ${mentor.name}!", Toast.LENGTH_SHORT).show()
                                                bookingForAssignMentorDialog = null
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Assign (Request)", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onUpdateBookingStatus(
                                                    bookingToAssign.bookingId,
                                                    "Confirmed",
                                                    "Slot Confirmed! Chat with assigned senior mentor ${mentor.name}.",
                                                    mentor.name,
                                                    mentor.email,
                                                    ""
                                                )
                                                Toast.makeText(context, "Assigned ${mentor.name} & Confirmed Slot!", Toast.LENGTH_SHORT).show()
                                                bookingForAssignMentorDialog = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("✅ Assign & Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                TextButton(onClick = { bookingForAssignMentorDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG 3: Assign a Student to a specific Mentor
    val mentorToPair = mentorForAssignStudentDialog
    if (mentorToPair != null) {
        val candidateBookings = remember(seniorChatBookings, mentorToPair.name) {
            seniorChatBookings.filter {
                it.mentorName.isBlank() || !it.mentorName.trim().equals(mentorToPair.name.trim(), ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { mentorForAssignStudentDialog = null },
            title = {
                Column {
                    Text("➕ Assign Student to Mentor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Mentor: ${mentorToPair.name} (${mentorToPair.specialty})", fontSize = 12.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Medium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (candidateBookings.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No unassigned student bookings found.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Text("Select a student booking request to pair with ${mentorToPair.name}:", fontSize = 11.sp, color = Color(0xFF64748B))

                        candidateBookings.forEach { booking ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(booking.userName.ifBlank { "Student Request" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (booking.userEmail.isNotBlank()) {
                                                Text(booking.userEmail, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                booking.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Slot: ${booking.selectedDate} at ${booking.selectedTime} • Paid: ₹${booking.amountPaid}", fontSize = 11.sp, color = Color(0xFF334155))
                                    if (booking.mentorName.isNotBlank()) {
                                        Text("Current Mentor: ${booking.mentorName}", fontSize = 10.sp, color = Color(0xFFEA580C))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateBookingStatus(
                                                    booking.bookingId,
                                                    "Waiting for Senior Confirmation",
                                                    booking.adminMessage.ifBlank { "Payment verified by Admin. Assigned to Senior Mentor ${mentorToPair.name}." },
                                                    mentorToPair.name,
                                                    mentorToPair.email,
                                                    ""
                                                )
                                                Toast.makeText(context, "Sent assignment request to ${mentorToPair.name}!", Toast.LENGTH_SHORT).show()
                                                mentorForAssignStudentDialog = null
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Assign (Request)", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onUpdateBookingStatus(
                                                    booking.bookingId,
                                                    "Confirmed",
                                                    "Slot Confirmed! Chat with assigned senior mentor ${mentorToPair.name}.",
                                                    mentorToPair.name,
                                                    mentorToPair.email,
                                                    ""
                                                )
                                                Toast.makeText(context, "Assigned & Confirmed Slot for ${booking.userName}!", Toast.LENGTH_SHORT).show()
                                                mentorForAssignStudentDialog = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("✅ Assign & Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                TextButton(onClick = { mentorForAssignStudentDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG 4: Confirm Remove Senior Mentor
    val mentorRemoving = mentorToDelete
    if (mentorRemoving != null) {
        AlertDialog(
            onDismissRequest = { mentorToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626))
                    Text("Remove Mentor", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Are you sure you want to remove '${mentorRemoving.name}'? This mentor will be unregistered and removed from both the student and admin portals.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = mentorRemoving.name
                        mentorToDelete = null
                        scope.launch {
                            FirebaseRealtimeService.deleteSeniorMentor(name)
                            FirebaseService.deleteSeniorMentorFromFirestore(name)
                            Toast.makeText(context, "Mentor '$name' removed successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mentorToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG 5: Register New Senior Mentor
    if (showAddMentorDialog) {
        var newName by remember { mutableStateOf("") }
        var newSpecialty by remember { mutableStateOf("") }
        var newQualification by remember { mutableStateOf("") }
        var newExperience by remember { mutableStateOf("") }
        var newBio by remember { mutableStateOf("") }
        var newSocial by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMentorDialog = false },
            title = {
                Text("Register New Senior Mentor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Mentor Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSpecialty,
                        onValueChange = { newSpecialty = it },
                        label = { Text("Specialty / Subject (e.g. KCET Guidance)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newQualification,
                        onValueChange = { newQualification = it },
                        label = { Text("Qualification (e.g. B.E / MBBS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newExperience,
                        onValueChange = { newExperience = it },
                        label = { Text("Experience (e.g. 1+ Years Coaching)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBio,
                        onValueChange = { newBio = it },
                        label = { Text("Short Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSocial,
                        onValueChange = { newSocial = it },
                        label = { Text("Instagram / Telegram Handle") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newMentor = SeniorMentorProfile(
                                name = newName.trim(),
                                specialty = newSpecialty.trim().ifBlank { "KCET Guidance" },
                                qualification = newQualification.trim().ifBlank { "Senior Mentor Guide" },
                                experience = newExperience.trim().ifBlank { "1+ Years Exam Strategy coaching" },
                                bio = newBio.trim().ifBlank { "Passionate KCET senior guide helping aspirants clear exams." },
                                telegramOrInstagram = newSocial.trim(),
                                email = newEmail.trim(),
                                phone = newPhone.trim(),
                                status = "Active"
                            )
                            showAddMentorDialog = false
                            scope.launch {
                                FirebaseRealtimeService.registerSeniorMentor(newMentor)
                                FirebaseService.saveSeniorMentorToFirestore(newMentor)
                                Toast.makeText(context, "Senior Mentor '${newMentor.name}' registered!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a mentor name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Save Mentor", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMentorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG: Broadcast Push Notification on Upload / Edit
    if (showBroadcastUploadNotifDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastUploadNotifDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📢 Broadcast Notification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Notify students when you upload notes, question banks, update cutoffs, or schedule dates.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("⚡ Quick Preset Templates:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Triple("📚 Study Notes", "STUDY_MATERIAL", "NAV_RESOURCES"),
                            Triple("🏛️ Cutoffs Matrix", "CUTOFFS", "NAV_HOME"),
                            Triple("🏆 180 Mock Test", "MOCK_TEST", "NAV_PRACTICE"),
                            Triple("📅 KEA Schedule", "SCHEDULE", "NAV_HOME"),
                            Triple("💬 Mentorship Live", "MENTORSHIP", "NAV_SENIOR_CHAT"),
                            Triple("✏️ Custom", "CUSTOM", "NAV_HOME")
                        )

                        presets.forEach { (label, presetKey, navAction) ->
                            val isSelected = uploadNotifPreset == presetKey
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    uploadNotifPreset = presetKey
                                    uploadNotifActionType = navAction
                                    when (presetKey) {
                                        "STUDY_MATERIAL" -> {
                                            uploadNotifTitle = "📚 New KCET Study Material & Notes Uploaded"
                                            uploadNotifMessage = "Admin uploaded solved question banks, formula sheets, and study resources in the Resources section!"
                                        }
                                        "CUTOFFS" -> {
                                            uploadNotifTitle = "🏛️ KCET College Cutoffs Matrix Updated"
                                            uploadNotifMessage = "Admin updated official cutoff ranks, fees, and placement packages across top Karnataka engineering colleges."
                                        }
                                        "MOCK_TEST" -> {
                                            uploadNotifTitle = "🏆 180 Questions Full Mock Test Available"
                                            uploadNotifMessage = "New full-length 180 questions KCET mock exam (PCM) is live with answers and detailed explanations. Practice now!"
                                        }
                                        "SCHEDULE" -> {
                                            uploadNotifTitle = "📅 Official KCET Schedule & Timelines Updated"
                                            uploadNotifMessage = "Admin updated important dates for document verification, option entry, and counselling rounds."
                                        }
                                        "MENTORSHIP" -> {
                                            uploadNotifTitle = "💬 1-on-1 Senior Live Chat Sessions Live"
                                            uploadNotifMessage = "Verified senior mentors are available for 1-on-1 counselling guidance and rank strategy. Book a session now!"
                                        }
                                        else -> {
                                            uploadNotifTitle = ""
                                            uploadNotifMessage = ""
                                        }
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("🎯 Target Audience:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uploadNotifTargetMode == "ALL",
                            onClick = {
                                uploadNotifTargetMode = "ALL"
                                uploadNotifSelectedStudent = null
                            },
                            label = { Text("🌐 All Students") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uploadNotifTargetMode == "SPECIFIC",
                            onClick = { uploadNotifTargetMode = "SPECIFIC" },
                            label = { Text("🎯 Single Student") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uploadNotifTargetMode == "SPECIFIC") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Student:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allStudents.take(8).forEach { student ->
                                val isSel = uploadNotifSelectedStudent?.uid == student.uid
                                FilterChip(
                                    selected = isSel,
                                    onClick = { uploadNotifSelectedStudent = student },
                                    label = { Text(student.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uploadNotifTitle,
                        onValueChange = { uploadNotifTitle = it },
                        label = { Text("Notification Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uploadNotifMessage,
                        onValueChange = { uploadNotifMessage = it },
                        label = { Text("Notification Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadNotifTitle.isBlank() || uploadNotifMessage.isBlank()) {
                            Toast.makeText(context, "Please enter title and message", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val targetUid = if (uploadNotifTargetMode == "SPECIFIC") uploadNotifSelectedStudent?.uid ?: "ALL" else "ALL"
                        val targetEmail = if (uploadNotifTargetMode == "SPECIFIC") uploadNotifSelectedStudent?.email else null

                        onSendDirectNotification(
                            uploadNotifTitle.trim(),
                            uploadNotifMessage.trim(),
                            when (uploadNotifPreset) {
                                "STUDY_MATERIAL" -> "MATERIAL_UPDATE"
                                "MOCK_TEST" -> "EXAM_ALERT"
                                "CUTOFFS", "SCHEDULE" -> "COUNSELLING"
                                "MENTORSHIP" -> "BOOKING"
                                else -> "ANNOUNCEMENT"
                            },
                            targetUid,
                            targetEmail,
                            uploadNotifActionType
                        )
                        Toast.makeText(context, "📢 Push notification broadcasted to students!", Toast.LENGTH_LONG).show()
                        showBroadcastUploadNotifDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast Push", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastUploadNotifDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==============================================================
    // DIALOG: Add Question Directly to Specific Test Series Set
    // ==============================================================
    val currentSetForAdd = selectedSetForAddQuestion
    if (currentSetForAdd != null) {
        val targetSet = currentSetForAdd
        AlertDialog(
            onDismissRequest = { selectedSetForAddQuestion = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF6366F1),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Add Question to Test Series", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(targetSet.title, fontSize = 11.sp, color = Color(0xFF4F46E5), maxLines = 1)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Subject:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS, Subject.BIOLOGY).forEach { sub ->
                            val isSelected = singleQSubject == sub
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { singleQSubject = sub }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sub.name.take(4),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = singleQText,
                        onValueChange = { singleQText = it },
                        label = { Text("Question Text *") },
                        placeholder = { Text("Enter problem statement...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = singleOptA,
                        onValueChange = { singleOptA = it },
                        label = { Text("Option A *") },
                        placeholder = { Text("Option A text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = singleOptB,
                        onValueChange = { singleOptB = it },
                        label = { Text("Option B *") },
                        placeholder = { Text("Option B text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = singleOptC,
                        onValueChange = { singleOptC = it },
                        label = { Text("Option C") },
                        placeholder = { Text("Option C text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = singleOptD,
                        onValueChange = { singleOptD = it },
                        label = { Text("Option D") },
                        placeholder = { Text("Option D text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Correct Option Key *:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(Option.A, Option.B, Option.C, Option.D).forEach { opt ->
                            val isSelected = singleCorrectOpt == opt
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF059669) else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { singleCorrectOpt = opt }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = "Opt ${opt.name}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = singleExplanation,
                        onValueChange = { singleExplanation = it },
                        label = { Text("Solution Explanation & Steps") },
                        placeholder = { Text("Explain solution formula & steps for students...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (singleQText.isBlank() || singleOptA.isBlank() || singleOptB.isBlank()) {
                            Toast.makeText(context, "Please enter question text and at least Options A & B", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onAddQuestionToTestSeries(
                            targetSet.id,
                            singleQSubject,
                            singleQText.trim(),
                            singleOptA.trim(),
                            singleOptB.trim(),
                            singleOptC.trim().ifBlank { "None of the above" },
                            singleOptD.trim().ifBlank { "All of the above" },
                            singleCorrectOpt,
                            singleExplanation.trim().ifBlank { "Option ${singleCorrectOpt.name} is the correct answer." },
                            targetSet.isPremium
                        )
                        Toast.makeText(context, "✅ Question added to ${targetSet.title} in Firebase!", Toast.LENGTH_LONG).show()
                        selectedSetForAddQuestion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Question to Test Series", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSetForAddQuestion = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==============================================================
    // DIALOG: View & Manage Questions for Specific Test Series Set
    // ==============================================================
    val currentSetForView = selectedSetForViewQuestions
    if (currentSetForView != null) {
        val targetSet = currentSetForView
        val setQuestions = questions.filter { it.testSetId == targetSet.id }

        AlertDialog(
            onDismissRequest = { selectedSetForViewQuestions = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF059669),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📋", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Questions in ${targetSet.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                        Text("${setQuestions.size} Questions stored in Firebase", fontSize = 11.sp, color = Color(0xFF059669))
                    }
                }
            },
            text = {
                if (setQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No questions currently uploaded for this set.", color = Color.Gray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val set = selectedSetForViewQuestions
                                    selectedSetForViewQuestions = null
                                    selectedSetForAddQuestion = set
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("➕ Add First Question Now")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(setQuestions) { q ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF4F46E5).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = q.subject.name,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4338CA),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                onDeleteQuestion(q.id)
                                                Toast.makeText(context, "Question deleted from set", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Q: ${q.questionText}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        listOf(
                                            Triple("A", q.optionA, q.correctOption == Option.A),
                                            Triple("B", q.optionB, q.correctOption == Option.B),
                                            Triple("C", q.optionC, q.correctOption == Option.C),
                                            Triple("D", q.optionD, q.correctOption == Option.D)
                                        ).forEach { (label, optText, isCorrect) ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isCorrect) Color(0xFFDCFCE7) else Color.White,
                                                border = BorderStroke(0.5.dp, if (isCorrect) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "($label) $optText",
                                                        fontSize = 11.sp,
                                                        color = if (isCorrect) Color(0xFF15803D) else Color(0xFF334155),
                                                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (isCorrect) {
                                                        Text("✓ Correct", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (q.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 Solution Explanation: ${q.explanation}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val set = selectedSetForViewQuestions
                        selectedSetForViewQuestions = null
                        selectedSetForAddQuestion = set
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Another Question", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSetForViewQuestions = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showExportDossierDialog) {
        val filteredList = uniqueStudentsList.filter { student ->
            val q = studentSearchQuery.trim()
            val matchesSearch = q.isBlank() ||
                student.name.contains(q, ignoreCase = true) ||
                student.email.contains(q, ignoreCase = true) ||
                student.kcetTargetRank.contains(q, ignoreCase = true) ||
                student.targetStream.contains(q, ignoreCase = true) ||
                student.formattedStudentId.contains(q, ignoreCase = true) ||
                student.uid.contains(q, ignoreCase = true)

            val matchesFilter = when (selectedStudentFilter) {
                "PREMIUM" -> student.isPremium
                "BLOCKED" -> student.isBlocked || student.isForceLoggedOut
                else -> true
            }
            matchesSearch && matchesFilter
        }

        AlertDialog(
            onDismissRequest = { showExportDossierDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Export Student Dossier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "👥 ${filteredList.size} Student Records Selected",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF312E81),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Export includes complete personal details, Student IDs, Passwords, Target Ranks, Streams, Account Status, and CBT Mock Test scores formatted clearly one-by-one.",
                                fontSize = 11.sp,
                                color = Color(0xFF4338CA),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Option 1: Print / Save as PDF
                    Card(
                        onClick = {
                            val html = StudentDossierExporter.generateDossierHtml(filteredList, allStudentTestResults)
                            StudentDossierExporter.printDossier(context, html, "KCET_Students_Complete_Directory")
                            showExportDossierDialog = false
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF4F46E5), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🖨️ Print / Save as PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Print directly or save clean multi-page PDF with student cards", color = Color(0xFFC7D2FE), fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 2: Download Text Dossier (.TXT)
                    Card(
                        onClick = {
                            val txt = StudentDossierExporter.generateDossierPlainText(filteredList, allStudentTestResults)
                            StudentDossierExporter.shareOrDownloadDossier(context, txt, "KCET_Students_Complete_Directory_2026.txt", "text/plain")
                            showExportDossierDialog = false
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📄 Download Text Dossier (.TXT)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Formatted student-by-student text document with all fields", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 3: Download Spreadsheet (.CSV / Excel)
                    Card(
                        onClick = {
                            val csv = StudentDossierExporter.generateDossierCsv(filteredList, allStudentTestResults)
                            StudentDossierExporter.shareOrDownloadDossier(context, csv, "KCET_Students_Complete_Directory_2026.csv", "text/csv")
                            showExportDossierDialog = false
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFFECFDF5), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📊 Download Spreadsheet (.CSV)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Columns for Excel / Google Sheets with IDs and Passwords", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 4: Copy to Clipboard
                    Card(
                        onClick = {
                            val txt = StudentDossierExporter.generateDossierPlainText(filteredList, allStudentTestResults)
                            StudentDossierExporter.copyToClipboard(context, txt, "KCET Student Directory")
                            showExportDossierDialog = false
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFFFEF3C7), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📋 Copy Full Dossier to Clipboard", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Copy formatted text of all students for instant pasting", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 5: Live In-App Preview
                    Card(
                        onClick = {
                            showExportDossierDialog = false
                            showDossierPreviewDialog = true
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFFFAF5FF), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("👁️ Preview Dossier On-Screen", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Scroll, search and inspect formatted records live", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDossierDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDossierPreviewDialog) {
        val filteredList = uniqueStudentsList.filter { student ->
            val q = studentSearchQuery.trim()
            val matchesSearch = q.isBlank() ||
                student.name.contains(q, ignoreCase = true) ||
                student.email.contains(q, ignoreCase = true) ||
                student.kcetTargetRank.contains(q, ignoreCase = true) ||
                student.targetStream.contains(q, ignoreCase = true) ||
                student.formattedStudentId.contains(q, ignoreCase = true) ||
                student.uid.contains(q, ignoreCase = true)

            val matchesFilter = when (selectedStudentFilter) {
                "PREMIUM" -> student.isPremium
                "BLOCKED" -> student.isBlocked || student.isForceLoggedOut
                else -> true
            }
            matchesSearch && matchesFilter
        }
        val fullPlainText = remember(filteredList, allStudentTestResults) {
            StudentDossierExporter.generateDossierPlainText(filteredList, allStudentTestResults)
        }

        Dialog(onDismissRequest = { showDossierPreviewDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📄 Live Student Dossier Preview",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${filteredList.size} Student records formatted one-by-one",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { showDossierPreviewDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                StudentDossierExporter.copyToClipboard(context, fullPlainText, "Student Dossier")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Full Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val html = StudentDossierExporter.generateDossierHtml(filteredList, allStudentTestResults)
                                StudentDossierExporter.printDossier(context, html, "KCET_Students_Complete_Directory")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                            Text(
                                text = fullPlainText,
                                color = Color(0xFFE2E8F0),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    val currentBookingForReceipt = selectedBookingForReceipt
    if (currentBookingForReceipt != null) {
        val booking = currentBookingForReceipt
        val matchingReceipt = paymentReceipts.find { 
            (booking.userId.isNotBlank() && it.userId == booking.userId) ||
            (booking.userEmail.isNotBlank() && it.userEmail.equals(booking.userEmail, ignoreCase = true)) ||
            (booking.paymentRefNumber.isNotBlank() && it.transactionRef == booking.paymentRefNumber)
        }
        val displayImgUri = booking.receiptImageUri.ifBlank { matchingReceipt?.receiptImageUri.orEmpty() }

        Dialog(onDismissRequest = { selectedBookingForReceipt = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🧾 Student Payment Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Booking ID: ${booking.bookingId}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { selectedBookingForReceipt = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Student & Payment Details Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Student Name:", fontSize = 12.sp, color = Color.Gray)
                                Text(booking.userName.ifBlank { "Student" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Student Email:", fontSize = 12.sp, color = Color.Gray)
                                Text(booking.userEmail.ifBlank { "N/A" }, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Service:", fontSize = 12.sp, color = Color.Gray)
                                Text("1-on-1 Senior Mentorship Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4F46E5))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Requested Slot:", fontSize = 12.sp, color = Color.Gray)
                                Text("${booking.selectedDate} at ${booking.selectedTime}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0284C7))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid:", fontSize = 12.sp, color = Color.Gray)
                                Text("₹${booking.amountPaid}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF166534))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment UTR / Ref:", fontSize = 12.sp, color = Color.Gray)
                                Text(booking.paymentRefNumber.ifBlank { "Not provided" }, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFFB45309))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Uploaded Payment Receipt Screenshot / Proof:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val receiptBitmap = remember(displayImgUri) {
                        ImageUtils.loadReceiptBitmap(context, displayImgUri)
                    }

                    if (receiptBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = receiptBitmap.asImageBitmap(),
                                    contentDescription = "Payment Receipt Proof",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else if (displayImgUri.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = displayImgUri,
                                    contentDescription = "Payment Receipt Proof",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No Screenshot Image File Attached", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                                Text("Student verified payment via UTR / Txn Ref number: ${booking.paymentRefNumber.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color(0xFF78350F), textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UTR Ref", booking.paymentRefNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UTR Reference copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy UTR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onUpdateBookingStatus(
                                    booking.bookingId,
                                    "Confirmed",
                                    "Payment receipt verified and slot confirmed by Admin.",
                                    booking.mentorName,
                                    booking.assignedMentorEmail,
                                    ""
                                )
                                Toast.makeText(context, "Payment Verified & Slot Confirmed!", Toast.LENGTH_SHORT).show()
                                selectedBookingForReceipt = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify & Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
    }
}
}
}



// =========================================================================
// ⚔️ ADMIN 1 VS 1 CHALLENGE CONTROL CENTER
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Admin1v1ChallengeControlCenter(
    challenges: List<com.example.data.ChallengeData>,
    questionSets: List<com.example.data.ChallengeQuestionSet>,
    onPauseTimer: (String, String) -> Unit,
    onResumeTimer: (String) -> Unit,
    onTerminate: (String, String) -> Unit,
    onForceExpire: (String) -> Unit,
    onToggleQuestionSetStatus: (String, Boolean) -> Unit,
    onDeleteQuestionSet: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var selectedChallengeForDetails by remember { mutableStateOf<com.example.data.ChallengeData?>(null) }
    var challengeToTerminate by remember { mutableStateOf<com.example.data.ChallengeData?>(null) }
    var terminateReason by remember { mutableStateOf("Terminated by Admin due to schedule conflict") }
    var challengeToPause by remember { mutableStateOf<com.example.data.ChallengeData?>(null) }
    var pauseReason by remember { mutableStateOf("Paused by Admin for system review") }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val pendingCount = challenges.count { it.status == "PENDING" }
    val acceptedCount = challenges.count { it.status == "ACCEPTED" || it.status == "READY" }
    val inProgressCount = challenges.count { it.status == "IN_PROGRESS" }
    val completedCount = challenges.count { it.status == "COMPLETED" || it.status == "DRAW" }
    val expiredCount = challenges.count { it.status == "EXPIRED" }
    val rejectedCount = challenges.count { it.status == "REJECTED" }
    val terminatedCount = challenges.count { it.status == "TERMINATED" }
    val xpCount = challenges.count { it.isXpChallenge }
    val friendlyCount = challenges.count { !it.isXpChallenge }

    val filteredList = remember(challenges, searchQuery, selectedStatusFilter) {
        challenges.filter { ch ->
            val matchesSearch = searchQuery.isBlank() ||
                    ch.challengeId.contains(searchQuery, ignoreCase = true) ||
                    ch.challengerName.contains(searchQuery, ignoreCase = true) ||
                    ch.challengedName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedStatusFilter) {
                "PENDING" -> ch.status == "PENDING"
                "ACTIVE" -> ch.status == "ACCEPTED" || ch.status == "READY" || ch.status == "IN_PROGRESS"
                "COMPLETED" -> ch.status == "COMPLETED" || ch.status == "DRAW"
                "TERMINATED_EXPIRED" -> ch.status == "EXPIRED" || ch.status == "TERMINATED" || ch.status == "REJECTED"
                "PAUSED" -> ch.paused
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedByDescending { it.createdAt }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF59E0B))))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚔️ 1 VS 1 CHALLENGE CONTROL CENTER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Realtime Monitoring, Timer Pause/Resume & Question Set Management",
                            fontSize = 11.sp,
                            color = Color(0xFFC7D2FE)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            text = "LIVE SYSTEM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatBox("TOTAL", "${challenges.size}", Color(0xFF6366F1))
                    AdminStatBox("PENDING", "$pendingCount", Color(0xFFF59E0B))
                    AdminStatBox("RUNNING", "${acceptedCount + inProgressCount}", Color(0xFF10B981))
                    AdminStatBox("COMPLETED", "$completedCount", Color(0xFF3B82F6))
                    AdminStatBox("EXPIRED/TERM", "${expiredCount + terminatedCount + rejectedCount}", Color(0xFFEF4444))
                    AdminStatBox("XP WAGERS", "$xpCount", Color(0xFFD97706))
                    AdminStatBox("FRIENDLY", "$friendlyCount", Color(0xFF14B8A6))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Prep Duration Setting
        var prepHoursInput by remember { mutableStateOf("3") }
        var prepMinutesInput by remember { mutableStateOf("0") }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            OutlinedTextField(
                value = prepHoursInput,
                onValueChange = { prepHoursInput = it },
                label = { Text("Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = prepMinutesInput,
                onValueChange = { prepMinutesInput = it },
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                scope.launch { 
                    val hours = prepHoursInput.toLongOrNull() ?: 0L
                    val minutes = prepMinutesInput.toLongOrNull() ?: 0L
                    com.example.data.ChallengeService.updatePrepDuration(hours, minutes)
                    Toast.makeText(context, "Duration updated to $hours hours $minutes minutes", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Update")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by student name or challenge ID...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        ScrollableTabRow(
            selectedTabIndex = when (selectedStatusFilter) {
                "PENDING" -> 1
                "ACTIVE" -> 2
                "PAUSED" -> 3
                "COMPLETED" -> 4
                "TERMINATED_EXPIRED" -> 5
                else -> 0
            },
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            val filters = listOf(
                "ALL" to "All (${challenges.size})",
                "PENDING" to "Pending ($pendingCount)",
                "ACTIVE" to "Active/Running (${acceptedCount + inProgressCount})",
                "PAUSED" to "Paused (${challenges.count { it.paused }})",
                "COMPLETED" to "Completed ($completedCount)",
                "TERMINATED_EXPIRED" to "Expired/Terminated (${expiredCount + terminatedCount})"
            )
            filters.forEachIndexed { index, (key, label) ->
                FilterChip(
                    selected = selectedStatusFilter == key,
                    onClick = { selectedStatusFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Sets Management Collapsible Banner
        var showQuestionSetSection by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF475569))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showQuestionSetSection = !showQuestionSetSection },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📝 1v1 PCM Question Sets (${questionSets.size} Active Sets)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(if (showQuestionSetSection) "▲ Hide" else "▼ Manage Sets", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }

                if (showQuestionSetSection) {
                    Spacer(modifier = Modifier.height(10.dp))
                    questionSets.forEach { qSet ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(qSet.name.ifBlank { "Question Set ${qSet.setId}" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${qSet.questions.size} Questions (Physics • Chemistry • Math PCM)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (qSet.isActive) "ACTIVE" else "DISABLED", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (qSet.isActive) Color(0xFF34D399) else Color(0xFFFCA5A5))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = qSet.isActive,
                                        onCheckedChange = { onToggleQuestionSetStatus(qSet.setId, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LIVE CHALLENGES LIST (${filteredList.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No challenges matching search or filter criteria", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredList.forEach { ch ->
                    AdminChallengeCard(
                        challenge = ch,
                        now = currentTime,
                        onPause = { challengeToPause = ch },
                        onResume = { onResumeTimer(ch.challengeId) },
                        onTerminate = { challengeToTerminate = ch },
                        onForceExpire = { onForceExpire(ch.challengeId) },
                        onViewDetails = { selectedChallengeForDetails = ch }
                    )
                }
            }
        }
    }

    // Pause Dialog
    val currentChallengeToPause = challengeToPause
    if (currentChallengeToPause != null) {
        val ch = currentChallengeToPause
        AlertDialog(
            onDismissRequest = { challengeToPause = null },
            title = { Text("⏸ Pause Challenge Timer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pause timer for challenge ${ch.challengeId}? Both students will see the timer paused instantly.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pauseReason,
                        onValueChange = { pauseReason = it },
                        label = { Text("Pause Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPauseTimer(ch.challengeId, pauseReason)
                        challengeToPause = null
                        Toast.makeText(context, "Challenge timer paused by Admin", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Pause Timer")
                }
            },
            dismissButton = {
                TextButton(onClick = { challengeToPause = null }) { Text("Cancel") }
            }
        )
    }

    // Terminate Dialog
    val currentChallengeToTerminate = challengeToTerminate
    if (currentChallengeToTerminate != null) {
        val ch = currentChallengeToTerminate
        AlertDialog(
            onDismissRequest = { challengeToTerminate = null },
            title = { Text("🚫 Terminate Challenge", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Column {
                    Text("Terminate challenge ${ch.challengeId}? If XP entry was deducted, it will be automatically refunded to both players.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = terminateReason,
                        onValueChange = { terminateReason = it },
                        label = { Text("Termination Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTerminate(ch.challengeId, terminateReason)
                        challengeToTerminate = null
                        Toast.makeText(context, "Challenge terminated and refunded", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Terminate Challenge")
                }
            },
            dismissButton = {
                TextButton(onClick = { challengeToTerminate = null }) { Text("Cancel") }
            }
        )
    }

    // Challenge Details / Result Dialog
    val currentChallengeForDetails = selectedChallengeForDetails
    if (currentChallengeForDetails != null) {
        val ch = currentChallengeForDetails
        val qSet = com.example.data.ChallengeService.getQuestionSetById(ch.questionSetId)
        val questions = qSet?.questions ?: emptyList()
        var adminExpandedQId by remember { mutableStateOf<Int?>(null) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedChallengeForDetails = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth().heightIn(max = 750.dp).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚔️ Admin View Result (${ch.challengeId})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { selectedChallengeForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Challenge Type: ${ch.challengeType} • Status: ${ch.status}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("XP Tx Status: ${ch.xpTransactionStatus} • Entry: ${ch.entryXP} XP • Prize: ${ch.prizeXP} XP", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("Question Set: ${ch.questionSetId} (${questions.size} Questions)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("Created: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.US).format(java.util.Date(ch.createdAt))}", fontSize = 10.sp, color = Color.Gray)

                            HorizontalDivider(color = Color(0xFF334155))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Student A (Challenger)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    Text(ch.challengerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Status: ${ch.challengerStatus}", fontSize = 10.sp, color = Color(0xFFFCD34D))
                                    Text("Score: ${ch.challengerScore ?: 0} / 20", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Correct: ${ch.challengerCorrectCount} | Wrong: ${ch.challengerWrongCount} | Unanswered: ${ch.challengerUnansweredCount}", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                                    Text("Accuracy: ${String.format(java.util.Locale.US, "%.1f", ch.challengerAccuracy)}%", fontSize = 9.5.sp, color = Color(0xFF34D399))
                                    Text("Time Spent: ${ch.challengerTimeTakenSec ?: 0}s", fontSize = 9.5.sp, color = Color(0xFFCBD5E1))
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Student B (Challenged)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF472B6))
                                    Text(ch.challengedName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End)
                                    Text("Status: ${ch.challengedStatus}", fontSize = 10.sp, color = Color(0xFFFCD34D))
                                    Text("Score: ${ch.challengedScore ?: 0} / 20", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Correct: ${ch.challengedCorrectCount} | Wrong: ${ch.challengedWrongCount} | Unanswered: ${ch.challengedUnansweredCount}", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                                    Text("Accuracy: ${String.format(java.util.Locale.US, "%.1f", ch.challengedAccuracy)}%", fontSize = 9.5.sp, color = Color(0xFF34D399))
                                    Text("Time Spent: ${ch.challengedTimeTakenSec ?: 0}s", fontSize = 9.5.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.End)
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155))

                            Text(
                                text = if (ch.isDraw) "🤝 Draw!" else if (ch.winnerId == ch.challengerId) "🏆 Winner: Student A (${ch.challengerName})" else if (ch.winnerId == ch.challengedId) "🏆 Winner: Student B (${ch.challengedName})" else "Challenge Status: ${ch.status}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ch.isDraw) Color(0xFFFDE68A) else Color(0xFF34D399),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Question-by-Question Comparison", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    questions.forEachIndexed { index, q ->
                        val qNum = index + 1
                        val isExpanded = adminExpandedQId == q.id
                        val correctOptStr = q.correctOption.name
                        val aAns = ch.challengerAnswers[q.id.toString()] ?: "-"
                        val bAns = ch.challengedAnswers[q.id.toString()] ?: "-"
                        val aCorrect = aAns == correctOptStr
                        val bCorrect = bAns == correctOptStr

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Question $qNum (${q.subject})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                                    TextButton(onClick = { adminExpandedQId = if (isExpanded) null else q.id }) {
                                        Text(if (isExpanded) "▲ Hide Explanation" else "▼ View Explanation", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                    }
                                }

                                Text(q.questionText, fontSize = 11.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Student A Answer: $aAns", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                    Text("Student B Answer: $bAns", fontSize = 10.sp, color = Color(0xFFF472B6))
                                    Text("Correct: $correctOptStr", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Student A: " + if (aAns == "-") "⚠️ Unanswered" else if (aCorrect) "✅ Correct" else "❌ Wrong",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (aAns == "-") Color(0xFFF59E0B) else if (aCorrect) Color(0xFF34D399) else Color(0xFFFCA5A5)
                                    )
                                    Text(
                                        text = "Student B: " + if (bAns == "-") "⚠️ Unanswered" else if (bCorrect) "✅ Correct" else "❌ Wrong",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bAns == "-") Color(0xFFF59E0B) else if (bCorrect) Color(0xFF34D399) else Color(0xFFFCA5A5)
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = Color(0xFF334155))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Options:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                    Text("A. ${q.optionA}", fontSize = 10.sp, color = Color.White)
                                    Text("B. ${q.optionB}", fontSize = 10.sp, color = Color.White)
                                    Text("C. ${q.optionC}", fontSize = 10.sp, color = Color.White)
                                    Text("D. ${q.optionD}", fontSize = 10.sp, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Explanation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    Text(q.explanation.ifBlank { "No explanation provided." }, fontSize = 10.sp, color = Color(0xFFE2E8F0))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AdminChallengeCard(
    challenge: com.example.data.ChallengeData,
    now: Long,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onTerminate: () -> Unit,
    onForceExpire: () -> Unit,
    onViewDetails: () -> Unit
) {
    val remainingMs = challenge.getEffectiveRemainingMs(now)
    val formattedTime = com.example.ui.screens.formatMsToClock(remainingMs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(
            1.dp,
            if (challenge.paused) Color(0xFFEF4444) else if (challenge.status == "IN_PROGRESS") Color(0xFF10B981) else Color(0xFF475569)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID: ${challenge.challengeId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (challenge.status) {
                        "PENDING" -> Color(0xFFD97706)
                        "ACCEPTED", "READY" -> Color(0xFF0284C7)
                        "IN_PROGRESS" -> Color(0xFF059669)
                        "COMPLETED" -> Color(0xFF4F46E5)
                        else -> Color(0xFF64748B)
                    }
                ) {
                    Text(
                        text = if (challenge.paused) "PAUSED" else challenge.status,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("${challenge.challengerName} VS ${challenge.challengedName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Type: ${if (challenge.isXpChallenge) "🔥 200 XP Wager" else "🤝 Friendly"} • Timer: $formattedTime", fontSize = 11.sp, color = Color(0xFFCBD5E1))

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!challenge.paused && (challenge.status == "PENDING" || challenge.status == "ACCEPTED" || challenge.status == "READY")) {
                    Button(
                        onClick = onPause,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("⏸ Pause Timer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (challenge.paused) {
                    Button(
                        onClick = onResume,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("▶ Resume Timer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (challenge.status == "PENDING" || challenge.status == "ACCEPTED" || challenge.status == "READY" || challenge.status == "IN_PROGRESS") {
                    OutlinedButton(
                        onClick = onTerminate,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("🚫 Terminate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (challenge.status == "PENDING") {
                    OutlinedButton(
                        onClick = onForceExpire,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("⏰ Force Expire", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("👁️ View Details", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun AdminStatBox(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
        }
    }
}


