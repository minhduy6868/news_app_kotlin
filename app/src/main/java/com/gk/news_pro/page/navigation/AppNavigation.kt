package com.gk.news_pro.page.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.screen.account_screen.AccountScreen
import com.gk.news_pro.page.screen.auth.LoginScreen
import com.gk.news_pro.page.screen.auth.RegisterScreen
import com.gk.news_pro.page.screen.detail_screen.NewsDetailScreen
import com.gk.news_pro.page.screen.explore_sceen.ExploreViewModel
import com.gk.news_pro.page.screen.explore_screen.ExploreScreen
import com.gk.news_pro.page.screen.favorite_screen.FavoriteScreen
import com.gk.news_pro.page.screen.home_screen.HomeScreen
import com.gk.news_pro.page.screen.home_screen.HomeViewModel
import com.google.ai.client.generativeai.BuildConfig
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Explore : Screen("explore", "Explore", Icons.Filled.DateRange)
    object Favorite : Screen("favorite", "Favorite", Icons.Filled.Favorite)
    object Account : Screen("account", "Account", Icons.Filled.AccountCircle)
    object NewsDetail : Screen("news_detail/{articleId}", "News Detail") {
        fun createRoute(articleId: String) = "news_detail/$articleId"
    }
    object Login : Screen("login", "Đăng nhập")
    object Register : Screen("register", "Đăng ký")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(Screen.Home, Screen.Explore, Screen.Favorite, Screen.Account)
    val newsRepository = NewsRepository()
    val geminiRepository = GeminiRepository()
    val userRepository = UserRepository()
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactory(newsRepository))
    val coroutineScope = rememberCoroutineScope()
    val isLoggedIn by remember { mutableStateOf(userRepository.isLoggedIn()) }
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

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
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    userRepository = userRepository,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
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
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNewsClick = { news ->
                        if (BuildConfig.DEBUG) {
                            Log.d("AppNavigation", "Navigating to news detail with ID: ${news.article_id}")
                        }
                        navController.navigate(Screen.NewsDetail.createRoute(news.article_id))
                    }
                )
            }
            composable(Screen.Explore.route) {
                val exploreViewModel: ExploreViewModel = viewModel(factory = ViewModelFactory(newsRepository))
                ExploreScreen(viewModel = exploreViewModel)
            }
            composable(Screen.Favorite.route) {  }
            composable(Screen.Account.route) {
                AccountScreen(
                    userRepository = userRepository,
                    onLogout = {
                        coroutineScope.launch {
                            userRepository.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(
                route = Screen.NewsDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                if (BuildConfig.DEBUG) {
                    Log.d("NewsDetailScreen", "Received articleId: $articleId")
                }
                val news = viewModel.getNewsById(articleId)
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
                            onClick = { navController.navigate(Screen.Home.route) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Quay lại Trang chủ")
                        }
                    }
                }
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