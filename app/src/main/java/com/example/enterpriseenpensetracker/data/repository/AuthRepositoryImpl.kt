package com.example.enterpriseenpensetracker.data.repository

import com.example.enterpriseenpensetracker.domain.model.User
import com.example.enterpriseenpensetracker.domain.model.UserRole
import com.example.enterpriseenpensetracker.domain.repository.AuthRepository
import com.example.enterpriseenpensetracker.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User not found")
            getUserDetails(uid)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override suspend fun signup(
        name: String,
        email: String,
        password: String,
        role: String,
        orgId: String
    ): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Signup failed")
            
            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = UserRole.valueOf(role),
                organizationId = orgId
            )
            
            firestore.collection("users").document(uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Here we might want to fetch full details, but for now just send minimal
                // or use a separate flow for full details
                trySend(User(uid = firebaseUser.uid, email = firebaseUser.email ?: ""))
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun forgotPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override fun getUserDetailsFlow(uid: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Firestore error"))
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("User not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getUserDetails(uid: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User details not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }
}
