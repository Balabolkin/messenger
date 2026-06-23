package com.eltex.messengerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eltex.messengerapp.feature.splash.SplashViewModel
import com.eltex.messengerapp.ui.theme.MessengerAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val splashViewModel: SplashViewModel = hiltViewModel()

            val isReady by splashViewModel.isReady.collectAsStateWithLifecycle()
            val startDestination by splashViewModel.startDestination.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition {
                !isReady
            }

            LaunchedEffect(Unit) {
                splashViewModel.checkAuth()
            }

            MessengerAppTheme {
                if (isReady) {
                    startDestination?.let { destination ->
                        Navigation(startDestination = destination)
                    }
                }
            }
//            Navigation(startDestination = NavDestinations.Main)
        }
    }
}
