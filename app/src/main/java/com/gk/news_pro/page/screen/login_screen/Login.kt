package com.gk.news_pro.page.screen.login_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gk.news_pro.page.screen.login_screen.LoginScreen

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen()


        }
    }
}