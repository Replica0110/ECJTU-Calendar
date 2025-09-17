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
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.moriafly.salt.ui.SaltTheme


@Composable
fun DateHeaderCard(
    dateInfo: DateInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    val (date, weekDay, weekNum) = dateInfo

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
            if (weekNum.isNotBlank()) {
                Text(
                    text = "$weekDay · 第${weekNum}周",
                    fontSize = 14.sp,
                    color = SaltTheme.colors.onHighlight.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = weekDay,
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
        DateHeaderCard(dateInfo = DateInfo("2023-09-01", "星期一", "1"))
    }
}


