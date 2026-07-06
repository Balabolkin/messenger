package com.eltex.messengerapp.feature.group.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.group.domain.GroupMember
import com.eltex.messengerapp.feature.group.domain.MemberRole
import com.eltex.messengerapp.ui.common.InitialsAvatar
import com.eltex.messengerapp.ui.theme.AppColors
import com.eltex.messengerapp.ui.theme.BrandDark
import com.eltex.messengerapp.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreenRoute(
    onBack: () -> Unit,
    viewModel: GroupMembersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GroupMembersEffect.NavigateBack -> {
                    onBack()
                }
                is GroupMembersEffect.NavigateToProfile -> {
                }
                is GroupMembersEffect.ShowError -> {
                }
            }
        }
    }

    GroupMembersScreen(
        state = state,
        onBack = { viewModel.onBackClicked() },
        onMemberClick = { memberId ->
            viewModel.onMemberClicked(memberId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    state: GroupMembersState,
    onBack: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    val groupName = state.groupInfo?.displayName ?: stringResource(R.string.default_group_name)
    val membersCount = state.groupInfo?.membersCount ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.navigate_back),
                            tint = Color.White,
                            modifier = Modifier.size(Dimens.IconLarge)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDark
                )
            )
        },
        containerColor = AppColors.BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundLight)
                .padding(paddingValues)
        ) {
            // Group heading
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandDark)
                    .padding(bottom = Dimens.Padding16),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.LoadingHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    return@Column
                }

                // Group avatar
                InitialsAvatar(
                    name = groupName,
                    modifier = Modifier
                        .size(Dimens.GroupAvatarSize)
                        .clip(CircleShape)
                        .border(Dimens.GroupAvatarBorder, Color.White, CircleShape),
                    fontSize = Dimens.GroupAvatarTextSize,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Dimens.Padding12))

                // Name
                Text(
                    text = groupName,
                    fontSize = Dimens.TextLarge,
                    lineHeight = Dimens.LineHeight22,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Padding2))

                // Members count
                Text(
                    text = stringResource(R.string.group_members_count, membersCount),
                    fontWeight = FontWeight.Normal,
                    fontSize = Dimens.TextSmall,
                    lineHeight = Dimens.LineHeight20,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Members
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.Padding16)
                    .padding(top = Dimens.Padding16),
                verticalArrangement = Arrangement.spacedBy(Dimens.Padding8)
            ) {
                items(state.members, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        onClick = { onMemberClick(member.id) },
                        isLast = state.members.lastOrNull()?.id == member.id
                    )
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: GroupMember,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(Dimens.CornerRadius12))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Padding16, vertical = Dimens.Padding12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(
                name = member.name ?: member.username,
                modifier = Modifier.size(Dimens.MemberAvatarSize),
                fontSize = Dimens.MemberAvatarTextSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(Dimens.Padding12))

            Text(
                text = member.name ?: member.username,
                fontWeight = FontWeight.Normal,
                fontSize = Dimens.TextLarge,
                lineHeight = Dimens.LineHeight24,
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )

            when {
                member.role == MemberRole.ADMIN -> {
                    Text(
                        text = stringResource(R.string.role_admin),
                        fontWeight = FontWeight.Normal,
                        fontSize = Dimens.TextSmall,
                        lineHeight = Dimens.LineHeight20,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.Padding64)
                    .height(Dimens.DividerHeight)
                    .background(AppColors.Divider)
            )
        }
    }
}