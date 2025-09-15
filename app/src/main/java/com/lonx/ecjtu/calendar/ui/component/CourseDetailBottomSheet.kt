package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.domain.model.Course
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailBottomSheet(
    course: Course,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SaltTheme.colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // 课程名称
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            RoundedColumn {
                DetailItem(title = "教师", content = course.teacher, icon = painterResource(R.drawable.ic_teacher))
                DetailItem(title = "地点", content = course.location, icon = painterResource(R.drawable.ic_location))
                DetailItem(title = "节次", content = course.time, icon = painterResource(R.drawable.ic_class_time))
                DetailItem(title = "上课周", content = course.duration, icon = painterResource(R.drawable.ic_class_time))
            }
        }
    }
}
@Composable
private fun DetailItem(title: String, content: String,icon: Painter? = null) {
    Item(
        text = "$title: $content",
        onClick = {},
        iconPainter = icon,
        iconPaddingValues = PaddingValues(2.dp),
        arrowType = ItemArrowType.None
    )
}