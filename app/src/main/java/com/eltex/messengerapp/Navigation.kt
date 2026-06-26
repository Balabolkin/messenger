package com.eltex.messengerapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.eltex.messengerapp.feature.auth.ui.AuthScreenRoute
import com.eltex.messengerapp.feature.main.MainScreen
import com.eltex.messengerapp.feature.profile.ui.ProfileScreen
import com.eltex.messengerapp.feature.chat.ui.ChatScreen
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
        composable<NavDestinations.Main> {
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

        composable<NavDestinations.Chat> { backStackEntry ->
            val chatDest = backStackEntry.toRoute<NavDestinations.Chat>()
            ChatScreen(
                roomId = chatDest.roomId,
                roomName = chatDest.roomName,
                roomType = chatDest.roomType,
                onBack = { navController.popBackStack() }
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
        val roomType: String
    ) : NavDestinations
}

