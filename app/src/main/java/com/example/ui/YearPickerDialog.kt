package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPickerDialog(
    currentSelectedYear: Int,
    todayBadiYear: Int,
    currentNotationSystem: YearNotationSystem,
    onSelectYear: (Int) -> Unit,
    onToggleNotationSystem: (YearNotationSystem) -> Unit,
    onDismiss: () -> Unit
) {
    var notationMode by remember { mutableStateOf(currentNotationSystem) }
    var searchQuery by remember { mutableStateOf("") }

    // Range of Badí' years: 1 B.E. (1844) to 250 B.E. (2093+)
    val yearsList = remember { (1..250).toList() }
    val filteredYears = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            yearsList
        } else {
            val q = searchQuery.trim().lowercase()
            yearsList.filter { yr ->
                val details = BadiCalendarEngine.getYearDetails(yr)
                yr.toString().contains(q) ||
                        details.gregorianYearSpan.lowercase().contains(q) ||
                        details.vahidYearInfo.transliteration.lowercase().contains(q) ||
                        details.vahidYearInfo.translation.lowercase().contains(q)
            }
        }
    }

    val listState = rememberLazyListState()

    // Scroll to current selected year on open
    LaunchedEffect(currentSelectedYear) {
        val targetIndex = (currentSelectedYear - 1).coerceIn(0, (yearsList.size - 1).coerceAtLeast(0))
        if (targetIndex >= 0 && targetIndex < filteredYears.size) {
            listState.scrollToItem((targetIndex - 2).coerceAtLeast(0))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
                .testTag("year_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Select Badí' Year",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "1 B.E. (1844 AD) onwards",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_year_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Short vs Long System Segmented Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isShort = notationMode == YearNotationSystem.SHORT_SYSTEM
                    val isLong = notationMode == YearNotationSystem.LONG_SYSTEM

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isShort) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                notationMode = YearNotationSystem.SHORT_SYSTEM
                                onToggleNotationSystem(YearNotationSystem.SHORT_SYSTEM)
                            }
                            .padding(vertical = 8.dp)
                            .testTag("select_short_notation_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Short (e.g. 183 B.E.)",
                            color = if (isShort) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isShort) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLong) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                notationMode = YearNotationSystem.LONG_SYSTEM
                                onToggleNotationSystem(YearNotationSystem.LONG_SYSTEM)
                            }
                            .padding(vertical = 8.dp)
                            .testTag("select_long_notation_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Long (Váḥid / Kull)",
                            color = if (isLong) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isLong) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Jump to Current Year & Search Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("year_search_input"),
                        placeholder = {
                            Text("Search year, name, or AD...", fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Current year pill
                    Button(
                        onClick = {
                            onSelectYear(todayBadiYear)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("jump_to_current_year_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Now ($todayBadiYear)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Informational pill for Long System
                if (notationMode == YearNotationSystem.LONG_SYSTEM) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1 Váḥid = 19 Years • 1 Kull-i-Shay' = 19 Váḥids (361 Years)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Years list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredYears, key = { it }) { yr ->
                        val isSelected = (yr == currentSelectedYear)
                        val isCurrent = (yr == todayBadiYear)
                        val details = remember(yr) { BadiCalendarEngine.getYearDetails(yr) }

                        val cardBg = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }

                        val borderColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelectYear(yr)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("year_item_$yr")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (notationMode == YearNotationSystem.SHORT_SYSTEM) {
                                                "${details.year} B.E."
                                            } else {
                                                "Year ${details.vahidYearNumber}: ${details.vahidYearInfo.transliteration} (${details.vahidYearInfo.translation})"
                                            },
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )

                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "CURRENT",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (notationMode == YearNotationSystem.SHORT_SYSTEM) {
                                            "${details.gregorianYearSpan} • Váḥid ${details.vahidNumber} (${details.vahidYearInfo.transliteration})"
                                        } else {
                                            "${details.year} B.E. • Váḥid ${details.vahidNumber} • Kull-i-Shay' ${details.kullIShayNumber} • ${details.gregorianYearSpan}"
                                        },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = details.vahidYearInfo.arabic,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
