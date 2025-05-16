package com.gk.news_pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gk.news_pro.page.main_viewmodel.PrefsManager
import com.gk.news_pro.page.navigation.AppNavigation
import com.gk.news_pro.page.screen.splash_screen.SplashScreen
import com.gk.news_pro.ui.theme.NewsProTheme
import com.loc.newsapp.screen.onboarding_screen.OnBoardingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppEntryPoint()
                }
            }
        }
    }
}
@Composable
fun AppEntryPoint() {
    val context = LocalContext.current
    val prefsManager = PrefsManager.getInstance(context)
    val coroutineScope = rememberCoroutineScope()
    val screenState = remember {
        mutableStateOf(
            when {
                prefsManager.isFirstLaunch() -> ScreenState.Onboarding
                else -> ScreenState.Splash
            }
        )
    }

    // Handle splash screen timeout
    LaunchedEffect(screenState.value) {
        if (screenState.value == ScreenState.Splash) {
            // Simulate a minimum splash duration (2 seconds)
            delay(2000)
            screenState.value = ScreenState.Navigation
        }
    }

    when (screenState.value) {
        ScreenState.Splash -> {
            SplashScreen(
                onSplashFinish = {
                    screenState.value = ScreenState.Navigation
                }
            )
        }
        ScreenState.Onboarding -> {
            OnBoardingScreen(
                onFinish = {
                    coroutineScope.launch {
                        prefsManager.setFirstLaunchCompleted()
                        screenState.value = ScreenState.Navigation
                    }
                }
            )
        }
        ScreenState.Navigation -> {
            AppNavigation()
        }
    }
}

private enum class ScreenState {
    Splash, Onboarding, Navigation
}