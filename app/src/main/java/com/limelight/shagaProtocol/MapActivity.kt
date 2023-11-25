package com.limelight.shagaProtocol

 import android.Manifest
 import android.annotation.SuppressLint
 import android.content.Intent
 import android.content.pm.PackageManager
 import android.graphics.Bitmap
 import android.graphics.BitmapFactory
 import android.graphics.Canvas
 import android.graphics.Color
 import android.graphics.Paint
 import android.graphics.Rect
 import android.os.Build
 import android.os.Bundle
 import android.util.Log
 import android.view.LayoutInflater
 import android.view.MotionEvent
 import android.view.View
 import android.view.ViewManager
 import android.view.ViewTreeObserver
 import android.widget.Button
 import android.widget.FrameLayout
 import android.widget.TextView
 import android.widget.Toast
 import androidx.annotation.RequiresApi
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.app.ActivityCompat
 import androidx.lifecycle.lifecycleScope
 import androidx.recyclerview.widget.LinearLayoutManager
 import androidx.recyclerview.widget.RecyclerView
 import com.google.android.gms.location.FusedLocationProviderClient
 import com.google.android.gms.location.LocationServices
 import com.google.android.material.floatingactionbutton.FloatingActionButton
 import com.google.android.material.slider.Slider
 import com.limelight.R
 import com.limelight.solanaWallet.SolanaApi
 import com.limelight.solanaWallet.SolanaApi.solana
 import com.limelight.solanaWallet.SolanaPreferenceManager
 import com.limelight.solanaWallet.WalletActivity
 import com.mapbox.geojson.Point
 import com.mapbox.maps.CameraOptions
 import com.mapbox.maps.EdgeInsets
 import com.mapbox.maps.MapView
 import com.mapbox.maps.MapboxMap
 import com.mapbox.maps.Style
 import com.mapbox.maps.ViewAnnotationOptions
 import com.mapbox.maps.plugin.annotation.annotations
 import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
 import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
 import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
 import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
 import com.mapbox.maps.plugin.gestures.OnMapClickListener
 import com.mapbox.maps.plugin.locationcomponent.location
 import com.mapbox.maps.viewannotation.ViewAnnotationManager
 import com.solana.api.AccountInfo
 import com.solana.api.AccountInfoSerializer
 import com.solana.api.getAccountInfo
 import com.solana.api.getMultipleAccountsInfo
 import com.solana.core.PublicKey
 import com.solana.networking.serialization.serializers.base64.BorshAsBase64JsonArraySerializer
 import com.solana.networking.serialization.serializers.solana.AnchorAccountSerializer
 import kotlinx.coroutines.CoroutineScope
 import kotlinx.coroutines.Deferred
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.async
 import kotlinx.coroutines.awaitAll
 import kotlinx.coroutines.launch
 import kotlinx.coroutines.withContext
 import org.bitcoinj.core.Base58
 import org.json.JSONObject
 import java.io.BufferedReader
 import java.io.InputStreamReader
 import java.lang.Math.sqrt
 import java.net.HttpURLConnection
 import java.net.URL
 import java.text.SimpleDateFormat
 import java.util.Date
 import java.util.Locale
 import kotlin.math.pow


/* simplified flow:
Map initializes.
User clicks "Populate Map."
Solana-Fetch the list of Affairs asynchronously.
Deserialize the data.
Populate the map with markers.
Enable or activate the map click listener.
*/

const val REAL_TIME = "Real-Time"  // Lowest Latency
const val FAST_PACED = "Fast-Paced" // Moderate Latency
const val BALANCED = "Balanced" // High Latency but balanced
const val STRATEGY_FOCUS = "Strategy Focus" // Very High Latency, suitable for strategy games
const val CINEMATIC = "Cinematic" // Extreme Latency, prioritizing visual quality
const val CUSTOM = "Custom" // User-Defined


