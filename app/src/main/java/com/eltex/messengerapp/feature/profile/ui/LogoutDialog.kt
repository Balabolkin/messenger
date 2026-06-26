package com.eltex.messengerapp.feature.profile.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eltex.messengerapp.R
import com.eltex.messengerapp.ui.theme.MessengerAppTheme

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logout_dialog_title)) },
        text = { Text(stringResource(R.string.logout_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.logout_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.logout_dialog_dismiss))
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LogoutDialogPreview() {
    MessengerAppTheme {
        val showDialog = remember { true }

        if (showDialog) {
            LogoutDialog({}, {})
        }
    }
}