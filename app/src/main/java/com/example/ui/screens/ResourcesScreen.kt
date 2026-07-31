package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfDownloadHelper
import com.example.data.ResourceItem
import com.example.ui.components.PdfViewerDialog
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    resources: List<ResourceItem>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activePreviewPdf by remember { mutableStateOf<ResourceItem?>(null) }
    var activePdfDialog by remember { mutableStateOf<Pair<ResourceItem, File>?>(null) }

    val downloadProgressMap = remember { mutableStateMapOf<String, Float>() }
    val downloadedStateMap = remember { mutableStateMapOf<String, Boolean>() }

    val sheetState = rememberModalBottomSheetState()
    val categories = listOf("All", "Syllabus", "PYQ Papers", "Formula Sheets", "PDF Notes")

    // Check existing downloaded files on launch
    LaunchedEffect(resources) {
        resources.forEach { item ->
            downloadedStateMap[item.id] = PdfDownloadHelper.isPdfDownloaded(context, item.id)
        }
    }

    val filteredList = resources.filter { item ->
        val matchesCategory = (selectedCategory == "All") || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    val startDownload: (ResourceItem) -> Unit = { item ->
        scope.launch {
            Toast.makeText(context, "Downloading ${item.title}...", Toast.LENGTH_SHORT).show()
            downloadProgressMap[item.id] = 0.1f

            val result = PdfDownloadHelper.downloadPdf(
                context = context,
                item = item,
                onProgress = { progress ->
                    downloadProgressMap[item.id] = progress
                }
            )

            downloadProgressMap.remove(item.id)

            if (result.isSuccess) {
                val file = result.getOrNull()!!
                downloadedStateMap[item.id] = true
                Toast.makeText(context, "Downloaded ${item.title}! Tap to view offline.", Toast.LENGTH_LONG).show()
                // Automatically open PDF viewer
                activePdfDialog = Pair(item, file)
            } else {
                Toast.makeText(context, "Download failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Text(
                text = "STUDY MATERIAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "KCET Study Resources",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Download official syllabus, PYQ papers, and formula revision sheets to view offline anytime.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search syllabus, papers, notes...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF4F46E5)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4F46E5),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4F46E5),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Resources List
        items(filteredList) { item ->
            val isDownloaded = downloadedStateMap[item.id] == true
            val isDownloading = downloadProgressMap.containsKey(item.id)
            val downloadProgress = downloadProgressMap[item.id] ?: 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .clickable {
                        if (isDownloaded) {
                            val pdfFile = PdfDownloadHelper.getPdfFile(context, item.id)
                            activePdfDialog = Pair(item, pdfFile)
                        } else if (!isDownloading) {
                            activePreviewPdf = item
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDownloaded) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = if (isDownloaded) Color(0xFF166534) else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = item.category.uppercase(),
                                    fontSize = 10.sp,
                                    color = Color(0xFF4F46E5),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (isDownloaded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "OFFLINE READY",
                                        fontSize = 9.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Text(
                            text = "${item.fileFormat} • ${item.fileSize} • ${item.yearOrSubject}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(36.dp),
                                color = Color(0xFF4F46E5),
                                strokeWidth = 3.dp
                            )
                        } else if (isDownloaded) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7))
                                    .clickable {
                                        val pdfFile = PdfDownloadHelper.getPdfFile(context, item.id)
                                        activePdfDialog = Pair(item, pdfFile)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "View PDF",
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF))
                                    .clickable {
                                        startDownload(item)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download PDF",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // PDF Download Details Modal Sheet
    activePreviewPdf?.let { pdf ->
        ModalBottomSheet(
            onDismissRequest = { activePreviewPdf = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = pdf.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pdf.description,
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "📄 Format: ${pdf.fileFormat}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text(text = "💾 File Size: ${pdf.fileSize}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text(text = "🏷️ Subject Tag: ${pdf.yearOrSubject}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text(text = "🌐 Direct Online Link: Active", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val pdfItem = pdf
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            activePreviewPdf = null
                            startDownload(pdfItem)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Download PDF For Offline Viewing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // Active Offline PDF Render Dialog
    activePdfDialog?.let { (item, file) ->
        PdfViewerDialog(
            resource = item,
            pdfFile = file,
            onDismiss = { activePdfDialog = null }
        )
    }
}
