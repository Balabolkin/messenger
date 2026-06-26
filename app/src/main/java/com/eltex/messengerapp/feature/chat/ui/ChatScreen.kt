package com.eltex.messengerapp.feature.chat.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.chats.data.AttachmentDto
import com.eltex.messengerapp.feature.chats.data.ChatsDateParser
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.ui.ChatsDateFormatter
import com.eltex.messengerapp.feature.chats.ui.InitialsAvatar
import com.eltex.messengerapp.ui.theme.AppColors
import com.eltex.messengerapp.ui.theme.BrandDark
import com.eltex.messengerapp.ui.theme.BrandMinor
import com.eltex.messengerapp.ui.theme.BrandPrimary
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    roomId: String,
    roomName: String,
    roomType: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var fullscreenVideoUrl by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (roomType == "d") Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        Text(
                            text = roomName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (roomType != "d") {
                            Text(
                                text = "Участники: 32", // Mock value as per mockup designs
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Placeholder spacing or user options button
                    if (roomType == "d") {
                        Spacer(modifier = Modifier.width(48.dp))
                    } else {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward), // placeholder / info icon
                                contentDescription = "Информация",
                                tint = Color.Transparent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDark
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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

                // Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(BrandMinor)
                ) {
                    if (state.isLoading && state.messages.isEmpty()) {
                        CircularProgressIndicator(
                            color = BrandPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (state.messages.isEmpty()) {
                        Text(
                            text = "Сообщений нет",
                            color = AppColors.TextSecondary,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Empty spacing at the bottom
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            itemsIndexed(
                                items = state.messages,
                                key = { _, msg -> msg._id }
                            ) { index, msg ->
                                val isMyMessage = msg.u?._id == currentUserId
                                
                                // Determine grouping for other's messages
                                val showSenderInfo = if (roomType != "d" && !isMyMessage) {
                                    index == state.messages.size - 1 ||
                                            state.messages[index + 1].u?._id != msg.u?._id ||
                                            isDifferentDay(msg.ts, state.messages[index + 1].ts)
                                } else false

                                MessageBubbleRow(
                                    message = msg,
                                    isMyMessage = isMyMessage,
                                    showSenderInfo = showSenderInfo,
                                    downloadProgress = state.downloadingFiles[msg.attachments?.firstOrNull()?.title_link ?: msg.attachments?.firstOrNull()?.video_url],
                                    onImageClick = { url -> fullscreenImageUrl = url },
                                    onVideoClick = { url -> fullscreenVideoUrl = url },
                                    onDocClick = { attachment ->
                                        val url = attachment.title_link ?: return@MessageBubbleRow
                                        val fileName = attachment.title ?: "file"
                                        viewModel.downloadAndOpenFile(context, url, fileName) { file ->
                                            openFile(context, file)
                                        }
                                    }
                                )

                                // Insert date header if day changes
                                if (index == state.messages.size - 1 || isDifferentDay(msg.ts, state.messages[index + 1].ts)) {
                                    DateHeaderItem(msg.ts)
                                }
                            }

                            // Empty spacing at the top
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }

                // Message Input Area (Visual mockup as per task description)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Прикрепить",
                            tint = BrandPrimary
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                text = "Текст сообщения",
                                color = AppColors.TextSecondary,
                                fontSize = 15.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
                    )

                    if (messageText.isNotBlank()) {
                        IconButton(onClick = { messageText = "" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = "Отправить",
                                tint = BrandPrimary
                            )
                        }
                    }
                }
            }

            // Fullscreen Image Viewer
            fullscreenImageUrl?.let { imageUrl ->
                FullscreenImageViewer(
                    imageUrl = imageUrl,
                    onDismiss = { fullscreenImageUrl = null }
                )
            }

            // Fullscreen Video Player
            fullscreenVideoUrl?.let { videoUrl ->
                FullscreenVideoPlayer(
                    videoUrl = videoUrl,
                    onDismiss = { fullscreenVideoUrl = null }
                )
            }
        }
    }
}

