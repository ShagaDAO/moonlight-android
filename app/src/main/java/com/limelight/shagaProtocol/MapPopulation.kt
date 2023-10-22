package com.limelight.shagaProtocol

import android.content.Context
import android.util.Log
import com.limelight.R
import com.limelight.solanaWallet.SolanaApi
import com.limelight.solanaWallet.SolanaPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern
import kotlin.math.roundToLong
import java.util.concurrent.FutureTask
import java.util.concurrent.Callable


class MapPopulation {
    data class Coordinates(val latitude: Double, val longitude: Double)
    data class MarkerProperties( // just SolanaApi.AffairsData + Latency field & Coordinates
        val ipAddress: String,
        val coordinates: Coordinates,
        val latency: Long,
        val gpuName: String,
        val cpuName: String,
        val sunshinePublicKey: String,
        val totalRamMb: UInt,
        val solPerHour: Double,
        val affairState: String,
        val affairStartTime: ULong,
        val affairTerminationTime: ULong
    )

    // Function to build marker properties, ip & latency are calculated in MapUtils.kt
    suspend fun buildMarkerProperties(context: Context, affair: DecodedAffairsData): Result<MarkerProperties> {
        return MarkerUtils.buildMarkerProperties(context, affair)
    }

    object NetworkUtils {
        // Create a shared OkHttpClient instance
        private val client = OkHttpClient()

        // Moved outside the function as a private const
        private const val IP_PATTERN = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$"

        fun isValidIpAddress(ipAddress: String): Boolean {
            val ipPattern = Pattern.compile(IP_PATTERN)
            return ipPattern.matcher(ipAddress).matches()
        }


        fun pingIpAddress(ipAddress: String, timeoutMs: Long): Result<Long> {
            try {
                // Step 1: Validate the IP address
                if (!isValidIpAddress(ipAddress)) {
                    throw IllegalArgumentException("IP address is invalid")
                }

                // Step 2: Execute the ping command
                val process = Runtime.getRuntime().exec("ping -c 1 $ipAddress")
                val future = FutureTask(Callable {
                    val buf = BufferedReader(InputStreamReader(process.inputStream))
                    var latency: Long? = null

                    buf.forEachLine { line ->
                        val timePos = line.indexOf("time=")
                        if (timePos != -1) {
                            val timeStr = line.substring(timePos + 5)
                            val endPos = timeStr.indexOf(" ")

                            if (endPos != -1) {
                                try {
                                    val latencyFloat = timeStr.substring(0, endPos).toFloat()
                                    val latencyLong = latencyFloat.roundToLong()
                                    latency = latencyLong
                                } catch (e: NumberFormatException) {
                                    Log.e("pingIpAddress", "Failed to convert latency to Long", e)
                                    throw e
                                }
                                return@forEachLine
                            }
                        }
                    }
                    latency
                })

                val executor = Executors.newSingleThreadExecutor()
                executor.execute(future)

                val latency = future.get(timeoutMs, TimeUnit.MILLISECONDS)  // 500ms timeout
                future.cancel(true) // cancel the future task

                // Step 4: Check if latency was extracted
                latency?.let {
                    Log.d("shagaMapActivityPopulation", "Parsed Latency: $it")
                    return Result.success(it)
                } ?: throw Exception("Failed to extract latency from ping output")

            } catch (e: TimeoutException) {
                // Log timeout and return failure
                Log.e("shagaMapActivityPopulation", "Ping timed out for IP: $ipAddress")
                return Result.failure(e)
            } catch (e: Exception) {
                // Log other errors and return failure
                Log.e("shagaMapActivityPopulation", "Ping failed for IP: $ipAddress", e)
                return Result.failure(e)
            }
        }


    }


    object MarkerUtils {


        suspend fun buildMarkerProperties(context: Context, affair: DecodedAffairsData): Result<MarkerProperties> {
            // Use already decoded ipAddress
            val ipAddressString = affair.ipAddress

            // Extract coordinates directly from affair
            val coordinatesString = affair.coordinates
            val latLong = coordinatesString.split(',').map { it.trim().toDouble() }  // Split and convert to Double
            val latitude = latLong[0]
            val longitude = latLong[1]

            val coordinates = Coordinates(latitude, longitude)  // Use your Coordinates class here


            val timeout = SolanaPreferenceManager.getLatencySliderValue(context).toLong()

            val latencyResult = NetworkUtils.pingIpAddress(ipAddressString, timeout)
            Log.d("buildMarkerProperties", "Latency Result: $latencyResult")

            if (latencyResult.isSuccess) {
                // Use already decoded cpuName and gpuName
                val cpuNameString = affair.cpuName
                val gpuNameString = affair.gpuName

                // Convert PublicKey to its string representation, if the class provides such a method.
                val authorityString = affair.authority.toString()  // Replace `toString()` with the actual method if available

                // Initialize the string representation of affairState directly from the enum
                var affairStateString = when (affair.affairState) {
                    SolanaApi.AffairState.Available -> "Available"
                    SolanaApi.AffairState.Unavailable -> "Unavailable"
                    else -> "UNKNOWN"  // Default to "UNKNOWN"
                }

                val solPerHourInSol = affair.solPerHour.toDouble()


                // Now build MarkerProperties
                val markerProperties = MarkerProperties(
                    ipAddress = ipAddressString,
                    coordinates = coordinates,
                    latency = latencyResult.getOrThrow(),
                    gpuName = gpuNameString,
                    cpuName = cpuNameString,
                    sunshinePublicKey = authorityString,
                    totalRamMb = affair.totalRamMb,
                    solPerHour = solPerHourInSol,
                    affairState = affairStateString,
                    affairStartTime = affair.activeRentalStartTime,
                    affairTerminationTime = affair.affairTerminationTime
                )

                Log.d("buildMarkerProperties", "Created MarkerProperties: $markerProperties") // Log the created MarkerProperties object

                return Result.success(markerProperties)
            } else {
                val failureReasons = mutableListOf<String>()
                latencyResult.exceptionOrNull()?.let { failureReasons.add("latency: ${it.message}") }

                return Result.failure(Exception("Failed to build marker properties due to: ${failureReasons.joinToString(", ")}"))
            }
        }



    }
}
