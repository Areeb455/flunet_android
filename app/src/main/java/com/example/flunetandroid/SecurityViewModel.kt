package com.example.flunetandroid

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

// The data model is now enhanced to include detailed risk information.
data class Vulnerability(
    val deviceIp: String,
    val port: Int,
    val description: String,
    val severity: Severity,
    val riskInfo: String // New field for detailed explanation
)

enum class Severity {
    Low, Medium, High
}

data class SecurityScanState(
    val vulnerabilities: List<Vulnerability> = emptyList(),
    val devicesScanned: Int = 0,
    val status: String = "Ready to scan"
)

class SecurityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityScanState())
    val uiState = _uiState.asStateFlow()

    fun startSecurityScan(context: Context) {
        viewModelScope.launch {
            _uiState.value = SecurityScanState(status = "Discovering devices on network...")

            // Step 1: Perform a quick discovery of live hosts.
            val liveHosts = withContext(Dispatchers.IO) {
                discoverLiveHosts(context)
            }

            _uiState.value = SecurityScanState(
                status = "Scanning ${liveHosts.size} devices for vulnerabilities...",
                devicesScanned = liveHosts.size
            )

            // Step 2: Perform the detailed port scan only on the live hosts.
            val foundVulnerabilities = withContext(Dispatchers.IO) {
                performPortScan(liveHosts)
            }

            _uiState.value = SecurityScanState(
                vulnerabilities = foundVulnerabilities,
                devicesScanned = liveHosts.size,
                status = "Scan Complete"
            )
        }
    }

    // This is the new, fast discovery function.
    private fun discoverLiveHosts(context: Context): List<String> {
        val liveHosts = mutableListOf<String>()
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            val ipString = String.format("%d.%d.%d.%d", (ipAddress and 0xff), (ipAddress shr 8 and 0xff), (ipAddress shr 16 and 0xff), (ipAddress shr 24 and 0xff))
            val subnet = ipString.substring(0, ipString.lastIndexOf("."))

            (1..254).toList().parallelStream().forEach { i ->
                val host = "$subnet.$i"
                try {
                    if (InetAddress.getByName(host).isReachable(1000)) {
                        synchronized(liveHosts) {
                            liveHosts.add(host)
                        }
                    }
                } catch (e: Exception) { /* Ignore */ }
            }
        } catch (e: Exception) { /* Ignore */ }
        return liveHosts
    }

    // This function now only scans the hosts it's given.
    private fun performPortScan(hosts: List<String>): List<Vulnerability> {
        val vulnerabilities = mutableListOf<Vulnerability>()
        val portsToScan = getVulnerablePorts()

        hosts.parallelStream().forEach { host ->
            for (portInfo in portsToScan) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, portInfo.key), 200)
                        synchronized(vulnerabilities) {
                            vulnerabilities.add(
                                Vulnerability(
                                    deviceIp = host,
                                    port = portInfo.key,
                                    description = portInfo.value.description,
                                    severity = portInfo.value.severity,
                                    riskInfo = portInfo.value.riskInfo
                                )
                            )
                        }
                    }
                } catch (e: Exception) { /* Port is closed */ }
            }
        }
        return vulnerabilities.sortedBy { it.deviceIp }
    }

    // The data map is now enhanced with detailed risk information.
    private fun getVulnerablePorts(): Map<Int, VulnerabilityInfo> {
        return mapOf(
            21 to VulnerabilityInfo("FTP", Severity.Medium, "FTP is an unencrypted protocol for file transfer. An attacker could potentially intercept data or credentials."),
            22 to VulnerabilityInfo("SSH", Severity.Low, "SSH provides secure remote access. Ensure it is protected with a strong password or key-based authentication."),
            23 to VulnerabilityInfo("Telnet", Severity.High, "Telnet is an unencrypted remote access protocol. An attacker can easily intercept all communication, including passwords."),
            80 to VulnerabilityInfo("HTTP", Severity.Low, "An unencrypted web server is running. While common, sensitive information should always be sent over HTTPS (port 443)."),
            445 to VulnerabilityInfo("SMB", Severity.Medium, "SMB is used for file sharing. Ensure that shares are protected with strong passwords to prevent unauthorized access."),
            3389 to VulnerabilityInfo("RDP", Severity.High, "Remote Desktop Protocol allows full remote control of a computer. If exposed to the internet, it is a very high-risk target for attackers."),
            5900 to VulnerabilityInfo("VNC", Severity.High, "VNC provides remote control of a computer's screen. Like RDP, it should not be exposed to the internet without a secure tunnel.")
        )
    }
}

// A helper data class for the vulnerable ports map
data class VulnerabilityInfo(val description: String, val severity: Severity, val riskInfo: String)

