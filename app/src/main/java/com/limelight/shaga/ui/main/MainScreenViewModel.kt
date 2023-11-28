package com.limelight.shaga.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limelight.shagaProtocol.ActiveRentalInfo
import com.limelight.shagaProtocol.ShagaTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    private val _activeRentalInfo = MutableStateFlow<ActiveRentalInfo?>(null)

    val uiState = _activeRentalInfo.map { activeRental -> MainScreenState(activeRental) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainScreenState(null))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _activeRentalInfo.value = ShagaTransactions.TransactionsObject.checkRentalStatus()
        }
    }
}