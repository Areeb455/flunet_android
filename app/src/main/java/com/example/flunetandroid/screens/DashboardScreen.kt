package com.example.flunetandroid.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// Import for Pull-to-Refresh
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// The main entry point now handles the critical permission logic for the scanner
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(devices: List<DiscoveredDevice>, isScanning: Boolean, onScanClick: () -> Unit) {
    // We request location permission, as it's required for ARP scanning on modern Android
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (locationPermissionState.status.isGranted) {
        // If permission is already granted, show the main dashboard content
        DashboardContent(devices = devices, isScanning = isScanning, onScanClick = onScanClick)
    } else {
        // If permission is not granted, show the explanatory screen to the user
        PermissionRequestScreen(
            title = "Permission Required for Scanning",
            description = "On modern Android, location permission is required to discover devices on your Wi-Fi network. FluNet needs this permission to perform a network scan. Your location data is not read, stored, or shared.",
            onGrantPermission = { locationPermissionState.launchPermissionRequest() }
        )
    }
}

// A clean UI to explain why the location permission is needed
@Composable
fun PermissionRequestScreen(title: String, description: String, onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrantPermission) {
            Text("Grant Permission")
        }
    }
}


// This is the main dashboard UI, which is only shown after permission is granted.
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardContent(devices: List<DiscoveredDevice>, isScanning: Boolean, onScanClick: () -> Unit) {
    // This automatically triggers the scan once when the content is first shown
    LaunchedEffect(key1 = true) {
        if (devices.isEmpty() && !isScanning) {
            onScanClick()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isScanning,
        onRefresh = onScanClick // Pulling down now calls the scan function
    )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("FluNet Dashboard", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Your network, simplified.", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            StatusCard(isScanning = isScanning)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Discovered Devices (${devices.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning && devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!isScanning && devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No devices found.\nPull down to scan again.", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(devices) { device ->
                        DeviceCard(device = device)
                    }
                }
            }
        }

        // The visual indicator for the pull-to-refresh action
        PullRefreshIndicator(
            refreshing = isScanning,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// Helper function to provide a more descriptive name and icon for the UI
fun guessDeviceType(device: DiscoveredDevice): Pair<DeviceType, String> {
    if (device.ip.endsWith(".1")) return Pair(DeviceType.ROUTER, "Gateway Router")

    val lowerCaseName = device.name.lowercase()
    return when {
        device.name.isNotEmpty() && device.name.equals(Build.MODEL, ignoreCase = true) -> Pair(DeviceType.PHONE, "This Device (${device.name})")
        lowerCaseName.contains("android") || lowerCaseName.contains("phone") -> Pair(DeviceType.PHONE, device.name.ifEmpty { "Android Device" })
        lowerCaseName.contains("tv") || lowerCaseName.contains("chromecast") -> Pair(DeviceType.TV, device.name.ifEmpty { "Smart TV" })
        lowerCaseName.contains("pc") || lowerCaseName.contains("desktop") || lowerCaseName.contains("laptop") -> Pair(DeviceType.DESKTOP, device.name.ifEmpty { "Computer" })
        device.name.isNotEmpty() -> Pair(DeviceType.UNKNOWN, device.name)
        else -> Pair(DeviceType.UNKNOWN, "Network Device")
    }
}

@Composable
fun StatusCard(isScanning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Status", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (isScanning) {
                    Text("Scanning...", fontSize = 14.sp, color = Color.Cyan)
                } else {
                    Text("Scan Complete", fontSize = 14.sp, color = Color.Green)
                }
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isScanning) Color.Cyan else Color.Green)
            )
        }
    }
}

@Composable
fun DeviceCard(device: DiscoveredDevice) {
    val (deviceType, displayName) = guessDeviceType(device)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = deviceType.icon,
                contentDescription = "Device Icon",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(device.ip, color = Color.Gray, fontSize = 12.sp)
                Text(device.mac, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

