package com.gk.news_pro


import OnBoardingScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gk.news_pro.page.navigation.AppNavigation
import com.gk.news_pro.ui.theme.NewsProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding = remember { mutableStateOf(true) }

                    if (showOnboarding.value) {
                        OnBoardingScreen(onFinish = {
                            showOnboarding.value = false
                        })
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }}