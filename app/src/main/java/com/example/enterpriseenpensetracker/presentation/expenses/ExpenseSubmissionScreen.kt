package com.example.enterpriseenpensetracker.presentation.expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.enterpriseenpensetracker.domain.model.Expense
import com.example.enterpriseenpensetracker.presentation.auth.AuthViewModel
import com.example.enterpriseenpensetracker.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSubmissionScreen(
    onExpenseSubmitted: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(submitState) {
        if (submitState is Resource.Success) {
            com.example.enterpriseenpensetracker.utils.NotificationHelper.showNotification(
                context,
                "Expense Submitted",
                "Your claim '$title' for $amount has been sent for approval."
            )
            onExpenseSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Expense Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount ($)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri == null) "Attach Receipt Image" else "Image Attached")
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val expense = Expense(
                        employeeId = currentUser?.uid ?: "",
                        employeeName = currentUser?.name ?: "",
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        description = description,
                        organizationId = currentUser?.organizationId ?: ""
                    )
                    viewModel.submitExpense(expense, imageUri)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = submitState !is Resource.Loading
            ) {
                if (submitState is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit for Approval")
                }
            }
            
            if (submitState is Resource.Error) {
                Text(
                    text = (submitState as Resource.Error).message ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
