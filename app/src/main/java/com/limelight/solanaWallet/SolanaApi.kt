package com.limelight.solanaWallet

import com.solana.Solana
import com.solana.api.getBalance
import com.solana.api.getRecentBlockhash
import com.solana.core.PublicKey
import com.solana.networking.HttpNetworkingRouter
import com.solana.networking.RPCEndpoint
import com.solana.networking.serialization.serializers.solana.PublicKeyAs32ByteSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume


object SolanaApi {
    private val endPoint = RPCEndpoint.devnetSolana
    private val network = HttpNetworkingRouter(endPoint)
    val solana = Solana(network)
    val scope = CoroutineScope(Dispatchers.IO)

    suspend fun getRecentBlockHashFromApi(): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            solana.api.getRecentBlockhash { result ->
                continuation.resume(result)
            }
        }
    }

    @Serializable
    data class StartRentalInstructionArgs(
        val rentalTerminationTime: ULong,
        val privatePairHashCode: String?
    )

    @Serializable
    data class AffairsListData(
        val activeAffairs: List<@Serializable(with = PublicKeyAs32ByteSerializer::class) PublicKey>
    )

    @Serializable
    enum class AffairState {
        Unavailable,
        Available
    }

    @Serializable
    data class AffairsData(
        @Serializable(with = PublicKeyAs32ByteSerializer::class)
        val authority: PublicKey,
        @Serializable(with = PublicKeyAs32ByteSerializer::class)
        val client: PublicKey,
        @Serializable(with = PublicKeyAs32ByteSerializer::class)
        val rental: PublicKey?,

        val coordinates: String,

        val ipAddress: String,
        val cpuName: String,
        val gpuName: String,

        val totalRamMb: UInt,
        val solPerHour: ULong,

        val affairState: AffairState,

        val affairTerminationTime: ULong,
        val activeRentalStartTime: ULong,
        val dueRentAmount: ULong
    )


    interface BalanceCallback {
        fun onBalanceReceived(balanceInLamports: Long?)
    }

    @JvmStatic
    fun getBalance(publicKey: PublicKey, callback: BalanceCallback) {
        scope.launch {
            val result = solana.api.getBalance(publicKey)
            callback.onBalanceReceived(result.getOrNull())
        }
    }
}