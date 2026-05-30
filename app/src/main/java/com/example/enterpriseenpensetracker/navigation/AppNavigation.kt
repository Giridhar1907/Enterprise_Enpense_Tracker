package com.example.enterpriseenpensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.enterpriseenpensetracker.presentation.auth.AuthViewModel
import com.example.enterpriseenpensetracker.presentation.auth.LoginScreen
import com.example.enterpriseenpensetracker.presentation.auth.SignupScreen
import com.example.enterpriseenpensetracker.presentation.dashboard.DashboardScreen
import com.example.enterpriseenpensetracker.presentation.expenses.ExpenseListScreen
import com.example.enterpriseenpensetracker.presentation.expenses.ExpenseSubmissionScreen
import com.example.enterpriseenpensetracker.presentation.expenses.ExpenseDetailScreen
import com.example.enterpriseenpensetracker.presentation.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser = authViewModel.currentUser.collectAsState().value

    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                } }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onSignupSuccess = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                } }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToExpenses = { navController.navigate(Screen.ExpenseList.route) },
                onNavigateToSubmit = { navController.navigate(Screen.SubmitExpense.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToDetail = { id -> navController.navigate(Screen.ExpenseDetail.createRoute(id)) }
            )
        }
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateToDetail = { id -> navController.navigate(Screen.ExpenseDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ExpenseDetail.route) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            ExpenseDetailScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SubmitExpense.route) {
            ExpenseSubmissionScreen(
                onExpenseSubmitted = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
