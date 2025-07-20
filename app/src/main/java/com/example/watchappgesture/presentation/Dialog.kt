package com.example.watchappgesture.presentation

import android.R
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Text

object DialogState {
    var dialogVisible by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogDescription by mutableStateOf("")
    var dialogOnDismiss by mutableStateOf({ dialogVisible = false })
}



@Composable
fun MessageDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String = "Title",
    description: String = "No description provided.",
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = { },
        dismissButton = {
            AlertDialogDefaults.DismissButton(
                colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(
                    contentColor = Color.Black,
                    containerColor = Color(0xFF00FA9A)
                ),
                onClick = onDismiss,
                content = {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Dismiss",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    )
}
