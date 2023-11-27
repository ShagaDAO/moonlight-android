package com.limelight.shagaProtocol

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limelight.AppView
import com.limelight.PcView
import com.limelight.R
import com.limelight.binding.PlatformBinding
import com.limelight.computers.ComputerManagerService
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvHTTP
import com.limelight.nvstream.http.PairingManager
import com.limelight.nvstream.jni.MoonBridge
import com.limelight.shaga.ui.connection.ConnectionScreen
import com.limelight.shaga.ui.connection.ConnectionScreenViewModel
import com.limelight.solanaWallet.EncryptionHelper
import com.limelight.utils.Dialog
import com.limelight.utils.ServerHelper
import com.limelight.utils.SpinnerDialog
import com.solana.core.PublicKey
import okio.FileNotFoundException
import okio.IOException
import org.xmlpull.v1.XmlPullParserException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException


class PairingActivity : ComponentActivity() {

    private var managerBinder: ComputerManagerService.ComputerManagerBinder? = null
    private var addThread: Thread? = null
    private var isPairingDone = false

    private val viewModel by viewModels<ConnectionScreenViewModel>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("shagaPairingActivity", "Service connected")
            managerBinder = service as ComputerManagerService.ComputerManagerBinder
            startAddThread()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("shagaPairingActivity", "Service disconnected")
            joinAddThread()
            managerBinder = null
        }
    }

    private fun isWrongSubnetSiteLocalAddress(address: String): Boolean {
        Log.d("shagaPairingActivity", "Checking subnet for address: $address")
        return try {
            val targetAddress = InetAddress.getByName(address)
            if (targetAddress !is Inet4Address || !targetAddress.isSiteLocalAddress) {
                Log.d("shagaPairingActivity", "Address is not an IPv4 or not a site local address")
                false
            } else {
                var foundMatchingInterface = false
                for (iface in NetworkInterface.getNetworkInterfaces()) {
                    for (addr in iface.interfaceAddresses) {
                        if (addr.address !is Inet4Address || !addr.address.isSiteLocalAddress) {
                            continue
                        }

                        val targetAddrBytes = targetAddress.address
                        val ifaceAddrBytes = addr.address.address

                        var addressMatches = true
                        for (i in 0 until addr.networkPrefixLength.toInt()) {
                            if (ifaceAddrBytes[i / 8].toInt() and (1 shl (i % 8)) !=
                                targetAddrBytes[i / 8].toInt() and (1 shl (i % 8))
                            ) {
                                addressMatches = false
                                break
                            }
                        }

                        if (addressMatches) {
                            Log.d("shagaPairingActivity", "Found matching interface")
                            foundMatchingInterface = true
                            break
                        }
                    }

                    if (foundMatchingInterface) break
                }

                if (!foundMatchingInterface) {
                    Log.d("shagaPairingActivity", "Did not find a matching interface")
                }

                !foundMatchingInterface
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.e("shagaPairingActivity", "Exception occurred: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun parseRawUserInputToUri(rawUserInput: String): URI? {
        try {
            val uri = URI("moonlight://$rawUserInput")
            if (uri.host != null && uri.host.isNotEmpty()) {
                return uri
            }
        } catch (ignored: URISyntaxException) {
        }

        try {
            val uri = URI("moonlight://[$rawUserInput]")
            if (uri.host != null && uri.host.isNotEmpty()) {
                return uri
            }
        } catch (ignored: URISyntaxException) {
        }

        return null
    }


    private fun doAddPc() {
        Log.d("shagaPairingActivity", "Entered doAddPc function")

        var wrongSiteLocal = false
        var invalidInput = false
        var success = false
        var portTestResult: Int // Initialize later, similar to Java

        val details = ComputerDetails() // Initialize ComputerDetails, same as Java

        try {
            // Fetch the IP address from the intent
            val rawUserInput = intent.getStringExtra("ipAddress") ?: ""
            Log.d("shagaPairingActivity", "Raw User Input: $rawUserInput") // Log raw user input

            // Parse raw user input to URI
            val uri = parseRawUserInputToUri(rawUserInput)
            Log.d("shagaPairingActivity", "Parsed URI: $uri") // Log parsed URI

            // Check if we parsed a host address successfully
            if (uri != null && !uri.host.isNullOrEmpty()) {
                val host = uri.host
                val port = uri.port.takeIf { it != -1 } ?: NvHTTP.DEFAULT_HTTP_PORT

                Log.d("shagaPairingActivity", "Parsed Host: $host") // Log parsed host
                Log.d("shagaPairingActivity", "Parsed Port: $port") // Log parsed port

                details.manualAddress = ComputerDetails.AddressTuple(host, port)
                success = managerBinder?.addComputerBlocking(details) ?: false

                Log.d(
                    "shagaPairingActivity",
                    "Add Computer Blocking Success: $success"
                ) // Log the 'success' flag

                if (!success) {
                    wrongSiteLocal = isWrongSubnetSiteLocalAddress(host)
                    Log.d(
                        "shagaPairingActivity",
                        "Wrong Site Local: $wrongSiteLocal"
                    ) // Log if it is a wrong site local address
                }
            } else {
                success = false
                invalidInput = true
                Log.d(
                    "shagaPairingActivity",
                    "Invalid Input or URI: $invalidInput"
                ) // Log invalid input flag
            }

        } catch (e: InterruptedException) { // InterruptedException catch block
            Log.e("shagaPairingActivity", "InterruptedException occurred: ${e.message}")
            throw e // Propagate the exception, same as Java
        } catch (e: IllegalArgumentException) { // IllegalArgumentException catch block
            Log.e("shagaPairingActivity", "IllegalArgumentException occurred: ${e.message}")
            e.printStackTrace()
            success = false
            invalidInput = true
        }

        // Keep the SpinnerDialog open while testing connectivity
        Log.d("shagaPairingActivity", "Checking connectivity conditions")
        if (!success && !wrongSiteLocal && !invalidInput) {
            Log.d("shagaPairingActivity", "Testing client connectivity")
            portTestResult = MoonBridge.testClientConnectivity(
                ServerHelper.CONNECTION_TEST_SERVER,
                443,
                MoonBridge.ML_PORT_FLAG_TCP_47984 or MoonBridge.ML_PORT_FLAG_TCP_47989
            )
        } else {
            Log.d("shagaPairingActivity", "Skipping client connectivity test")
            portTestResult = MoonBridge.ML_TEST_RESULT_INCONCLUSIVE
        }

        Log.d("shagaPairingActivity", "Dismissing spinner dialog")

        Log.d("shagaPairingActivity", "Evaluating conditions for displaying dialogs")
        when {
            invalidInput -> {
                Log.d("shagaPairingActivity", "Showing invalid input dialog")
                Dialog.displayDialog(
                    this,
                    getString(R.string.conn_error_title),
                    getString(R.string.addpc_unknown_host),
                    false
                )
            }

            wrongSiteLocal -> {
                Log.d("shagaPairingActivity", "Showing wrong site local dialog")
                Dialog.displayDialog(
                    this,
                    getString(R.string.conn_error_title),
                    getString(R.string.addpc_wrong_sitelocal),
                    false
                )
            }

            !success -> {
                Log.d("shagaPairingActivity", "Showing failure dialog")
                val dialogText =
                    if (portTestResult != MoonBridge.ML_TEST_RESULT_INCONCLUSIVE && portTestResult != 0) {
                        getString(R.string.nettest_text_blocked)
                    } else {
                        getString(R.string.addpc_fail)
                    }
                Dialog.displayDialog(
                    this,
                    getString(R.string.conn_error_title),
                    dialogText,
                    false
                )
            }

            else -> {
                Log.d(
                    "shagaPairingActivity",
                    "Operation successful, showing success toast and proceeding with pairing"
                )
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.addpc_success), Toast.LENGTH_LONG)
                        .show()
                    // Call publicDoPairShaga here
                    val pcViewInstance = PcView.getInstance()
                    if (pcViewInstance != null) {
                        val authority = intent.getStringExtra("authority")
                        authority?.let { PublicKey(it) }?.let {
                            doPairShaga(
                                details,
                                it
                            )
                        } // pass authority as publicKey if not null
                    } else {
                        Log.e(
                            "shagaPairingActivity",
                            "PcView instance is null. Cannot proceed with publicDoPairShaga."
                        )
                        Toast.makeText(
                            this,
                            "Failed to initiate pairing; internal error.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        Log.d("shagaPairingActivity", "Exiting doAddPc function")
    }


    private fun doPairShaga(computer: ComputerDetails, sunshinePublicKey: PublicKey) {
        // Logging entry into the function
        Log.d("shagaPairingActivity", "Entered doPairShaga")

        // Check if the computer is offline or active address is null
        if (computer.state == ComputerDetails.State.OFFLINE || computer.activeAddress == null) {
            Log.d("shagaPairingActivity", "Computer is OFFLINE or activeAddress is null")
            viewModel.updateUI(
                success = false,
                message = "Computer is OFFLINE or activeAddress is null"
            )
            return
        }

        // Check if managerBinder is null
        if (managerBinder == null) {
            Log.d("shagaPairingActivity", "managerBinder is null")
            viewModel.updateUI(
                success = false,
                message = "Manager Binder is null"
            )
            return
        }
        // Initiate a new thread for the pairing logic
        Thread {
            var message: String? = null
            var success = false

            try {
                // Logging the stopping of computer updates
                Log.d("shagaPairingActivity", "Stopping computer updates")
                PcView.publicStopComputerUpdates(true)

                // Initialize NvHTTP object
                val httpConn = NvHTTP(
                    ServerHelper.getCurrentAddressFromComputer(computer),
                    computer.httpsPort,
                    managerBinder!!.getUniqueId(),
                    computer.serverCert,
                    PlatformBinding.getCryptoProvider(this@PairingActivity) // Using PairingActivity's context
                )
                // Check the current pair state
                when (httpConn.getPairState()) {
                    PairingManager.PairState.PAIRED -> {
                        Log.d("shagaPairingActivity", "Already Paired")
                        message = null
                        success = true
                    }

                    else -> {
                        // Generate a PIN string for pairing
                        val pinStr = PairingManager.generatePinString()
                        Log.d("shagaPairingActivity", "Generated PIN String: $pinStr")

                        // Initialize PairingManager
                        val pm = httpConn.getPairingManager()
                        Log.d(
                            "shagaPairingActivity",
                            "PairingManager initialized: $pm"
                        )  // Debug log to check if PairingManager is initialized correctly

                        // Convert the public key
                        val ed25519PublicKey = sunshinePublicKey.toByteArray()
                        Log.d(
                            "shagaPairingActivity",
                            "ED25519 Public Key: ${ed25519PublicKey.joinToString(", ") { it.toString() }}"
                        )  // Debug log to check the ED25519 Public Key

                        val x25519PublicKey =
                            EncryptionHelper.mapPublicEd25519ToX25519(ed25519PublicKey)
                        Log.d(
                            "shagaPairingActivity",
                            "X25519 Public Key: ${x25519PublicKey.joinToString(", ") { it.toString() }}"
                        )  // Debug log to check the X25519 Public Key

                        // Get Server Info
                        val serverInfo = httpConn.getServerInfo(true)
                        Log.d(
                            "shagaPairingActivity",
                            "Server Info: $serverInfo"
                        )  // Debug log to check the Server Info

                        // Execute the pairing process
                        val pairState = pm.publicPairShaga(serverInfo, pinStr, x25519PublicKey)
                        Log.d(
                            "shagaPairingActivity",
                            "Pairing State: $pairState"
                        )  // Debug log to check the Pairing State


                        // Determine the result of the pairing process
                        message = when (pairState) {
                            PairingManager.PairState.PIN_WRONG -> "Incorrect PIN"
                            PairingManager.PairState.FAILED -> if (computer.runningGameId != 0) "Computer is in-game" else "Pairing failed"
                            PairingManager.PairState.ALREADY_IN_PROGRESS -> "Pairing already in progress"
                            PairingManager.PairState.PAIRED -> {
                                managerBinder!!.getComputer(computer.uuid).serverCert =
                                    pm.getPairedCert()
                                managerBinder!!.invalidateStateForComputer(computer.uuid)
                                null
                            }

                            else -> null
                        }
                        success = (pairState == PairingManager.PairState.PAIRED)
                        Log.d(
                            "shagaPairingActivity",
                            "Pairing state: $pairState, Message: $message"
                        )
                    }
                }
            } catch (e: UnknownHostException) {
                Log.e("shagaPairingActivity", "UnknownHostException: ${e.message}")
                message = "Unknown Host"
            } catch (e: FileNotFoundException) {
                Log.e("shagaPairingActivity", "FileNotFoundException: ${e.message}")
                message = "File Not Found"
            } catch (e: XmlPullParserException) {
                Log.e("shagaPairingActivity", "XmlPullParserException: ${e.message}")
                message = e.message
            } catch (e: IOException) {
                Log.e("shagaPairingActivity", "IOException: ${e.message}")
                message = e.message
            } finally {
                isPairingDone = true
                runOnUiThread {
                    if (success) {
                        // Pairing was successful, now call the function to display the App list
                        doShagaAppList(
                            computer,
                            true,
                            false
                        )  // Assuming doAppList is accessible here
                        viewModel.updateUI(success = true, message = "Pairing successful")
                    } else {
                        viewModel.updateUI(success = false, message = message ?: "Unknown error")
                        if (!isFinishing) {
                            finish()
                        }
                    }
                }
                Log.d(
                    "shagaPairingActivity",
                    "Exiting Thread with Success=$success, Message=$message"
                )
            }
        }.start()
    }


    private fun doShagaAppList(
        computer: ComputerDetails?,
        newlyPaired: Boolean,
        showHiddenGames: Boolean
    ) {
        // Add logs for debugging
        Log.d("ShagaAppList", "Entered doShagaAppList")
        Log.d("ShagaAppList", "Computer details: ${computer?.toString()}")
        Log.d("ShagaAppList", "managerBinder: $managerBinder")
        // Check if computer object is null
        if (computer == null) {
            Log.d("ShagaAppList", "Computer object is null")
            return
        }
        // Check if the computer is offline
        if (computer.state == ComputerDetails.State.OFFLINE) {
            Toast.makeText(this, getString(R.string.error_pc_offline), Toast.LENGTH_SHORT).show()
            return
        }
        // Check if managerBinder is null
        if (managerBinder == null) {
            Toast.makeText(this, getString(R.string.error_manager_not_running), Toast.LENGTH_LONG)
                .show()
            return
        }
        // Create an intent to start AppView activity
        if (computer.name != null && computer.uuid != null) {
            val intent = Intent(this, AppView::class.java).apply {
                putExtra(AppView.NAME_EXTRA, computer.name)
                putExtra(AppView.UUID_EXTRA, computer.uuid)
                putExtra(AppView.NEW_PAIR_EXTRA, newlyPaired)
                putExtra(AppView.SHOW_HIDDEN_APPS_EXTRA, showHiddenGames)
            }
            // Add logs for debugging
            Log.d("ShagaAppList", "Starting AppView activity")
            // Start the AppView activity
            startActivity(intent)
        } else {
            Log.d("ShagaAppList", "Computer name or UUID is null")
        }
    }


    private fun startAddThread() {
        Log.d("shagaPairingActivity", "Starting add thread")
        addThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Log.d("shagaPairingActivity", "Calling doAddPc from startAddThread")
                    doAddPc()
                    Log.d("shagaPairingActivity", "Successfully executed doAddPc, breaking loop")
                    break
                } catch (e: InterruptedException) {
                    Log.e("shagaPairingActivity", "Thread interrupted: ${e.message}")
                    return@Thread
                }
            }
        }.apply {
            name = "UI - PairingActivity"
            Log.d("shagaPairingActivity", "Starting thread with name: $name")
            start()
        }
    }

    private fun joinAddThread() {
        Log.d("shagaPairingActivity", "Entering joinAddThread()")
        if (addThread != null) {
            Log.d("shagaPairingActivity", "Interrupting addThread")
            addThread?.interrupt()

            try {
                Log.d("shagaPairingActivity", "Joining addThread")
                addThread?.join()
            } catch (e: InterruptedException) {
                Log.e("shagaPairingActivity", "InterruptedException while joining addThread", e)
                e.printStackTrace()
                // Since we can't handle the InterruptedException here,
                // we will re-interrupt the thread to set the interrupt status back to true.
                Thread.currentThread().interrupt()
            }

            Log.d("shagaPairingActivity", "Setting addThread to null")
            addThread = null
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("shagaPairingActivity", "onStop() called")
        // Close dialogs when the activity stops
        Dialog.closeDialogs()
        SpinnerDialog.closeDialogs(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("shagaPairingActivity", "onDestroy() called")
        // Unbind from the service and join the thread
        if (managerBinder != null) {
            Log.d("shagaPairingActivity", "Unbinding service and joining thread")
            joinAddThread()
            unbindService(serviceConnection)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("shagaPairingActivity", "onCreate() called")

        // Retrieve the clientAccount, authority, and IP address from the intent
        val clientAccount = intent.getStringExtra("clientAccount")
        val ipAddress = intent.getStringExtra("ipAddress")
        val authority = intent.getStringExtra("authority")

        if (authority != null) {
            isPairingDone = false
            // Log the retrieved values for debugging
            Log.d(
                "shagaPairingActivity",
                "Received clientAccount: $clientAccount, authority: $authority, and ipAddress: $ipAddress"
            )

            // Bind to the ComputerManagerService
            Log.d("shagaPairingActivity", "Binding to ComputerManagerService")
            bindService(
                Intent(this, ComputerManagerService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        } else {
            // Handle the null case for authority
            Log.e("shagaPairingActivity", "Authority is null. Cannot proceed.")
            // You could also update the UI here to inform the user
            viewModel.updateUI(false, "Authority is missing. Cannot proceed.")
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ConnectionScreen(uiState, onRetryClick = { doAddPc() })
        }
    }


    companion object {
        fun start(context: Context, clientString: String, ipAddress: String, authority: String) {
            val intent = Intent(context, PairingActivity::class.java)
            intent.putExtra("clientAccount", clientString)
            intent.putExtra("ipAddress", ipAddress)
            intent.putExtra("authority", authority)
            context.startActivity(intent)
        }
    }
}
