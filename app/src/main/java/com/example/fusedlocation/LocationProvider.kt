package com.example.fusedlocation

import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

class LocationProvider(private val context: Context) {
    private var fusedLocationProvider: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest: LocationRequest by lazy { LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 1000).build() }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            println("####, the locationResult provider is ${p0.locations.last().provider}, the lat is" +
                    " ${p0.locations.last().latitude}, the long is ${p0.locations.last().longitude}")
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            println("####, the availability is ${p0.isLocationAvailable}")
        }
    }

    fun startLocationUpdate() {
        val builder = LocationSettingsRequest.Builder()
        val locationSettingRequest = builder.addLocationRequest(locationRequest).build()
        val settingClient = LocationServices.getSettingsClient(context)
        settingClient.checkLocationSettings(locationSettingRequest)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun stopLocationUpdate() {
        fusedLocationProvider.removeLocationUpdates(locationCallback)
    }
}