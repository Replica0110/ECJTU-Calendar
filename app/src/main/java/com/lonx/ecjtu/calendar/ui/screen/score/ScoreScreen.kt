package com.lonx.ecjtu.calendar.ui.screen.score

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.domain.model.Score
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.component.SurfaceTag
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.ui.viewmodel.ScoreViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
@Destination<RootGraph>(label = "我的成绩")
fun ScoreScreen(
    topAppBarScrollBehavior: ScrollBehavior
) {
    val viewModel: ScoreViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // 只有当没有数据时才加载，避免重复请求
    LaunchedEffect(uiState.scores) {
        if (uiState.scores.isEmpty() && uiState.error == null && !uiState.isLoading) {
            viewModel.loadScores()
        }
    }

    // 监听 Effect 并显示 Toast
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScoreEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
//        if (!uiState.isLoading && uiState.error == null) {
//            Card(
//                modifier = Modifier.padding(12.dp)
//            ) {
//                SuperDropdown(
//                    title = "选择学期",
//                    items = uiState.availableTerms,
//                    selectedIndex = uiState.availableTerms.indexOf(uiState.currentTerm),
//                    onSelectedIndexChange = {
//                        if (uiState.availableTerms[it] != uiState.currentTerm) {
//                            viewModel.onTermSelected(uiState.availableTerms[it])
//                        }
//                    }
//                )
//            }
//        }
        uiState.error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MessageCard(
                    message = errorMessage,
                    type = MessageType.Warning,
                    onClick = {
                        // 点击错误消息时执行手动刷新（从网络抓取并保存到数据库）
                        viewModel.loadScores(refresh = true)
                    }
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .fillMaxHeight()
                .weight(1f),
            overscrollEffect = null
        ) {
            item {
                if (!uiState.isLoading && uiState.error == null) {
                    // 显示上次刷新时间
                    val lastRefreshText = if (uiState.lastRefreshMillis > 0L) {
                        android.text.format.DateUtils.getRelativeTimeSpanString(
                            uiState.lastRefreshMillis,
                            System.currentTimeMillis(),
                            android.text.format.DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                    } else {
                        "未从教务获取"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "更新于：$lastRefreshText",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }

                    uiState.scores.forEach {
                        ScoreCard(score = it, term = uiState.currentTerm)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

}


@Composable
private fun ScoreCard(score: Score, term: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = score.courseName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SurfaceTag(text = "学分: ${score.credit}")
                    SurfaceTag(text = score.courseType)
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        InfoItem(title = "课程代码", value = score.courseCode)
                        if (score.retakeScore.isNotEmpty()) {
                            InfoItem(title = "重考成绩", value = score.retakeScore)
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        InfoItem(title = "学期", value = term)
                        if (score.relearnScore.isNotEmpty()) {
                            InfoItem(title = "重修成绩", value = score.relearnScore)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = score.finalScore,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InfoItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onBackgroundVariant
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoreCardPreview() {
    CalendarTheme {
        ScoreCard(
            score = Score(
                courseName = "高等数学",
                courseCode = "MATH101",
                credit = 5.0,
                finalScore = "95",
                retakeScore = "88",
                relearnScore = "99",
                courseType = "必修"
            ),
            term = "2023.1"
        )
    }
}