fun fetchSolPriceInUSDC(): Double {
    var urlConnection: HttpURLConnection? = null
    try {
        val url = URL("https://api.coingecko.com/api/v3/simple/price?ids=solana&vs_currencies=usd")
        urlConnection = url.openConnection() as HttpURLConnection
        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))

        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        val response = stringBuilder.toString()

        val jsonObject = JSONObject(response)
        return jsonObject.getJSONObject("solana").getDouble("usd")
    } catch (e: Exception) {
        e.printStackTrace()
        throw RuntimeException("Failed to fetch Solana price from CoinGecko.")
    } finally {
        urlConnection?.disconnect()
    }
}

class MapActivity : AppCompatActivity(), OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapboxMap: MapboxMap
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val annotationToPropertiesMap: MutableMap<PointAnnotation, MapPopulation.MarkerProperties> = mutableMapOf()
    private var userLocation: Point? = null
    private var solToUsdcRate: Double? = null
    private var lastButtonPressTime: Long? = null
    private val annotationToViewMap: MutableMap<PointAnnotation, View> = mutableMapOf()
    private val processedAuthorities = HashSet<PublicKey>()



    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        SolanaPreferenceManager.initialize(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                solToUsdcRate = fetchSolPriceInUSDC()
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                }
            }
        }

        mapView = findViewById(R.id.mapView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {

            mapboxMap = mapView.getMapboxMap() // this initializes the map
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
            viewAnnotationManager = mapView.viewAnnotationManager
            // Add custom images for markers here
            it.addImage("Lenders", BitmapFactory.decodeResource(resources, R.drawable.gaming_pc))


            // Make the points clickable & show view annotation when clicked
            pointAnnotationManager.addClickListener { clickedAnnotation ->
                // Retrieve the MarkerProperties associated with this PointAnnotation
                val markerProperties = annotationToPropertiesMap[clickedAnnotation]
                // If MarkerProperties exist for this PointAnnotation, proceed
                if (markerProperties != null) {
                    // Add or show the ViewAnnotation using the clicked PointAnnotation and associated MarkerProperties
                    addViewAnnotationWithProperties(clickedAnnotation, markerProperties)
                }
                true // Return true to indicate that we've handled this click event

            }

            onMapReady()

            initializeLocation() // initialize location

            initializeLocationPuck() // this initializes the location

            setUpSlider()

        }

        // Moved button initialization to its own function
        initializePopulateMapButton()

        initializeRefreshLatencyButton()

        initializeBackButton()

        // Initialize new Go To Wallet Activity Button
        val goToWalletActivityButton: Button = findViewById(R.id.goToWalletActivityButton)
        goToWalletActivityButton.setOnClickListener {
            val intent = Intent(this@MapActivity, WalletActivity::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpSlider() {
        val latencySlider: Slider = findViewById(R.id.latencySlider)
        val sliderLabel: TextView = findViewById(R.id.sliderLabel)

        val storedSliderValue = SolanaPreferenceManager.getLatencySliderValue(this)
        latencySlider.value = storedSliderValue

        sliderLabel.text = when (storedSliderValue) {
            50f -> REAL_TIME
            100f -> FAST_PACED
            150f -> BALANCED
            200f -> STRATEGY_FOCUS
            250f -> CINEMATIC
            else -> CUSTOM
        }

        latencySlider.setOnTouchListener { v, event ->
            val location = IntArray(2)
            latencySlider.getLocationOnScreen(location)
            val rect = Rect(location[0], location[1],
                location[0] + latencySlider.width, location[1] + latencySlider.height)

            if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                return@setOnTouchListener false
            }

            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            false
        }

        latencySlider.setLabelFormatter { value: Float ->
            "${value.toInt()} ms"
        }


        latencySlider.addOnChangeListener { slider, value, fromUser ->
            Log.d("SliderValue", "Slider Value: $value")
            SolanaPreferenceManager.storeLatencySliderValue(value)
            sliderLabel.text = when (value) {
                50f -> REAL_TIME
                100f -> FAST_PACED
                150f -> BALANCED
                200f -> STRATEGY_FOCUS
                250f -> CINEMATIC
                else -> CUSTOM
            }
        }

        latencySlider.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Ensure this listener is removed so that it doesn't get called multiple times
                latencySlider.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val location = IntArray(2)
                latencySlider.getLocationOnScreen(location)

                val textX = 16  // 16 pixels from the left edge of the screen
                val textY = location[1] + latencySlider.height - 350  // Adjusted height

                sliderLabel.x = textX.toFloat()
                sliderLabel.y = textY.toFloat()
            }
        })
    }


    private fun createCompositeBitmap(baseBitmap: Bitmap, latency: String): Bitmap {
        val newBitmap = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, baseBitmap.config)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        // Create a paint for the text
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 250f
        textPaint.isAntiAlias = true

        // Create a paint for the text stroke
        val strokePaint = Paint()
        strokePaint.color = Color.BLACK
        strokePaint.textSize = 250f
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 50f
        strokePaint.isAntiAlias = true

        // Calculate the position to place the 'ms' text at the bottom right of the image
        val msXPos = baseBitmap.width - 450f // Fixed X position for " ms", moved 100f units to the left
        val msYPos = baseBitmap.height - 120f // Fixed Y position for " ms"

        // Draw 'ms' first
        canvas.drawText(" ms", msXPos, msYPos, strokePaint)
        canvas.drawText(" ms", msXPos, msYPos, textPaint)

        // Calculate the position to place the latency number right next to 'ms'
        val numberWidth = textPaint.measureText(latency)
        val numberXPos = msXPos - numberWidth // Position for the latency number, it's to the left of " ms"

        // Draw the latency number
        canvas.drawText(latency, numberXPos, msYPos, strokePaint)
        canvas.drawText(latency, numberXPos, msYPos, textPaint)

        return newBitmap
    }

    private fun addGamingPCMarkerWithProperties(marker: MapPopulation.MarkerProperties): PointAnnotation {

        // Create the composite bitmap using latency
        val baseBitmap = BitmapFactory.decodeResource(resources, R.drawable.gaming_pc)
        val compositeBitmap = createCompositeBitmap(baseBitmap, "${marker.latency}")

        val uniqueIconID = "Lenders_${marker.sunshinePublicKey}"

        mapboxMap.getStyle { it.addImage(uniqueIconID, compositeBitmap) }

        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(marker.coordinates.longitude, marker.coordinates.latitude))
            .withIconImage(uniqueIconID)
            .withIconSize(0.2)
        val createdAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
        annotationToPropertiesMap[createdAnnotation] = marker

        return createdAnnotation
    }

    private fun addViewAnnotationWithProperties(pointAnnotation: PointAnnotation, markerProperties: MapPopulation.MarkerProperties) {
        if (::pointAnnotationManager.isInitialized) {

            val layoutInflater = LayoutInflater.from(this)
            val view = layoutInflater.inflate(R.layout.view_annotation_item_callout, null)
            // Step 1: Identify the Button within the Inflated View
            val startRentingButton: Button = view.findViewById(R.id.startRentingButton)

            // Step 2: Add OnClickListener
            startRentingButton.setOnClickListener {
                val intent = Intent(this@MapActivity, RentingActivity::class.java)
                intent.putExtra("sunshinePublicKey", markerProperties.sunshinePublicKey)
                intent.putExtra("latency", markerProperties.latency)
                solToUsdcRate?.let { rate ->
                    intent.putExtra("solToUsdcRate", rate)
                }
                Log.d("shagaMapActivity", "Sending sunshinePublicKey: ${markerProperties.sunshinePublicKey}")
                startActivity(intent)
            }

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            view.layoutParams = layoutParams

            if (!populateViewAnnotationWithMarkerProperties(view, markerProperties)) {
                Log.e("ViewPopulationError", "Failed to populate the view annotation.")
                return
            }

            // Add the view to the annotationToViewMap, used for when we need to update the latency field
            annotationToViewMap[pointAnnotation] = view

            val options = ViewAnnotationOptions.Builder()
                .geometry(pointAnnotation.geometry)
                .build()

            try {
                viewAnnotationManager.addViewAnnotation(view, options)
            } catch (e: NullPointerException) {
                Log.e("ViewAnnotationError", "Caught NullPointerException: ${e.message}")
            }
        } else {
            Log.e("InitializationError", "pointAnnotationManager or viewAnnotationManager is not initialized.")
        }
    }


    private fun populateViewAnnotationWithMarkerProperties(view: View, markerProperties: MapPopulation.MarkerProperties): Boolean {
        return try {

            // Populate the recycler view with game icons
            val gameIconRecyclerView: RecyclerView = view.findViewById(R.id.gameIconRecyclerView)
            gameIconRecyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            val adapter = GameIconAdapter(markerProperties.gameIcons)
            gameIconRecyclerView.adapter = adapter

            // Populate Latency
            val latencyTextView: TextView = view.findViewById(R.id.latency)
            latencyTextView.text = "Latency: ${markerProperties.latency} ms"

            view.findViewById<TextView>(R.id.gpuName).text = "GPU Model: ${markerProperties.gpuName}"
            view.findViewById<TextView>(R.id.cpuName).text = "CPU Model: ${markerProperties.cpuName}"
            val totalRamGb = markerProperties.totalRamMb.toDouble() / 1024.0
            view.findViewById<TextView>(R.id.totalRamGb).text = "Total RAM: ${String.format("%.2f", totalRamGb)} GB"

            view.findViewById<TextView>(R.id.usdcPerHour).text = "Cost per hour: ${markerProperties.usdcPerHour} $"

            val affairTerminationTimeInMillis = markerProperties.affairTerminationTime.toLong() * 1000
            val affairTerminationDate = Date(affairTerminationTimeInMillis)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            view.findViewById<TextView>(R.id.affairTerminationTime).text = "Rent available until: ${format.format(affairTerminationDate)}"

            view.findViewById<TextView>(R.id.sunshinePublicKey).text = "Server's ID: ${markerProperties.sunshinePublicKey}"

            val closeButton: Button = view.findViewById(R.id.closeButton)
            closeButton.setOnClickListener {
                (view.parent as? ViewManager)?.removeView(view)
            }

            true
        } catch (e: Exception) {
            Log.e("ViewPopulationError", "Error populating view annotation: ${e.message}")
            false
        }
    }

    private fun initializeLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                Log.d("shagaMapActivity", "Success Listener Triggered")
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    userLocation = Point.fromLngLat(lon, lat)
                    mapView.getMapboxMap().setCamera(
                        com.mapbox.maps.CameraOptions.Builder()
                            .center(Point.fromLngLat(lon, lat))
                            .zoom(12.0)
                            .build()
                    )
                }
            }.addOnFailureListener { e ->
                Log.d("shagaMapActivity", "Failed to get location: $e")
            }
        }
    }


    private fun initializeLocationPuck() {
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocation()
            } else {
                // Handle permission denial
                Log.d("shagaMapActivity", "Location permission denied")
            }
        }
    }


   private fun initializeRefreshLatencyButton() {
        val refreshLatencyButton: FloatingActionButton = findViewById(R.id.refreshLatencyButton)

        refreshLatencyButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val lastTime = lastButtonPressTime ?: 0L

            // Check if 3 or more seconds have passed since the last button press
            if (currentTime - lastTime >= 3000) {
                // Update the lastButtonPressTime
                lastButtonPressTime = currentTime
                // Launch a coroutine to call the suspend function
                lifecycleScope.launch(Dispatchers.Main) {
                    updateLatenciesAndViews()
                }

            } else {
                // Notify the user that they need to wait
                Toast.makeText(this, "Please wait for 3 seconds before refreshing again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun updateLatenciesAndViews() {
        val deferreds: MutableList<Deferred<Unit>> = mutableListOf()

        // Create and collect Deferred objects for each operation
        annotationToPropertiesMap.forEach { (pointAnnotation, markerProperties) ->
            val deferred = CoroutineScope(Dispatchers.IO).async {
                try {
                    val timeout = SolanaPreferenceManager.getLatencySliderValue(this@MapActivity).toLong()
                    val latencyResult = MapPopulation.MarkerUtils.retryLatencyCheck(markerProperties.ipAddress, timeout)

                    if (latencyResult.isSuccess) {
                        val newLatency = latencyResult.getOrNull() ?: return@async // exit if null
                        val updatedMarkerProperties = markerProperties.copy(latency = newLatency)

                        annotationToPropertiesMap[pointAnnotation] = updatedMarkerProperties

                        withContext(Dispatchers.Main) {
                            updateViewAnnotation(pointAnnotation, updatedMarkerProperties)
                            updateMarkerIcon(pointAnnotation, updatedMarkerProperties)
                        }
                    } else {
                        Log.e("PingError", "Failed to update latency for IP: ${markerProperties.ipAddress}")
                    }
                } catch (e: Exception) {
                    Log.e("CoroutineError", "An error occurred in coroutine", e)
                }
            }
            deferreds.add(deferred)
        }

        // Wait for all operations to complete in the IO dispatcher context
        withContext(Dispatchers.IO) {
            deferreds.awaitAll()
        }
    }

    private fun updateViewAnnotation(pointAnnotation: PointAnnotation, markerProperties: MapPopulation.MarkerProperties) {
        val view = annotationToViewMap[pointAnnotation] ?: return
        val latencyTextView: TextView = view.findViewById(R.id.latency)
        latencyTextView.text = "Latency: ${markerProperties.latency} ms"
    }

    private fun updateMarkerIcon(pointAnnotation: PointAnnotation, markerProperties: MapPopulation.MarkerProperties) {
        // Step 1: Create a new composite bitmap with updated latency
        val baseBitmap = BitmapFactory.decodeResource(resources, R.drawable.gaming_pc)
        val updatedCompositeBitmap = createCompositeBitmap(baseBitmap, "${markerProperties.latency}")
        // Step 2: Add the new bitmap to the map style, replacing the old one
        val uniqueIconID = "Lenders_${markerProperties.sunshinePublicKey}"
        mapboxMap.getStyle { it.addImage(uniqueIconID, updatedCompositeBitmap) }
        // Step 3: Create new PointAnnotationOptions
        val newPointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(pointAnnotation.point)  // Preserve the existing point
            .withIconImage(uniqueIconID)       // Set the new image
            .withIconSize(0.2)
        // Step 4: Delete the old PointAnnotation
        pointAnnotationManager.delete(pointAnnotation)
        // Step 4.1: Remove the old annotation from annotationToPropertiesMap
        annotationToPropertiesMap.remove(pointAnnotation)
        // Step 5: Create a new PointAnnotation with updated options
        val newPointAnnotation = pointAnnotationManager.create(newPointAnnotationOptions)
        // Step 6: Update the mapping in annotationToPropertiesMap
        annotationToPropertiesMap[newPointAnnotation] = markerProperties
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializePopulateMapButton() {
        val button = findViewById<FloatingActionButton>(R.id.populateMapButton)

        button.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val lastTime = lastButtonPressTime ?: 0L

            // Check if 3 or more seconds have passed since the last button press
            if (currentTime - lastTime >= 3000) {
                // Update the lastButtonPressTime
                lastButtonPressTime = currentTime

                Log.d("shagaMapActivity", "Populate Map Button Clicked")
            val programAddress = getString(R.string.program_address)

            val affairsListAddressPubkeyPair = ShagaTransactions.ProgramAddressHelper.findAffairList(PublicKey(programAddress))
            val affairsListAddressPubkey = affairsListAddressPubkeyPair.first


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("shagaMapActivity", "Fetching the AffairsListData")
                    Log.d("shagaMapActivity", "Public Key: $affairsListAddressPubkey")

                    val result: AccountInfo<SolanaApi.AffairsListData?>? = try {
                        solana.api.getAccountInfo(
                            serializer = AccountInfoSerializer(BorshAsBase64JsonArraySerializer(AnchorAccountSerializer(
                                "AffairsListData", SolanaApi.AffairsListData.serializer()))),
                            account = affairsListAddressPubkey
                        ).getOrThrow()
                    } catch (e: kotlinx.serialization.SerializationException) {
                        Log.e("shagaMapActivity", "Serialization Exception: ${e.message}")
                        null
                    } catch (e: java.io.IOException) {
                        Log.e("shagaMapActivity", "IO Exception: ${e.message}")
                        null
                    } catch (e: Exception) {
                        Log.e("shagaMapActivity", "Generic Exception: ${e.message}")
                        null
                    }

                    // Log the result for debugging
                    if (result != null) {
                        Log.d("shagaMapActivity", "Result: $result")
                    } else {
                        Log.e("shagaMapActivity", "Result is null")
                    }

                    // Check if data is null or empty
                    val affairsListData: AccountInfo<SolanaApi.AffairsListData?>? = result
                    if (affairsListData?.data == null) {
                        Log.e("shagaMapActivity", "Affairs list data is null.")
                        return@launch
                    }

                    // Unwrap the data field from AccountInfo to get SolanaApi.AffairsListData
                    val actualAffairsListData: SolanaApi.AffairsListData = affairsListData.data!!

                    Log.d("shagaMapActivity", "Size of serialized AffairsListData: ${actualAffairsListData.toString().length}")

                    Log.d("shagaMapActivity", "Extracting the list of public keys")

                    // Extract the list of public keys
                    val publicKeys: List<PublicKey> = actualAffairsListData.activeAffairs

                    Log.d("shagaMapActivity", "Size of the list of public keys: ${publicKeys.size}")

                    publicKeys.forEach {
                        Log.d("shagaMapActivity", "Public key: $it")
                    }

                    // Heap size logging
                    Log.d("shagaMapActivity", "Available heap size: ${Runtime.getRuntime().freeMemory()}")
                    Log.d("shagaMapActivity", "Max heap size: ${Runtime.getRuntime().maxMemory()}")

                    // Fetch multiple accounts' data
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("shagaMapActivity", "Fetching multiple accounts")
                            // Fetch multiple accounts
                            val multipleAccountsResult = solana.api.getMultipleAccountsInfo(
                                serializer = SolanaApi.AffairsData.serializer(),
                                accounts = publicKeys
                            )

                            // Check for success
                            if (multipleAccountsResult.isSuccess) {
                                val multipleAccountsData = multipleAccountsResult.getOrNull()

                                // If the data is null, log an error and return
                                if (multipleAccountsData == null) {
                                    Log.e("shagaMapActivity", "Multiple accounts data is null.")
                                    return@launch
                                }

                                Log.d("shagaMapActivity", "Filtering and extracting data")

                                // Filter out nulls and extract data
                                val affairsDataList: List<SolanaApi.AffairsData> =
                                    multipleAccountsData.filterNotNull().mapNotNull { it.data }

                                // If the list is empty, log an error and return
                                if (affairsDataList.isEmpty()) {
                                    Log.e("shagaMapActivity", "No valid AffairsData found.")
                                    return@launch
                                }

                                Log.d("shagaMapActivity", "Saving the fetched AffairsData to local HashMap")
                                val lamportsToSolConversionRate: ULong = 1_000_000_000uL  // 1 Sol = 1,000,000,000 Lamports


                                // Filter out affairs that are already in the HashMap.
                                val filteredAffairsDataList = affairsDataList.filter { affairData ->
                                    !processedAuthorities.contains(affairData.authority)
                                }

                                // Data transformation & local temporary storage
                                val deferreds = filteredAffairsDataList.map { affairData ->
                                    async(Dispatchers.IO) {
                                        affairData.let { nonNullData ->
                                            nonNullData.authority.let { authority: PublicKey ->  // Explicitly specify the type

                                                val authorityBytes =
                                                    authority.toByteArray()  // Convert PublicKey to byte array
                                                if (authorityBytes.isNotEmpty()) {  // Check for null or empty
                                                    val authorityKey =
                                                        Base58.encode(authorityBytes)  // Use Base58 encoding

                                                    // Since ipAddress, cpuName, and gpuName are now Strings, we can use them directly.
                                                    val coordinatesString = nonNullData.coordinates
                                                    val ipAddressString =
                                                        nonNullData.ipAddress ?: ""
                                                    val cpuNameString = nonNullData.cpuName ?: ""
                                                    val gpuNameString = nonNullData.gpuName ?: ""

                                                    // Create a new DecodedAffairsData object with decoded and other values
                                                    val decodedData = DecodedAffairsData(
                                                        authorityKey = authorityKey,
                                                        authority = nonNullData.authority,
                                                        client = nonNullData.client,
                                                        rental = nonNullData.rental,
                                                        coordinates = coordinatesString,
                                                        ipAddress = ipAddressString,
                                                        cpuName = cpuNameString,
                                                        gpuName = gpuNameString,
                                                        totalRamMb = nonNullData.totalRamMb,
                                                        solPerHour = nonNullData.solPerHour.toDouble() / lamportsToSolConversionRate.toDouble(),
                                                        affairState = nonNullData.affairState,  // Assuming affairState is an enum or similar directly usable type
                                                        affairTerminationTime = nonNullData.affairTerminationTime,
                                                        activeRentalStartTime = nonNullData.activeRentalStartTime,
                                                        dueRentAmount = nonNullData.dueRentAmount
                                                    )

                                                    // Store the decoded data in the HashMap
                                                    AffairsDataHolder.affairsMap[authorityKey] = decodedData
                                                }
                                            }
                                        }
                                    }
                                }
                                deferreds.awaitAll()

                                Log.d("shagaMapActivity", "AffairsMap: ${AffairsDataHolder.affairsMap.keys.joinToString(", ")}")
                                // Initialize MapPopulation
                                val mapPopulation = MapPopulation()
                                // Start another coroutine to work on UI
                                lifecycleScope.launch(Dispatchers.IO) {
                                    Log.d("shagaMapActivity", "Starting UI coroutine")

                                    // Convert the DecodedAffairsData to MarkerProperties
                                    val deferredMarkerProperties = AffairsDataHolder.affairsMap.values.map { decodedData ->
                                        async(Dispatchers.IO) {
                                            // Check if this public key is already processed
                                            val currentPublicKey = (decodedData.authority)
                                            if (!processedAuthorities.contains(currentPublicKey)) {
                                                runCatching {
                                                    solToUsdcRate?.let { rate ->
                                                        mapPopulation.buildMarkerProperties(this@MapActivity, decodedData, rate)
                                                    }
                                                }.getOrNull()
                                            } else {
                                                null // Skip this one, it's already processed
                                            }
                                        }
                                    }

                                    val validMarkerProperties = deferredMarkerProperties.awaitAll()

                                    Log.d("shagaMapActivity", "Adding markers to map")

                                    // Filter and map successful results to their values
                                    val successfulMarkerProperties = validMarkerProperties.mapNotNull { result ->
                                        result?.takeIf { it.isSuccess }?.getOrThrow()
                                    }

                                    // Add markers for all successful results
                                    successfulMarkerProperties.forEach { markerProperty ->
                                        val sunshinePublicKey = PublicKey(markerProperty.sunshinePublicKey)

                                        synchronized(processedAuthorities) {
                                            if (!processedAuthorities.contains(sunshinePublicKey)) {
                                                addGamingPCMarkerWithProperties(markerProperty)
                                                processedAuthorities.add(sunshinePublicKey)
                                            }
                                        }
                                    }

                                    // Adjust the camera to fit all markers and user's location
                                    userLocation?.let { nonNullUserLocation ->
                                        if (successfulMarkerProperties.isNotEmpty()) {
                                            Log.d("shagaMapActivity", "Adjusting camera")
                                            adjustCameraToShowAllPoints(
                                                successfulMarkerProperties,
                                                nonNullUserLocation,
                                                mapView
                                            )
                                        } else {
                                            Log.e("shagaMapActivity", "No valid marker properties found")
                                        }
                                    } ?: run {
                                        Log.e("shagaMapActivity", "User location is null")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("shagaMapActivity", "Nested Coroutine Error: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("shagaMapActivity", "Main Coroutine Error: ${e.message}")
                    e.printStackTrace()
                }
            }
            } else {
                Toast.makeText(this, "Please wait for 3 seconds before refreshing again", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initializeBackButton() {
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }


    private fun onMapReady() {
        val mapLoadingOverview: TextView = findViewById(R.id.mapLoadingOverview)
        mapLoadingOverview.visibility = View.GONE
        Log.d("MapActivity", "Map is ready.")
    }


    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                solToUsdcRate = fetchSolPriceInUSDC()
                launch(Dispatchers.Main) {
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                }
            }
        }

        Log.d("MapActivity", "onResume called.")
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Log.d("MapActivity", "onStart called.")
    }



    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d("MapActivity", "onStop called.")
    }



    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        Log.d("MapActivity", "onLowMemory called.")
    }



    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        Log.d("MapActivity", "onDestroy called.")
    }


    private fun findNearbyPointAnnotation(clickedPoint: Point, threshold: Double = 0.01): PointAnnotation? {
        for ((annotation, _) in annotationToPropertiesMap) {
            val annotationPoint = annotation.geometry
            val distance = sqrt(
                (annotationPoint.latitude() - clickedPoint.latitude()).pow(2.0) +
                        (annotationPoint.longitude() - clickedPoint.longitude()).pow(2.0)
            )
            if (distance < threshold) {
                return annotation
            }
        }
        return null
    }

    override fun onMapClick(point: Point): Boolean {
        val nearbyAnnotation = findNearbyPointAnnotation(point)
        nearbyAnnotation?.let { annotation ->
            val markerProperties = annotationToPropertiesMap[annotation]
            markerProperties?.let {
                addViewAnnotationWithProperties(annotation, it)
            }
        }
        return true
    }

    fun adjustCameraToShowAllPoints(markerPropertiesList: List<MapPopulation.MarkerProperties>?, userLocation: Point?, mapView: MapView) {
        if (markerPropertiesList == null || userLocation == null || markerPropertiesList.isEmpty()) {
            Log.e("adjustCamera", "Invalid input parameters")
            return
        }

        val points: MutableList<Point> = ArrayList()
        // Convert marker coordinates to Point and add to the list
        for (marker in markerPropertiesList) {
            val point = Point.fromLngLat(marker.coordinates.longitude, marker.coordinates.latitude)
            points.add(point)
        }
        // Add the user's location to the list
        points.add(userLocation)
        // Calculate camera options to fit these points
        var cameraOptions = mapView.getMapboxMap().cameraForCoordinates(points, EdgeInsets(0.0, 0.0, 0.0, 0.0), null, null)
        // Modify the zoom level to zoom out by 20%
        val currentZoom = cameraOptions.zoom ?: 0.0
        val adjustedZoom = currentZoom * 0.8  // Reduce zoom level by 20%
        // Build a new CameraOptions object with the adjusted zoom level
        cameraOptions = CameraOptions.Builder()
            .center(cameraOptions.center)
            .zoom(adjustedZoom)
            .build()
        // Set the calculated camera options to move the camera
        mapView.getMapboxMap().setCamera(cameraOptions)
    }

}