@Composable
fun MessageBubbleRow(
    message: MessageDto,
    isMyMessage: Boolean,
    showSenderInfo: Boolean,
    downloadProgress: Float?,
    onImageClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onDocClick: (AttachmentDto) -> Unit
) {
    val senderName = message.u?.name ?: message.u?.username ?: "Пользователь"
    val avatarUrl = "https://study-chat.eltex-co.ru/avatar/${message.u?.username}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMyMessage) {
            if (showSenderInfo) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = senderName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = {
                            InitialsAvatar(name = senderName)
                        },
                        loading = {
                            InitialsAvatar(name = senderName)
                        }
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = (LocalConfiguration.current.screenWidthDp * 0.7f).dp)
        ) {
            if (showSenderInfo) {
                Text(
                    text = senderName,
                    color = BrandPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }

            val bubbleShape = if (isMyMessage) {
                RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
            } else {
                RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(if (isMyMessage) Color(0xFFE1F5FE) else Color(0xFFF5F5F5))
            ) {
                Column {
                    // Check for attachments first
                    val attachment = message.attachments?.firstOrNull()
                    if (attachment != null) {
                        when {
                            attachment.image_url != null -> {
                                ImageAttachmentBubble(
                                    attachment = attachment,
                                    hasText = !message.msg.isNullOrBlank(),
                                    onClick = { onImageClick(attachment.image_url) }
                                )
                            }
                            attachment.video_url != null -> {
                                VideoAttachmentBubble(
                                    attachment = attachment,
                                    downloadProgress = downloadProgress,
                                    onClick = { onVideoClick(attachment.video_url) }
                                )
                            }
                            attachment.title_link != null -> {
                                DocAttachmentBubble(
                                    attachment = attachment,
                                    downloadProgress = downloadProgress,
                                    onClick = { onDocClick(attachment) }
                                )
                            }
                        }
                    }

                    // Message text
                    if (!message.msg.isNullOrBlank()) {
                        val annotatedText = parseClickableText(message.msg)
                        val context = LocalContext.current
                        
                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                                    try {
                                        val intent = when (annotation.tag) {
                                            "URL" -> Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                            "EMAIL" -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${annotation.item}"))
                                            "PHONE" -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${annotation.item}"))
                                            else -> null
                                        }
                                        intent?.let { context.startActivity(it) }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            style = TextStyle(color = Color.Black, fontSize = 15.sp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    // Time and delivery status
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tsTime = ChatsDateParser.parse(message.ts) ?: 0L
                        val timeText = if (tsTime > 0L) {
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(tsTime), ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                        } else ""
                        
                        Text(
                            text = timeText,
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary.copy(alpha = 0.7f)
                        )

                        if (isMyMessage) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val isRead = message.unread == false
                            Icon(
                                imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isRead) Color(0xFF25CBA3) else AppColors.TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageAttachmentBubble(
    attachment: AttachmentDto,
    hasText: Boolean,
    onClick: () -> Unit
) {
    val fullUrl = if (attachment.image_url?.startsWith("http") == true) {
        attachment.image_url
    } else {
        "https://study-chat.eltex-co.ru${attachment.image_url}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                if (hasText) {
                    RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                } else {
                    RoundedCornerShape(12.dp)
                }
            )
            .clickable { onClick() }
    ) {
        SubcomposeAsyncImage(
            model = fullUrl,
            contentDescription = attachment.title ?: "Изображение",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Ошибка", tint = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun VideoAttachmentBubble(
    attachment: AttachmentDto,
    downloadProgress: Float?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size((LocalConfiguration.current.screenWidthDp * 0.35f).dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Play button
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Проиграть",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Optional Loader
        if (downloadProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { downloadProgress },
                    color = BrandPrimary,
                )
            }
        }
    }
}

@Composable
fun DocAttachmentBubble(
    attachment: AttachmentDto,
    downloadProgress: Float?,
    onClick: () -> Unit
) {
    val filename = attachment.title ?: "Документ"
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (downloadProgress != null) {
                CircularProgressIndicator(
                    progress = { downloadProgress },
                    color = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Файл",
                    tint = BrandPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatFileName(filename),
            color = Color.Black,
            fontSize = 15.sp,
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Clip,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DateHeaderItem(ts: JsonElement?) {
    val timestamp = ChatsDateParser.parse(ts) ?: return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatDateHeader(timestamp),
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FullscreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "https://study-chat.eltex-co.ru$imageUrl"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = fullUrl,
            contentDescription = "Изображение во весь экран",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White
            )
        }
    }
}

@Composable
fun FullscreenVideoPlayer(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val fullUrl = if (videoUrl.startsWith("http")) videoUrl else "https://study-chat.eltex-co.ru$videoUrl"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoPath(fullUrl)
                    val mediaController = MediaController(context)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)
                    setOnPreparedListener { start() }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White
            )
        }
    }
}

fun formatFileName(fileName: String, maxLength: Int = 28): String {
    if (fileName.length <= maxLength) return fileName
    val extensionIndex = fileName.lastIndexOf('.')
    val ext = if (extensionIndex != -1) fileName.substring(extensionIndex) else ""
    val nameWithoutExt = if (extensionIndex != -1) fileName.substring(0, extensionIndex) else fileName

    val extLen = ext.length
    val availableLen = maxLength - extLen - 3
    if (availableLen <= 2) return fileName

    val startLen = availableLen / 2 + availableLen % 2
    val endLen = availableLen / 2

    val startPart = nameWithoutExt.take(startLen)
    val endPart = nameWithoutExt.takeLast(endLen)

    return "$startPart...$endPart$ext"
}

fun parseClickableText(text: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        val urlRegex = """https?://[^\s]+""".toRegex()
        urlRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = BrandPrimary,
                    textDecoration = TextDecoration.Underline
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
            addStringAnnotation(
                tag = "URL",
                annotation = match.value,
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        val emailRegex = """[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""".toRegex()
        emailRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = BrandPrimary,
                    textDecoration = TextDecoration.Underline
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
            addStringAnnotation(
                tag = "EMAIL",
                annotation = match.value,
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        val phoneRegex = """\+?[78]\s?\(?\d{3}\)?\s?\d{3}[-\s]?\d{2}[-\s]?\d{2}""".toRegex()
        phoneRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = BrandPrimary,
                    textDecoration = TextDecoration.Underline
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
            addStringAnnotation(
                tag = "PHONE",
                annotation = match.value,
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}

fun isDifferentDay(ts1: JsonElement?, ts2: JsonElement?): Boolean {
    val t1 = ChatsDateParser.parse(ts1) ?: return true
    val t2 = ChatsDateParser.parse(ts2) ?: return true
    val d1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(t1), ZoneId.systemDefault()).toLocalDate()
    val d2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(t2), ZoneId.systemDefault()).toLocalDate()
    return d1 != d2
}

fun formatDateHeader(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val zoneId = ZoneId.systemDefault()
    val date = LocalDateTime.ofInstant(instant, zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)
    val yesterday = today.minusDays(1)

    return when {
        date == today -> "Сегодня"
        date == yesterday -> "Вчера"
        date.year == today.year -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
            date.format(formatter)
        }
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
            date.format(formatter)
        }
    }
}

fun openFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Не удалось открыть файл: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
