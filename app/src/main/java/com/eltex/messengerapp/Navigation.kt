package com.eltex.messengerapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eltex.messengerapp.feature.main.MainScreen
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

        }

        composable<NavDestinations.Chats> {

        }

        composable<NavDestinations.Profile> {

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
}

