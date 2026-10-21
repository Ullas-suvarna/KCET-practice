package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Subject {
    PHYSICS, CHEMISTRY, MATHEMATICS, BIOLOGY
}

enum class Option {
    A, B, C, D
}

enum class QuestionPaletteStatus {
    NOT_VISITED, NOT_ANSWERED, ANSWERED, MARKED_FOR_REVIEW, ANSWERED_AND_MARKED
}

data class Question(
    val id: Int = 0,
    val subject: Subject = Subject.PHYSICS,
    val questionText: String = "",
    val imageUrl: String? = null,
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: Option = Option.A,
    val explanation: String = "",
    val testSetId: String = "default",
    val isPremium: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this(0, Subject.PHYSICS, "", null, "", "", "", "", Option.A, "", "default", false)
}

data class TestSeriesSet(
    val id: String = "",
    val title: String = "",
    val category: String = "Full Mock Tests", // Full Mock Tests, Previous Year Papers, Chapter-wise Tests, Daily Practice
    val questionCount: Int = 20,
    val durationMins: Int = 20,
    val isPremium: Boolean = true,
    val description: String = "",
    val createdDate: String = "2026"
)

data class UserAnswer(
    val questionId: Int,
    val selectedOption: Option? = null,
    val isMarkedForReview: Boolean = false
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attemptId: String = "",
    val testName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int = 20,
    val score: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val timeTakenSeconds: Long,
    val percentage: Float,
    val isQuit: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: Int,
    val subject: String,
    val questionText: String,
    val imageUrl: String? = null,
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "A",
    val explanation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ResourceItem(
    val id: String,
    val title: String,
    val category: String, // Syllabus, PYQ, Formula, Tips, Notes
    val fileFormat: String = "PDF",
    val fileSize: String,
    val yearOrSubject: String,
    val downloadUrl: String,
    val description: String
)

data class CounsellingStep(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String,
    val keyChecklist: List<String>
)

data class Announcement(
    val id: String,
    val title: String,
    val date: String,
    val message: String,
    val isImportant: Boolean = false
)

data class ImportantDateItem(
    val id: String = "",
    val eventTitle: String = "",
    val dateOrDeadline: String = "",
    val status: String = "Upcoming", // Completed, Active Now, Upcoming, Crucial
    val description: String = "",
    val category: String = "Exam", // Exam, Counselling, Application, Results, Admit Card
    val isHighlighted: Boolean = false
)

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isPremium: Boolean = false,
    val kcetTargetRank: String = "Under 1000",
    val targetStream: String = "Engineering (B.E / B.Tech)",
    val registrationDate: String = "2026",
    val isForceLoggedOut: Boolean = false,
    val isBlocked: Boolean = false,
    val blockReason: String = "",
    val signInMethod: String = "Email & Password",
    val studentId: String = "",
    val lastUpdatedMillis: Long = 0L,
    val password: String = ""
) {
    val formattedStudentId: String
        get() {
            if (studentId.isNotBlank() && studentId.startsWith("KCET-")) return studentId
            if (uid.startsWith("KCET-")) return uid
            val key = (email.ifBlank { uid }).lowercase().trim()
            val num = kotlin.math.abs(key.hashCode()) % 900000 + 100001
            return "KCET-%06d".format(num)
        }

    val displayPassword: String
        get() {
            if (password.isNotBlank()) return password
            val cleanEmail = email.lowercase().trim()
            return when {
                cleanEmail.contains("karthik") -> "karthik123"
                cleanEmail.contains("ananya") -> "ananya@kcet"
                cleanEmail.contains("praveen") -> "praveen2026"
                cleanEmail.contains("sneha") -> "sneha@123"
                cleanEmail.contains("rahul") -> "rahul2026"
                cleanEmail.contains("meghana") -> "meghana@123"
                cleanEmail.contains("ullas") -> "ullas12345"
                cleanEmail.contains("@") -> cleanEmail.substringBefore("@") + "123"
                uid.isNotBlank() && uid.length >= 6 -> uid.take(8) + "@kcet"
                else -> "KCET@2026"
            }
        }
}

data class AdminTestResultItem(
    val docId: String = "",
    val userId: String = "",
    val userName: String = "Student",
    val userEmail: String = "",
    val studentId: String = "",
    val testName: String = "CBT Mock Test",
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int = 20,
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unansweredCount: Int = 0,
    val timeTakenSeconds: Long = 0,
    val percentage: Float = 0f
)

