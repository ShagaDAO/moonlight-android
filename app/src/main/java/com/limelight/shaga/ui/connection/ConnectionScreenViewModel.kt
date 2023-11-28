package com.limelight.shaga.ui.connection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ConnectionScreenState(
            message = "PAIRING STATUS AWAITING...",
            isError = false
        )
    )
    val uiState = _uiState.asStateFlow()

    fun updateUI(success: Boolean, message: String?) {
        _uiState.value = if (success) {
            ConnectionScreenState(
                message = "Pairing Status: success",
                isError = false
            )
        } else {
            ConnectionScreenState(
                message = "Pairing Status: Failed\n(${message ?: "Unknown Error"})",
                isError = true
            )
        }
    }
}