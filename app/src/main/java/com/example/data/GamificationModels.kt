package com.example.data

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progress: Float = 0f,
    val category: String = "Achievement"
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val kcetTargetRank: String,
    val points: Int,
    val accuracy: Float,
    val testsCount: Int,
    val avatarInitials: String,
    val isCurrentUser: Boolean = false
)

data class SubjectAnalytics(
    val subject: Subject,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val accuracyPercentage: Float,
    val avgTimePerQuestionSec: Float
)

data class TopicAccuracy(
    val topicName: String,
    val subject: Subject,
    val correct: Int,
    val total: Int,
    val accuracy: Float,
    val status: String // "Strong" (>=75%), "Moderate" (50-74%), "Needs Practice" (<50%)
)

data class AnalyticsSummary(
    val totalTestsTaken: Int,
    val overallAccuracy: Float,
    val totalPoints: Int,
    val avgTimePerQuestionSec: Float,
    val subjectBreakdown: List<SubjectAnalytics>,
    val topicAccuracies: List<TopicAccuracy>,
    val weakAreas: List<String>,
    val recommendations: List<String>
)
