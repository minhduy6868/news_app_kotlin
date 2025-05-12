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
        title = "Bắt đầu hành trình tin tức cùng NewsPro",
        description = "NewsPro mang đến cho bạn trải nghiệm đọc tin tức thông minh và cá nhân hóa",
        img = R.drawable.onboarding1
    ),
    Page(
        title = "Khám phá thế giới tin tức trong tầm tay",
        description = "Từ tin tức xã hội đến công nghệ, bạn sẽ không bỏ lỡ điều gì",
        img = R.drawable.onboarding2
    ),
    Page(
        title = "Tin tức chuẩn, nhanh, dành riêng cho bạn",
        description = "Đọc tin tiện lợi, tiết kiệm thời gian và đúng gu bạn",
        img = R.drawable.onboarding3
    )
)
