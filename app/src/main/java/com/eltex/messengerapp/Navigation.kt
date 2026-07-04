package com.eltex.messengerapp

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.eltex.messengerapp.feature.auth.ui.AuthScreenRoute
import com.eltex.messengerapp.feature.main.MainScreen
import com.eltex.messengerapp.feature.profile.ui.ProfileScreen
import com.eltex.messengerapp.feature.chat.ui.ChatScreen
import com.eltex.messengerapp.feature.chats.creation.dm.ui.DmCreationBottomSheet
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    startDestination: Any
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<NavDestinations.Main>(
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
            }
        ) {
            MainScreen(navController)
        }

        composable<NavDestinations.Auth> {

            AuthScreenRoute(
                onLoginSuccess = {
                    navController.navigate(NavDestinations.Main) {
                        popUpTo(NavDestinations.Auth) { inclusive = true }
                    }
                }
            )

        }

        composable<NavDestinations.Chats> {

        }

        composable<NavDestinations.Profile> {
            ProfileScreen(
                onLogout = {
                    navController.navigate(NavDestinations.Auth) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<NavDestinations.Chat>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        ) { backStackEntry ->
            val chatDest = backStackEntry.toRoute<NavDestinations.Chat>()
            ChatScreen(
                roomId = chatDest.roomId,
                roomName = chatDest.roomName,
                roomType = chatDest.roomType,
                avatarName = chatDest.avatarName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<NavDestinations.CreateChat> {
            DmCreationBottomSheet(
                onDismiss = { navController.navigateUp() },
                onOpenChat = { chatId, chatName, avatarName ->
                    navController.navigate(
                        NavDestinations.Chat(
                            roomId = chatId,
                            roomName = chatName,
                            roomType = "d",
                            avatarName = avatarName
                        )
                    ) {
                        popUpTo(NavDestinations.CreateChat) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Serializable
sealed interface NavDestinations {
    @Serializable
    object Main

    @Serializable
    object Auth

    @Serializable
    object Chats

    @Serializable
    object Profile

    @Serializable
    data class Chat(
        val roomId: String,
        val roomName: String,
        val roomType: String,
        val avatarName: String? = null
    ) : NavDestinations

    @Serializable
    object CreateChat
}

