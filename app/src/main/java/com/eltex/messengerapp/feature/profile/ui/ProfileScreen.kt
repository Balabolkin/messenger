package com.eltex.messengerapp.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.ui.ProfileAvatar
import com.eltex.messengerapp.ui.theme.BrandDark
import com.eltex.messengerapp.ui.theme.BrandMinor
import com.eltex.messengerapp.ui.theme.BrandPrimary
import com.eltex.messengerapp.ui.theme.MessengerAppTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToLogin -> onLogout()
//                is ProfileEffect.ShowError -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BrandMinor)
    ) {
        when {
            state.isLoading && state.user == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error != null && state.user == null -> {
                Text(
                    text = "Ошибка: ${state.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }

            else -> {
                state.user?.let { user ->
                    ProfileContent(
                        user = user,
                        onLogoutClick = { viewModel.messageHandler(ProfileMessage.ShowLogoutDialog) }
                    )
                }
            }
        }

        if (state.isLogoutDialogVisible) {
            LogoutDialog(
                onConfirm = { viewModel.messageHandler(ProfileMessage.Logout) },
                onDismiss = { viewModel.messageHandler(ProfileMessage.DismissLogoutDialog) }
            )
        }
    }

}


@Composable
fun ProfileContent(
    user: User,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandMinor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .background(
                        color = BrandDark
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ProfileAvatar(
                    initials = user.getInitials(),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(46.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally),
                text = user.getFullName(),
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(600),
                color = Color.Black
            )

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PageSize.Fixed(48.dp).pageSize),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Row {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Выйти",
                            tint = BrandPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(
                                    scaleX = -1f
                                )
                        )
                        Text(
                            text = "Выйти",
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight(400),
                            modifier = Modifier.padding(start = 16.dp)
                        )


                    }
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = "Arrow from IOS",
                        tint = Color(0xFFCBCBCC),
                    )
                }

            }
        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MessengerAppTheme {
        val mockUser = User(
            id = "1",
            firstName = "Иван",
            lastName = "Иванов",
            patronymic = "Иванович",
            username = "ivan.ivanovich",
            avatarUrl = null
        )

        ProfileContent(
            user = mockUser,
            onLogoutClick = {}
        )
    }
}