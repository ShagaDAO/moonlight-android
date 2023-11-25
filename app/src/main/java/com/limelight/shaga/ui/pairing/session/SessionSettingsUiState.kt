package com.limelight.shaga.ui.pairing.session

import android.graphics.Bitmap
import com.limelight.shagaProtocol.DecodedAffairsData

sealed interface SessionSettingsUiState {
    val totalSteps: Int
        get() = 2
    val stepNumber: Int

    sealed interface Info : SessionSettingsUiState {
        override val stepNumber: Int
            get() = 0

        data object Loading : Info
        data object InvalidKey : Info
        data object Error : Info
        data class Display(
            val data: DecodedAffairsData,
            val usdPricePerHour: String?,
            val expectedLatency: Long?,
            val gameIconUrls: List<String>
        ) : Info
    }

    data class Payment(
        val data: DecodedAffairsData,
        val sliderValue: Float,
        val sliderLabel: String,
        val sliderMaxLabel: String,
        val totalPriceSol: Double,
        val accountBalanceSol: Double?,
        val selectedRentTimeMillis: Double,
        val isMakingTransaction: Boolean
    ) : SessionSettingsUiState {
        override val stepNumber: Int
            get() = 1

        val hasEnoughFunds = accountBalanceSol != null && accountBalanceSol >= totalPriceSol
    }

    sealed interface Wallet : SessionSettingsUiState {
        override val stepNumber: Int
            get() = 1

        data object Error : Wallet

        data class Display(
            val data: DecodedAffairsData,
            val priceSol: Double,
            val accountBalanceSol: Double,
            val key: String,
            val qrCode: Result<Bitmap>?,
            val isRefreshingBalance: Boolean,
            val selectedRentTimeMillis: Double,
            val isMakingTransaction: Boolean
        ) : Wallet
    }
}