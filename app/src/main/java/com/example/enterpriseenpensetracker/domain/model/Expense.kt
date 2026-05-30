package com.example.enterpriseenpensetracker.domain.model

import java.util.Date

data class Expense(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val receiptUrl: String? = null,
    val status: ExpenseStatus = ExpenseStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedBy: String? = null,
    val managerName: String? = null,
    val approvalRemarks: String? = null,
    val organizationId: String = ""
)

enum class ExpenseStatus {
    PENDING,
    APPROVED,
    REJECTED
}
