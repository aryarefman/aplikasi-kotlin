package com.example.my_kotlin_app_gmaps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var inputLatitude: TextInputEditText
    private lateinit var inputLongitude: TextInputEditText
    private lateinit var btnAddMarker: MaterialButton
    private lateinit var btnSearchLocation: MaterialButton
    private lateinit var btnMyLocation: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var btnClearAll: MaterialButton
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var markerCounter = 1
    private var currentLocationMarker: Marker? = null
    private lateinit var locationCallback: LocationCallback
    private val markersList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initLocationCallback()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        inputLatitude = findViewById(R.id.input_latitude)
        inputLongitude = findViewById(R.id.input_longitude)
        btnAddMarker = findViewById(R.id.btn_add_marker)
        btnSearchLocation = findViewById(R.id.btn_search_location)
        btnMyLocation = findViewById(R.id.btn_my_location)
        btnClear = findViewById(R.id.btn_clear)
        btnClearAll = findViewById(R.id.btn_clear_all)

        setupButtonListeners()
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleNewLocation(location)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
    }

    private fun setupButtonListeners() {
        btnSearchLocation.setOnClickListener {
            searchLocation()
        }

        btnAddMarker.setOnClickListener {
            addMarkerFromInput()
        }

        btnMyLocation.setOnClickListener {
            getCurrentLocation()
        }

        btnClear.setOnClickListener {
            inputLatitude.text?.clear()
            inputLongitude.text?.clear()
            Toast.makeText(this, "Input dikosongkan", Toast.LENGTH_SHORT).show()
        }

        btnClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val surabaya = LatLng(-7.2930192, 112.8079525)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(surabaya, 12f))

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
        }

        mMap.setOnMarkerClickListener { marker ->
            if (marker != currentLocationMarker) {
                showDeleteMarkerDialog(marker)
                true
            } else {
                marker.showInfoWindow()
                false
            }
        }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Permission lokasi diperlukan untuk fitur ini",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission lokasi belum diberikan", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "🔍 Mencari lokasi...", Toast.LENGTH_SHORT).show()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0
        ).apply {
            setMaxUpdates(1)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun handleNewLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        currentLocationMarker?.remove()

        currentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("My Location")
                .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
        )

        currentLocationMarker?.showInfoWindow()

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f),
            1000,
            null
        )

        Toast.makeText(
            this,
            "✅ Lokasi: ${String.format("%.4f", location.latitude)}, ${
                String.format(
                    "%.4f",
                    location.longitude
                )
            }",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun searchLocation() {
        val latString = inputLatitude.text.toString()
        val lngString = inputLongitude.text.toString()

        if (latString.isEmpty() || lngString.isEmpty()) {
            Toast.makeText(this, "Harap isi latitude dan longitude", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val latitude = latString.toDouble()
            val longitude = lngString.toDouble()

            if (latitude < -90 || latitude > 90) {
                Toast.makeText(this, "Latitude harus antara -90 dan 90", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            if (longitude < -180 || longitude > 180) {
                Toast.makeText(this, "Longitude harus antara -180 dan 180", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val searchLocation = LatLng(latitude, longitude)

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, 15f))

            Toast.makeText(
                this,
                "🔍 Preview lokasi - Klik 'Add Marker' untuk menambahkan marker",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Format koordinat tidak valid. Gunakan angka desimal.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addMarkerFromInput() {
        val latString = inputLatitude.text.toString()
        val lngString = inputLongitude.text.toString()

        if (latString.isEmpty() || lngString.isEmpty()) {
            Toast.makeText(this, "Harap isi latitude dan longitude", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val latitude = latString.toDouble()
            val longitude = lngString.toDouble()

            if (latitude < -90 || latitude > 90) {
                Toast.makeText(this, "Latitude harus antara -90 dan 90", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            if (longitude < -180 || longitude > 180) {
                Toast.makeText(this, "Longitude harus antara -180 dan 180", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val newLocation = LatLng(latitude, longitude)

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(newLocation)
                    .title("Marker #$markerCounter")
                    .snippet("Lat: $latitude, Lng: $longitude\nKlik untuk hapus")
            )

            marker?.let {
                markersList.add(it)
                it.showInfoWindow()
            }

            markerCounter++

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))

            inputLatitude.text?.clear()
            inputLongitude.text?.clear()

            Toast.makeText(
                this,
                "✅ Marker #${markerCounter - 1} ditambahkan! Klik marker untuk hapus.",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Format koordinat tidak valid. Gunakan angka desimal.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteMarkerDialog(marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Marker?")
            .setMessage("Apakah Anda yakin ingin menghapus ${marker.title}?")
            .setPositiveButton("Hapus") { _, _ ->
                markersList.remove(marker)
                marker.remove()
                Toast.makeText(this, "✅ ${marker.title} dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showClearAllConfirmation() {
        if (markersList.isEmpty() && currentLocationMarker == null) {
            Toast.makeText(this, "Tidak ada marker untuk dihapus", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Marker?")
            .setMessage("Apakah Anda yakin ingin menghapus semua marker di peta?")
            .setPositiveButton("Hapus Semua") { _, _ ->
                mMap.clear()
                markersList.clear()
                currentLocationMarker = null
                markerCounter = 1
                Toast.makeText(this, "✅ Semua marker dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}