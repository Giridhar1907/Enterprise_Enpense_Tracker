package com.example.enterpriseenpensetracker.domain.repository

import com.example.enterpriseenpensetracker.domain.model.Expense
import com.example.enterpriseenpensetracker.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun submitExpense(expense: Expense, imageUri: android.net.Uri?): Resource<Unit>
    fun getExpenses(userId: String, role: String): Flow<Resource<List<Expense>>>
    fun getExpenseById(expenseId: String): Flow<Resource<Expense>>
    suspend fun updateExpenseStatus(expenseId: String, status: String, remarks: String?, managerId: String, managerName: String): Resource<Unit>
    fun getAllExpensesForAdmin(): Flow<Resource<List<Expense>>>
}
