package com.example.enterpriseenpensetracker.domain.repository

import com.example.enterpriseenpensetracker.domain.model.User
import com.example.enterpriseenpensetracker.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String, role: String, orgId: String): Resource<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    fun getUserDetailsFlow(uid: String): Flow<Resource<User>>
    suspend fun forgotPassword(email: String): Resource<Unit>
    suspend fun getUserDetails(uid: String): Resource<User>
}
