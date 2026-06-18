package com.eltex.messengerapp.feature.auth.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.auth.domain.Empty
import com.eltex.messengerapp.feature.auth.domain.LoginError
import com.eltex.messengerapp.feature.auth.domain.PasswordError
import com.eltex.messengerapp.ui.theme.MessengerAppTheme

@Composable
fun AuthScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthEffect.ShowSuccess -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is AuthEffect.ShowError -> {
                    errorMessage = effect.message
                    showErrorDialog = true
                }
            }
        }
    }

    AuthScreen(
        state = viewModel.state,
        modifier = modifier,
        onEvent = viewModel::accept,
        showErrorDialog = showErrorDialog,
        errorMessage = errorMessage,
        onDismissError = { showErrorDialog = false }
    )
}

@Composable
fun AuthScreen(
    state: AuthState,
    modifier: Modifier = Modifier,
    onEvent: (AuthMessage) -> Unit = {},
    showErrorDialog: Boolean = false,
    errorMessage: String = "",
    onDismissError: () -> Unit = {},
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val isButtonEnabled = state.login.isNotBlank() && state.password.isNotBlank()

    val shape = RoundedCornerShape(10.dp)
    val placeholderColor = Color(0xFF878787)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0x88000000)
                        )
                    )
                )
        )

        // Контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                value = state.login,
                onValueChange = {
                    onEvent(AuthMessage.LoginChanged(it))
                },
                isError = state.loginError != null,
                singleLine = true,
                label = {
                    Text(
                        stringResource(R.string.login_hint),
                        color = placeholderColor
                    )
                },
                supportingText = {
                    Text(state.loginError.toReadableString().orEmpty())
                },
                shape = shape,
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = placeholderColor,
                    unfocusedLabelColor = placeholderColor,
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    disabledContainerColor = Color.White.copy(alpha = 0.8f),
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                value = state.password,
                onValueChange = {
                    onEvent(AuthMessage.PasswordChanged(it))
                },
                isError = state.passwordError != null,
                singleLine = true,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                label = {
                    Text(stringResource(R.string.password_hint))
                },
                supportingText = {
                    Text(state.passwordError.toReadableString().orEmpty())
                },
                shape = shape,
                trailingIcon = {
                    val icon = if (isPasswordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    }

                    val description = if (isPasswordVisible) {
                        stringResource(R.string.hide_password_description)
                    } else {
                        stringResource(R.string.show_password_description)
                    }

                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            tint = Color.White
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = placeholderColor,
                    unfocusedLabelColor = placeholderColor,
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    disabledContainerColor = Color.White.copy(alpha = 0.8f),
                )
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter),
            onClick = {
                onEvent(AuthMessage.Submit)
            },
            enabled = isButtonEnabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2196F3),
                disabledContainerColor = Color.White.copy(alpha = 0.5f),
                disabledContentColor = Color(0xFF2196F3).copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = stringResource(R.string.login),
                color = if (isButtonEnabled) Color(0xFF1F1F1F) else Color(0xFF1F1F1F).copy(alpha = 0.5f)
            )
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text(text = "Ошибка") },
            text = { Text(text = errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("Ок")
                }
            }
        )
    }
}

@Composable
fun LoginError?.toReadableString(): String? = when (this) {
    Empty -> stringResource(R.string.login_empty_error)
    null -> null
}

@Composable
fun PasswordError?.toReadableString(): String? = when (this) {
    Empty -> stringResource(R.string.password_empty_error)
    PasswordError.TooShort -> stringResource(R.string.password_too_short_error)
    null -> null
}

@Preview(showBackground = true)
@Composable
fun AuthScreenEmptyPreview() {
    MessengerAppTheme {
        AuthScreen(AuthState())
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenEmptyErrorPreview() {
    MessengerAppTheme {
        AuthScreen(
            AuthState(loginError = Empty, passwordError = Empty)
        )
    }
}