package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.moriafly.salt.ui.SaltTheme


@Composable
fun DateHeaderCard(
    dateInfo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val dateRegex = remember { Regex("(\\d{4}-\\d{2}-\\d{2})\\s+([^（]+)（第(\\d+)周）") }

    val (date, weekDay, weekNum) = dateRegex.find(dateInfo)
        ?.let { Triple(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
        ?: Triple(dateInfo, "", "")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SaltTheme.colors.highlight
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = date,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SaltTheme.colors.onHighlight
            )
            // 星期和周次信息
            if (weekDay.isNotBlank()) {
                Text(
                    text = "$weekDay · 第${weekNum}周",
                    fontSize = 14.sp,
                    color = SaltTheme.colors.onHighlight.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Preview
@Composable
fun DateHeaderCardPreview() {
    CalendarTheme {
        DateHeaderCard(dateInfo = "2023-05-24 星期三（第14周）")
    }
}

@Preview
@Composable
fun DateHeaderCardPreview2() {
    CalendarTheme {
        DateHeaderCard(dateInfo = "2025-08-22 星期五（第26周）")
    }
}
