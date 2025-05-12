package com.gk.news_pro.page.utils.usecases
import com.gk.news_pro.page.utils.local.LocalUserManager


class SaveAppEntry(
    private val localUserManager: LocalUserManager
) {

    suspend operator fun invoke() {
        localUserManager.saveAppEntry()
    }
}