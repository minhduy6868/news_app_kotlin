package com.gk.news_pro.page.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.screen.account_screen.AccountScreen
import com.gk.news_pro.page.screen.detail_screen.NewsDetailScreen
import com.gk.news_pro.page.screen.explore_screen.ExploreScreen
import com.gk.news_pro.page.screen.favorite_screen.FavoriteScreen
import com.gk.news_pro.page.screen.home_screen.HomeScreen
import com.gk.news_pro.page.screen.home_screen.HomeViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Explore : Screen("explore", "Explore", Icons.Filled.DateRange)
    object Favorite : Screen("favorite", "Favorite", Icons.Filled.Favorite)
    object Account : Screen("account", "Account", Icons.Filled.AccountCircle)
    object NewsDetail : Screen("news_detail/{articleId}", "News Detail", Icons.Filled.Home) {
        fun createRoute(articleId: String) = "news_detail/$articleId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Explore, Screen.Favorite, Screen.Account)
    val newsRepository = NewsRepository()
    val geminiRepository = GeminiRepository()
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactory(newsRepository))

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/{")

            if (currentRoute != Screen.NewsDetail.route.substringBefore("/{")) {
                ModernBottomBar(
                    items = items,
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNewsClick = { news ->
                        Log.d("AppNavigation", "Navigating to news_detail with articleId: ${news.article_id}")
                        navController.navigate(Screen.NewsDetail.createRoute(news.article_id))
                    }
                )
            }
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Favorite.route) { FavoriteScreen() }
            composable(Screen.Account.route) { AccountScreen() }
            composable(
                route = Screen.NewsDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                Log.d("NewsDetailScreen", "Received articleId: $articleId")
                val news = viewModel.getNewsById(articleId)
                if (news != null) {
                    NewsDetailScreen(
                        navController = navController,
                        news = news,
                        geminiRepository = geminiRepository
                    )
                } else {
                    Log.e("NewsDetailScreen", "No news found for articleId: $articleId")
                    Text(
                        text = "Article not found",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
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
                            imageVector = screen.icon,
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