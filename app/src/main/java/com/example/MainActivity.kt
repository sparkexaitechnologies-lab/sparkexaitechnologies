package com.example

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.util.NotificationHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.repository.SparkexRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SparkexViewModel
import com.example.ui.viewmodel.SparkexViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()

        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = SparkexRepository(
            context = this,
            chatSessionDao = database.chatSessionDao(),
            chatMessageDao = database.chatMessageDao(),
            generatedImageDao = database.generatedImageDao(),
            userProfileDao = database.userProfileDao()
        )

        // Initialize ViewModel
        val factory = SparkexViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[SparkexViewModel::class.java]

        setContent {
            val profile by viewModel.userProfile.collectAsState()
            val darkTheme = when (profile.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "chat",
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(220)
                            ) + fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(220))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(220)
                            ) + fadeOut(animationSpec = tween(220)) +
                            scaleOut(targetScale = 1.08f, animationSpec = tween(220))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(220)
                            ) + fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 1.08f, animationSpec = tween(220))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(220)
                            ) + fadeOut(animationSpec = tween(220)) +
                            scaleOut(targetScale = 0.92f, animationSpec = tween(220))
                        }
                    ) {
                        // 1. ChatGPT-level Chat Screen (Start Destination)
                        composable("chat") {
                            ChatScreen(
                                viewModel = viewModel,
                                onNavigateToSidebar = { navController.navigate("sidebar") },
                                onNavigateToSettings = { navController.navigate("profile") }, // Mapping settings to profile
                                onNavigateToImages = { navController.navigate("images") },
                                onNavigateToHelp = { navController.navigate("help") },
                                onNavigateToVideoCreator = { navController.navigate("video_creator") },
                                onNavigateToMemberships = { navController.navigate("memberships") }
                            )
                        }

                        // 1.5 Sidebar Full-Page Screen
                        composable("sidebar") {
                            SidebarScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 2. User Profile and Settings Screen (Redesigned)
                        composable("profile") {
                            ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToSubSetting = { subRoute -> navController.navigate("profile/$subRoute") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Profile Sub-settings
                        composable("profile/{subRoute}") { backStackEntry ->
                            val subRoute = backStackEntry.arguments?.getString("subRoute") ?: ""
                            SubSettingScreen(
                                viewModel = viewModel,
                                subRoute = subRoute,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 3. AI Graphic Creative Gallery
                        composable("images") {
                            ImagesViewerScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Onboarding & Help Guide Screen
                        composable("help") {
                            HelpScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 5. Legal Terms of Service & Privacy
                        composable(
                            route = "legal/{type}",
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val legalType = backStackEntry.arguments?.getString("type") ?: "privacy"
                            LegalScreen(
                                type = legalType,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Veo Cinematic Video Studio Screen
                        composable("video_creator") {
                            VideoCreatorScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Premium Memberships Screen
                        composable("memberships") {
                            MembershipsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
