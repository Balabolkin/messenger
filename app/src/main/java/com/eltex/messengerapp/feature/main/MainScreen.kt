package com.eltex.messengerapp.feature.main

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.eltex.messengerapp.NavDestinations
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.profile.ui.ProfileScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.eltex.messengerapp.feature.chats.ui.ChatsScreen
import com.eltex.messengerapp.feature.chats.ui.ChatsViewModel
import com.eltex.messengerapp.ui.theme.BrandPrimary
import com.eltex.messengerapp.ui.theme.MessengerAppTheme

enum class Tab(
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    Chats(R.string.chats, Icons.Default.ChatBubbleOutline, Icons.Default.ChatBubble),
    Profile(R.string.profile, Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
}

@Composable
fun MainScreen(
    navController: NavController = rememberNavController()
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Chats) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomNavItem(
                        tab = Tab.Chats,
                        isSelected = selectedTab == Tab.Chats,
                        onClick = { selectedTab = Tab.Chats },
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    CustomNavItem(
                        tab = Tab.Profile,
                        isSelected = selectedTab == Tab.Profile,
                        onClick = { selectedTab = Tab.Profile },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    ) { insets ->
        Crossfade(
            modifier = Modifier.padding(bottom = insets.calculateBottomPadding()),
            targetState = selectedTab
        ) { tab ->
            when (tab) {
                Tab.Chats -> {
                    val chatsViewModel: ChatsViewModel = hiltViewModel()
                    ChatsScreen(viewModel = chatsViewModel)
                }
                Tab.Profile -> ProfileScreen(
                    onLogout = {
                        navController.navigate(NavDestinations.Auth) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun CustomNavItem(
    tab: Tab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .size(75.dp, 50.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Icon(
                    imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                    contentDescription = stringResource(tab.titleRes),
                    tint = if (isSelected) BrandPrimary else Color(0xFF868686),
                    modifier = Modifier.size(20.dp)
                )
                if (tab == Tab.Profile) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(10.dp)
                            .background(
                                color = Color(0xFF25CBA3),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            Text(

                text = stringResource(tab.titleRes),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) BrandPrimary else Color(0xFF868686)
            )
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MessengerAppTheme {
        MainScreen()
    }
}