package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import com.example.data.CollegeInfo
import com.example.data.CollegeRecommendationQuery
import com.example.data.RecommendedCollegeItem
import com.example.data.ImageUtils
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCollegeRecommendationScreen(
    viewModel: MainViewModel,
    isPremiumUser: Boolean = false,
    onUpgradeClick: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colleges by viewModel.colleges.collectAsState()
    val favoriteIds by viewModel.favoriteCollegeIds.collectAsState()
    val comparedIds by viewModel.comparedCollegeIds.collectAsState()
    val historyList by viewModel.recommendationHistory.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsStateWithLifecycle()

    val rankRanges = listOf(
        "1 – 1,000",
        "1,000 – 2,500",
        "2,500 – 5,000",
        "5,000 – 10,000",
        "10,000 – 15,000",
        "15,000 – 25,000",
        "25,000 – 50,000",
        "50,000 – 75,000",
        "75,000 – 100,000+"
    )
    var selectedRankRange by remember { mutableStateOf("2,500 – 5,000") }
    var expandedRankDropdown by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val mappedRank = when {
        selectedRankRange.contains("1 – 1,000") || selectedRankRange.contains("100 to 1,000") -> 500
        selectedRankRange.contains("1,000 – 2,500") -> 1750
        selectedRankRange.contains("2,500 – 5,000") -> 3750
        selectedRankRange.contains("1,000 to 5,000") -> 3000
        selectedRankRange.contains("5,000 – 10,000") || selectedRankRange.contains("5,000 to 15,000") -> 7500
        selectedRankRange.contains("10,000 – 15,000") -> 12500
        selectedRankRange.contains("15,000 – 25,000") || selectedRankRange.contains("15,000 to 30,000") -> 20000
        selectedRankRange.contains("25,000 – 50,000") || selectedRankRange.contains("30,000 to 50,000") -> 37500
        selectedRankRange.contains("50,000 – 75,000") -> 62500
        selectedRankRange.contains("75,000 – 100,000") || selectedRankRange.contains("50,000 to 100,000+") -> 85000
        else -> 3000
    }

    var selectedCategory by remember { mutableStateOf("GM") }
    var budgetMaxFee by remember { mutableStateOf(9999999f) }
    var selectedDistrict by remember { mutableStateOf("Any District") }
    var selectedCollegeType by remember { mutableStateOf("All") }
    var hostelRequired by remember { mutableStateOf(true) }
    var selectedPlacementQuality by remember { mutableStateOf("Any") }
    var selectedNaacGrade by remember { mutableStateOf("Any") }
    var nirfOnly by remember { mutableStateOf(false) }

    var recommendations by remember { mutableStateOf<List<RecommendedCollegeItem>>(emptyList()) }
    var hasCalculated by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var selectedCollegeForDetails by remember { mutableStateOf<CollegeInfo?>(null) }
    var showCompareDialog by remember { mutableStateOf(false) }

    val categories = listOf("GM", "2A", "2B", "3A", "3B", "SC", "ST")
    val branches = listOf(
        "Computer Science & Engineering",
        "Artificial Intelligence & Machine Learning",
        "Data Science & Analytics",
        "Information Science & Engineering",
        "Electronics & Communication Engineering",
        "Electrical & Electronics Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Aerospace Engineering",
        "Biotechnology & Biochemical"
    )
    val districts = listOf(
        "Any District",
        "Bengaluru Urban",
        "Bengaluru Rural",
        "Mysuru",
        "Dakshina Kannada",
        "Udupi",
        "Dharwad",
        "Belagavi",
        "Tumakuru",
        "Shivamogga",
        "Davanagere",
        "Ballari",
        "Kalaburagi",
        "Hassan",
        "Mandya",
        "Bagalkote",
        "Bidar",
        "Chamarajanagar",
        "Chikkaballapura",
        "Chikkamagaluru",
        "Chitradurga",
        "Gadag",
        "Haveri",
        "Kodagu",
        "Kolar",
        "Koppal",
        "Raichur",
        "Ramanagara",
        "Uttara Kannada",
        "Vijayanagara",
        "Vijayapura",
        "Yadgir"
    )
    val collegeTypes = listOf("All", "Autonomous", "Government", "Private")

    // Run initial recommendation if not done yet
    LaunchedEffect(Unit) {
        if (!hasCalculated) {
            val query = CollegeRecommendationQuery(
                rank = mappedRank,
                category = selectedCategory,
                budgetMaxFee = budgetMaxFee.toInt(),
                preferredDistrict = selectedDistrict,
                collegeType = selectedCollegeType,
                hostelRequired = hostelRequired,
                placementQuality = selectedPlacementQuality,
                preferredNaacGrade = selectedNaacGrade,
                nirfOnly = nirfOnly
            )
            recommendations = viewModel.generateCollegeRecommendations(query)
            hasCalculated = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏛️ College Recommendation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0D9488).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "KCET 2026",
                                    color = Color(0xFF0D9488),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Admin & Smart Rank Range Cutoff Analyzer",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
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
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recommendation History",
                            tint = Color(0xFF0284C7)
                        )
                    }
                    if (comparedIds.isNotEmpty()) {
                        BadgedBox(
                            badge = { Badge { Text(comparedIds.size.toString()) } }
                        ) {
                            IconButton(onClick = { showCompareDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = "Compare Colleges",
                                    tint = Color(0xFF7C3AED)
                                )
                            }
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
                                    text = "👑 KCET Premium — College Recommendation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Unlock College Recommendations & Cutoffs based on your KCET Rank Range & Budget",
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

            // Hero Input Form Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D9488).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Your KCET Profile Filters",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Set rank & preferences for AI matching",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            TextButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Filters",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 1: KCET Rank Range + Category
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expandedRankDropdown,
                                onExpandedChange = { expandedRankDropdown = !expandedRankDropdown },
                                modifier = Modifier.weight(1.3f)
                            ) {
                                OutlinedTextField(
                                    value = selectedRankRange,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("KCET Rank Range") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.FormatListNumbered,
                                            contentDescription = null,
                                            tint = Color(0xFF0D9488)
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRankDropdown)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRankDropdown,
                                    onDismissRequest = { expandedRankDropdown = false }
                                ) {
                                    rankRanges.forEach { range ->
                                        DropdownMenuItem(
                                            text = { Text(range) },
                                            onClick = {
                                                selectedRankRange = range
                                                expandedRankDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = expandedCategoryDropdown,
                                onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0D9488),
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCategoryDropdown,
                                    onDismissRequest = { expandedCategoryDropdown = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                selectedCategory = cat
                                                expandedCategoryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (!isPremiumUser) {
                                    Toast.makeText(context, "🔒 College Recommendation is a Premium Feature. Upgrade to unlock!", Toast.LENGTH_LONG).show()
                                    onUpgradeClick()
                                    return@Button
                                }
                                val query = CollegeRecommendationQuery(
                                    rank = mappedRank,
                                    rankRange = selectedRankRange,
                                    category = selectedCategory,
                                    budgetMaxFee = budgetMaxFee.toInt(),
                                    preferredDistrict = selectedDistrict,
                                    collegeType = selectedCollegeType,
                                    hostelRequired = hostelRequired,
                                    placementQuality = selectedPlacementQuality,
                                    preferredNaacGrade = selectedNaacGrade,
                                    nirfOnly = nirfOnly
                                )
                                recommendations = viewModel.generateCollegeRecommendations(query)
                                Toast.makeText(context, "🤖 Found ${recommendations.size} matching colleges!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🤖 Find Best Recommended Colleges",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Results Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recommended Colleges (${recommendations.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Sorted by Admission Match % for Rank Range: $selectedRankRange ($selectedCategory)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (recommendations.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = recommendations.take(5).joinToString("\n\n") {
                                "🏛️ ${it.college.name} (${it.college.district})\nBranch: ${it.branchName}\nAdmission Chance: ${it.admissionChance} (${it.admissionChancePercentage}%)\nCutoff: #${it.closingRank} | Fees: ₹${it.totalEstimatedCostPerYear}/yr\nAvg Salary: ₹${it.college.avgPackageLpa} LPA"
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "🤖 KCET 2026 AI Recommended Colleges:\n\n$text\n\nGenerated via KCET Gen Z App.")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share College Recommendations"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Recommendations",
                                tint = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

            // College Items List
            if (recommendations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍 No Exact Matches for Current Filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try adjusting your budget max fee, increasing rank range, or changing district filter to 'Any District'.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(recommendations) { index, item ->
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
                                        text = "🔒 Unlock ${recommendations.size - 1}+ More Recommended Colleges",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Get complete AI admission probability %, placement LPA figures, fees & college comparisons for your KCET rank.",
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
                        RecommendedCollegeCard(
                            item = item,
                            isFavorite = favoriteIds.contains(item.college.id),
                            isCompared = comparedIds.contains(item.college.id),
                            onToggleFavorite = { viewModel.toggleFavoriteCollege(item.college.id) },
                            onToggleCompare = { viewModel.toggleCompareCollege(item.college.id) },
                            onViewDetails = { selectedCollegeForDetails = item.college }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Filter BottomSheet / Dialog
    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = {
                Text("⚙️ Advanced Recommendation Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // District
                    Column {
                        Text("District Preference", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(districts) { dist ->
                                FilterChip(
                                    selected = selectedDistrict == dist,
                                    onClick = { selectedDistrict = dist },
                                    label = { Text(dist, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // College Type
                    Column {
                        Text("College Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(collegeTypes) { type ->
                                FilterChip(
                                    selected = selectedCollegeType == type,
                                    onClick = { selectedCollegeType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Hostel & NIRF Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("In-Campus Hostel Required", fontSize = 12.sp)
                        Switch(checked = hostelRequired, onCheckedChange = { hostelRequired = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NIRF Ranked Colleges Only (Top 100)", fontSize = 12.sp)
                        Switch(checked = nirfOnly, onCheckedChange = { nirfOnly = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFilterSheet = false
                        val query = CollegeRecommendationQuery(
                            rank = mappedRank,
                            rankRange = selectedRankRange,
                            category = selectedCategory,
                            budgetMaxFee = budgetMaxFee.toInt(),
                            preferredDistrict = selectedDistrict,
                            collegeType = selectedCollegeType,
                            hostelRequired = hostelRequired,
                            placementQuality = selectedPlacementQuality,
                            preferredNaacGrade = selectedNaacGrade,
                            nirfOnly = nirfOnly
                        )
                        recommendations = viewModel.generateCollegeRecommendations(query)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text("Apply & Search")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Recommendation History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("📜 Recommendation History", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                if (historyList.isEmpty()) {
                    Text("No past recommendations saved yet.", fontSize = 13.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { hist ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Rank #${hist.query.rank} (${hist.query.category}) • ${hist.query.preferredBranch}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Top Match: ${hist.topMatchCollegeName} (${hist.resultsCount} matches)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // College Details Modal
    if (selectedCollegeForDetails != null) {
        CollegeDetailModalDialog(
            college = selectedCollegeForDetails!!,
            onDismiss = { selectedCollegeForDetails = null }
        )
    }

    // Comparison Modal Dialog
    if (showCompareDialog && comparedIds.isNotEmpty()) {
        val comparedColleges = colleges.filter { comparedIds.contains(it.id) }
        CollegeCompareModalDialog(
            colleges = comparedColleges,
            onDismiss = { showCompareDialog = false },
            onClear = {
                viewModel.clearComparison()
                showCompareDialog = false
            }
        )
    }
}

@Composable
private fun RecommendedCollegeCard(
    item: RecommendedCollegeItem,
    isFavorite: Boolean,
    isCompared: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleCompare: () -> Unit,
    onViewDetails: () -> Unit
) {
    val college = item.college
    val context = LocalContext.current

    val (badgeBg, badgeText) = when (item.admissionChance) {
        "High" -> Color(0xFF16A34A) to "HIGH CHANCE 🔥 (${item.admissionChancePercentage}%)"
        "Medium" -> Color(0xFFD97706) to "MEDIUM CHANCE 🎯 (${item.admissionChancePercentage}%)"
        "Moderate" -> Color(0xFF2563EB) to "MODERATE (${item.admissionChancePercentage}%)"
        else -> Color(0xFFE11D48) to "LOW / BACKUP (${item.admissionChancePercentage}%)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (item.admissionChance == "High") Color(0xFF16A34A).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image & Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val collegeImg = college.imageResUrl.ifBlank { "https://images.unsplash.com/photo-1562774053-701939374585?w=800&q=80" }
                val isBase64Rec = remember(collegeImg) { ImageUtils.isBase64Image(collegeImg) }
                val recBitmap = remember(collegeImg, isBase64Rec) {
                    if (isBase64Rec) ImageUtils.base64ToBitmap(collegeImg) else null
                }

                if (recBitmap != null) {
                    Image(
                        bitmap = recBitmap.asImageBitmap(),
                        contentDescription = college.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = collegeImg,
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
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Top Badge
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = badgeBg,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Favorite Icon
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE11D48) else Color.White
                    )
                }

                // College Name & Location on Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = college.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = college.code,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${college.district} • ${college.collegeType}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NAAC ${college.naacGrade} • NIRF #${college.nirfRank}",
                            color = Color(0xFFFDE047),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Cutoff & Fee Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Branch & Closing Rank", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${item.branchCode} - #${item.closingRank}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tuition + Hostel Fee / yr", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "₹${college.tuitionFeePerYear / 1000}k + ₹${college.hostelFeePerYear / 1000}k",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Placement Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Avg: ₹${college.avgPackageLpa} LPA | Highest: ₹${college.highestPackageLpa} LPA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    Text(
                        text = "Placed: ${college.placementPercentage}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Match Explanation Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0D9488).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.aiMatchExplanation,
                            fontSize = 11.sp,
                            color = Color(0xFF115E59),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleCompare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCompared) Color(0xFF7C3AED) else Color(0xFF475569)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCompared) "Compared ✓" else "Compare", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CollegeDetailModalDialog(
    college: CollegeInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = college.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "Code: ${college.code} • District: ${college.district} • Type: ${college.collegeType}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                HorizontalDivider()

                Text("🏛️ Academic & Ranking", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• NAAC Rating: ${college.naacGrade}", fontSize = 12.sp)
                Text("• NIRF Ranking: #${college.nirfRank} in India", fontSize = 12.sp)
                Text("• Student Rating: ⭐ ${college.studentRating} / 5.0", fontSize = 12.sp)

                Text("💰 Annual Fee Structure", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• Tuition Fee: ₹${college.tuitionFeePerYear} / year", fontSize = 12.sp)
                Text("• Hostel & Mess Fee: ₹${college.hostelFeePerYear} / year", fontSize = 12.sp)

                Text("💼 Placement Highlights", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• Highest Package: ₹${college.highestPackageLpa} LPA", fontSize = 12.sp)
                Text("• Average Package: ₹${college.avgPackageLpa} LPA", fontSize = 12.sp)
                Text("• Placement Record: ${college.placementPercentage}% students placed", fontSize = 12.sp)
                Text("• Top Recruiters: ${college.topRecruiters.joinToString(", ")}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${college.mapLocationQuery}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Maps", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val url = if (college.websiteUrl.startsWith("http")) college.websiteUrl else "https://${college.websiteUrl}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Website", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CollegeCompareModalDialog(
    colleges: List<CollegeInfo>,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 College Side-by-Side Comparison", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colleges) { col ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(col.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                Text("${col.district} • ${col.code}", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• NAAC: ${col.naacGrade}", fontSize = 11.sp)
                                Text("• NIRF: #${col.nirfRank}", fontSize = 11.sp)
                                Text("• Tuition Fee: ₹${col.tuitionFeePerYear / 1000}k", fontSize = 11.sp)
                                Text("• Hostel Fee: ₹${col.hostelFeePerYear / 1000}k", fontSize = 11.sp)
                                Text("• Avg Salary: ₹${col.avgPackageLpa} LPA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                Text("• Placed: ${col.placementPercentage}%", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text("Clear Selection", color = Color.Red) }
        }
    )
}
