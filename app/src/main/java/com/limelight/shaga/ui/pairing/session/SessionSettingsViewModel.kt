package com.limelight.shaga.ui.pairing.session

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limelight.shagaProtocol.DecodedAffairsData
import com.limelight.shagaProtocol.MapPopulation
import com.limelight.shagaProtocol.ShagaTransactions
import com.limelight.shagaProtocol.fetchSolPriceInUSDC
import com.limelight.solanaWallet.SolanaApi
import com.limelight.solanaWallet.SolanaPreferenceManager
import com.limelight.solanaWallet.WalletInitializer
import com.limelight.solanaWallet.WalletManager
import com.limelight.solanaWallet.encodeAsBitmap
import com.solana.api.getMultipleAccountsInfo
import com.solana.api.sendTransaction
import com.solana.core.HotAccount
import com.solana.core.PublicKey
import com.solana.core.TransactionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bitcoinj.core.Base58

class SessionSettingsViewModel(pairingString: String) : ViewModel() {
    private val parsedPairingString = parsePairingString(pairingString)

    private val _currentStep = MutableStateFlow(Step.SESSION_INFO)
    private val _affairsDataResult = MutableStateFlow<Result<DecodedAffairsData>?>(null)
    private val _expectedLatency = MutableStateFlow<Long?>(null)
    private val _solToUsdRate = MutableStateFlow<Result<Double>?>(null)
    private val _gameIconUrls = MutableStateFlow<List<String>>(emptyList())

    private val _sliderValue = MutableStateFlow(0f)
    private val _accountBalanceSol = MutableStateFlow<Double?>(null)
    private val _isRefreshingBalance = MutableStateFlow(false)
    private val _isMakingTransaction = MutableStateFlow(false)

    private val _walletQrCode = MutableStateFlow<Result<Bitmap>?>(null)

    private val stateFactory = SessionSettingsStateFactory(
        parsedPairingString = parsedPairingString,
        currentStep = _currentStep,
        affairsDataResult = _affairsDataResult,
        expectedLatency = _expectedLatency,
        solToUsdRate = _solToUsdRate,
        gameIconUrls = _gameIconUrls,
        sliderValue = _sliderValue,
        accountBalanceSol = _accountBalanceSol,
        walletQrCode = _walletQrCode,
        isRefreshingBalance = _isRefreshingBalance,
        isMakingTransaction = _isMakingTransaction
    )

    private val walletManager = WalletManager.getInstance()

