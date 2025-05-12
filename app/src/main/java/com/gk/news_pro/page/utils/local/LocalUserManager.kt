package com.gk.news_pro.page.utils.local

import kotlinx.coroutines.flow.Flow

interface LocalUserManager {

    suspend fun saveAppEntry()

    fun readAppEntry() : Flow<Boolean>
}