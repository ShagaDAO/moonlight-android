package com.limelight.shaga.ui.pairing.session

import com.limelight.shagaProtocol.DecodedAffairsData

sealed interface SessionSettingsUiEvent {
    data object PaymentError : SessionSettingsUiEvent

    data class PaymentSuccess(
        val data: DecodedAffairsData,
        val clientString: String
    ) : SessionSettingsUiEvent
}