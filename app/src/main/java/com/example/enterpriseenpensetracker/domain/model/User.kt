package com.example.enterpriseenpensetracker.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.EMPLOYEE,
    val organizationId: String = ""
)

enum class UserRole {
    EMPLOYEE,
    MANAGER,
    ADMIN
}
