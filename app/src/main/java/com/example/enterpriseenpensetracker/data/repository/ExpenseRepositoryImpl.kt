package com.example.enterpriseenpensetracker.data.repository

import android.net.Uri
import com.example.enterpriseenpensetracker.domain.model.Expense
import com.example.enterpriseenpensetracker.domain.model.ExpenseStatus
import com.example.enterpriseenpensetracker.domain.repository.ExpenseRepository
import com.example.enterpriseenpensetracker.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * Interview Note: Why this Repository implementation?
 * 1. Abstraction: The Domain layer doesn't know we're using Firebase. We could swap this for 
 *    Retrofit/Room without changing business logic.
 * 2. Single Source of Truth: We use callbackFlow to convert Firebase listeners into a reactive Flow.
 * 3. Separation of Concerns: Image upload (Storage) and metadata (Firestore) are handled here, 
 *    providing a clean Unit result to the UI.
 */
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ExpenseRepository {

    override suspend fun submitExpense(expense: Expense, imageUri: Uri?): Resource<Unit> {
        return try {
            var finalExpense = expense
            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("receipts/$fileName")
                ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await()
                finalExpense = expense.copy(receiptUrl = downloadUrl.toString())
            }
            
            val docRef = firestore.collection("expenses").document()
            val expenseToSave = finalExpense.copy(id = docRef.id)
            docRef.set(expenseToSave).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override fun getExpenses(userId: String, role: String): Flow<Resource<List<Expense>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val query = if (role == "MANAGER") {
            // Managers see all expenses
            firestore.collection("expenses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            // Employees see only theirs
            firestore.collection("expenses")
                .whereEqualTo("employeeId", userId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Firestore error"))
                return@addSnapshotListener
            }
            
            val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
            trySend(Resource.Success(expenses))
        }
        
        awaitClose { listener.remove() }
    }

    override fun getExpenseById(expenseId: String): Flow<Resource<Expense>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("expenses").document(expenseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Firestore error"))
                    return@addSnapshotListener
                }
                val expense = snapshot?.toObject(Expense::class.java)
                if (expense != null) {
                    trySend(Resource.Success(expense))
                } else {
                    trySend(Resource.Error("Expense not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateExpenseStatus(
        expenseId: String,
        status: String,
        remarks: String?,
        managerId: String,
        managerName: String
    ): Resource<Unit> {
        return try {
            firestore.collection("expenses").document(expenseId).update(
                mapOf(
                    "status" to status,
                    "approvalRemarks" to remarks,
                    "approvedBy" to managerId,
                    "managerName" to managerName
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override fun getAllExpensesForAdmin(): Flow<Resource<List<Expense>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("expenses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Firestore error"))
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(Resource.Success(expenses))
            }
        awaitClose { listener.remove() }
    }
}
