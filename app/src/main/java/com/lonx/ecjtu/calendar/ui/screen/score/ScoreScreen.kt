package com.lonx.ecjtu.calendar.ui.screen.score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.domain.model.Score
import com.lonx.ecjtu.calendar.ui.viewmodel.ScoreViewModel
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.ItemOuterTextButton
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, UnstableSaltUiApi::class)
@Composable
fun ScoreScreen(
    viewModel: ScoreViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // 只有当没有数据时才加载，避免重复请求
    LaunchedEffect(uiState.scores) {
        if (uiState.scores.isEmpty() && uiState.error == null && !uiState.isLoading) {
            viewModel.loadScores()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {
        TitleBar(text = "我的成绩", showBackBtn = false, onBack = {})
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaltTheme.colors.background)
            ) {
                ScoreContent(
                    uiState = uiState,
                    onTermSelected = { newTerm ->
                        if (uiState.currentTerm != newTerm){
                            viewModel.onTermSelected(newTerm)
                        }
                    }
                )

                if (uiState.isLoading) {
                    RoundedColumn {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            color = SaltTheme.colors.highlight,
                            trackColor = SaltTheme.colors.subBackground
                        )
                    }
                }

                uiState.error?.let { errorMessage ->
                    RoundedColumn {
                        ItemInfo(
                            text = errorMessage,
                            infoType = ItemInfoType.Error
                        )
                        ItemOuterTextButton(text = "重试", onClick = { viewModel.loadScores() })
                    }
                }
            }
        }
    }

}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun ScoreContent(
    uiState: ScoreScreenState,
    onTermSelected: (String) -> Unit
) {
    if (!uiState.isLoading && uiState.error == null) {
        RoundedColumn {
            if (uiState.availableTerms.isNotEmpty()) {

                ItemDropdown(
                    text = "选择学期",
                    value = uiState.currentTerm
                ) {
                    for (term in uiState.availableTerms) {
                        PopupMenuItem(text = term, selected = term == uiState.currentTerm, onClick = { onTermSelected(term) })
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.scores) { score ->
                ScoreItem(score = score, modifier = Modifier.padding(horizontal = 12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun ScoreItem(score: Score, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SaltTheme.colors.subBackground)
            .padding(horizontal = 16.dp, vertical = 20.dp)
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                        fontSize = 13.sp,
                        color = SaltTheme.colors.subText
                    )
                    if (score.retakeScore.isNotBlank()) {
                        Text(
                            text = "重考成绩: ${score.retakeScore}",
                            fontSize = 13.sp,
                            color = SaltTheme.colors.subText
                        )
                    }
                    if (score.relearnScore.isNotBlank()) {
                        Text(
                            text = "重修成绩: ${score.relearnScore}",
                            fontSize = 13.sp,
                            color = SaltTheme.colors.subText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = score.finalScore,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = SaltTheme.colors.highlight
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
            .background(SaltTheme.colors.highlight.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = SaltTheme.colors.highlight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}