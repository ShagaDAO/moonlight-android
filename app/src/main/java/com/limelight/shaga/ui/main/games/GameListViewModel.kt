package com.limelight.shaga.ui.main.games

import androidx.lifecycle.ViewModel
import com.limelight.AppView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameListViewModel : ViewModel() {
    var ipAddress = ""

    private val _uiState = MutableStateFlow<GameListUiState?>(null)
    val uiState = _uiState.asStateFlow()

    fun update(itemList: ArrayList<AppView.AppObject>) {
        _uiState.value = GameListUiState(ArrayList(itemList), ipAddress)
    }

}