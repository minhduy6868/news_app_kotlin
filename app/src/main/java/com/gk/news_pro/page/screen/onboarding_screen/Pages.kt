package com.gk.news_pro.onboarding_screen.components

import androidx.annotation.DrawableRes
import com.gk.news_pro.R

data class Page(
    val title: String,
    val description: String,
    @DrawableRes val img:Int
)

val pages = listOf(
    Page(
        title = "Chào mừng bạn đến với NewsPro",
        description = "Cập nhật tin tức nhanh chóng, tức thời, chuẩn hóa theo sở thích của bạn!",
        img = R.drawable.onboarding1

    ),
    Page(
        title = "Chào mừng bạn đến với NewsPro",
        description = "Cập nhật tin tức nhanh chóng, tức thời, chuẩn hóa theo sở thích của bạn!",
        img = R.drawable.onboarding2

    ),
    Page(
        title = "Chào mừng bạn đến với NewsPro",
        description = "Cập nhật tin tức nhanh chóng, tức thời, chuẩn hóa theo sở thích của bạn!",
        img = R.drawable.onboarding3

    )
)
