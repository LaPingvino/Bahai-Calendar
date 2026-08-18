package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devotional.*
import com.example.ui.components.ChantingCounterDialog
import com.example.ui.components.DevotionalProgramPlayerDialog
import com.example.ui.components.QiblihCompassDialog
import com.example.ui.components.WritingDetailDialog
import com.example.ui.theme.HolyDayGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionalScreen(
    currentLocation: CityLocation,
    onSelectLocation: (CityLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<WritingCategory?>(null) }
    var activeWritingDetail by remember { mutableStateOf<HolyWriting?>(null) }
    var activeProgramPlayer by remember { mutableStateOf<DevotionalProgram?>(null) }
    var showChantingCounter by remember { mutableStateOf(false) }
    var showQiblihCompass by remember { mutableStateOf(false) }

    val filteredWritings = remember(searchQuery, selectedCategory) {
        DevotionalRepository.ALL_WRITINGS.filter { writing ->
            val matchesCategory = selectedCategory == null || writing.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    writing.title.contains(searchQuery, ignoreCase = true) ||
                    writing.author.contains(searchQuery, ignoreCase = true) ||
                    writing.textEnglish.contains(searchQuery, ignoreCase = true) ||
                    writing.category.displayName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("devotional_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // Quick Tools Row (95 Counter, Qiblih Compass, Fasting Readings)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Devotional & Spiritual Tools",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 95 Counter button
                        OutlinedButton(
                            onClick = { showChantingCounter = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_chanting_counter_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("📿", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("95 Alláh-u-Abhá", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Qiblih compass button
                        OutlinedButton(
                            onClick = { showQiblihCompass = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_qiblih_compass_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("🧭", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Qiblih Direction", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devotional_search_field"),
                placeholder = { Text("Search prayers, Hidden Words, authors...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Sacred Texts") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                items(WritingCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        leadingIcon = {
                            Text(category.iconEmoji, fontSize = 12.sp)
                        },
                        label = { Text(category.displayName) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Devotional Programs Section (if no query or looking for programs)
        if (searchQuery.isBlank() && (selectedCategory == null || selectedCategory == WritingCategory.DEVOTIONAL_PROGRAMS)) {
            item {
                Text(
                    text = "Devotional Gatherings & Programs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(DevotionalRepository.PRESET_PROGRAMS) { program ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeProgramPlayer = program },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HolyDayGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(program.themeEmoji, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = program.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = program.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${program.writingIds.size} Readings • Tap to Start",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Program",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Sacred Writings & Prayers
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory != null) selectedCategory!!.displayName else "Sacred Texts & Prayers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredWritings.size} texts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Writings List
        if (filteredWritings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No prayers or writings found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredWritings) { writing ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeWritingDetail = writing },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(writing.category.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = writing.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (writing.textArabicPersian != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = HolyDayGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "عربي/فارسي",
                                        fontSize = 10.sp,
                                        color = HolyDayGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Author: ${writing.author} • ${writing.sourceReference}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = writing.textEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    activeWritingDetail?.let { writing ->
        WritingDetailDialog(
            writing = writing,
            onDismiss = { activeWritingDetail = null }
        )
    }

    activeProgramPlayer?.let { program ->
        val writingsForProgram = program.writingIds.mapNotNull { id ->
            DevotionalRepository.ALL_WRITINGS.find { it.id == id }
        }
        DevotionalProgramPlayerDialog(
            program = program,
            writings = writingsForProgram,
            onDismiss = { activeProgramPlayer = null }
        )
    }

    if (showChantingCounter) {
        ChantingCounterDialog(
            onDismiss = { showChantingCounter = false }
        )
    }

    if (showQiblihCompass) {
        QiblihCompassDialog(
            currentLocation = currentLocation,
            onDismiss = { showQiblihCompass = false }
        )
    }
}
