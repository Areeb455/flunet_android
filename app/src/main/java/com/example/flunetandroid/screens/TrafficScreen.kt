package com.example.flunetandroid.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.flunetandroid.TrafficViewModel

@Composable
fun TrafficScreen(trafficViewModel: TrafficViewModel) {
    // Collect all the new, separate states from the ViewModel
    val status by trafficViewModel.status.collectAsState()
    val downloadPoints by trafficViewModel.downloadPoints.collectAsState()
    val uploadPoints by trafficViewModel.uploadPoints.collectAsState()
    val downloadSpeed by trafficViewModel.downloadSpeed.collectAsState()
    val uploadSpeed by trafficViewModel.uploadSpeed.collectAsState()

    // Create separate Point data for each line
    val downloadPointsData = downloadPoints.mapIndexed { index, value -> Point(index.toFloat(), value) }
    val uploadPointsData = uploadPoints.mapIndexed { index, value -> Point(index.toFloat(), value) }

    // Calculate peak download for the Y-axis range
    val peakDownload = downloadPoints.maxOrNull() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Live Traffic Monitor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Status: $status", color = Color.Green, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        // Updated Stats Card to show separate Download, Upload, and Peak
        StatsCard(download = downloadSpeed, upload = uploadSpeed, peak = peakDownload)

        Spacer(Modifier.height(24.dp))

        if (downloadPointsData.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f), // Allow card to grow
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // New: Chart Legend
                    ChartLegend()
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxRange = (peakDownload + 20f).coerceAtLeast(30f)
                    val steps = 5

                    val xAxisData = AxisData.Builder()
                        .axisStepSize(40.dp) // Increased step size for less clutter
                        .backgroundColor(Color.Transparent)
                        .steps(downloadPointsData.size.coerceAtLeast(1) - 1)
                        .labelData { "" } // No labels on X-axis for a cleaner look
                        .axisLineColor(Color.Gray.copy(alpha = 0.5f))
                        .build()

                    val yAxisData = AxisData.Builder()
                        .steps(steps)
                        .backgroundColor(Color.Transparent)
                        .labelAndAxisLinePadding(20.dp) // Padding to prevent overlap
                        .labelData { i ->
                            val value = i * (maxRange / steps)
                            String.format("%.0f", value) // Use whole numbers for cleaner labels
                        }
                        .axisLineColor(Color.Gray.copy(alpha = 0.5f))
                        .axisLabelColor(Color.White)
                        .build()

                    val lineChartData = LineChartData(
                        linePlotData = LinePlotData(
                            lines = listOf(
                                // Download Line (Cyan)
                                Line(
                                    dataPoints = downloadPointsData,
                                    lineStyle = LineStyle(color = Color.Cyan, width = 8f, lineType = LineType.SmoothCurve()),
                                    intersectionPoint = IntersectionPoint(color = Color.Cyan, radius = 4.dp),
                                    selectionHighlightPoint = SelectionHighlightPoint(color = Color.White),
                                    shadowUnderLine = ShadowUnderLine(alpha = 0.5f, brush = Brush.verticalGradient(colors = listOf(Color.Cyan, Color.Transparent))),
                                    selectionHighlightPopUp = SelectionHighlightPopUp(popUpLabel = { _, y -> String.format("DL: %.2f Mbps", y) })
                                ),
                                // Upload Line (Magenta)
                                Line(
                                    dataPoints = uploadPointsData,
                                    lineStyle = LineStyle(color = Color.Magenta, width = 6f, lineType = LineType.SmoothCurve()),
                                    intersectionPoint = IntersectionPoint(color = Color.Magenta, radius = 3.dp),
                                    selectionHighlightPoint = SelectionHighlightPoint(color = Color.White),
                                    shadowUnderLine = ShadowUnderLine(alpha = 0.4f, brush = Brush.verticalGradient(colors = listOf(Color.Magenta, Color.Transparent))),
                                    selectionHighlightPopUp = SelectionHighlightPopUp(popUpLabel = { _, y -> String.format("UL: %.2f Mbps", y) })
                                )
                            )
                        ),
                        xAxisData = xAxisData,
                        yAxisData = yAxisData,
                        gridLines = GridLines(color = Color.Gray.copy(alpha = 0.3f)),
                        backgroundColor = Color.Transparent,
                        isZoomAllowed = false,
//                        scrollOffset = downloadPoints.size.toFloat()
                    )

                    LineChart(
                        modifier = Modifier
                            .fillMaxSize()
                            // Removed horizontal padding to fix the clipping issue
                            .padding(bottom = 16.dp, top = 8.dp),
                        lineChartData = lineChartData
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Initializing monitor...", color = Color.Gray, fontSize = 18.sp)
            }
        }
    }
}

// New Composable for the chart legend
@Composable
fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color.Cyan, label = "Download")
        Spacer(modifier = Modifier.width(24.dp))
        LegendItem(color = Color.Magenta, label = "Upload")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun StatsCard(download: Float, upload: Float, peak: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(label = "Download", value = download, modifier = Modifier.weight(1f))
        StatItem(label = "Upload", value = upload, modifier = Modifier.weight(1f))
        StatItem(label = "Peak DL", value = peak, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatItem(label: String, value: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(String.format("%.2f", value), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text("Mbps", color = Color.Cyan, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

