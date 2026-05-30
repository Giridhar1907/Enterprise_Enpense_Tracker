package com.example.enterpriseenpensetracker.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object ExpenseList : Screen("expense_list")
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    object SubmitExpense : Screen("submit_expense")
    object Profile : Screen("profile")
}
