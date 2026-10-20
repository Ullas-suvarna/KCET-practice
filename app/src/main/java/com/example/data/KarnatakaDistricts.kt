package com.example.data

/**
 * Complete and canonical list of all 31 districts of Karnataka,
 * with aliases, search helpers, and matching logic for engineering colleges.
 */
object KarnatakaDistricts {

    val ALL_31_DISTRICTS: List<String> = listOf(
        "Bagalkote",
        "Ballari (Bellary)",
        "Belagavi (Belgaum)",
        "Bengaluru Rural",
        "Bengaluru Urban",
        "Bidar",
        "Chamarajanagar",
        "Chikkaballapura",
        "Chikkamagaluru",
        "Chitradurga",
        "Dakshina Kannada (Mangaluru)",
        "Davanagere",
        "Dharwad (Hubballi)",
        "Gadag",
        "Hassan",
        "Haveri",
        "Kalaburagi (Gulbarga)",
        "Kodagu (Coorg)",
        "Kolar",
        "Koppal",
        "Mandya",
        "Mysuru (Mysore)",
        "Raichur",
        "Ramanagara",
        "Shivamogga (Shimoga)",
        "Tumakuru (Tumkur)",
        "Udupi",
        "Uttara Kannada (Karwar)",
        "Vijayanagara",
        "Vijayapura (Bijapur)",
        "Yadgir"
    )

    val FILTER_OPTIONS: List<String> = listOf("All Districts") + ALL_31_DISTRICTS

    val POPULAR_CHIPS: List<String> = listOf(
        "All Districts",
        "Bengaluru Urban",
        "Mysuru (Mysore)",
        "Dakshina Kannada (Mangaluru)",
        "Dharwad (Hubballi)",
        "Belagavi (Belgaum)",
        "Tumakuru (Tumkur)",
        "Shivamogga (Shimoga)",
        "Ballari (Bellary)",
        "Kalaburagi (Gulbarga)",
        "Hassan",
        "Udupi"
    )

    private val ALIAS_MAP: Map<String, List<String>> = mapOf(
        "dakshina kannada (mangaluru)" to listOf("dakshina kannada", "mangaluru", "mangalore", "d.k", "dk", "surathkal", "moodabidri"),
        "dharwad (hubballi)" to listOf("dharwad", "hubballi", "hubli"),
        "belagavi (belgaum)" to listOf("belagavi", "belgaum"),
        "kalaburagi (gulbarga)" to listOf("kalaburagi", "gulbarga"),
        "mysuru (mysore)" to listOf("mysuru", "mysore"),
        "ballari (bellary)" to listOf("ballari", "bellary"),
        "vijayapura (bijapur)" to listOf("vijayapura", "bijapur"),
        "shivamogga (shimoga)" to listOf("shivamogga", "shimoga", "shimogga"),
        "tumakuru (tumkur)" to listOf("tumakuru", "tumkur"),
        "kodagu (coorg)" to listOf("kodagu", "coorg", "madikeri"),
        "uttara kannada (karwar)" to listOf("uttara kannada", "karwar", "bhatkal", "kumta"),
        "bengaluru urban" to listOf("bengaluru urban", "bangalore urban", "bengaluru", "bangalore", "blore"),
        "bengaluru rural" to listOf("bengaluru rural", "bangalore rural", "devenahalli", "doddaballapura"),
        "chikkaballapura" to listOf("chikkaballapura", "chikkaballapur", "chikballapur"),
        "chikkamagaluru" to listOf("chikkamagaluru", "chikmagalur", "chickmagalur"),
        "bagalkote" to listOf("bagalkote", "bagalkot"),
        "chamarajanagar" to listOf("chamarajanagar", "chamarajnagar"),
        "davanagere" to listOf("davanagere", "davangere"),
        "ramanagara" to listOf("ramanagara", "ramanagar", "ramnagar")
    )

    /**
     * Checks if a college matches the user's selected district filter.
     */
    fun matchesDistrict(
        collegeDistrict: String,
        selectedFilter: String,
        collegeName: String = "",
        mapLocationQuery: String = ""
    ): Boolean {
        if (selectedFilter.isBlank() || selectedFilter.equals("All Districts", ignoreCase = true) || selectedFilter.equals("All", ignoreCase = true)) {
            return true
        }

        val cleanFilter = selectedFilter.trim().lowercase()
        val cleanColDist = collegeDistrict.trim().lowercase()
        val cleanName = collegeName.trim().lowercase()
        val cleanLocation = mapLocationQuery.trim().lowercase()

        // 1. Direct equality or substring check
        if (cleanColDist.isNotBlank()) {
            if (cleanColDist == cleanFilter || cleanColDist.contains(cleanFilter) || cleanFilter.contains(cleanColDist)) {
                return true
            }
        }

        // 2. Alias resolution
        val aliases = ALIAS_MAP[cleanFilter] ?: run {
            // Check if cleanFilter matches key without parenthesis
            val baseName = cleanFilter.substringBefore("(").trim()
            ALIAS_MAP[baseName] ?: listOf(baseName)
        }

        for (alias in aliases) {
            if (cleanColDist.contains(alias) || alias.contains(cleanColDist)) {
                return true
            }
            if (cleanName.contains(alias) || cleanLocation.contains(alias)) {
                return true
            }
        }

        // 3. Reverse alias check: if college district has aliases that match filter
        for ((key, values) in ALIAS_MAP) {
            if (values.any { cleanColDist.contains(it) || it.contains(cleanColDist) }) {
                if (key.contains(cleanFilter) || cleanFilter.contains(key) || values.any { cleanFilter.contains(it) || it.contains(cleanFilter) }) {
                    return true
                }
            }
        }

        return false
    }
}
