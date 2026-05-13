package com.example.thingsfire.topics

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// 1. Updated Routes to include HOME
object Dest {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
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
            HomeScreen(onLogout = {
                navController.navigate(Dest.LOGIN) {
                    popUpTo(Dest.HOME) { inclusive = true }
                }
            })
        }
    }
}