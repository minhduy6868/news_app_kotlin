package com.gk.news_pro.page.main_viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gk.news_pro.data.local.AppDatabase
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.HeyGenRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.PostRepository
import com.gk.news_pro.data.repository.RadioRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.screen.account_screen.AccountViewModel
import com.gk.news_pro.page.screen.auth.LoginViewModel
import com.gk.news_pro.page.screen.auth.RegisterViewModel
import com.gk.news_pro.page.screen.create_post.CreatePostViewModel
import com.gk.news_pro.page.screen.explore_sceen.ExploreViewModel
import com.gk.news_pro.page.screen.favorite_screen.FavoriteViewModel
import com.gk.news_pro.page.screen.home_screen.HomeViewModel
import com.gk.news_pro.page.screen.news_feed.NewsFeedViewModel
import com.gk.news_pro.page.screen.offline_list_news_screen.OfflineListNewsViewModel
import com.gk.news_pro.page.screen.radio_screen.RadioViewModel

class ViewModelFactory(
    private val repositories: Any,
    private val context: Context? = null
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
                if (repositories is List<*> && repositories.size >= 3 &&
                    repositories[0] is NewsRepository &&
                    repositories[1] is UserRepository &&
                    repositories[2] is HeyGenRepository
                ) {
                    if (context == null) {
                        throw IllegalArgumentException("Context must be provided for ExploreViewModel")
                    }
                    ExploreViewModel(
                        newsRepository = repositories[0] as NewsRepository,
                        userRepository = repositories[1] as UserRepository,
                        prefsManager = PrefsManager.getInstance(context),
                        context = context, // Truyền context
                        geminiRepository = GeminiRepository(), // Khởi tạo GeminiRepository
                        heyGenRepository = repositories[2] as HeyGenRepository
                    ) as T
                } else {
                    throw IllegalArgumentException("Repositories list must contain NewsRepository, UserRepository, and HeyGenRepository for ExploreViewModel")
                }
            }
            modelClass.isAssignableFrom(RadioViewModel::class.java) -> {
                if (repositories is List<*> && repositories.size >= 2 &&
                    repositories[0] is RadioRepository && repositories[1] is UserRepository
                ) {
                    RadioViewModel(repositories[0] as RadioRepository, repositories[1] as UserRepository) as T
                } else if (repositories is UserRepository) {
                    val radioRepo = RadioRepository()
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
            modelClass.isAssignableFrom(OfflineListNewsViewModel::class.java) -> {
                if (repositories is AppDatabase) {
                    OfflineListNewsViewModel(repositories) as T
                } else {
                    throw IllegalArgumentException("Repository must be AppDatabase for OfflineListNewsViewModel")
                }
            }
            modelClass.isAssignableFrom(NewsFeedViewModel::class.java) -> {
                if (repositories is List<*> && repositories.size >= 2 &&
                    repositories[0] is PostRepository && repositories[1] is UserRepository
                ) {
                    NewsFeedViewModel(
                        postRepository = repositories[0] as PostRepository,
                        userRepository = repositories[1] as UserRepository
                    ) as T
                } else {
                    throw IllegalArgumentException("Repositories list must contain PostRepository and UserRepository for NewsFeedViewModel")
                }
            }
            modelClass.isAssignableFrom(CreatePostViewModel::class.java) -> {
                if (repositories is List<*> && repositories.size >= 2 &&
                    repositories[0] is PostRepository && repositories[1] is UserRepository
                ) {
                    CreatePostViewModel(
                        postRepository = repositories[0] as PostRepository,
                        userRepository = repositories[1] as UserRepository
                    ) as T
                } else {
                    throw IllegalArgumentException("Repositories list must contain PostRepository and UserRepository for CreatePostViewModel")
                }
            }
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> {
                if (repositories is UserRepository && context != null) {
                    AccountViewModel(repositories, context) as T
                } else {
                    throw IllegalArgumentException("Repository must be UserRepository and context must be provided for AccountViewModel")
                }
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}