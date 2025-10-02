package com.example.flunetandroid.screens

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
// New imports for Pull-to-Refresh
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flunetandroid.NetworkType
import com.example.flunetandroid.WifiHealthState
import com.example.flunetandroid.WifiHealthViewModel
import com.example.flunetandroid.ui.theme.FlunetAndroidTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

// This is the main entry point for the screen.
// It now handles the permission request logic.
@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiHealthScreen(viewModel: WifiHealthViewModel) {
    // Create a permission state holder
    val phoneStatePermission = rememberPermissionState(
        permission = Manifest.permission.READ_PHONE_STATE
    )

    // Check the permission status and show the appropriate UI
    if (phoneStatePermission.status.isGranted) {
        // If permission is granted, show the main health content
        WifiHealthContent(viewModel = viewModel)
    } else {
        // If permission is not granted, show an explanatory screen
        PermissionRequestScreen(
            onGrantPermission = { phoneStatePermission.launchPermissionRequest() }
        )
    }
}

// This is the UI that shows when permission is needed.
@Composable
fun PermissionRequestScreen(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "To analyze your cellular signal strength, FluNet needs permission to read your phone's network state. This information is used only to display your signal quality and is not stored or shared.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrantPermission) {
            Text("Grant Permission")
        }
    }
}


// This is the main UI content, now with the pull-to-refresh logic.
@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WifiHealthContent(viewModel: WifiHealthViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // This will automatically call the fetch function once when the screen appears.
    LaunchedEffect(Unit) {
        viewModel.setRefreshing(true)   // ✅ tell UI we’re loading
        viewModel.fetchWifiHealth(context)
        viewModel.setRefreshing(false)  // ✅ hide after load
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                viewModel.setRefreshing(true)
                viewModel.fetchWifiHealth(context)
                viewModel.setRefreshing(false)
            }}

    )

    // --- Pull-to-Refresh State ---


    // The Box is necessary to layer the indicator over the content.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
             .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The title now dynamically changes based on the network type
            Text(
                if (state.networkType == NetworkType.WIFI) "Wi-Fi Health" else "Cellular Health",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Live signal analysis.",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (state.networkType != NetworkType.NONE) {
                SignalStrengthIndicator(percent = state.signalStrengthPercent)
                Text("Signal Strength", color = Color.White, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // The UI now adapts: the Channel Analysis card is only shown for Wi-Fi connections.
            if (state.networkType == NetworkType.WIFI) {
                ChannelAnalysisCard(state = state)
            } else {
                NetworkInfoCard(state = state)
            }
        }

        // The visual indicator for the pull-to-refresh action
        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// This is a new, more generic card for non-Wi-Fi connections
@Composable
fun NetworkInfoCard(state: WifiHealthState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(state.status, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Network Type:", color = Color.Gray)
                Text(state.networkType.name, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun SignalStrengthIndicator(
    percent: Int,
    radius: Dp = 100.dp,
    strokeWidth: Dp = 12.dp
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercent = animateFloatAsState(
        targetValue = if(animationPlayed) percent / 100f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500, delayMillis = 300)
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val color = if (percent > 70) Color.Green else if (percent > 40) Color.Yellow else Color.Red

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(radius * 2f)
    ) {
        Canvas(modifier = Modifier.size(radius * 2f)) {
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.5f),
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -210f,
                sweepAngle = 240f * currentPercent.value,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percent%",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun ChannelAnalysisCard(state: WifiHealthState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(state.status, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Channel:", color = Color.Gray)
                Text("${state.channel} (2.4 GHz)", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Channel Interference:", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            ChannelInterferenceChart(
                interferenceData = state.channelInterference,
                currentChannel = state.channel
            )

            if(state.isChannelCrowded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your Wi-Fi channel is crowded.",
                    color = Color.Yellow,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ChannelInterferenceChart(interferenceData: Map<Int, Int>, currentChannel: Int) {
    val maxInterference = interferenceData.values.maxOrNull()?.toFloat() ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        (1..11).forEach { channel ->
            val interference = interferenceData.getOrDefault(channel, 0)
            val barHeight = (interference / maxInterference).coerceIn(0f, 1f)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight(barHeight)
                        .width(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (channel == currentChannel) Color.Cyan else Color.Gray)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = channel.toString(),
                    color = if (channel == currentChannel) Color.Cyan else Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

