package com.example.thingsfire.topics

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

object Dest {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val CHANNEL_DETAIL = "channel/{channelId}"
    const val FIELD_CHART = "channel/{channelId}/field/{fieldNumber}"

    fun channelDetail(channelId: String): String = "channel/$channelId"

    fun fieldChart(channelId: String, fieldNumber: Int): String {
        return "channel/$channelId/field/$fieldNumber"
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    // 1. Get Firebase instance to check current user status
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // 2. Determine where to start:
    // If user is already logged in, go to HOME. If not, go to LOGIN.
    val startScreen = if (currentUser != null) Dest.HOME else Dest.LOGIN

    NavHost(
        navController = navController,
        startDestination = startScreen
    ) {
        composable(route = Dest.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Dest.SIGNUP)
                },
                onLoginSuccess = {
                    navController.navigate(Dest.HOME) {
                        // Clear the login screen from the backstack
                        popUpTo(Dest.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Dest.SIGNUP) {
            SignUpScreen(
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Dest.HOME) {
            HomeScreen(
                onLogout = {
                    // Ensure Firebase is signed out before navigating back
                    auth.signOut()
                    navController.navigate(Dest.LOGIN) {
                        // Clear the home screen from the backstack
                        popUpTo(Dest.HOME) { inclusive = true }
                    }
                },
                onOpenChannel = { channelId ->
                    navController.navigate(Dest.channelDetail(channelId))
                }
            )
        }

        composable(
            route = Dest.CHANNEL_DETAIL,
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId").orEmpty()

            ChannelDetailScreen(
                channelId = channelId,
                onBack = { navController.popBackStack() },
                onFieldClick = { fieldNumber ->
                    navController.navigate(Dest.fieldChart(channelId, fieldNumber))
                }
            )
        }

        composable(
            route = Dest.FIELD_CHART,
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType },
                navArgument("fieldNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId").orEmpty()
            val fieldNumber = backStackEntry.arguments?.getInt("fieldNumber") ?: 1

            FieldChartScreen(
                channelId = channelId,
                fieldNumber = fieldNumber,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
