package com.eltex.messengerapp.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eltex.messengerapp.R
import com.eltex.messengerapp.ui.theme.BrandPrimary
import com.eltex.messengerapp.ui.theme.MessengerAppTheme

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Text(
                    text = stringResource(R.string.logout_dialog_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight(600),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 4.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.logout_dialog_message),
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF868686),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFE5E5EA)
                )

                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = stringResource(R.string.logout_dialog_dismiss),
                        fontSize = 17.sp,
                        fontWeight = FontWeight(400),
                        color = BrandPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                            .clickable { onDismiss() },
                        textAlign = TextAlign.Center
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight(),
                        thickness = 0.5.dp,
                        color = Color(0xFFE5E5EA)
                    )

                    Text(
                        text = stringResource(R.string.logout_dialog_confirm),
                        fontSize = 17.sp,
                        fontWeight = FontWeight(400),
                        color = BrandPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                            .clickable { onConfirm() },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
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