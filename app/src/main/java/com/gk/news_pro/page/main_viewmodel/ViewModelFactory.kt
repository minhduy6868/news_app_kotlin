package com.gk.news_pro.page.main_viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gk.news_pro.data.repository.HeyGenRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.RadioRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.screen.auth.LoginViewModel
import com.gk.news_pro.page.screen.auth.RegisterViewModel
import com.gk.news_pro.page.screen.explore_sceen.ExploreViewModel
import com.gk.news_pro.page.screen.favorite_screen.FavoriteViewModel
import com.gk.news_pro.page.screen.home_screen.HomeViewModel
import com.gk.news_pro.page.screen.radio_screen.RadioViewModel

class ViewModelFactory(
    private val repositories: Any,
    private val context: Context? = null // Thêm context với giá trị mặc định là null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                if (repositories is NewsRepository) {
                    HomeViewModel(repositories) as T
                } else {
                    throw IllegalArgumentException("Repository must be NewsRepository for HomeViewModel")
                }
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                if (repositories is List<*> && repositories.size >= 2 &&
                    repositories[0] is NewsRepository && repositories[1] is UserRepository
                ) {
                    if (context == null) {
                        throw IllegalArgumentException("Context must be provided for ExploreViewModel")
                    }
                    ExploreViewModel(
                        newsRepository = repositories[0] as NewsRepository,
                        userRepository = repositories[1] as UserRepository,
                        heyGenRepository = HeyGenRepository(context)
                    ) as T
                } else {
                    throw IllegalArgumentException("Repositories list must contain NewsRepository and UserRepository for ExploreViewModel")
                }
            }
            modelClass.isAssignableFrom(RadioViewModel::class.java) -> {
                if (repositories is List<*> && repositories.size >= 2 &&
                    repositories[0] is RadioRepository && repositories[1] is UserRepository
                ) {
                    RadioViewModel(repositories[0] as RadioRepository, repositories[1] as UserRepository) as T
                } else if (repositories is UserRepository) {
                    // Allow creating RadioViewModel with just UserRepository for FavoriteScreen
                    val radioRepo = RadioRepository() // Create a default RadioRepository
                    RadioViewModel(radioRepo, repositories) as T
                } else {
                    throw IllegalArgumentException("Invalid repositories for RadioViewModel")
                }
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                if (repositories is UserRepository) {
                    LoginViewModel(repositories) as T
                } else {
                    throw IllegalArgumentException("Repository must be UserRepository for LoginViewModel")
                }
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                if (repositories is UserRepository) {
                    RegisterViewModel(repositories) as T
                } else {
                    throw IllegalArgumentException("Repository must be UserRepository for RegisterViewModel")
                }
            }
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                if (repositories is UserRepository) {
                    FavoriteViewModel(repositories) as T
                } else {
                    throw IllegalArgumentException("Repository must be UserRepository for FavoriteViewModel")
                }
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}