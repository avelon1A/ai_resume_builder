package com.airesumebuilder.navigation

import androidx.compose.runtime.*
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.airesumebuilder.presentation.ui.*
import com.airesumebuilder.presentation.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object ResumeBuilder : Screen("resume_builder")
    data object ResumeResult : Screen("resume_result/{resumeId}") {
        fun createRoute(resumeId: String) = "resume_result/$resumeId"
    }
    data object ResumeEditor : Screen("resume_editor/{resumeId}") {
        fun createRoute(resumeId: String) = "resume_editor/$resumeId"
    }
    data object CoverLetter : Screen("cover_letter")
    data object MyResumes : Screen("my_resumes")
    data object Paywall : Screen("paywall")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()

    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Welcome.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onContinue = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userName = userName,
                onGenerateResume = { navController.navigate(Screen.ResumeBuilder.route) },
                onGenerateCoverLetter = { navController.navigate(Screen.CoverLetter.route) },
                onMyResumes = { navController.navigate(Screen.MyResumes.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ResumeBuilder.route) {
            ResumeBuilderScreen(
                onNavigateBack = { navController.popBackStack() },
                onGenerated = { resumeId ->
                    navController.navigate(Screen.ResumeResult.createRoute(resumeId))
                }
            )
        }

        composable(
            route = Screen.ResumeResult.route,
            arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: return@composable
            ResumeResultScreen(
                resumeId = resumeId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.ResumeEditor.createRoute(resumeId)) }
            )
        }

        composable(
            route = Screen.ResumeEditor.route,
            arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: return@composable
            ResumeEditorScreen(
                resumeId = resumeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CoverLetter.route) {
            CoverLetterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyResumes.route) {
            MyResumesScreen(
                onNavigateBack = { navController.popBackStack() },
                onResumeClick = { resumeId ->
                    navController.navigate(Screen.ResumeResult.createRoute(resumeId))
                }
            )
        }

        composable(Screen.Paywall.route) {
            PaywallScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubscribe = {
                    // TODO: Integrate billing
                    navController.popBackStack()
                }
            )
        }
    }
}
