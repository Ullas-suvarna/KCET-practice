package com.example.data

data class CollegeInfo(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val district: String = "Bengaluru Urban",
    val collegeType: String = "Autonomous", // Government, Private, Autonomous
    val imageResUrl: String = "",
    val logoResUrl: String = "",
    val naacGrade: String = "A++",
    val nirfRank: Int = 50,
    val tuitionFeePerYear: Int = 105000,
    val hostelFeePerYear: Int = 85000,
    val highestPackageLpa: Float = 58.0f,
    val avgPackageLpa: Float = 14.5f,
    val placementPercentage: Int = 94,
    val topRecruiters: List<String> = listOf("Microsoft", "Google", "Amazon", "Bosch", "Cisco"),
    val studentRating: Float = 4.8f,
    val hasHostel: Boolean = true,
    val websiteUrl: String = "https://rvce.edu.in",
    val mapLocationQuery: String = "RV College of Engineering Bengaluru",
    val distanceKmFromBlr: Int = 12,
    val brochureUrl: String = "",
    val isFavorite: Boolean = false,
    val cutoffRank: Int = 0
)

data class BranchCutoffInfo(
    val collegeId: String = "",
    val branchCode: String = "CSE",
    val branchName: String = "Computer Science & Engineering",
    val categoryCutoffs: Map<String, Int> = mapOf(
        "GM" to 1200, "2A" to 2100, "2B" to 2300, "3A" to 1400, "3B" to 1500, "SC" to 12000, "ST" to 18000
    )
)

fun doesCutoffMatchCollege(cutoff: BranchCutoffInfo, college: CollegeInfo): Boolean {
    val cutoffColId = cutoff.collegeId.trim()
    if (cutoffColId.isBlank()) return false
    val colId = college.id.trim()
    val colCode = college.code.trim()
    val colName = college.name.trim()
    val cleanColId = colId.removePrefix("col_").trim()

    return cutoffColId.equals(colId, ignoreCase = true) ||
           (colCode.isNotBlank() && cutoffColId.equals(colCode, ignoreCase = true)) ||
           cutoffColId.equals(cleanColId, ignoreCase = true) ||
           (cutoffColId.length >= 3 && colId.contains(cutoffColId, ignoreCase = true)) ||
           (colName.isNotBlank() && (
               colName.equals(cutoffColId, ignoreCase = true) ||
               colName.contains(cutoffColId, ignoreCase = true) ||
               cutoffColId.contains(colName, ignoreCase = true)
           ))
}

data class EngineeringBranchInfo(
    val id: String = "",
    val branchCode: String = "CSE",
    val branchName: String = "Computer Science & Engineering",
    val iconEmoji: String = "💻",
    val category: String = "Tech & AI", // Tech & AI, Core, Allied & Emerging
    val overview: String = "",
    val futureDemandLevel: String = "Very High Demand 🔥",
    val futureScope: String = "",
    val careerOpportunities: List<String> = listOf("Software Engineer", "Cloud Architect", "AI Research Scientist", "Data Engineer"),
    val higherStudiesOptions: List<String> = listOf("M.Tech at IITs/IISc", "MS in US/Germany", "MBA in Tech Management"),
    val requiredSkills: List<String> = listOf("Data Structures", "Algorithms", "System Design", "Python / C++"),
    val avgStartingSalaryLpa: Float = 12.5f,
    val topOfferingCollegesCount: Int = 185,
    val districtOrPlace: String = "Bengaluru Urban"
)

data class CollegeRecommendationQuery(
    val rank: Int = 5000,
    val rankRange: String = "",
    val category: String = "GM",
    val preferredBranch: String = "Computer Science & Engineering",
    val budgetMaxFee: Int = 200000,
    val preferredDistrict: String = "Bengaluru Urban",
    val collegeType: String = "All", // "All", "Government", "Private", "Autonomous"
    val hostelRequired: Boolean = true,
    val placementQuality: String = "Any", // "High (10+ LPA)", "Medium (5-10 LPA)", "Any"
    val preferredNaacGrade: String = "Any", // "A++", "A+", "A", "Any"
    val nirfOnly: Boolean = false
)

data class RecommendedCollegeItem(
    val college: CollegeInfo,
    val branchCode: String,
    val branchName: String,
    val closingRank: Int,
    val admissionChance: String, // "High", "Medium", "Low"
    val admissionChancePercentage: Int,
    val aiMatchExplanation: String,
    val isBackupCollege: Boolean = false,
    val totalEstimatedCostPerYear: Int = college.tuitionFeePerYear + college.hostelFeePerYear
)

data class SavedRecommendationHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val query: CollegeRecommendationQuery,
    val topMatchCollegeName: String = "",
    val resultsCount: Int = 0
)

data class DemandedBranchOption(
    val code: String,
    val name: String,
    val emoji: String = "💻"
)

val DemandedEngineeringBranches = listOf(
    DemandedBranchOption("CSE", "Computer Science & Engineering", "💻"),
    DemandedBranchOption("AIML", "Artificial Intelligence & Machine Learning", "🤖"),
    DemandedBranchOption("DS", "Data Science & Big Analytics", "📊"),
    DemandedBranchOption("ISE", "Information Science & Engineering", "🌐"),
    DemandedBranchOption("ECE", "Electronics & Communication Engineering", "⚡"),
    DemandedBranchOption("EEE", "Electrical & Electronics Engineering", "💡"),
    DemandedBranchOption("ME", "Mechanical Engineering", "⚙️"),
    DemandedBranchOption("CE", "Civil Engineering", "🏗️"),
    DemandedBranchOption("AE", "Aerospace & Aeronautical Engineering", "✈️"),
    DemandedBranchOption("BT", "Biotechnology & Biochemical", "🧬")
)

val AdminCollegeTypes = listOf("Private", "Autonomous", "Government", "Other")
