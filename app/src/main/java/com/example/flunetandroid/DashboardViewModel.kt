package com.example.flunetandroid

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flunetandroid.screens.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class DashboardViewModel : ViewModel() {

    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun startNetworkScan(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            _devices.value = emptyList()

            val foundDevices = withContext(Dispatchers.IO) {
                scanNetwork(context)
            }

            _devices.value = foundDevices
            _isScanning.value = false
        }
    }

    private fun scanNetwork(context: Context): List<DiscoveredDevice> {
        val reachableDevices = mutableListOf<DiscoveredDevice>()
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress

            val ipString = String.format(
                "%d.%d.%d.%d",
                (ipAddress and 0xff),
                (ipAddress shr 8 and 0xff),
                (ipAddress shr 16 and 0xff),
                (ipAddress shr 24 and 0xff)
            )

            val subnet = ipString.substring(0, ipString.lastIndexOf("."))

            (1..254).toList().parallelStream().forEach { i ->
                val host = "$subnet.$i"
                try {
                    val address = InetAddress.getByName(host)
                    if (address.isReachable(500)) {

                        // Try hostname via mDNS
                        var deviceName = getMdnsName(host)

                        // Fallback: ARP + MAC vendor
                        val mac = getMacFromArp(host)
                        if (deviceName.isBlank()) {
                            deviceName = mac?.let { guessVendor(it) } ?: "Unknown Device"
                        }

                        synchronized(reachableDevices) {
                            reachableDevices.add(DiscoveredDevice(ip = host, mac = mac ?: "N/A", name = deviceName))
                        }
                    }
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }

        // Sort devices by IP
        return reachableDevices.sortedBy { device ->
            device.ip.split(".").map { it.toInt() }.toTypedArray().let {
                it[0] * 1000000 + it[1] * 10000 + it[2] * 100 + it[3]
            }
        }
    }

    // Get device name via mDNS (works for phones, Apple devices, smart TVs)
    private fun getMdnsName(ip: String): String {
        return try {
            val jmDNS = JmDNS.create(InetAddress.getByName(ip))
            val services: Array<ServiceInfo> = jmDNS.list("_services._dns-sd._udp.local.")
            jmDNS.close()
            if (services.isNotEmpty()) services[0].name else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun getMacFromArp(ip: String): String? {
        try {
            val reader = BufferedReader(FileReader("/proc/net/arp"))
            reader.useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 4 && parts[0] == ip) {
                        val mac = parts[3]
                        if (mac.matches("..:..:..:..:..:..".toRegex())) {
                            return mac.uppercase()
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return null
    }

    private fun guessVendor(mac: String): String {
        val prefix = mac.substring(0, 8) // First 3 octets
        return when {
            prefix.startsWith("FC:DB:B3") -> "Samsung Device"
            prefix.startsWith("DC:85:DE") -> "Xiaomi Device"
            prefix.startsWith("F4:0F:24") -> "Apple Device"
            prefix.startsWith("00:1A:2B") -> "Cisco Router"
            prefix.startsWith("3C:5A:B4") -> "OnePlus Device"
            prefix.startsWith("5C:AA:FD") -> "Realme Device"
            prefix.startsWith("40:9C:28") -> "Dell Laptop"
            prefix.startsWith("AC:37:43") -> "HP Laptop"
            else -> "Unknown Device"
        }
    }
}
