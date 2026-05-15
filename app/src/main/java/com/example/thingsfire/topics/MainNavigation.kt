package com.example.thingsfire.topics

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// 1. Updated Routes to include HOME
object Dest {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val CHANNEL_DETAIL = "channel/{channelId}"
    const val FIELD_CHART = "channel/{channelId}/field/{fieldNumber}"
    const val PREVIEW_CHANNEL_ID = "3347987"

    fun channelDetail(channelId: String): String = "channel/$channelId"

    fun fieldChart(channelId: String, fieldNumber: Int): String {
        return "channel/$channelId/field/$fieldNumber"
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Dest.LOGIN
    ) {
        composable(route = Dest.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Dest.SIGNUP)
                },
                // Add a new parameter to LoginScreen to handle successful login
                onLoginSuccess = {
                    navController.navigate(Dest.HOME) {
                        // Pop up to LOGIN to prevent user from going back to login screen
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

        // 2. Add the Home screen destination
        composable(route = Dest.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Dest.LOGIN) {
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
