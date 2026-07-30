package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Subject {
    PHYSICS, CHEMISTRY, MATHEMATICS
}

enum class Option {
    A, B, C, D
}

enum class QuestionPaletteStatus {
    NOT_VISITED, NOT_ANSWERED, ANSWERED, MARKED_FOR_REVIEW, ANSWERED_AND_MARKED
}

data class Question(
    val id: Int,
    val subject: Subject,
    val questionText: String,
    val imageUrl: String? = null,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Option,
    val explanation: String
)

data class UserAnswer(
    val questionId: Int,
    val selectedOption: Option? = null,
    val isMarkedForReview: Boolean = false
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int = 20,
    val score: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val timeTakenSeconds: Long,
    val percentage: Float
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: Int,
    val subject: String,
    val questionText: String,
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

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isPremium: Boolean = false,
    val kcetTargetRank: String = "Under 1000",
    val targetStream: String = "Engineering (B.E / B.Tech)",
    val registrationDate: String = "2026"
)
