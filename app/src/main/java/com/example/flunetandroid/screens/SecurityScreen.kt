package com.example.flunetandroid.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flunetandroid.SecurityScanState
import com.example.flunetandroid.SecurityViewModel
import com.example.flunetandroid.Severity
import com.example.flunetandroid.Vulnerability
import com.example.flunetandroid.ui.theme.FlunetAndroidTheme

@Composable
fun SecurityScreen(viewModel: SecurityViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // The UI now shows an initial "Start Scan" button for manual control.
    if (state.status == "Ready to scan") {
        InitialSecurityScanScreen(onScanClick = { viewModel.startSecurityScan(context) })
    } else {
        // This is the main results/progress screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Security Scan", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Scanning for common vulnerabilities.", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            ScanStatusCard(state = state)
            Spacer(modifier = Modifier.height(24.dp))

            if (state.vulnerabilities.isNotEmpty()) {
                Text(
                    text = "Found ${state.vulnerabilities.size} Potential Issues",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.vulnerabilities) { vulnerability ->
                        VulnerabilityCard(vulnerability = vulnerability)
                    }
                }
            } else if (state.status == "Scan Complete") {
                Text("No common vulnerabilities found.", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InitialSecurityScanScreen(onScanClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Network Vulnerability Scan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Scan all devices on your network for common open ports that could pose a security risk.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onScanClick) {
            Text("Start Security Scan")
        }
    }
}

@Composable
fun ScanStatusCard(state: SecurityScanState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(state.status, modifier = Modifier.weight(1f), color = Color.White)
            if (state.status.contains("Scanning") || state.status.contains("Discovering")) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else if (state.status == "Scan Complete") {
                Text("${state.devicesScanned} devices checked", color = Color.Gray)
            }
        }
    }
}

// The VulnerabilityCard is now interactive and expandable.
@Composable
fun VulnerabilityCard(vulnerability: Vulnerability) {
    var expanded by remember { mutableStateOf(false) }
    val severityColor = when (vulnerability.severity) {
        Severity.High -> Color.Red
        Severity.Medium -> Color.Yellow
        Severity.Low -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Vulnerability",
                    tint = severityColor,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(severityColor.copy(alpha = 0.1f)).padding(8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(vulnerability.deviceIp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(vulnerability.description, color = Color.Gray, fontSize = 14.sp)
                }
                Text(
                    text = vulnerability.severity.name.uppercase(),
                    color = severityColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // This section smoothly expands to show the detailed risk info when the card is tapped.
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Risk Information", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(vulnerability.riskInfo, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SecurityScreenPreview() {
    val previewState = SecurityScanState(
        vulnerabilities = listOf(
            Vulnerability("192.168.1.1", 80, "HTTP (Unencrypted)", Severity.Medium, "This is a risk."),
            Vulnerability("192.168.1.55", 3389, "RDP (Remote Desktop)", Severity.High, "This is a high risk.")
        ),
        devicesScanned = 15,
        status = "Scan Complete"
    )
    FlunetAndroidTheme {
        // We can't easily preview the ViewModel, so we'll just show one of the states
        Column(Modifier.padding(16.dp)) {
            VulnerabilityCard(vulnerability = previewState.vulnerabilities[0])
        }
    }
}

