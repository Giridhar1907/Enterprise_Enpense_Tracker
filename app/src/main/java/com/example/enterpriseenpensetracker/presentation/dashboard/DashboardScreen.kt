package com.example.enterpriseenpensetracker.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.enterpriseenpensetracker.utils.PdfGenerator
import com.example.enterpriseenpensetracker.presentation.auth.AuthViewModel
import com.example.enterpriseenpensetracker.presentation.expenses.ExpenseItem
import com.example.enterpriseenpensetracker.presentation.expenses.ExpenseViewModel
import com.example.enterpriseenpensetracker.utils.Resource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToExpenses: () -> Unit,
    onNavigateToSubmit: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val stats by expenseViewModel.stats.collectAsState()
    val expensesResource by expenseViewModel.expenses.collectAsState()
    val context = LocalContext.current

    // Request notification permission for Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted -> }
        
        LaunchedEffect(Unit) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            expenseViewModel.getExpenses(it.uid, it.role.name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Enterprise Dashboard", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = currentUser?.organizationId ?: "Loading...", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    if (currentUser?.role == com.example.enterpriseenpensetracker.domain.model.UserRole.ADMIN) {
                        IconButton(onClick = {
                            if (expensesResource is Resource.Success) {
                                val list = (expensesResource as Resource.Success<List<com.example.enterpriseenpensetracker.domain.model.Expense>>).data ?: emptyList()
                                PdfGenerator.generateExpenseReport(context, list)
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Export Report")
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToSubmit,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("New Claim") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Section
            Text(
                text = "Hello, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "User"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Here's what's happening with your expenses today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Analytics Grid
            Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsCard(
                        title = "Pending Items", 
                        value = stats.pendingRequests.toString(), 
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Total Disbursed", 
                        value = "$${String.format(Locale.getDefault(), "%.2f", stats.approvedAmount)}", 
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (currentUser?.role != com.example.enterpriseenpensetracker.domain.model.UserRole.EMPLOYEE) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnalyticsCard(
                            title = "Rejected Total", 
                            value = "$${String.format(Locale.getDefault(), "%.2f", stats.rejectedAmount)}", 
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Total Volume", 
                            value = "$${String.format(Locale.getDefault(), "%.2f", stats.totalExpenses)}", 
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Utilization Section
            Text("Budget Utilization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (expensesResource is Resource.Success) {
                        val expenses = (expensesResource as Resource.Success<List<com.example.enterpriseenpensetracker.domain.model.Expense>>).data ?: emptyList()
                        val categories = expenses.groupBy { it.category }
                        if (categories.isEmpty()) {
                            Text("No category data available", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            categories.forEach { (category, list) ->
                                val approvedTotal = list.filter { it.status == com.example.enterpriseenpensetracker.domain.model.ExpenseStatus.APPROVED }.sumOf { it.amount }
                                val totalRequested = list.sumOf { it.amount }
                                val percentage = if (stats.totalExpenses > 0) totalRequested / stats.totalExpenses else 0.0
                                
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "$${String.format(Locale.getDefault(), "%.2f", totalRequested)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percentage.toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                        color = if (percentage > 0.6) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Recent Activity Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateToExpenses) {
                    Text("See All")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            when (val resource = expensesResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val recentExpenses = resource.data?.take(5) ?: emptyList()
                    if (recentExpenses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No recent transactions", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        recentExpenses.forEach { expense ->
                            ExpenseItem(expense = expense, onClick = { onNavigateToDetail(expense.id) })
                        }
                    }
                }
                is Resource.Error -> {
                    Text("Error loading data", color = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
