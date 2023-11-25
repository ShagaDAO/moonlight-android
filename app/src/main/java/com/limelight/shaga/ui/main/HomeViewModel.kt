package com.limelight.shaga.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limelight.shagaProtocol.ShagaTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _hasActiveRental = MutableStateFlow<Boolean?>(null)

    val uiState = _hasActiveRental.map { hasActive -> HomeState(hasActive) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeState(null))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _hasActiveRental.value = ShagaTransactions.TransactionsObject.checkRentalStatus()
        }
    }
}