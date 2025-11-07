package com.lonx.ecjtu.calendar.ui.screen.score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.domain.model.Score
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.ui.viewmodel.ScoreViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
@Destination<RootGraph>(label = "我的成绩")
fun ScoreScreen(
    topAppBarScrollBehavior: ScrollBehavior
) {
    val viewModel: ScoreViewModel = koinViewModel()
    val windowSize = getWindowSize()
    val uiState by viewModel.uiState.collectAsState()
    // 只有当没有数据时才加载，避免重复请求
    LaunchedEffect(uiState.scores) {
        if (uiState.scores.isEmpty() && uiState.error == null && !uiState.isLoading) {
            viewModel.loadScores()
        }
    }

    Column(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        if (!uiState.isLoading && uiState.error == null) {
            Card(
                modifier = Modifier.padding(12.dp)
            ) {
                SuperDropdown(
                    title = "选择学期",
                    items = uiState.availableTerms,
                    selectedIndex = uiState.availableTerms.indexOf(uiState.currentTerm),
                    onSelectedIndexChange = {
                        if (uiState.availableTerms[it] != uiState.currentTerm) {
                            viewModel.onTermSelected(uiState.availableTerms[it])
                        }
                    }
                )
            }
        }
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
                        viewModel.loadScores()
                    }
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .height(windowSize.height.dp)
                .weight(1f),
            overscrollEffect = null
        ) {
            item {
                if (!uiState.isLoading && uiState.error == null) {


                    uiState.scores.forEach {
                        ScoreCard(score = it)
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
private fun ScoreCard(score: Score, modifier: Modifier = Modifier) {
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
                    ScoreTag(text = "学分: ${score.credit}")
                    ScoreTag(text = score.courseType)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "课程代码: ${score.courseCode}",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                    if (score.retakeScore.isNotBlank()) {
                        Text(
                            text = "重考成绩: ${score.retakeScore}",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }
                    if (score.relearnScore.isNotBlank()) {
                        Text(
                            text = "重修成绩: ${score.relearnScore}",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
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
private fun ScoreTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MiuixTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = MiuixTheme.colorScheme.onPrimary,
            style = MiuixTheme.textStyles.body2,
            maxLines = 1,
        )
    }
}

// 添加预览函数
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
            )
        )
    }
}
