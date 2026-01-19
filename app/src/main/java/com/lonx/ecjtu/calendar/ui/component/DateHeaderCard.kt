package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme


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
            .padding(12.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.primary
        ),
        insideMargin = PaddingValues(16.dp),
        onClick = { onClick() },
        onLongPress = { onLongClick() },
        content = {
            Text(
                color = MiuixTheme.colorScheme.onPrimary,
                text = date,
                style = MiuixTheme.textStyles.title4,
                fontWeight = FontWeight.SemiBold
            )
            if (weekNum.isNotBlank()){
                Text(
                    color = MiuixTheme.colorScheme.onPrimary,
                    text = "$weekDay · 第${weekNum}周",
                    style = MiuixTheme.textStyles.headline1,
                    fontWeight = FontWeight.Normal
                )
            } else {
                Text(
                    color = MiuixTheme.colorScheme.onPrimary,
                    text = weekDay,
                    style = MiuixTheme.textStyles.headline1,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    )
}


@Preview
@Composable
fun DateHeaderCardPreview() {
    CalendarTheme {
        DateHeaderCard(
            dateInfo = DateInfo("2023-09-01", "星期一", "1"),
            onClick = {},
            onLongClick = {}
        )
    }
}