    val uiState: StateFlow<SessionSettingsUiState> = stateFactory.createFlow().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SessionSettingsUiState.Info.Loading
    )

    private val _uiEvents = MutableSharedFlow<SessionSettingsUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        if (parsedPairingString != null) {
            val (address, _) = parsedPairingString

            viewModelScope.launch(Dispatchers.IO) {
                launch {
                    _solToUsdRate.value = runCatching { fetchSolPriceInUSDC() }
                }

                _affairsDataResult.value = try {
                    SolanaApi.solana.api.getMultipleAccountsInfo(
                        serializer = SolanaApi.AffairsData.serializer(),
                        accounts = listOf(PublicKey.valueOf(address))
                    ).map { accounts ->
                        decodeAffairsData(
                            accounts.filterNotNull().first { it.data != null }.data!!
                        )!!
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }

                _affairsDataResult.value?.getOrNull()?.let { data ->
                    launch {
                        _expectedLatency.value =
                            MapPopulation.MarkerUtils.retryLatencyCheck(data.ipAddress, 1000)
                                .getOrNull()
                    }

                    launch {
                        _gameIconUrls.value =
                            MapPopulation.NetworkUtils.fetchAppList(data.ipAddress).map { app ->
                                MapPopulation.NetworkUtils.buildGameIconUrl(
                                    data.ipAddress,
                                    app.appId
                                )
                            }
                    }
                }
            }
        }
    }

    fun initWallet(context: Context) {
        walletManager.setup(context) { balance ->
            _accountBalanceSol.value = balance
            _isRefreshingBalance.value = false
        }

        SolanaPreferenceManager.initialize(context)

        if (!SolanaPreferenceManager.getIsWalletInitialized()) {
            WalletInitializer.initializeWallet(context)
            SolanaPreferenceManager.setIsWalletInitialized(true)
        }

        refreshBalance()
    }

    private fun refreshBalance() {
        _isRefreshingBalance.value = true
        SolanaPreferenceManager.getStoredPublicKey()?.let { key ->
            walletManager.fetchAndDisplayBalance(key)
        }
    }

    private fun parsePairingString(value: String): Pair<String, String>? {
        val parts = value.split("_")
        return if (parts.size == 2) {
            val address = parts[0]
            val password = parts[1]
            if (address.isNotBlank() && password.isNotBlank()) {
                address to password
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun decodeAffairsData(data: SolanaApi.AffairsData): DecodedAffairsData? {
        val authorityBytes = data.authority.toByteArray()  // Convert PublicKey to byte array
        return if (authorityBytes.isNotEmpty()) {  // Check for null or empty
            val authorityKey = Base58.encode(authorityBytes)  // Use Base58 encoding

            val lamportsToSolConversionRate: ULong =
                1_000_000_000uL  // 1 Sol = 1,000,000,000 Lamports

            // Create a new DecodedAffairsData object with decoded and other values
            DecodedAffairsData(
                authorityKey = authorityKey,
                authority = data.authority,
                client = data.client,
                rental = data.rental,
                coordinates = data.coordinates,
                ipAddress = data.ipAddress,
                cpuName = data.cpuName,
                gpuName = data.gpuName,
                totalRamMb = data.totalRamMb,
                solPerHour = data.solPerHour.toDouble() / lamportsToSolConversionRate.toDouble(),
                affairState = data.affairState,  // Assuming affairState is an enum or similar directly usable type
                affairTerminationTime = data.affairTerminationTime,
                activeRentalStartTime = data.activeRentalStartTime,
                dueRentAmount = data.dueRentAmount
            )
        } else {
            null
        }
    }

    fun changeSliderValue(value: Float) {
        _sliderValue.value = value
    }

    fun generateWalletQrCode() {
        _walletQrCode.value = runCatching {
            val key = SolanaPreferenceManager.getStoredPublicKey()!!.toBase58()
            encodeAsBitmap(key, 500, 500)!!
        }
    }

    fun moveToNextStep() {
        when (_currentStep.value) {
            Step.SESSION_INFO -> {
                _currentStep.value = Step.PAYMENT
            }

            Step.PAYMENT -> {
                (uiState.value as? SessionSettingsUiState.Payment)?.let { state ->
                    if (state.hasEnoughFunds) {
                        makeTransaction(state.data, state.selectedRentTimeMillis)
                    } else {
                        _currentStep.value = Step.NOT_ENOUGH_BALANCE
                    }
                }
            }

            Step.NOT_ENOUGH_BALANCE -> {
                (uiState.value as? SessionSettingsUiState.Wallet.Display)?.let { state ->
                    if (state.accountBalanceSol >= state.priceSol) {
                        makeTransaction(state.data, state.selectedRentTimeMillis)
                    } else {
                        refreshBalance()
                    }
                }
            }
        }
    }

    private fun makeTransaction(data: DecodedAffairsData, selectedRentTimeMillis: Double) {
        _isMakingTransaction.value = true

        val doOnError = {
            _isMakingTransaction.value = false
            _uiEvents.tryEmit(SessionSettingsUiEvent.PaymentError)
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("shagaRentingActivity", "Button clicked, starting coroutine.")
            // Step 1
            val selectedRentTimeSeconds = (selectedRentTimeMillis / 1000).toULong()
            val currentTimeSeconds =
                (System.currentTimeMillis() / 1000).toULong() // Convert to ULong
            val rentalTerminationTimeSeconds =
                currentTimeSeconds + selectedRentTimeSeconds // Both are ULong
            Log.d(
                "shagaRentingActivity",
                "Calculated selectedRentTimeSeconds: $rentalTerminationTimeSeconds"
            )
            // Step 3
            val shagaTransactions = ShagaTransactions()
            val clientAccount = SolanaPreferenceManager.getStoredHotAccount()
            if (clientAccount == null) {
                doOnError()
                Log.e("shagaRentingActivity", "Failed to obtain fee payer account. Cannot proceed.")
                return@launch
            }
            Log.d(
                "shagaRentingActivity",
                "Obtained client, publicKey: ${clientAccount.publicKey.toBase58()}"
            )
            val secretKey: ByteArray = clientAccount.obtainSecretKey()
            val intermediateHotAccount: HotAccount = HotAccount(secretKey)
            // Step 4
            val rentalArgs = SolanaApi.StartRentalInstructionArgs(
                rentalTerminationTime = rentalTerminationTimeSeconds,
                privatePairHashCode = parsedPairingString!!.second
            )
            // Step 5
            val txInstruction = shagaTransactions.startRental(
                authority = data.authority,
                client = clientAccount.publicKey,
                args = rentalArgs
            )
            Log.d("shagaRentingActivity", "Authrotiyy ID: ${data.authority.toBase58()}")
            Log.d("shagaRentingActivity", "Generated transaction instruction.")
            // Step 6
            val recentBlockHashResult = SolanaApi.getRecentBlockHashFromApi()
            if (recentBlockHashResult.isFailure) {
                doOnError()
                Log.e("shagaRentingActivity", "Failed to fetch recent blockhash.")
                return@launch
            }
            val recentBlockHash = recentBlockHashResult.getOrNull()
            if (recentBlockHash == null) {
                doOnError()
                return@launch
            }
            Log.d("shagaRentingActivity", "Fetched recentBlockHash: $recentBlockHash")
            // Step 7
            val transaction = TransactionBuilder()
                .addInstruction(txInstruction)
                .setRecentBlockHash(recentBlockHash)
                .setSigners(listOf(intermediateHotAccount))
                .build()
            Log.d(
                "shagaRentingActivity",
                "Built transaction. Instructions count: ${transaction.instructions.size}"
            )

            val sendTransactionResult = SolanaApi.solana.api.sendTransaction(
                transaction = transaction,
                signers = listOf(intermediateHotAccount),
                recentBlockHash = recentBlockHash
            )

            if (sendTransactionResult.isFailure) {
                val exception = sendTransactionResult.exceptionOrNull() // Get the exception
                Log.e(
                    "shagaRentingActivity",
                    "Failed to send transaction: ${exception?.message}",
                    exception
                )
                doOnError()
                return@launch
            }

            val clientString = clientAccount.publicKey.toString()
            SolanaPreferenceManager.storeAuthority(data.authority.toString())

            _isMakingTransaction.value = false
            _uiEvents.tryEmit(SessionSettingsUiEvent.PaymentSuccess(data, clientString))
        }
    }

    enum class Step {
        SESSION_INFO,
        PAYMENT,
        NOT_ENOUGH_BALANCE
    }
}