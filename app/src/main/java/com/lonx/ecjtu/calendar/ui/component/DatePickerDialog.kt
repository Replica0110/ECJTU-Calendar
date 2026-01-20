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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
            .background(MiuixTheme.colorScheme.primary)
            .padding(16.dp),
    ) {
        Text(
            text = selectedDate.format(yearFormatter),
            style = MiuixTheme.textStyles.title2,
            color = MiuixTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onYearClick)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = selectedDate.format(dateFormatter),
            style = MiuixTheme.textStyles.title3,
            color = MiuixTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDateClick)
        )
    }
}

@Composable
private fun CalendarView(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier.background(MiuixTheme.colorScheme.background).padding(start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(displayedMonth.minusMonths(1)) }) {
                Icon(MiuixIcons.Regular.ChevronBackward, modifier = Modifier.rotate(180f), contentDescription = "上个月")
            }
            Text(
                text = "${displayedMonth.year}年${displayedMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(displayedMonth.plusMonths(1)) }) {
                Icon(MiuixIcons.Regular.ChevronForward, contentDescription = "下一个月")
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
    }
}

@Composable
private fun CalendarGrid(month: YearMonth, selectedDate: LocalDate, onDateClick: (LocalDate) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    Column(modifier = Modifier.background(MiuixTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }
            weekdays.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.paragraph,
                    color = MiuixTheme.colorScheme.secondary
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
        isSelected -> MiuixTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> MiuixTheme.colorScheme.onPrimary
        isToday -> MiuixTheme.colorScheme.primary
        else -> MiuixTheme.colorScheme.onSurface
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
            .background(MiuixTheme.colorScheme.background)
    ) {
        items(yearRange) { year ->
            val isSelected = year == selectedYear
            Text(
                text = year.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onYearSelected(year) }
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                style = if (isSelected) MiuixTheme.textStyles.title2 else MiuixTheme.textStyles.title4,
                color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onDismiss,
            text = "取消"
        )
        Spacer(modifier = Modifier.width(8.dp))

        TextButton(
            onClick = onConfirm,
            text = "确定",
            colors = ButtonDefaults.textButtonColorsPrimary()
        )
    }
}

