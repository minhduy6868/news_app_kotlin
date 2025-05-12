package com.gk.news_pro.page.utils.usecases

import com.gk.news_pro.page.utils.local.LocalUserManager

import kotlinx.coroutines.flow.Flow

class ReadAppEntry(
    private val localUserManager: LocalUserManager
) {

    suspend operator fun invoke(): Flow<Boolean> {
      return localUserManager.readAppEntry()
    }
}