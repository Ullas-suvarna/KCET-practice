package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SyllabusChapter
import com.example.data.SyllabusVideo
import com.example.data.SyllabusVideoRepository
import com.example.data.SubjectType
import com.example.data.VideoCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyllabusVideosScreen() {
    val context = LocalContext.current
    val allChapters = remember { SyllabusVideoRepository.getSyllabusChapters() }

    var selectedSubject by remember { mutableStateOf<SubjectType?>(null) } // null = All
    var selectedCategory by remember { mutableStateOf(VideoCategory.ALL) }
    var selectedClassFilter by remember { mutableStateOf("All") } // "All", "Class 11", "Class 12"
    var searchQuery by remember { mutableStateOf("") }

    // Track active embedded playing video
    var activePlayingVideo by remember { mutableStateOf<SyllabusVideo?>(null) }
    var activeChapterSynopsis by remember { mutableStateOf<SyllabusChapter?>(null) }

    // State for tracking bookmarked videos & completed chapters
    val bookmarkedVideoIds = remember { mutableStateMapOf<String, Boolean>() }
    val completedChapterIds = remember { mutableStateMapOf<String, Boolean>("math_02" to true, "phy_01" to true) }

    // Expanded state for chapter synopsis drawers
    val expandedChapterIds = remember { mutableStateMapOf<String, Boolean>("math_01" to true) }

    // Filter logic
    val filteredChapters = allChapters.filter { ch ->
        val matchesSubject = selectedSubject == null || ch.subject == selectedSubject
        val matchesClass = when (selectedClassFilter) {
            "Class 11" -> ch.classLevel.contains("11")
            "Class 12" -> ch.classLevel.contains("12")
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            ch.chapterName.contains(searchQuery, ignoreCase = true) ||
                    ch.synopsisPoints.any { it.contains(searchQuery, ignoreCase = true) } ||
                    ch.videos.any { it.title.contains(searchQuery, ignoreCase = true) }
        }
        val matchesCategory = if (selectedCategory == VideoCategory.ALL) true else {
            ch.videos.any { it.category == selectedCategory }
        }

        matchesSubject && matchesClass && matchesSearch && matchesCategory
    }

    // Progress calculation
    val completedCount = completedChapterIds.count { it.value }
    val totalChaptersCount = allChapters.size
    val progressPercent = if (totalChaptersCount > 0) (completedCount.toFloat() / totalChaptersCount) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("syllabus_videos_screen")
    ) {
        // --- Top Header Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFDC2626), Color(0xFFE11D48))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "Videos Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KCET Syllabus & Videos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Chapter Synopsis, One-Shots & PYQ Marathons",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Syllabus Progress Bar ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Syllabus Mastery Progress",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "$completedCount / $totalChaptersCount Chapters (${(progressPercent * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFDBEAFE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Subject Filter Selector Row ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SubjectChip(
                            label = "🌟 All Subjects",
                            isSelected = selectedSubject == null,
                            activeColor = Color(0xFF4F46E5),
                            onClick = { selectedSubject = null }
                        )
                    }
                    items(SubjectType.values()) { sub ->
                        val col = when (sub) {
                            SubjectType.PHYSICS -> Color(0xFF4F46E5)
                            SubjectType.CHEMISTRY -> Color(0xFF059669)
                            SubjectType.MATHEMATICS -> Color(0xFFD97706)
                            SubjectType.BIOLOGY -> Color(0xFFDC2626)
                        }
                        SubjectChip(
                            label = sub.displayName,
                            isSelected = selectedSubject == sub,
                            activeColor = col,
                            onClick = { selectedSubject = sub }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Video Type Category Pills ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(VideoCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Search & Class Filter Bar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search chapters, formulas, or video titles...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Class Filter Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("All", "Class 11", "Class 12").forEach { cls ->
                            val isSel = selectedClassFilter == cls
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color(0xFF334155) else Color(0xFFE2E8F0))
                                    .clickable { selectedClassFilter = cls }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cls,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Chapters & Videos Main List ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredChapters, key = { it.id }) { chapter ->
                ChapterSyllabusCard(
                    chapter = chapter,
                    isCompleted = completedChapterIds[chapter.id] == true,
                    isExpanded = expandedChapterIds[chapter.id] == true,
                    onToggleComplete = {
                        val current = completedChapterIds[chapter.id] ?: false
                        completedChapterIds[chapter.id] = !current
                    },
                    onToggleExpand = {
                        val current = expandedChapterIds[chapter.id] ?: false
                        expandedChapterIds[chapter.id] = !current
                    },
                    onPlayVideo = { video ->
                        activePlayingVideo = video
                        activeChapterSynopsis = chapter
                    },
                    bookmarkedVideoIds = bookmarkedVideoIds,
                    onBookmarkToggle = { vidId ->
                        val current = bookmarkedVideoIds[vidId] ?: false
                        bookmarkedVideoIds[vidId] = !current
                    },
                    selectedCategory = selectedCategory
                )
            }
        }
    }

    // --- Embedded YouTube Video Player Dialog Modal ---
    activePlayingVideo?.let { video ->
        EmbeddedVideoPlayerDialog(
            video = video,
            chapter = activeChapterSynopsis,
            onDismiss = {
                activePlayingVideo = null
                activeChapterSynopsis = null
            },
            onOpenYoutube = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.youtubeVideoId}"))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun SubjectChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = if (isSelected) activeColor else Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) activeColor else Color(0xFFE2E8F0)
        )
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterSyllabusCard(
    chapter: SyllabusChapter,
    isCompleted: Boolean,
    isExpanded: Boolean,
    onToggleComplete: () -> Unit,
    onToggleExpand: () -> Unit,
    onPlayVideo: (SyllabusVideo) -> Unit,
    bookmarkedVideoIds: Map<String, Boolean>,
    onBookmarkToggle: (String) -> Unit,
    selectedCategory: VideoCategory
) {
    val subjectThemeColor = when (chapter.subject) {
        SubjectType.PHYSICS -> Color(0xFF4F46E5)
        SubjectType.CHEMISTRY -> Color(0xFF059669)
        SubjectType.MATHEMATICS -> Color(0xFFD97706)
        SubjectType.BIOLOGY -> Color(0xFFDC2626)
    }

    val visibleVideos = chapter.videos.filter {
        selectedCategory == VideoCategory.ALL || it.category == selectedCategory
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Chapter Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(subjectThemeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = chapter.subject.displayName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = subjectThemeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = chapter.classLevel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chapter.isHighYield) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 High Yield",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = chapter.weightageMarks,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter Title & Checkmark Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chapter.chapterName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onToggleComplete() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Complete Chapter",
                        tint = if (isCompleted) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle Expand Synopsis Notes Drawer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 Chapter Synopsis & Formula Shortcuts (${chapter.synopsisPoints.size} Key Points)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4338CA)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF4338CA),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expandable Synopsis Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEEF2FF))
                        .padding(12.dp)
                ) {
                    chapter.synopsisPoints.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "• ", fontWeight = FontWeight.Bold, color = subjectThemeColor, fontSize = 13.sp)
                            Text(text = point, fontSize = 12.sp, color = Color(0xFF1E293B), lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Lectures Section Label
            Text(
                text = "🎥 Curated Video Lectures (${visibleVideos.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Video Cards List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleVideos.forEach { video ->
                    VideoItemCard(
                        video = video,
                        onPlay = { onPlayVideo(video) },
                        isBookmarked = bookmarkedVideoIds[video.id] == true,
                        onBookmarkToggle = { onBookmarkToggle(video.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoItemCard(
    video: SyllabusVideo,
    onPlay: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit
) {
    val categoryBadgeBg = when (video.category) {
        VideoCategory.ONE_SHOT -> Color(0xFFFEF3C7)
        VideoCategory.MARATHON -> Color(0xFFFCE7F3)
        VideoCategory.CHAPTER_FULL -> Color(0xFFE0E7FF)
        VideoCategory.ALL -> Color(0xFFF1F5F9)
    }

    val categoryBadgeText = when (video.category) {
        VideoCategory.ONE_SHOT -> Color(0xFFD97706)
        VideoCategory.MARATHON -> Color(0xFFBE185D)
        VideoCategory.CHAPTER_FULL -> Color(0xFF4338CA)
        VideoCategory.ALL -> Color(0xFF334155)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button Thumbnail Mock
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryBadgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.category.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryBadgeText
                        )
                    }

                    Text(
                        text = "⏱️ ${video.duration}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📺 ${video.channelName} • ${video.viewsCount}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Row {
                        IconButton(
                            onClick = onBookmarkToggle,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark Video",
                                tint = if (isBookmarked) Color(0xFFD97706) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Embedded In-App YouTube Player Dialog ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbeddedVideoPlayerDialog(
    video: SyllabusVideo,
    chapter: SyllabusChapter?,
    onDismiss: () -> Unit,
    onOpenYoutube: () -> Unit
) {
    var isVideoLoading by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Video Player Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "${video.channelName} • ${video.duration}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenYoutube) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open in YouTube App",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }
                    }
                }

                // In-App Android WebView YouTube Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true

                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isVideoLoading = false
                                    }
                                }

                                val embedHtml = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                        <style>
                                            * { margin: 0; padding: 0; box-sizing: border-box; }
                                            body, html { width: 100%; height: 100%; background-color: #000; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                                            .video-container { position: relative; width: 100%; height: 100%; }
                                            iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                        </style>
                                    </head>
                                    <body>
                                        <div class="video-container">
                                            <iframe src="https://www.youtube.com/embed/${video.youtubeVideoId}?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                                        </div>
                                    </body>
                                    </html>
                                """.trimIndent()

                                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isVideoLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.85f))
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFE11D48),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading YouTube Video...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Video Info & Synopsis Drawer in Player Modal
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📝 Video Overview & Topics Discussed",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = video.description,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    video.topicsDiscussed.forEach { topic ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF334155))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "✓ $topic",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7DD3FC),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (chapter != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4338CA)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "📖 Chapter Key Formula Notes (${chapter.chapterName})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5B4FC)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    chapter.synopsisPoints.forEach { point ->
                                        Text(
                                            text = "• $point",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE0E7FF),
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = onOpenYoutube,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Full Video in YouTube App",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
