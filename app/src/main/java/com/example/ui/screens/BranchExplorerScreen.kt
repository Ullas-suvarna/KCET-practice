package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.EngineeringBranchInfo
import com.example.data.BranchCutoffInfo
import com.example.data.CollegeInfo
import com.example.data.CollegeSampleData
import com.example.data.DemandedEngineeringBranches
import com.example.data.doesCutoffMatchCollege
import com.example.data.DemandedBranchOption
import com.example.data.ImageUtils
import com.example.data.KarnatakaDistricts
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchExplorerScreen(
    viewModel: MainViewModel,
    isPremiumUser: Boolean = false,
    onUpgradeClick: () -> Unit = {},
    liveChatPrice: Int = 50,
    subscriptionPrice: Int = 299,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val branches by viewModel.engineeringBranches.collectAsState()
    val colleges by viewModel.colleges.collectAsState()
    val branchCutoffs by viewModel.branchCutoffs.collectAsState()

    var selectedBranchCode by remember { mutableStateOf("CSE") }
    var expandedBranchDropdown by remember { mutableStateOf(false) }

    val branchDropdownOptions = remember(branches) {
        val predefinedCodes = DemandedEngineeringBranches.map { it.code.uppercase() }.toSet()
        val customAdminBranches = branches.filter {
            it.branchCode.uppercase() !in predefinedCodes &&
            it.branchName.isNotBlank() &&
            !it.branchName.contains("college", ignoreCase = true) &&
            !it.branchName.contains("institute", ignoreCase = true)
        }.map {
            DemandedBranchOption(it.branchCode, it.branchName, it.iconEmoji)
        }
        DemandedEngineeringBranches + customAdminBranches
    }

    val activeBranch = remember(branches, selectedBranchCode) {
        val canonical = DemandedEngineeringBranches.find { it.code.equals(selectedBranchCode, ignoreCase = true) }
        val sample = CollegeSampleData.sampleBranches.find { it.branchCode.equals(selectedBranchCode, ignoreCase = true) }
        val admin = branches.find { it.branchCode.equals(selectedBranchCode, ignoreCase = true) }

        if (sample != null) {
            sample.copy(
                branchName = canonical?.name ?: sample.branchName,
                iconEmoji = canonical?.emoji ?: sample.iconEmoji,
                overview = if (admin != null && admin.overview.isNotBlank() && !admin.overview.contains("offering $selectedBranchCode", ignoreCase = true)) admin.overview else sample.overview
            )
        } else if (canonical != null) {
            EngineeringBranchInfo(
                branchCode = canonical.code,
                branchName = canonical.name,
                iconEmoji = canonical.emoji,
                overview = "${canonical.name} (${canonical.code}) is a high-demand engineering stream in Karnataka.",
                avgStartingSalaryLpa = admin?.avgStartingSalaryLpa ?: 12.0f
            )
        } else {
            admin ?: EngineeringBranchInfo(
                branchCode = selectedBranchCode,
                branchName = selectedBranchCode,
                overview = "Engineering stream $selectedBranchCode."
            )
        }
    }

    var rankInputText by remember { mutableStateOf("") }
    var selectedDistrictFilter by remember { mutableStateOf("All Districts") }
    var selectedCollegeTypeFilter by remember { mutableStateOf("All Types") }

    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Pair<CollegeInfo, BranchCutoffInfo>>?>(null) }
    var searchStatusText by remember { mutableStateOf("") }
    var showAllColleges by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }
    var districtSearchQuery by remember { mutableStateOf("") }

    val districts = KarnatakaDistricts.FILTER_OPTIONS
    val collegeTypes = listOf("All Types", "Autonomous", "Government", "Private", "Other")

    val userRank = rankInputText.toIntOrNull()

    var isSyncingFirebase by remember { mutableStateOf(false) }

    // Calculated list using admin-updated database and local records
    val defaultCutoffsWithColleges = remember(
        activeBranch,
        colleges,
        branchCutoffs,
        selectedDistrictFilter,
        selectedCollegeTypeFilter
    ) {
        val filteredColleges = colleges.filter { col ->
            val matchesDistrict = KarnatakaDistricts.matchesDistrict(
                collegeDistrict = col.district,
                selectedFilter = selectedDistrictFilter,
                collegeName = col.name,
                mapLocationQuery = col.mapLocationQuery
            )
            val matchesType = selectedCollegeTypeFilter == "All Types" ||
                    col.collegeType.equals(selectedCollegeTypeFilter, ignoreCase = true)
            val offersBranch = CollegeSampleData.doesCollegeOfferBranch(
                college = col,
                branchCode = activeBranch.branchCode,
                branchName = activeBranch.branchName
            ) || branchCutoffs.any {
                doesCutoffMatchCollege(it, col) && (
                    it.branchCode.equals(activeBranch.branchCode, ignoreCase = true) ||
                    it.branchCode.equals("ALL", ignoreCase = true)
                )
            }
            matchesDistrict && matchesType && offersBranch
        }

        filteredColleges.map { college ->
            val liveCutoff = branchCutoffs.find {
                doesCutoffMatchCollege(it, college) &&
                        (it.branchCode.equals(activeBranch.branchCode, ignoreCase = true) ||
                         it.branchName.contains(activeBranch.branchCode, ignoreCase = true))
            } ?: branchCutoffs.find {
                doesCutoffMatchCollege(it, college) && it.branchCode.equals("ALL", ignoreCase = true)
            }
            val cutoffInfo = liveCutoff ?: if (college.cutoffRank > 0) {
                BranchCutoffInfo(
                    collegeId = college.id,
                    branchCode = activeBranch.branchCode,
                    branchName = activeBranch.branchName,
                    categoryCutoffs = mapOf(
                        "GM" to college.cutoffRank,
                        "2A" to (college.cutoffRank * 1.5).toInt(),
                        "2B" to (college.cutoffRank * 1.7).toInt(),
                        "3A" to (college.cutoffRank * 1.2).toInt(),
                        "3B" to (college.cutoffRank * 1.25).toInt(),
                        "SC" to (college.cutoffRank * 4.5).toInt().coerceAtLeast(10000),
                        "ST" to (college.cutoffRank * 5.0).toInt().coerceAtLeast(15000)
                    )
                )
            } else {
                CollegeSampleData.getCutoffForCollegeAndBranch(
                    college = college,
                    branchCode = activeBranch.branchCode,
                    branchName = activeBranch.branchName
                )
            }
            val updatedFee = if (college.tuitionFeePerYear > 0) college.tuitionFeePerYear else when {
                college.collegeType.contains("Government", ignoreCase = true) -> 48000
                college.collegeType.contains("Aided", ignoreCase = true) -> 82000
                college.collegeType.contains("Autonomous", ignoreCase = true) -> 125000
                else -> 118000
            }
            Pair(college.copy(tuitionFeePerYear = updatedFee), cutoffInfo)
        }.sortedBy { pair ->
            pair.second.categoryCutoffs["GM"] ?: 999999
        }
    }

    val activeDisplayList = searchResults ?: defaultCutoffsWithColleges
    val displayedCutoffList = if (showAllColleges) activeDisplayList else activeDisplayList.take(5)

    fun triggerCutoffSearch() {
        isSearching = true
        searchStatusText = "Searching cutoffs & August 2026 updated fees for ${activeBranch.branchCode}..."

        coroutineScope.launch {
            kotlinx.coroutines.delay(500) // Simple smooth loading indicator
            val results = defaultCutoffsWithColleges
            isSearching = false
            searchResults = results
            searchStatusText = "Found ${results.size} Colleges for ${activeBranch.branchCode} with August 2026 Updated Fees!"
            showAllColleges = false
        }
    }

    // Reset search & view state when switching filters or active branch
    LaunchedEffect(selectedBranchCode, selectedDistrictFilter, selectedCollegeTypeFilter) {
        searchResults = null
        searchStatusText = ""
        showAllColleges = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌿 Engineering Branch Explorer",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "UGCET Aug 2026",
                                    color = Color(0xFF16A34A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF16A34A), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firebase Live • ${colleges.size} Colleges • 31 Districts",
                                fontSize = 11.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isSyncingFirebase) {
                                isSyncingFirebase = true
                                coroutineScope.launch {
                                    viewModel.syncCollegesAndCutoffsFromFirebase { success, msg ->
                                        isSyncingFirebase = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        if (isSyncingFirebase) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF16A34A)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync with Firebase",
                                tint = Color(0xFF16A34A)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Premium Lock Banner Card
            if (!isPremiumUser) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpgradeClick() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👑 KCET Premium — Branch Explorer",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Unlock 200+ Colleges, Category Cutoff Ranks (GM/2A/SC/ST) & AI Gemini Search",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onUpgradeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("⚡ Unlock ₹$subscriptionPrice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Search & Filter Panel with Branch Dropdown & Direct Search
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // 1. Select Branch Dropdown (Demanded Branches only)
                        ExposedDropdownMenuBox(
                            expanded = expandedBranchDropdown,
                            onExpandedChange = { expandedBranchDropdown = !expandedBranchDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${activeBranch.iconEmoji} ${activeBranch.branchCode} — ${activeBranch.branchName}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Engineering Branch (Demanded Branches)", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBranchDropdown) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF16A34A),
                                    unfocusedBorderColor = Color(0xFF16A34A),
                                    focusedContainerColor = Color(0xFFF0FDF4),
                                    unfocusedContainerColor = Color(0xFFF0FDF4)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBranchDropdown,
                                onDismissRequest = { expandedBranchDropdown = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                branchDropdownOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(opt.emoji, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "${opt.code} — ${opt.name}",
                                                        fontWeight = if (opt.code.equals(activeBranch.branchCode, ignoreCase = true)) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp,
                                                        color = if (opt.code.equals(activeBranch.branchCode, ignoreCase = true)) Color(0xFF16A34A) else Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Demanded Engineering Stream 🔥",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedBranchCode = opt.code
                                            expandedBranchDropdown = false
                                            searchResults = null
                                            searchStatusText = ""
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // KCET Rank Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = rankInputText,
                                onValueChange = { rankInputText = it.filter { ch -> ch.isDigit() } },
                                placeholder = { Text("Enter KCET Rank (e.g. 4500) for Chance Engine", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color(0xFF0284C7))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0284C7),
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                            if (rankInputText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(onClick = { rankInputText = "" }) {
                                    Text("Reset", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Place / District Selection (Supports all 31 Districts of Karnataka)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Place/Dist:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.width(75.dp)
                                )

                                Surface(
                                    onClick = { showDistrictDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (selectedDistrictFilter == "All Districts") "All Karnataka (31 Districts)" else selectedDistrictFilter,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E3A8A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedDistrictFilter != "All Districts") {
                                                IconButton(
                                                    onClick = {
                                                        selectedDistrictFilter = "All Districts"
                                                        searchResults = null
                                                    },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear District",
                                                        tint = Color(0xFF64748B),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDBEAFE)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Select ▾",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1D4ED8)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick Filter Chips + Full 31 Districts list
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = showDistrictDialog,
                                        onClick = { showDistrictDialog = true },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFF2563EB)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "📍 All 31 Districts... ▾",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    )
                                }
                                items(districts) { dist ->
                                    FilterChip(
                                        selected = selectedDistrictFilter == dist,
                                        onClick = {
                                            selectedDistrictFilter = dist
                                            searchResults = null
                                        },
                                        label = { Text(dist, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // College Type Selection (Private, Autonomous, Government, Other)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Type:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.width(75.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(collegeTypes) { cType ->
                                    FilterChip(
                                        selected = selectedCollegeTypeFilter == cType,
                                        onClick = {
                                            selectedCollegeTypeFilter = cType
                                            searchResults = null
                                        },
                                        label = { Text(cType, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Button (No AI connected, simple loading and direct results)
                        Button(
                            onClick = { triggerCutoffSearch() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSearching,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Searching Cutoffs & August 2026 Fees...", fontSize = 13.sp)
                            } else {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Search Cutoffs & August 2026 Fees", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (searchStatusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = searchStatusText,
                                fontSize = 11.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Active Branch Detailed View (Always shown for selected branch before/after search)
            if (activeBranch != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activeBranch.iconEmoji} ${activeBranch.branchName}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Branch Code: ${activeBranch.branchCode} • ${activeBranch.category}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = activeBranch.futureDemandLevel.ifBlank { "High Demand 🔥" },
                                        color = Color(0xFFD97706),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "📖 Branch Overview & Scope",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeBranch.overview.ifBlank { "${activeBranch.branchName} (${activeBranch.branchCode}) is a premier demanded engineering discipline in Karnataka." },
                                fontSize = 12.5.sp,
                                color = Color(0xFF334155),
                                lineHeight = 18.sp
                            )

                            if (activeBranch.futureScope.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🚀", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Future Scope: ${activeBranch.futureScope}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Avg Package", fontSize = 10.sp, color = Color(0xFF166534))
                                    Text("₹${activeBranch.avgStartingSalaryLpa} LPA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }

                                Column {
                                    Text("Highest Potential", fontSize = 10.sp, color = Color(0xFF166534))
                                    Text("₹${(activeBranch.avgStartingSalaryLpa * 2.8f).toInt()} LPA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }

                                Column {
                                    Text("Colleges Showing", fontSize = 10.sp, color = Color(0xFF166534))
                                    Text("${activeDisplayList.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            }

                            if (activeBranch.careerOpportunities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "💼 Top Career Roles",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(activeBranch.careerOpportunities) { role ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = role,
                                                fontSize = 11.sp,
                                                color = Color(0xFF1D4ED8),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (activeBranch.requiredSkills.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "🧠 Core Skills & Technologies",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(activeBranch.requiredSkills) { skill ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = skill,
                                                fontSize = 11.sp,
                                                color = Color(0xFF334155),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // College Cutoffs Table
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (displayedCutoffList.size < activeDisplayList.size) "Top 5 Colleges for ${activeBranch.branchCode}" else "Colleges for ${activeBranch.branchCode}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Place: $selectedDistrictFilter • Fees Updated for August 2026",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (displayedCutoffList.size < activeDisplayList.size) "Top 5 of ${activeDisplayList.size}" else "${activeDisplayList.size} Colleges",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (displayedCutoffList.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🏫",
                                        fontSize = 32.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No Colleges Available",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No engineering colleges found matching district '$selectedDistrictFilter' and '$selectedCollegeTypeFilter'. Try resetting filters or choosing another branch.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            selectedDistrictFilter = "All Districts"
                                            selectedCollegeTypeFilter = "All Types"
                                            rankInputText = ""
                                            searchResults = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Reset All Filters",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            displayedCutoffList.forEachIndexed { index, (college, cutoffInfo) ->
                                if (!isPremiumUser && index >= 1) {
                                    if (index == 1) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clickable { onUpgradeClick() },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "🔒 Unlock ${activeDisplayList.size - 1}+ More Colleges & Category Cutoffs",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Get full access to all Engineering Colleges in Karnataka with Category-wise Cutoffs (GM, 2A, 2B, 3A, 3B, SC, ST) & Fee structures.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFB45309),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Button(
                                                    onClick = onUpgradeClick,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                    shape = RoundedCornerShape(14.dp),
                                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 48.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("⭐ Unlock KCET Premium Pass (₹$subscriptionPrice)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (index < 3) Color(0xFF16A34A) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rank #${index + 1} Preference",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index < 3) Color(0xFF15803D) else Color(0xFF475569)
                                        )
                                    }

                                    BranchCollegeCutoffCard(
                                        college = college,
                                        cutoffInfo = cutoffInfo,
                                        category = "GM",
                                        branchCode = activeBranch.branchCode,
                                        userRank = userRank
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            if (isPremiumUser && activeDisplayList.size > 5) {
                                OutlinedButton(
                                    onClick = { showAllColleges = !showAllColleges },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF16A34A)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF16A34A))
                                ) {
                                    Icon(
                                        imageVector = if (showAllColleges) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showAllColleges) "Show Top 5 Only ▲" else "View All ${activeDisplayList.size} Colleges ▼",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    if (showDistrictDialog) {
        AlertDialog(
            onDismissRequest = {
                showDistrictDialog = false
                districtSearchQuery = ""
            },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF2563EB).copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Select District / Place",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "All 31 Districts of Karnataka",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                showDistrictDialog = false
                                districtSearchQuery = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Karnataka's 31 Districts (Live Database):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .clickable {
                                    if (!isSyncingFirebase) {
                                        isSyncingFirebase = true
                                        coroutineScope.launch {
                                            viewModel.syncCollegesAndCutoffsFromFirebase { success, msg ->
                                                isSyncingFirebase = false
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncingFirebase) "Syncing..." else "Firebase Sync",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = districtSearchQuery,
                        onValueChange = { districtSearchQuery = it },
                        placeholder = { Text("Search district e.g. Mysuru, Mangaluru, Belagavi...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            if (districtSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { districtSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }
            },
            text = {
                val filteredDistrictList = remember(districtSearchQuery) {
                    if (districtSearchQuery.isBlank()) {
                        KarnatakaDistricts.FILTER_OPTIONS
                    } else {
                        val q = districtSearchQuery.trim().lowercase()
                        KarnatakaDistricts.FILTER_OPTIONS.filter {
                            it.lowercase().contains(q) ||
                            (it == "All Districts" && ("all".contains(q) || "karnataka".contains(q)))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredDistrictList) { district ->
                        val isSelected = selectedDistrictFilter == district
                        val collegeCountInDistrict = remember(colleges, district) {
                            if (district == "All Districts") {
                                colleges.size
                            } else {
                                colleges.count { col ->
                                    KarnatakaDistricts.matchesDistrict(
                                        collegeDistrict = col.district,
                                        selectedFilter = district,
                                        collegeName = col.name,
                                        mapLocationQuery = col.mapLocationQuery
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = {
                                selectedDistrictFilter = district
                                searchResults = null
                                showDistrictDialog = false
                                districtSearchQuery = ""
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, Color(0xFFBFDBFE)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (district == "All Districts") Icons.Default.Public else Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (district == "All Districts") "All Districts (All Karnataka)" else district,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                                        )
                                        if (collegeCountInDistrict > 0) {
                                            Text(
                                                text = "$collegeCountInDistrict colleges available • Firebase Live",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDistrictDialog = false
                        districtSearchQuery = ""
                    }
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun BranchCollegeCutoffCard(
    college: CollegeInfo,
    cutoffInfo: BranchCutoffInfo,
    category: String,
    branchCode: String,
    userRank: Int?
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val cutoffRank = cutoffInfo.categoryCutoffs[category] ?: 15000
    val round2Estimate = (cutoffRank * 1.10f).toInt()

    val chanceStatus = remember(cutoffRank, userRank) {
        if (userRank == null || userRank <= 0) null
        else if (userRank <= cutoffRank) "HIGH_CHANCE"
        else if (userRank <= round2Estimate) "MODERATE_CHANCE"
        else "LOW_CHANCE"
    }

    // Current Autonomous, NUCAT/Deemed & UGCET tie-up status checks
    val isNucatDeemed = college.id == "col_nmamit" ||
            college.code.contains("NUCAT", ignoreCase = true) ||
            college.collegeType.contains("NUCAT", ignoreCase = true) ||
            college.collegeType.contains("Deemed", ignoreCase = true) ||
            college.name.contains("NMAM", ignoreCase = true)

    val isAutonomous = college.collegeType.contains("Autonomous", ignoreCase = true) ||
            college.name.contains("Autonomous", ignoreCase = true) ||
            listOf("RVCE", "BMSCE", "MSRIT", "PESU", "SJCE", "NIE", "BIT", "GIT", "SJEC", "MITE", "SCEM", "SIT").any { college.name.contains(it, ignoreCase = true) }

    val isUgcetTiedUp = !isNucatDeemed && !college.name.contains("Deemed", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isNucatDeemed -> Color(0xFF0284C7)
                chanceStatus == "HIGH_CHANCE" -> Color(0xFF16A34A)
                chanceStatus == "MODERATE_CHANCE" -> Color(0xFFD97706)
                chanceStatus == "LOW_CHANCE" -> Color(0xFFDC2626)
                else -> Color(0xFFE2E8F0)
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hero College Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                val imageUrl = if (college.imageResUrl.isNotBlank()) college.imageResUrl
                else "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80"

                val isBase64 = remember(imageUrl) { ImageUtils.isBase64Image(imageUrl) }
                val bannerBitmap = remember(imageUrl, isBase64) {
                    if (isBase64) ImageUtils.base64ToBitmap(imageUrl) else null
                }

                if (bannerBitmap != null) {
                    Image(
                        bitmap = bannerBitmap.asImageBitmap(),
                        contentDescription = college.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = college.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Top Badges (NAAC Grade & NIRF Rank)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = "NAAC ${college.naacGrade} • NIRF #${college.nirfRank}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // College Name & Location on Image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (college.logoResUrl.isNotBlank()) {
                        AsyncImage(
                            model = college.logoResUrl,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = college.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${college.district} • Code: ${college.code}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "Code: ${college.code}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = college.district,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isNucatDeemed) Color(0xFFE0F2FE) else if (isAutonomous) Color(0xFFDCFCE7) else Color(0xFFF3E8FF)
                        ) {
                            Text(
                                text = if (isNucatDeemed) "🎓 NUCAT Exam" else if (isAutonomous) "🏛️ Autonomous" else "🎓 VTU Affiliated",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNucatDeemed) Color(0xFF0369A1) else if (isAutonomous) Color(0xFF15803D) else Color(0xFF7E22CE),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isNucatDeemed) Color(0xFFFEE2E2) else if (isUgcetTiedUp) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (isNucatDeemed) "❌ No KCET (NUCAT Only)" else if (isUgcetTiedUp) "✅ UGCET 2026 KEA" else "⚠️ Direct Quota",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNucatDeemed) Color(0xFFB91C1C) else if (isUgcetTiedUp) Color(0xFFB45309) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Cutoff Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isNucatDeemed) Color(0xFFE0F2FE) else Color(0xFFDCFCE7)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isNucatDeemed) "NUCAT" else "#$cutoffRank",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNucatDeemed) Color(0xFF0369A1) else Color(0xFF15803D)
                            )
                            Text(
                                text = if (isNucatDeemed) "Exam Only" else "$category Cutoff",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNucatDeemed) Color(0xFF0284C7) else Color(0xFF166534)
                            )
                        }
                    }
                }
            }

            if (isNucatDeemed) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ NMAMIT (Nitte) is a Deemed University that admits via NUCAT Entrance Test (nucat.nitte.edu.in) & Management Quota. It does NOT accept KCET UGCET merit seats.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Metrics Row with Current August 2026 Fees
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💰 Aug 2026 Fee: ₹${college.tuitionFeePerYear / 1000}k/yr",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
                Text(
                    text = "💼 Avg: ₹${college.avgPackageLpa} LPA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "📊 Placed: ${college.placementPercentage}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0284C7)
                )
            }

            // Direct Official Website Link Row
            if (college.websiteUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0FDF4))
                        .clickable {
                            try {
                                val rawUrl = college.websiteUrl.trim()
                                val validUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) "https://$rawUrl" else rawUrl
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Website",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🌐 Official Website: ${college.websiteUrl.removePrefix("https://").removePrefix("http://")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Visit ↗",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Status Badge if Rank entered
            if (chanceStatus != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val (badgeBg, badgeFg, statusText) = when (chanceStatus) {
                    "HIGH_CHANCE" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "🟢 High Chance of Seat Allotment (Cutoff #${cutoffRank} > Your Rank #${userRank})")
                    "MODERATE_CHANCE" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "🟡 Moderate Chance in Round 2 / Extended (Est. Cutoff ~#${round2Estimate})")
                    else -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "🔴 High Competition / Dream College (Cutoff #${cutoffRank} < Your Rank #${userRank})")
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expanded Details View
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "📊 Complete Category KCET Cutoff Ranks (Round 1)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cutoffInfo.categoryCutoffs.forEach { (catKey, rankVal) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(
                                    if (catKey == category) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(catKey, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            Text("#$rankVal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Est. Round 2 Cutoff:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("#$round2Estimate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                    Column {
                        Text("Highest Package:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("₹${college.highestPackageLpa} LPA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                    Column {
                        Text("Hostel Fee:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("₹${college.hostelFeePerYear / 1000}k/yr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val rawUrl = college.websiteUrl.trim()
                                val validUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) "https://$rawUrl" else rawUrl
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Website", fontSize = 11.sp, color = Color(0xFF16A34A))
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val query = college.mapLocationQuery.ifBlank { "${college.name} ${college.district}" }
                                val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open maps", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map", fontSize = 11.sp, color = Color(0xFF0284C7))
                    }

                    if (college.brochureUrl.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(college.brochureUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open brochure", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Brochure", fontSize = 11.sp, color = Color(0xFFD97706))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isExpanded) "Tap to collapse ▲" else "Tap for college details & links ▼",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
