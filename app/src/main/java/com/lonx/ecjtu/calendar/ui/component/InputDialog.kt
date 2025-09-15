package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemOuterEdit
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding

@UnstableSaltUiApi
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null,
    cancelText: String = "取消",
    confirmText: String = "确认"
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        DialogTitle(text = title)

//        val focusRequester = remember { FocusRequester() }
        ItemOuterEdit(
            text = text,
            onChange = onChange,
            hint = hint,
            modifier = Modifier
//                .focusRequester(focusRequester)
        )
//        LaunchedEffect(Unit) {
//            focusRequester.requestFocus()
//        }

        Row(
            modifier = Modifier.outerPadding()
        ) {
            Button(onClick = {
                onDismissRequest()
            },
                text = cancelText,
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))
            Button(onClick = {
                onConfirm()
            },
                text = confirmText,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}
