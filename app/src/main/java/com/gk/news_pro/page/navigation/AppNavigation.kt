package com.gk.news_pro.page.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.HeyGenRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.PostRepository
import com.gk.news_pro.data.repository.RadioRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.screen.account_screen.AccountScreen
import com.gk.news_pro.page.screen.auth.LoginScreen
import com.gk.news_pro.page.screen.auth.RegisterScreen
import com.gk.news_pro.page.screen.detail_screen.NewsDetailScreen
import com.gk.news_pro.page.screen.explore_sceen.ExploreScreen
import com.gk.news_pro.page.screen.explore_sceen.ExploreViewModel
import com.gk.news_pro.page.screen.favorite_screen.FavoriteScreen
import com.gk.news_pro.page.screen.offline_list_news_screen.OfflineListNewsScreen
import com.gk.news_pro.page.screen.news_feed.NewsFeedScreen
import com.gk.news_pro.page.screen.radio_screen.RadioScreen
import com.gk.news_pro.page.screen.radio_screen.RadioViewModel
import com.gk.news_pro.page.screen.radio_screen.components.MiniPlayer
import com.gk.news_pro.page.utils.service.PlaybackState
import com.gk.news_pro.utils.MediaPlayerManager
import com.google.ai.client.generativeai.BuildConfig
import com.google.gson.Gson
import com.gk.news_pro.page.screen.about_screen.AboutScreen
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Radio : Screen("radio", "Radio", Icons.Filled.PlayArrow)
    object Explore : Screen("explore", "Tin Tức", Icons.Filled.DateRange)
    object NewsFeed : Screen("news_feed", "Bảng Tin", Icons.Filled.Share)
    object OfflineNews : Screen("offline_news", "Tin Ngoại Tuyến", Icons.Filled.DateRange)
    object Favorite : Screen("favorite", "Yêu Thích", Icons.Filled.Favorite)
    object Account : Screen("account", "Tài Khoản", Icons.Filled.AccountCircle)
    object About : Screen("about", "Giới Thiệu")
    object NewsDetail : Screen("news_detail/{newsJson}", "Chi Tiết Tin Tức") {
        fun createRoute(newsJson: String): String {
            val encodedJson = URLEncoder.encode(newsJson, "UTF-8")
            return "news_detail/$encodedJson"
        }
    }
    object Login : Screen("login", "Đăng nhập")
    object Register : Screen("register", "Đăng ký")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(Screen.Explore, Screen.NewsFeed, Screen.Radio, Screen.Account)
    val context = LocalContext.current
    val newsRepository = NewsRepository()
    val geminiRepository = GeminiRepository()
    val userRepository = UserRepository()
    val radioRepository = RadioRepository()
    val postRepository = PostRepository()
    val heyGenRepository = HeyGenRepository(context)
    val radioViewModel: RadioViewModel = viewModel(
        factory = ViewModelFactory(listOf(radioRepository, userRepository))
    )
    val coroutineScope = rememberCoroutineScope()
    val isLoggedIn by remember { mutableStateOf(userRepository.isLoggedIn()) }
    val startDestination = Screen.Explore.route
    val gson = Gson()

    LaunchedEffect(Unit) {
        radioViewModel.bindService(context)
    }

    LaunchedEffect(isLoggedIn) {
        Log.d("AppNavigation", "isLoggedIn: $isLoggedIn, startDestination: $startDestination")
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/{")
            val showBottomBar = bottomNavItems.any { it.route == currentRoute }

            if (showBottomBar) {
                ModernBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        if (screen == Screen.Account && !userRepository.isLoggedIn()) {
                            Log.d("AppNavigation", "User not logged in, redirecting to Login")
                            navController.navigate(Screen.Login.route)
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        userRepository = userRepository,
                        onLoginSuccess = {
                            Log.d("AppNavigation", "Login successful, navigating to Explore")
                            navController.navigate(Screen.Explore.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        userRepository = userRepository,
                        onRegisterSuccess = {
                            Log.d("AppNavigation", "Register successful, navigating to Login")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Radio.route) {
                    RadioScreen(
                        userRepository = userRepository,
                        viewModel = radioViewModel,
                        onStationClick = {
                            Log.d("AppNavigation", "Radio station clicked: ${it.name}")
                        }
                    )
                }
                composable(Screen.Explore.route) {
                    val exploreViewModel: ExploreViewModel = viewModel(
                        factory = ViewModelFactory(
                            repositories = listOf(newsRepository, userRepository, heyGenRepository),
                            context = context
                        )
                    )
                    ExploreScreen(
                        userRepository = userRepository,
                        context = context,
                        viewModel = exploreViewModel,
                        onNewsClick = { news ->
                            try {
                                val newsJson = gson.toJson(news)
                                Log.d("AppNavigation", "Serialized newsJson: $newsJson")
                                navController.navigate(Screen.NewsDetail.createRoute(newsJson))
                            } catch (e: Exception) {
                                Log.e("AppNavigation", "Error serializing news: ${e.message}", e)
                            }
                        }
                    )
                }
                composable(Screen.NewsFeed.route) {
                    NewsFeedScreen(
                        userRepository = userRepository,
                        postRepository = postRepository
                    )
                }
                composable(Screen.OfflineNews.route) {
                    Log.d("AppNavigation", "Navigating to OfflineListNewsScreen")
                    OfflineListNewsScreen(
                        navController = navController,
                        onNewsClick = { news ->
                            try {
                                val newsJson = gson.toJson(news)
                                Log.d("AppNavigation", "Serialized newsJson: $newsJson")
                                navController.navigate(Screen.NewsDetail.createRoute(newsJson))
                            } catch (e: Exception) {
                                Log.e("AppNavigation", "Error serializing news: ${e.message}", e)
                            }
                        }
                    )
                }
                composable(Screen.Favorite.route) {
                    Log.d("AppNavigation", "Navigating to FavoriteScreen")
                    FavoriteScreen(
                        navController = navController,
                        userRepository = userRepository,
                        onNewsClick = { news ->
                            try {
                                val newsJson = gson.toJson(news)
                                Log.d("AppNavigation", "Serialized newsJson: $newsJson")
                                navController.navigate(Screen.NewsDetail.createRoute(newsJson))
                            } catch (e: Exception) {
                                Log.e("AppNavigation", "Error serializing news: ${e.message}", e)
                            }
                        }
                    )
                }
                composable(Screen.Account.route) {
                    AccountScreen(
                        userRepository = userRepository,
                        onSignOut = {
                            coroutineScope.launch {
                                userRepository.signOut()
                                Log.d("AppNavigation", "User logged out, navigating to Login")
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToOfflineNews = {
                            navController.navigate(Screen.OfflineNews.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFavoriteScreen = {
                            navController.navigate(Screen.Favorite.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAboutScreen = {
                            navController.navigate(Screen.About.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen(navController = navController)
                }
                composable(
                    route = Screen.NewsDetail.route,
                    arguments = listOf(navArgument("newsJson") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedNewsJson = backStackEntry.arguments?.getString("newsJson") ?: ""
                    val newsJson = try {
                        URLDecoder.decode(encodedNewsJson, "UTF-8")
                    } catch (e: Exception) {
                        Log.e("NewsDetailScreen", "Error decoding newsJson: ${e.message}", e)
                        ""
                    }
                    val news = try {
                        Log.d("NewsDetailScreen", "Deserializing newsJson: $newsJson")
                        gson.fromJson(newsJson, News::class.java)
                    } catch (e: Exception) {
                        Log.e("NewsDetailScreen", "Error deserializing news: ${e.message}", e)
                        null
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d("NewsDetailScreen", "Received news: ${news?.title ?: "Not found"}")
                    }
                    if (news != null) {
                        NewsDetailScreen(
                            navController = navController,
                            news = news,
                            geminiRepository = geminiRepository
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Không tìm thấy bài viết",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate(Screen.Explore.route) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Quay lại Tin Tức")
                            }
                        }
                    }
                }
            }

            val playbackState by MediaPlayerManager.getPlaybackState()?.collectAsState() ?: return@Box
            val playingStation by radioViewModel.playingStation.collectAsState()

            if (playingStation != null && playbackState != PlaybackState.Idle) {
                MiniPlayer(
                    viewModel = radioViewModel,
                    onStationClick = {
                        navController.navigate(Screen.Radio.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ModernBottomBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(64.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(screen) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = screen.title,
                            modifier = Modifier.size(24.dp)
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(3.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}