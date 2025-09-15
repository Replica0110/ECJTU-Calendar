package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonType
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.noRippleClickable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// 用于控制日历和年份选择器切换的枚举
private enum class DatePickerView {
    CALENDAR, YEAR
}
private val DatePickerContentHeight = 320.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var currentView by remember { mutableStateOf(DatePickerView.CALENDAR) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.width(320.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePickerHeader(
                    selectedDate = selectedDate,
                    onYearClick = { currentView = DatePickerView.YEAR },
                    onDateClick = { currentView = DatePickerView.CALENDAR }
                )
                AnimatedContent(
                    targetState = currentView,
                    modifier = Modifier.height(DatePickerContentHeight),
                    transitionSpec = {
                        fadeIn(initialAlpha = 0.9f) togetherWith fadeOut(targetAlpha = 0.9f)
                    },
                    label = "ViewSwitcher"
                ) { view ->
                    when (view) {
                        DatePickerView.CALENDAR -> CalendarView(
                            displayedMonth = displayedMonth,
                            selectedDate = selectedDate,
                            onDateClick = { selectedDate = it },
                            onMonthChange = { displayedMonth = it }
                        )
                        DatePickerView.YEAR -> YearPickerView(
                            selectedYear = selectedDate.year,
                            onYearSelected = { year ->
                                selectedDate = selectedDate.withYear(year)
                                displayedMonth = displayedMonth.withYear(year)
                                currentView = DatePickerView.CALENDAR
                            }
                        )
                    }
                }


                ActionButtons(
                    onConfirm = { onDateSelected(selectedDate) },
                    onDismiss = onDismissRequest
                )
            }
        }
    }
}


@Composable
private fun DatePickerHeader(
    selectedDate: LocalDate,
    onYearClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SaltTheme.colors.highlight)
            .padding(16.dp),
    ) {
        Text(
            text = selectedDate.format(yearFormatter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onYearClick)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = selectedDate.format(dateFormatter),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDateClick)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CalendarView(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier.background(SaltTheme.colors.background).padding(start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(displayedMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
            }
            Text(
                text = "${displayedMonth.year}年${displayedMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                style = MaterialTheme.typography.bodyLarge,
                color = SaltTheme.colors.text,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(displayedMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一个月")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))


        AnimatedContent(
            targetState = displayedMonth,
            transitionSpec = {
                val direction = if (targetState.isAfter(initialState)) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                slideIntoContainer(direction) togetherWith slideOutOfContainer(direction)
            },
            label = "MonthSwitcher"
        ) { month ->
            CalendarGrid(
                month = month,
                selectedDate = selectedDate,
                onDateClick = onDateClick
            )
        }
        HorizontalDivider(color = Color.Gray)
    }
}

@Composable
private fun CalendarGrid(month: YearMonth, selectedDate: LocalDate, onDateClick: (LocalDate) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    Column(modifier = Modifier.background(SaltTheme.colors.background)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }
            weekdays.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = SaltTheme.textStyles.paragraph,
                    color = SaltTheme.colors.subText
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp),
            userScrollEnabled = false
        ) {
            items(startOffset) { Box(Modifier.size(40.dp)) }

            items(daysInMonth) { dayOfMonth ->
                val date = month.atDay(dayOfMonth + 1)
                DayCell(
                    date = date,
                    isSelected = date == selectedDate,
                    onClick = onDateClick
                )
            }
        }
    }
}


@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    val backgroundColor = when {
        isSelected -> SaltTheme.colors.highlight
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> SaltTheme.colors.onHighlight
        isToday -> SaltTheme.colors.highlight
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = contentColor,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun YearPickerView(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val yearRange = (1950..2100).toList()

    val selectedIndex = yearRange.indexOf(selectedYear)

    val offset = 2

    val initialIndex = (selectedIndex - offset).coerceAtLeast(0)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
    ) {
        items(yearRange) { year ->
            val isSelected = year == selectedYear
            Text(
                text = year.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { onYearSelected(year) }
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = if (isSelected) 24.sp else 18.sp,
                color = if (isSelected) SaltTheme.colors.highlight else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SaltTheme.colors.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            modifier = Modifier.padding(horizontal = 8.dp),
            onClick = onDismiss,
            text = "取消",
            type = ButtonType.Sub
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            modifier = Modifier.padding(horizontal = 8.dp),
            onClick = onConfirm,
            text = "确定"
        )
    }
}

