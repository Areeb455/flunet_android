package com.example.flunetandroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NetworkType { WIFI, CELLULAR, NONE }

data class WifiHealthState(
    @SerializedName("signal_strength_percent")
    val signalStrengthPercent: Int = 0,
    val networkType: NetworkType = NetworkType.NONE,
    val cellularNetworkType: String = "Unknown",
    val channel: Int = 0,
    @SerializedName("is_channel_crowded")
    val isChannelCrowded: Boolean = false,
    @SerializedName("channel_interference")
    val channelInterference: Map<Int, Int> = emptyMap(),
    // The initial status is now "Ready to scan"
    val status: String = "Ready to scan",
    val isRefreshing: Boolean = false
)

class WifiHealthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WifiHealthState())
    val uiState = _uiState.asStateFlow()

    // The init block has been removed. The scan is no longer automatic.
    fun setRefreshing(refreshing: Boolean) {
        _uiState.update { it.copy(isRefreshing = refreshing) }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun fetchWifiHealth(context: Context) {
        viewModelScope.launch {
            // Set refreshing state to true at the start of the fetch
            _uiState.value = _uiState.value.copy(isRefreshing = true, status = "Analyzing network...")
            delay(1500)

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            if (networkCapabilities == null) {
                _uiState.value = WifiHealthState(status = "Not connected", networkType = NetworkType.NONE, isRefreshing = false)
                return@launch
            }

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val connectionInfo = wifiManager.connectionInfo
                val rssi = connectionInfo.rssi
                val level = WifiManager.calculateSignalLevel(rssi, 100)
                val frequency = connectionInfo.frequency
                val channel = convertFrequencyToChannel(frequency)

                _uiState.value = WifiHealthState(
                    signalStrengthPercent = level,
                    networkType = NetworkType.WIFI,
                    cellularNetworkType = "Wi-Fi",
                    channel = channel,
                    isChannelCrowded = true,
                    channelInterference = mapOf(1 to 2, 3 to 1, 6 to 5, 9 to 2, 11 to 3),
                    status = "Analysis Complete",
                    isRefreshing = false
                )

            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    val signalPercent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val level = telephonyManager.signalStrength?.level ?: 0
                        (level * 25).coerceIn(0, 100)
                    } else {
                        @Suppress("DEPRECATION")
                        val gsmSignalStrength = telephonyManager.signalStrength?.gsmSignalStrength ?: 0
                        if (gsmSignalStrength != 99) gsmSignalStrength * 100 / 31 else 0
                    }

                    val networkTypeName = getNetworkTypeName(telephonyManager.dataNetworkType)

                    _uiState.value = WifiHealthState(
                        signalStrengthPercent = signalPercent,
                        networkType = NetworkType.CELLULAR,
                        cellularNetworkType = networkTypeName,
                        status = "Analysis Complete",
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = WifiHealthState(status = "Permission needed", networkType = NetworkType.CELLULAR, isRefreshing = false)
                }
            }
        }
    }

    private fun convertFrequencyToChannel(freq: Int): Int {
        return when (freq) {
            in 2412..2484 -> (freq - 2412) / 5 + 1
            in 5170..5825 -> (freq - 5170) / 5 + 34
            else -> -1
        }
    }

    private fun getNetworkTypeName(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G (LTE)"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "Cellular"
        }
    }
}

