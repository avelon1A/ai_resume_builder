package com.airesumebuilder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airesumebuilder.data.repository.AuthRepositoryImpl
import com.airesumebuilder.domain.model.*
import com.airesumebuilder.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect {
                _isLoggedIn.value = it
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> _authState.value = result.data
                is Resource.Error -> _authState.value = AuthState(error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            when (val result = authRepository.register(email, password, name)) {
                is Resource.Success -> _authState.value = result.data
                is Resource.Error -> _authState.value = AuthState(error = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState()
            _isLoggedIn.value = false
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
