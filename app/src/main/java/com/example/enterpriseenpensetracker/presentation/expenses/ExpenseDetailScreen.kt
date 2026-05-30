package com.example.enterpriseenpensetracker.presentation.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.enterpriseenpensetracker.domain.model.ExpenseStatus
import com.example.enterpriseenpensetracker.domain.model.UserRole
import com.example.enterpriseenpensetracker.presentation.auth.AuthViewModel
import com.example.enterpriseenpensetracker.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val expenseResource by viewModel.selectedExpense.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(expenseId) {
        viewModel.getExpenseById(expenseId)
    }

    var remarks by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val resource = expenseResource) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = resource.message ?: "Expense not found",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is Resource.Success -> {
                    val expense = resource.data!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = expense.title, style = MaterialTheme.typography.headlineMedium)
                        Text(text = "Amount: $${expense.amount}", style = MaterialTheme.typography.titleLarge)
                        Text(text = "Submitted by: ${expense.employeeName}", style = MaterialTheme.typography.bodyMedium)
                        if (expense.managerName != null) {
                            Text(text = "Approved/Rejected by: ${expense.managerName}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(text = "Status: ${expense.status.name}", color = MaterialTheme.colorScheme.primary)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                        Text(text = expense.description, style = MaterialTheme.typography.bodyLarge)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (expense.receiptUrl != null) {
                            Text(text = "Receipt", style = MaterialTheme.typography.titleMedium)
                            AsyncImage(
                                model = expense.receiptUrl,
                                contentDescription = "Receipt Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                        
                        if (expense.approvalRemarks != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Manager Remarks", style = MaterialTheme.typography.titleMedium)
                            Text(text = expense.approvalRemarks, style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (currentUser?.role == UserRole.MANAGER && expense.status == ExpenseStatus.PENDING) {
                            Text(text = "Review Request", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                label = { Text("Approval Remarks") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateStatus(
                                            expenseId, 
                                            "APPROVED", 
                                            remarks, 
                                            currentUser?.uid ?: "",
                                            currentUser?.name ?: "Unknown Manager"
                                        )
                                        com.example.enterpriseenpensetracker.utils.NotificationHelper.showNotification(
                                            context,
                                            "Expense Approved",
                                            "Request for '${expense.title}' has been approved."
                                        )
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Approve")
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateStatus(
                                            expenseId, 
                                            "REJECTED", 
                                            remarks, 
                                            currentUser?.uid ?: "",
                                            currentUser?.name ?: "Unknown Manager"
                                        )
                                        com.example.enterpriseenpensetracker.utils.NotificationHelper.showNotification(
                                            context,
                                            "Expense Rejected",
                                            "Request for '${expense.title}' was rejected."
                                        )
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
