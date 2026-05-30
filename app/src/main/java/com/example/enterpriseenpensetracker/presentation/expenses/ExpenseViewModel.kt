package com.example.enterpriseenpensetracker.presentation.expenses

import android.net.Uri
import com.example.enterpriseenpensetracker.domain.model.ExpenseStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterpriseenpensetracker.domain.model.Expense
import com.example.enterpriseenpensetracker.domain.repository.ExpenseRepository
import com.example.enterpriseenpensetracker.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Interview Note: Why MVVM with StateFlow?
 * 1. Unidirectional Data Flow (UDF): The UI observes state and sends events. This makes debugging easier.
 * 2. Lifecycle Awareness: ViewModel survives configuration changes (like rotation).
 * 3. Reactive UI: Compose automatically recomposes when StateFlow emits a new value.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<Resource<List<Expense>>>(Resource.Loading())
    val expenses = _expenses.asStateFlow()

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState = _submitState.asStateFlow()

    private val _selectedExpense = MutableStateFlow<Resource<Expense>>(Resource.Loading())
    val selectedExpense = _selectedExpense.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats = _stats.asStateFlow()

    fun getExpenses(userId: String, role: String) {
        viewModelScope.launch {
            val flow = if (role == "ADMIN") {
                repository.getAllExpensesForAdmin()
            } else {
                repository.getExpenses(userId, role)
            }
            flow.collectLatest { resource ->
                _expenses.value = resource
                if (resource is Resource.Success) {
                    calculateStats(resource.data ?: emptyList())
                }
            }
        }
    }

    fun getExpenseById(expenseId: String) {
        viewModelScope.launch {
            repository.getExpenseById(expenseId).collectLatest {
                _selectedExpense.value = it
            }
        }
    }

    private fun calculateStats(expenses: List<Expense>) {
        val pendingCount = expenses.count { it.status == ExpenseStatus.PENDING }
        val approvedAmount = expenses.filter { it.status == ExpenseStatus.APPROVED }.sumOf { it.amount }
        val rejectedAmount = expenses.filter { it.status == ExpenseStatus.REJECTED }.sumOf { it.amount }
        val totalAmount = expenses.sumOf { it.amount }
        
        _stats.value = DashboardStats(
            pendingRequests = pendingCount,
            approvedAmount = approvedAmount,
            rejectedAmount = rejectedAmount,
            totalExpenses = totalAmount
        )
    }

    fun submitExpense(expense: Expense, imageUri: Uri?) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading()
            _submitState.value = repository.submitExpense(expense, imageUri)
        }
    }

    fun updateStatus(expenseId: String, status: String, remarks: String?, managerId: String, managerName: String) {
        viewModelScope.launch {
            repository.updateExpenseStatus(expenseId, status, remarks, managerId, managerName)
        }
    }
}

data class DashboardStats(
    val totalExpenses: Double = 0.0,
    val pendingRequests: Int = 0,
    val approvedAmount: Double = 0.0,
    val rejectedAmount: Double = 0.0
)
