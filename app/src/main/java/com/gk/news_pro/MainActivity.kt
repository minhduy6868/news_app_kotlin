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
import com.gk.news_pro.page.main_viewmodel.PrefsManager
import com.gk.news_pro.page.navigation.AppNavigation
import com.gk.news_pro.page.screen.splash_screen.SplashScreen
import com.gk.news_pro.ui.theme.NewsProTheme
import com.loc.newsapp.screen.onboarding_screen.OnBoardingScreen
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsManager = PrefsManager.getInstance(context)
    val coroutineScope = rememberCoroutineScope()
    val showSplash = remember { mutableStateOf(true) }
    val showOnboarding = remember { mutableStateOf(false) }

    // Check if it's the first launch
    LaunchedEffect(Unit) {
        val isFirstLaunch = prefsManager.isFirstLaunch()
        if (isFirstLaunch) {
            showOnboarding.value = true
            // Mark first launch as complete
            coroutineScope.launch {
                prefsManager.setFirstLaunchCompleted()
            }
        }
    }

    when {
        showSplash.value -> {
            SplashScreen(
                onSplashFinish = {
                    showSplash.value = false
                }
            )
        }
        showOnboarding.value -> {
            OnBoardingScreen(
                onFinish = {
                    showOnboarding.value = false
                }
            )
        }
        else -> {
            AppNavigation()
        }
    }
}