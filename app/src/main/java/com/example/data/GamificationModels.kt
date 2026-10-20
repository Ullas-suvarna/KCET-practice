package com.example.data

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progress: Float = 0f,
    val category: String = "Achievement",
    val isVeryDifficult: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val kcetTargetRank: String,
    val points: Int,
    val accuracy: Float,
    val testsCount: Int,
    val avatarInitials: String,
    val isCurrentUser: Boolean = false,
    val uid: String = "",
    val email: String = ""
)

val defaultRegisteredStudents = listOf(
    LeaderboardEntry(1, "Ananya Rao", "Under 100", 1450, 95.2f, 18, "AR", uid = "mock_std_01", email = "ananya.rao.mock@kcet.local"),
    LeaderboardEntry(2, "Rohan Kulkarni", "Under 250", 1320, 91.0f, 15, "RK", uid = "mock_std_02", email = "rohan.kulkarni.mock@kcet.local"),
    LeaderboardEntry(3, "Deepika Bhat", "Under 350", 1180, 88.5f, 14, "DB", uid = "mock_std_03", email = "deepika.bhat.mock@kcet.local"),
    LeaderboardEntry(4, "Preetham Shetty", "Under 500", 1020, 84.0f, 12, "PS", uid = "mock_std_04", email = "preetham.shetty.mock@kcet.local"),
    LeaderboardEntry(5, "Suhas Patil", "Under 1200", 980, 82.5f, 11, "SP", uid = "mock_std_05", email = "suhas.patil.mock@kcet.local"),
    LeaderboardEntry(6, "Meghana Hegde", "Under 1500", 910, 80.0f, 10, "MH", uid = "mock_std_06", email = "meghana.hegde.mock@kcet.local"),
    LeaderboardEntry(7, "Karthik Gowda", "Under 2000", 870, 79.4f, 10, "KG", uid = "mock_std_07", email = "karthik.gowda.mock@kcet.local"),
    LeaderboardEntry(8, "Sneha Joshi", "Under 2500", 840, 78.8f, 9, "SJ", uid = "mock_std_08", email = "sneha.joshi.mock@kcet.local"),
    LeaderboardEntry(9, "Praveen Kumar", "Under 3000", 810, 77.5f, 9, "PK", uid = "mock_std_09", email = "praveen.kumar.mock@kcet.local"),
    LeaderboardEntry(10, "Divya Kamath", "Under 3500", 790, 76.8f, 8, "DK", uid = "mock_std_10", email = "divya.kamath.mock@kcet.local"),
    LeaderboardEntry(11, "Aditya Nayak", "Under 4000", 750, 75.0f, 8, "AN", uid = "mock_std_11", email = "aditya.nayak.mock@kcet.local"),
    LeaderboardEntry(12, "Pooja Murthy", "Under 4500", 720, 74.2f, 7, "PM", uid = "mock_std_12", email = "pooja.murthy.mock@kcet.local"),
    LeaderboardEntry(13, "Vinayak Hegde", "Under 5000", 690, 73.0f, 7, "VH", uid = "mock_std_13", email = "vinayak.hegde.mock@kcet.local"),
    LeaderboardEntry(14, "Shreya Rao", "Under 6000", 660, 72.1f, 6, "SR", uid = "mock_std_14", email = "shreya.rao.mock@kcet.local"),
    LeaderboardEntry(15, "Chetan Kumar", "Under 7000", 630, 71.0f, 6, "CK", uid = "mock_std_15", email = "chetan.kumar.mock@kcet.local"),
    LeaderboardEntry(16, "Bhavana K", "Under 8000", 600, 70.0f, 6, "BK", uid = "mock_std_16", email = "bhavana.k.mock@kcet.local"),
    LeaderboardEntry(17, "Kiran Patil", "Under 9000", 570, 69.2f, 5, "KP", uid = "mock_std_17", email = "kiran.patil.mock@kcet.local"),
    LeaderboardEntry(18, "Navyasree M", "Under 10000", 540, 68.0f, 5, "NM", uid = "mock_std_18", email = "navyasree.m.mock@kcet.local"),
    LeaderboardEntry(19, "Darshan B", "Under 12000", 510, 67.1f, 5, "DB", uid = "mock_std_19", email = "darshan.b.mock@kcet.local"),
    LeaderboardEntry(20, "Varun Swamy", "Under 15000", 480, 66.0f, 4, "VS", uid = "mock_std_20", email = "varun.swamy.mock@kcet.local"),
    LeaderboardEntry(21, "Tejaswi N", "Under 18000", 450, 65.0f, 4, "TN", uid = "mock_std_21", email = "tejaswi.n.mock@kcet.local"),
    LeaderboardEntry(22, "Sahana G", "Under 20000", 420, 64.0f, 4, "SG", uid = "mock_std_22", email = "sahana.g.mock@kcet.local"),
    LeaderboardEntry(23, "Kiran V", "Under 25000", 390, 62.5f, 3, "KV", uid = "mock_std_23", email = "kiran.v.mock@kcet.local"),
    LeaderboardEntry(24, "Rakshith M", "Under 30000", 360, 61.0f, 3, "RM", uid = "mock_std_24", email = "rakshith.m.mock@kcet.local"),
    LeaderboardEntry(25, "Akshatha P", "Under 35000", 330, 60.0f, 3, "AP", uid = "mock_std_25", email = "akshatha.p.mock@kcet.local")
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
