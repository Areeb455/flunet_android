package com.example.flunetandroid.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// This is the single, correct definition for a discovered device.
// It includes all the fields we need for the UI.
data class DiscoveredDevice(
    val ip: String,
    val mac: String,
    val name: String,
    val openPorts: List<Int> = emptyList() // Added the openPorts list
)

// The UI-specific device types remain the same
enum class DeviceType(val icon: ImageVector) {
    ROUTER(Icons.Default.Home),
    DESKTOP(Icons.Default.Settings),
    PHONE(Icons.Default.Phone),
    TV(Icons.Default.AddCircle),
    UNKNOWN(Icons.Default.Build) // A default icon
}
