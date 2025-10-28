package com.example.flunetandroid.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


data class DiscoveredDevice(
    val ip: String,
    val mac: String,
    val name: String,
    val openPorts: List<Int> = emptyList()
)

enum class DeviceType(val icon: ImageVector) {
    ROUTER(Icons.Default.Home),
    DESKTOP(Icons.Default.Settings),
    PHONE(Icons.Default.Phone),
    TV(Icons.Default.AddCircle),
    UNKNOWN(Icons.Default.Build) // A default icon
}
