package com.eltex.messengerapp.feature.chats.creation.dm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.chats.ui.InitialsAvatar
import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.ui.theme.BrandPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DmCreationBottomSheet(
    onDismiss: () -> Unit,
    onOpenChat: (String, String, String) -> Unit,
    viewModel: DmCreationViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DmCreationEffect.OpenChat -> {
                    onOpenChat(effect.chatId, effect.chatName, effect.avatarName)
                    scope.launch { sheetState.hide() }
                }

                is DmCreationEffect.ShowError -> {
                    errorMessage = effect.message
                    showErrorDialog = true
                }

                DmCreationEffect.CloseScreen -> {
                    scope.launch { sheetState.hide() }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        scrimColor = Color.Transparent,
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.create_chat_cancel),
                    color = BrandPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    lineHeight = 24.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.create_chat_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight(600),
                        color = Color.Black
                    )
                }

                Box(modifier = Modifier.size(60.dp))
            }

            SearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.messageHandler(DmCreationMessage.SearchQueryChanged(it)) }
            )

            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = stringResource(R.string.create_chat_contacts_header),
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF868686)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .height(300.dp)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.error != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = state.error
                                    ?: stringResource(R.string.create_chat_error_loading),
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            TextButton(
                                onClick = { viewModel.messageHandler(DmCreationMessage.DismissError) }
                            ) {
                                Text(stringResource(R.string.create_chat_retry))
                            }
                        }
                    }

                    state.users.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = if (state.searchQuery.length >= 2) {
                                    stringResource(
                                        R.string.create_chat_error_not_found,
                                        state.searchQuery
                                    )
                                } else {
                                    stringResource(R.string.create_chat_error_empty)
                                },
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn {
                            items(state.users, key = { it.id }) { user ->
                                UserRowItem(
                                    user = user,
                                    onClick = {
                                        viewModel.messageHandler(DmCreationMessage.SelectUser(user))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            containerColor = Color.White,
            textContentColor = Color(0xFF868686),
            titleContentColor = Color.Black,
            onDismissRequest = { showErrorDialog = false },
            title = { Text(stringResource(R.string.create_chat_error_title)) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.create_chat_ok))
                }
            }
        )
    }
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White)
            .border(
                1.dp,
                color = Color(0XFFCBCBCC),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.create_chat_search_hint),
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.create_chat_search_hint),
                            color = Color(0xFF868686),
                            fontSize = 14.sp
                        )
                    }
                    inner()
                }
            }
        )

        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.create_chat_clear),
                tint = Color.Gray,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
fun UserRowItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        ) {
            InitialsAvatar(
                name = user.getFullName(),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = user.getFullName(),
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}