data class RankVsMarksItem(
    val id: String = "",
    val marksRange: String = "",
    val expectedRankRange: String = "",
    val year: String = "2025",
    val description: String = "",
    val minMarks: Int = 0,
    val maxMarks: Int = 180
)

data class SeniorChatBooking(
    val bookingId: String = "",
    val userId: String = "kcet_std_001",
    val userName: String = "Karthik Gowda",
    val userEmail: String = "karthik.kcet2026@gmail.com",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val mentorName: String = "",
    val assignedMentorEmail: String = "",
    val instagramHandle: String = "",
    val supportEmail: String = "KcetGenZservice@gmail.com",
    val amountPaid: Int = 50,
    val paymentRefNumber: String = "",
    val receiptImageUri: String = "",
    val receiptUrl: String = "",
    val bookedAtTimestamp: Long = System.currentTimeMillis(),
    val status: String = "Under Verification", // "Under Verification", "Waiting for Senior Confirmation", "Confirmed", "Reschedule Needed", "Declined", "Rejected by Mentor", "Expired"
    val adminMessage: String = "",
    val rejectionReason: String = ""
)

fun SeniorChatBooking.isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
    if (status == "Completed" || status == "Expired") return true
    if (status == "Confirmed" && (adminMessage.contains("continued by admin", ignoreCase = true) || adminMessage.contains("session continued", ignoreCase = true))) return false
    return try {
        val dateStr = selectedDate.trim()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
        val date = dateFormat.parse(dateStr) ?: return false

        val calBooking = java.util.Calendar.getInstance().apply { time = date }

        val timeParts = selectedTime.split("-")
        val endTimeStr = if (timeParts.size > 1) timeParts[1].trim() else selectedTime.trim()

        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val endTimeDate = try { timeFormat.parse(endTimeStr) } catch (e: Exception) { null }

        if (endTimeDate != null) {
            val calTime = java.util.Calendar.getInstance().apply { time = endTimeDate }
            calBooking.set(java.util.Calendar.HOUR_OF_DAY, calTime.get(java.util.Calendar.HOUR_OF_DAY))
            calBooking.set(java.util.Calendar.MINUTE, calTime.get(java.util.Calendar.MINUTE))
            calBooking.set(java.util.Calendar.SECOND, 59)
            calBooking.set(java.util.Calendar.MILLISECOND, 999)
        } else {
            calBooking.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calBooking.set(java.util.Calendar.MINUTE, 59)
            calBooking.set(java.util.Calendar.SECOND, 59)
            calBooking.set(java.util.Calendar.MILLISECOND, 999)
        }

        nowMillis > calBooking.timeInMillis
    } catch (e: Exception) {
        false
    }
}

data class HelplineItem(
    val id: String = "",
    val title: String = "",
    val contactValue: String = "",
    val type: String = "EMAIL", // EMAIL, INSTAGRAM, PHONE, WHATSAPP
    val description: String = "",
    val actionUrl: String = "",
    val isHighlighted: Boolean = true
)

data class StudentNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val timestamp: String = "Just now",
    val type: String = "GENERAL", // BOOKING, ANNOUNCEMENT, SYSTEM, PAYMENT, EXAM_ALERT, STUDY_MATERIAL, COUNSELLING
    val isRead: Boolean = false,
    val actionType: String? = null,
    val targetUserId: String? = null,
    val targetUserEmail: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class PaymentReceipt(
    val receiptId: String = "",
    val userId: String = "kcet_std_001",
    val userName: String = "Karthik Gowda",
    val userEmail: String = "karthik.kcet2026@gmail.com",
    val userPhone: String = "",
    val amountPaid: Int = 299,
    val transactionRef: String = "",
    val receiptImageUri: String = "",
    val submittedAtTimestamp: Long = System.currentTimeMillis(),
    val status: String = "UNDER_VERIFICATION", // UNDER_VERIFICATION, VERIFIED, REJECTED
    val rejectionReason: String = "",
    val verifiedAtTimestamp: Long? = null
) {
    val submittedAtFormatted: String
        get() {
            return try {
                val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                sdf.format(java.util.Date(submittedAtTimestamp))
            } catch (e: Exception) {
                "Recently"
            }
        }
}

