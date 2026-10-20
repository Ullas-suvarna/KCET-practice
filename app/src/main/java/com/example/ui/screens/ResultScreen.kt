package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Option
import com.example.data.Question
import com.example.data.TestResultEntity
import com.example.data.UserAnswer
import com.example.ui.components.GlassCard

@Composable
fun ResultScreen(
    result: TestResultEntity,
    questions: List<Question>,
    userAnswers: Map<Int, UserAnswer>,
    bookmarkedQuestionIds: Set<Int> = emptySet(),
    onToggleBookmark: (Question) -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onBackToDashboard: () -> Unit,
    onRetakeTest: () -> Unit
) {
    var showReviewList by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Top Header
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Test Submitted Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = result.testName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Score Summary Glass Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL SCORE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF818CF8),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${result.score} / ${result.totalQuestions}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6366F1)
                        )
                    )

                    Text(
                        text = "Accuracy: ${String.format("%.1f", result.percentage)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            icon = Icons.Default.CheckCircle,
                            tint = Color(0xFF10B981),
                            value = "${result.correctCount}",
                            label = "Correct"
                        )

                        StatItem(
                            icon = Icons.Default.Cancel,
                            tint = Color(0xFFEF4444),
                            value = "${result.wrongCount}",
                            label = "Wrong"
                        )

                        StatItem(
                            icon = Icons.Default.Help,
                            tint = Color(0xFFF59E0B),
                            value = "${result.unansweredCount}",
                            label = "Skipped"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val timeMins = result.timeTakenSeconds / 60
                    val timeSecs = result.timeTakenSeconds % 60
                    Text(
                        text = "⏱️ Time Taken: ${timeMins}m ${timeSecs}s (Speed: ${(result.timeTakenSeconds / result.totalQuestions)}s / q)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Earned XP Gamification Reward Card
        item {
            val qCount = result.totalQuestions.coerceAtLeast(1)
            val attempted = result.correctCount + result.wrongCount
            val attemptedRatio = (attempted.toFloat() / qCount).coerceIn(0f, 1f)
            val isQualified = attemptedRatio > 0.5f

            val baseCorrectXP = maxOf(0, result.correctCount) * 15
            val completionBonus = (50f * attemptedRatio).toInt()
            val speedPerQ = if (qCount > 0) (result.timeTakenSeconds / qCount).toInt() else 60
            val speedBonus = if (speedPerQ < 40) (25f * attemptedRatio).toInt() else 0

            val earnedXP = if (isQualified) (baseCorrectXP + completionBonus + speedBonus).coerceAtLeast(0) else 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (earnedXP > 0) Color(0xFFDCFCE7) else Color(0xFFFEF2F2))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (earnedXP > 0) "⚡" else "⚠️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (earnedXP > 0) "+$earnedXP Gamification XP Earned!" else "0 XP Earned (Attempted <= 50%)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (earnedXP > 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                                Text(
                                    text = if (earnedXP > 0) "Points added to your Statewide Leaderboard rank" else "Attempt >50% of questions in any test to earn XP",
                                    fontSize = 11.sp,
                                    color = if (earnedXP > 0) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (earnedXP > 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                        ) {
                            Text(
                                text = if (earnedXP > 0) "CLAIMED" else "NO XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (earnedXP > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("• Correct: +$baseCorrectXP XP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                                Text("• Completion: +$completionBonus XP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                                if (speedBonus > 0) {
                                    Text("• Speed: +$speedBonus XP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D4ED8))
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "💡 Tip: You attempted $attempted out of $qCount questions (${(attemptedRatio * 100).toInt()}%). Attempt more than 50% of questions in any test (Mock, DPP, Chapterwise, Free, PYQ) to earn Gamification XP!",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showReviewList = !showReviewList },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text(if (showReviewList) "Hide Explanations" else "Review Answers", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRetakeTest,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retake Test")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenBookmarks,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Saved Bookmarks", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                }

                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81))
                ) {
                    Icon(imageVector = Icons.Default.Dashboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Review Solutions Section
        if (showReviewList) {
            item {
                Text(
                    text = "📖 Step-by-Step Question Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(questions) { idx, q ->
                val userOpt = userAnswers[q.id]?.selectedOption
                val isCorrect = userOpt == q.correctOption
                val isBookmarked = bookmarkedQuestionIds.contains(q.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userOpt == null) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else if (isCorrect) {
                            Color(0xFF10B981).copy(alpha = 0.12f)
                        } else {
                            Color(0xFFEF4444).copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Q${idx + 1}. ${q.subject.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )

                                IconButton(
                                    onClick = { onToggleBookmark(q) },
                                    modifier = Modifier.size(28.dp).padding(start = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) Color(0xFFD97706) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    userOpt == null -> Color.Gray
                                    isCorrect -> Color(0xFF10B981)
                                    else -> Color(0xFFEF4444)
                                }
                            ) {
                                Text(
                                    text = when {
                                        userOpt == null -> "UNANSWERED"
                                        isCorrect -> "CORRECT (+1)"
                                        else -> "WRONG (0)"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (!q.imageUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            coil.compose.AsyncImage(
                                model = q.imageUrl,
                                contentDescription = "Question Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Your Answer: ${userOpt?.name ?: "None"} | Correct Answer: ${q.correctOption.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "💡 Solution Explanation:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = q.explanation,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
