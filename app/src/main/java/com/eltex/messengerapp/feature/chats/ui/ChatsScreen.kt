package com.eltex.messengerapp.feature.chats.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.eltex.messengerapp.feature.chats.data.AttachmentDto
import com.eltex.messengerapp.feature.chats.data.ChatsDateParser
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import com.eltex.messengerapp.ui.theme.AppColors
import com.eltex.messengerapp.ui.theme.BrandPrimary
import com.eltex.messengerapp.ui.theme.BrandDark
import com.eltex.messengerapp.ui.theme.BrandMinor
import coil.compose.SubcomposeAsyncImage
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ChatsDateFormatter {
    fun format(timestamp: Long): String {
        return try {
            val instant = Instant.ofEpochMilli(timestamp)
            val zoneId = ZoneId.systemDefault()
            val dateTime = LocalDateTime.ofInstant(instant, zoneId)
            val now = LocalDateTime.now(zoneId)
            val todayStart = now.toLocalDate().atStartOfDay()

            if (dateTime.isAfter(todayStart)) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                dateTime.format(formatter)
            } else {
                val sevenDaysAgo = todayStart.minusDays(6)
                if (dateTime.isAfter(sevenDaysAgo)) {
                    val dayOfWeek = dateTime.dayOfWeek
                    when (dayOfWeek) {
                        DayOfWeek.MONDAY -> "пн"
                        DayOfWeek.TUESDAY -> "вт"
                        DayOfWeek.WEDNESDAY -> "ср"
                        DayOfWeek.THURSDAY -> "чт"
                        DayOfWeek.FRIDAY -> "пт"
                        DayOfWeek.SATURDAY -> "сб"
                        DayOfWeek.SUNDAY -> "вс"
                    }
                } else {
                    val formatter = DateTimeFormatter.ofPattern("dd.MM")
                    dateTime.format(formatter)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}

private val initialsGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFFEF5350), Color(0xFFE53935))),
    Brush.linearGradient(listOf(Color(0xFFEC407A), Color(0xFFD81B60))),
    Brush.linearGradient(listOf(Color(0xFFAB47BC), Color(0xFF8E24AA))),
    Brush.linearGradient(listOf(Color(0xFF7E57C2), Color(0xFF5E35B1))),
    Brush.linearGradient(listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))),
    Brush.linearGradient(listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))),
    Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00897B))),
    Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF43A047))),
    Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00))),
    Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFE64A19)))
)

private fun getGradientForName(name: String): Brush {
    val index = Math.abs(name.hashCode()) % initialsGradients.size
    return initialsGradients[index]
}

private fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val words = name.trim().split("\\s+".toRegex())
    return if (words.size == 1) {
        words[0].take(1).uppercase()
    } else {
        (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

private fun getLastMessageText(chat: SubscriptionDto): String {
    val lastMsg = chat.lastMessage
    if (lastMsg == null) return "Сообщений нет"
    if (!lastMsg.msg.isNullOrBlank()) return lastMsg.msg

    val attachment = lastMsg.attachments?.firstOrNull()
    if (attachment != null) {
        return when {
            attachment.image_url != null -> "Изображение"
            attachment.video_url != null -> "Видео"
            else -> "Документ"
        }
    }
    return "Сообщений нет"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel,
    onChatClick: (SubscriptionDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state
    val displayedChats = viewModel.getDisplayedChats()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onForeground()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onBackground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandDark)
                    .statusBarsPadding()
                    .padding(bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Чаты",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(
                        onClick = { /* Visual only */ },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать чат",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(36.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Поиск по чатам",
                                        color = AppColors.TextSecondary,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                            if (state.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить",
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.onSearchQueryChanged("") }
                                )
                            }
                        }
                    }
                )
            }
        },
        containerColor = Color.White,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Error Banner
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.error?.let { errorMsg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { viewModel.clearError() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ошибка",
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMsg,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && displayedChats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                } else if (displayedChats.isEmpty() && state.searchQuery.length >= 2) {
                    EmptySearchScreen(query = state.searchQuery)
                } else if (displayedChats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Чатов пока нет",
                            color = AppColors.TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(displayedChats, key = { it._id }) { chat ->
                            ChatRowItem(
                                chat = chat,
                                currentUserId = state.currentUserId,
                                onClick = { onChatClick(chat) }
                            )
                        }

                        // Infinite scroll pagination footer
                        if (displayedChats.size >= 20 && !state.isPaginating && state.searchQuery.length < 2) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadNextPage()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = BrandPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else if (state.isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = BrandPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRowItem(
    chat: SubscriptionDto,
    currentUserId: String?,
    onClick: () -> Unit
) {
    val displayName = chat.fname ?: chat.name ?: "Чат"
    
    // Avatar url logic
    val avatarUrl = if (chat.t == "d") {
        "https://study-chat.eltex-co.ru/avatar/${chat.name}"
    } else {
        "https://study-chat.eltex-co.ru/avatar/room/${chat.rid}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Avatar view with status badge
            Box(
                modifier = Modifier.size(52.dp)
            ) {
                SubcomposeAsyncImage(
                    model = avatarUrl,
                    contentDescription = displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        InitialsAvatar(name = displayName)
                    },
                    error = {
                        InitialsAvatar(name = displayName)
                    }
                )
                
                // Online/Verified status badge for direct chats (t == "d") except Favorites
                if (chat.t == "d" && displayName != "Избранное") {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF25CBA3), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name and Message
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Checkmarks status indicator (only for messages sent by us)
                    val lastMsg = chat.lastMessage
                    val isSentByMe = lastMsg?.u?._id == currentUserId
                    val rawMsgText = getLastMessageText(chat)
                    if (isSentByMe && lastMsg != null && rawMsgText != "Сообщений нет") {
                        val isRead = lastMsg.unread == false
                        Icon(
                            imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (isRead) "Просмотрено" else "Доставлено",
                            tint = if (isRead) Color(0xFF25CBA3) else AppColors.TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Time/Date of latest message
                    val lastMsgTime = ChatsDateParser.parse(chat.lastMessage?.ts)
                        ?: ChatsDateParser.parse(chat.ls)
                        ?: ChatsDateParser.parse(chat.ts)
                    val timeText = if (lastMsgTime != null) ChatsDateFormatter.format(lastMsgTime) else ""
                    
                    Text(
                        text = timeText,
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Prefix and message text
                    val rawMsgText = getLastMessageText(chat)
                    
                    // Group chat prefix formatting
                    val prefix = if (chat.t != "d" && chat.lastMessage != null && rawMsgText != "Сообщений нет") {
                        val lastMsg = chat.lastMessage
                        if (lastMsg.u?._id == currentUserId) {
                            "Вы: "
                        } else {
                            "${lastMsg.u?.name ?: lastMsg.u?.username ?: "Пользователь"}: "
                        }
                    } else ""

                    val messageContent = "$prefix$rawMsgText"

                    Text(
                        text = messageContent,
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread Count Badge (if unread > 0 and not sent by us)
                    val lastMsg = chat.lastMessage
                    val isSentByMe = lastMsg?.u?._id == currentUserId
                    if (chat.unread > 0 && !isSentByMe) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary)
                        ) {
                            Text(
                                text = chat.unread.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Horizontal divider starting after the avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 82.dp)
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(getGradientForName(name))
    ) {
        Text(
            text = getInitials(name),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptySearchScreen(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppColors.TextSecondary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Чат с названием ",
                    color = AppColors.TextSecondary,
                    fontSize = 16.sp
                )
                Text(
                    text = "\"$query\"",
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = " не найден",
                color = AppColors.TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}
