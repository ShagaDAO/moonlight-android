package com.limelight.shaga.ui.pairing.session

import android.graphics.Bitmap
import com.limelight.shaga.util.combine6
import com.limelight.shagaProtocol.DecodedAffairsData
import com.limelight.solanaWallet.SolanaPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.math.roundToInt

class SessionSettingsStateFactory(
    private val parsedPairingString: Pair<String, String>?,
    private val currentStep: StateFlow<SessionSettingsViewModel.Step>,
    private val affairsDataResult: StateFlow<Result<DecodedAffairsData>?>,
    private val expectedLatency: StateFlow<Long?>,
    private val solToUsdRate: StateFlow<Result<Double>?>,
    private val gameIconUrls: StateFlow<List<String>>,
    private val sliderValue: StateFlow<Float>,
    private val accountBalanceSol: StateFlow<Double?>,
    private val walletQrCode: MutableStateFlow<Result<Bitmap>?>,
    private val isRefreshingBalance: MutableStateFlow<Boolean>,
    private val isMakingTransaction: MutableStateFlow<Boolean>
) {
    fun createFlow(): Flow<SessionSettingsUiState> {
        return currentStep.flatMapLatest { step ->
            when (step) {
                SessionSettingsViewModel.Step.SESSION_INFO -> createSessionInfoStateFlow()
                SessionSettingsViewModel.Step.PAYMENT -> createPaymentStateFlow()
                SessionSettingsViewModel.Step.NOT_ENOUGH_BALANCE -> createWalletInfoStateFlow()
            }
        }
    }

    private fun createSessionInfoStateFlow(): Flow<SessionSettingsUiState> {
        return combine(
            affairsDataResult,
            solToUsdRate,
            expectedLatency,
            gameIconUrls
        ) { affairsDataResult, solToUsdRate, latency, gameIcons ->
            if (parsedPairingString != null) {
                affairsDataResult?.fold(
                    onSuccess = { data ->
                        val usdcPerHour = solToUsdRate?.fold(
                            onSuccess = { String.format("%.2f", data.solPerHour * it) },
                            onFailure = { null }
                        )
                        SessionSettingsUiState.Info.Display(data, usdcPerHour, latency, gameIcons)
                    },
                    onFailure = { SessionSettingsUiState.Info.Error }
                ) ?: SessionSettingsUiState.Info.Loading
            } else {
                SessionSettingsUiState.Info.InvalidKey
            }
        }
    }

    private fun createPaymentStateFlow(): Flow<SessionSettingsUiState> {
        return combine(
            affairsDataResult,
            sliderValue,
            accountBalanceSol,
            isMakingTransaction
        ) { affairsDataResult, sliderValue, accountBalance, isMakingTransaction ->
            val data = affairsDataResult?.getOrNull()!!
            val rentInfo = calculateRentInfo(data, sliderValue)

            SessionSettingsUiState.Payment(
                data = data,
                sliderValue = sliderValue,
                sliderLabel = rentInfo.selectedTimeLabel,
                sliderMaxLabel = rentInfo.maxTimeLabel,
                totalPriceSol = rentInfo.priceSol,
                accountBalanceSol = accountBalance,
                selectedRentTimeMillis = rentInfo.selectedRentTimeMillis,
                isMakingTransaction = isMakingTransaction
            )
        }
    }

    private fun createWalletInfoStateFlow(): Flow<SessionSettingsUiState> {
        return combine6(
            affairsDataResult,
            sliderValue,
            accountBalanceSol,
            walletQrCode,
            isRefreshingBalance,
            isMakingTransaction
        ) { affairsDataResult, sliderValue, accountBalance, qrCodeResult, isRefreshingBalance, isMakingTransaction ->
            val data = affairsDataResult?.getOrNull()
            val publicKey = SolanaPreferenceManager.getStoredPublicKey()?.toString()

            if (data == null || accountBalance == null || publicKey == null) {
                return@combine6 SessionSettingsUiState.Wallet.Error
            }

            val rentInfo = calculateRentInfo(data, sliderValue)

            SessionSettingsUiState.Wallet.Display(
                priceSol = rentInfo.priceSol,
                accountBalanceSol = accountBalance,
                key = publicKey,
                qrCode = qrCodeResult,
                isRefreshingBalance = isRefreshingBalance,
                selectedRentTimeMillis = rentInfo.selectedRentTimeMillis,
                data = data,
                isMakingTransaction = isMakingTransaction
            )
        }
    }

    private fun calculateRentInfo(data: DecodedAffairsData, sliderValue: Float): RentInfo {
        // Step 1: Get current time in milliseconds
        val currentTime = System.currentTimeMillis()
        // Step 2: Calculate the maximum and minimum time for renting in milliseconds
        val minTimeMillis: ULong =
            600000uL // 10 minutes in milliseconds, explicitly set as ULong for type safety
        val affairTerminationMillis: ULong =
            data.affairTerminationTime * 1000uL // Assuming affairTerminationTime is in seconds, convert it to milliseconds
        // Check for any time discrepancies and correct them
        var maxTimeMillis: ULong = if (affairTerminationMillis > currentTime.toULong()) {
            affairTerminationMillis - currentTime.toULong()
        } else {
            0uL
        }
        // Safety check
        maxTimeMillis = maxTimeMillis.coerceAtLeast(minTimeMillis)
        if (maxTimeMillis <= minTimeMillis) {
            maxTimeMillis =
                minTimeMillis + 1000000uL // Add extra 1,000,000 milliseconds (or 1,000 seconds) for safety
        }

        val maxTimeLabel = if (maxTimeMillis >= 3600000uL) {
            "${maxTimeMillis / 3600000uL}hr"
        } else {
            "${maxTimeMillis / 60000uL}m"
        }

        maxTimeMillis - minTimeMillis
        val selectedRentTimeMillis =
            minTimeMillis.toDouble() + sliderValue * (maxTimeMillis - minTimeMillis).toDouble()
        val expectedRentCost =
            (selectedRentTimeMillis / (60 * 60 * 1000.0)) * data.solPerHour  // Assuming solPerHour is now in SOL after conversion

        val selectedTimeLabel = formatLabel(sliderValue, minTimeMillis, maxTimeMillis)

        return RentInfo(
            selectedRentTimeMillis = selectedRentTimeMillis,
            maxTimeLabel = maxTimeLabel,
            selectedTimeLabel = selectedTimeLabel,
            priceSol = expectedRentCost
        )
    }

    private fun formatLabel(value: Float, minTimeMillis: ULong, maxTimeMillis: ULong): String {
        val minTimeMinutes =
            (minTimeMillis / 60000uL).toFloat() // 60 * 1000 = 60000 for milliseconds to minutes conversion
        val maxTimeMinutes = (maxTimeMillis / 60000uL).toFloat()

        val totalMinutes =
            (minTimeMinutes + (value * (maxTimeMinutes - minTimeMinutes))).roundToInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "$hours hr $minutes m"
            hours > 0 -> "$hours hr"
            else -> "$minutes m"
        }
    }

    private class RentInfo(
        val selectedRentTimeMillis: Double,
        val maxTimeLabel: String,
        val selectedTimeLabel: String,
        val priceSol: Double
    )
}