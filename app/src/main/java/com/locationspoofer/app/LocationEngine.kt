package com.locationspoofer.app

import android.location.Location

object LocationEngine {
    @Volatile var isSpoofingEnabled = false
    @Volatile var targetLat = 24.7136
    @Volatile var targetLon = 46.6753
    @Volatile var altitude = 600.0

    fun generateSpoofedLocation(): Location {
        val location = Location("gps")
        location.latitude = targetLat
        location.longitude = targetLon
        location.altitude = altitude
        location.accuracy = 5.0f
        location.time = System.currentTimeMillis()
        return location
    }
}
