package com.limelight.shagaProtocol

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.limelight.nvstream.http.NvApp
import com.limelight.nvstream.http.NvHTTP
import com.limelight.shagaProtocol.MapPopulation.NetworkUtils.fetchAppList
import com.limelight.solanaWallet.SolanaApi
import com.limelight.solanaWallet.SolanaPreferenceManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern
import kotlin.math.roundToLong
import java.util.concurrent.FutureTask
import java.util.concurrent.Callable


class MapPopulation() {
    data class Coordinates(val latitude: Double, val longitude: Double)
    data class MarkerProperties( // just SolanaApi.AffairsData + Latency field & Games icons
        val ipAddress: String,
        val coordinates: Coordinates,
        var latency: Long, // variable, because we can check the ping >1 time
        val gpuName: String,
        val cpuName: String,
        val sunshinePublicKey: String,
        val totalRamMb: UInt,
        val solPerHour: Double,
        val usdcPerHour: Double,
        val affairState: String,
        val affairStartTime: ULong,
        val affairTerminationTime: ULong,
        val appList: List<NvApp>,
        val gameIcons: List<Bitmap>
    )

    // Function to build marker properties, ip & latency are calculated in MapUtils.kt
    fun buildMarkerProperties(context: Context, affair: DecodedAffairsData, rate: Double): Result<MarkerProperties> {
        return MarkerUtils.buildMarkerProperties(context, affair, rate)
    }

    object NetworkUtils {
        // Create a shared OkHttpClient instance
        private val client = OkHttpClient()
        private val httpDefaultPort = NvHTTP.getDefaultHttpPort()


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


        fun fetchAppList(ipAddress: String): List<NvApp> {

            val url = "http://$ipAddress:$httpDefaultPort/shagaApplist"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                val appListRaw = response.body?.string() ?: ""
                Log.d("shagaMapActivityPopulation", "Fetched app list successfully.")
                return parseAppList(appListRaw)
            } catch (e: Exception) {
                Log.e("shagaMapActivityPopulation", "Failed to fetch app list.", e)
                e.printStackTrace()
                return emptyList()
            }
        }

        private fun parseAppList(xml: String): List<NvApp> {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            val apps = mutableListOf<NvApp>()
            var eventType = parser.eventType
            var currentApp: NvApp? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "App" -> currentApp = NvApp()
                            "IsHdrSupported" -> currentApp?.setHdrSupported(parser.nextText().toBoolean())
                            "AppTitle" -> currentApp?.setAppName(parser.nextText())
                            "ID" -> currentApp?.setAppId(parser.nextText())
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "App") {
                            currentApp?.let { apps.add(it) }
                        }
                    }
                }
                eventType = parser.next()
            }
            return apps
        }

        fun fetchGameIcon(ipAddress: String, appId: String): InputStream? {
            val url = "http://$ipAddress:$httpDefaultPort/shagaAppasset?appid=$appId&AssetType=2&AssetIdx=0"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                return response.body?.byteStream()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

    }


    object MarkerUtils {

        fun retryLatencyCheck(ipAddress: String, timeout: Long): Result<Long> {
            var latencyResult: Result<Long>  // Declare the latencyResult variable
            var retryCount = 0  // Counter for retry attempts

            // Loop to retry the latency check
            do {
                latencyResult = NetworkUtils.pingIpAddress(ipAddress, timeout)
                retryCount++
                Log.d("retryLatencyCheck", "Latency Result on attempt $retryCount: $latencyResult")
            } while (!latencyResult.isSuccess && retryCount < 3)  // Retry up to 3 times if not successful

            return latencyResult
        }

        fun buildMarkerProperties(context: Context, affair: DecodedAffairsData, rate: Double): Result<MarkerProperties> {
            // Use already decoded ipAddress
            val ipAddressString = affair.ipAddress

            // Extract coordinates directly from affair
            val coordinatesString = affair.coordinates
            val latLong = coordinatesString.split(',').map { it.trim().toDouble() }  // Split and convert to Double
            val latitude = latLong[0]
            val longitude = latLong[1]

            val coordinates = Coordinates(latitude, longitude)  // Use your Coordinates class here

            val timeout = SolanaPreferenceManager.getLatencySliderValue(context).toLong()
            val latencyResult = retryLatencyCheck(ipAddressString, timeout)

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
                val usdcPerHour = solPerHourInSol * rate // this is usdc/sol from coingecko
                val roundedUsdcPerHour = String.format("%.2f", usdcPerHour).toDouble()


                val appList = NetworkUtils.fetchAppList(ipAddressString)
                val gameIcons: List<Bitmap> = appList.mapNotNull { app ->
                    NetworkUtils.fetchGameIcon(ipAddressString, app.appId.toString())?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }

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
                    usdcPerHour = roundedUsdcPerHour,
                    affairState = affairStateString,
                    affairStartTime = affair.activeRentalStartTime,
                    affairTerminationTime = affair.affairTerminationTime,
                    appList = appList,
                    gameIcons = gameIcons
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
