package com.example.flunetandroid

import android.net.TrafficStats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.util.Log

// This data class is not strictly needed but kept for consistency
data class BandwidthReport(val mbps: Double)

class TrafficViewModel : ViewModel() {
    private val _status = MutableStateFlow("Monitoring...")
    val status: StateFlow<String> = _status.asStateFlow()

    // Data for the Download line on the graph
    private val _downloadPoints = MutableStateFlow<List<Float>>(emptyList())
    val downloadPoints: StateFlow<List<Float>> = _downloadPoints.asStateFlow()

    // New: Data for the Upload line on the graph
    private val _uploadPoints = MutableStateFlow<List<Float>>(emptyList())
    val uploadPoints: StateFlow<List<Float>> = _uploadPoints.asStateFlow()

    // Separate states for current download and upload speeds for the stats cards
    private val _downloadSpeed = MutableStateFlow(0f)
    val downloadSpeed: StateFlow<Float> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0f)
    val uploadSpeed: StateFlow<Float> = _uploadSpeed.asStateFlow()


    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            // Get initial baselines for Rx (download) and Tx (upload) separately
            var lastRxBytes = TrafficStats.getTotalRxBytes()
            var lastTxBytes = TrafficStats.getTotalTxBytes()
            var lastTimestamp = System.currentTimeMillis()

            if (lastRxBytes == TrafficStats.UNSUPPORTED.toLong() || lastTxBytes == TrafficStats.UNSUPPORTED.toLong()) {
                _status.value = "Device not supported"
                return@launch
            }

            while (isActive) {
                delay(1000) // Poll for new data every second

                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()
                val currentTimestamp = System.currentTimeMillis()

                val rxBytesInInterval = currentRxBytes - lastRxBytes
                val txBytesInInterval = currentTxBytes - lastTxBytes
                val timeInSeconds = (currentTimestamp - lastTimestamp) / 1000.0

                // The new, more accurate formulas for Download and Upload Mbps
                val downloadMbps = if (timeInSeconds > 0) (rxBytesInInterval * 8) / (1_000_000.0 * timeInSeconds) else 0.0
                val uploadMbps = if (timeInSeconds > 0) (txBytesInInterval * 8) / (1_000_000.0 * timeInSeconds) else 0.0

                // Detailed logging to help diagnose low numbers
                Log.d("TrafficViewModel", "Download: %.2f Mbps, Upload: %.2f Mbps (Bytes Rx: %d, Tx: %d)".format(downloadMbps, uploadMbps, rxBytesInInterval, txBytesInInterval))

                _downloadSpeed.value = downloadMbps.toFloat()
                _uploadSpeed.value = uploadMbps.toFloat()

                lastRxBytes = currentRxBytes
                lastTxBytes = currentTxBytes
                lastTimestamp = currentTimestamp

                // Update download points
                val currentDownloadPoints = _downloadPoints.value.toMutableList()
                currentDownloadPoints.add(downloadMbps.toFloat())
                if (currentDownloadPoints.size > 60) {
                    currentDownloadPoints.removeAt(0)
                }
                _downloadPoints.value = currentDownloadPoints

                // Update upload points
                val currentUploadPoints = _uploadPoints.value.toMutableList()
                currentUploadPoints.add(uploadMbps.toFloat())
                if (currentUploadPoints.size > 60) {
                    currentUploadPoints.removeAt(0)
                }
                _uploadPoints.value = currentUploadPoints
            }
        }
    }
}

