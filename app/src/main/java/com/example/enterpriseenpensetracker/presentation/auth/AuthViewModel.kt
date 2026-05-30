package com.example.enterpriseenpensetracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterpriseenpensetracker.domain.model.User
import com.example.enterpriseenpensetracker.domain.repository.AuthRepository
import com.example.enterpriseenpensetracker.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<User>?>(null)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUser().collectLatest { authUser ->
                if (authUser != null) {
                    // Set basic info immediately so UI isn't empty
                    if (_currentUser.value == null) {
                        _currentUser.value = authUser
                    }
                    
                    // Keep full details synced
                    repository.getUserDetailsFlow(authUser.uid).collectLatest { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                _currentUser.value = resource.data
                            }
                            is Resource.Error -> {
                                // If Firestore fails, at least keep basic auth info
                                if (_currentUser.value?.name.isNullOrEmpty()) {
                                    _currentUser.value = authUser
                                }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.login(email, password)
        }
    }

    fun signup(name: String, email: String, password: String, role: String, orgId: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.signup(name, email, password, role, orgId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = null
        }
    }
}
