package com.example.flunetandroid.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flunetandroid.DashboardViewModel
import com.example.flunetandroid.SecurityViewModel
import com.example.flunetandroid.TrafficViewModel
import com.example.flunetandroid.WifiHealthViewModel


// --- Data Models for Navigation ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Person)
    object Traffic : Screen("traffic", "Traffic", Icons.Default.Add)
    object WifiHealth : Screen("wifi_health", "Wi-Fi Health", Icons.Default.Settings)
    object Security : Screen("security", "Security", Icons.Default.Lock)
}
val items = listOf(
    Screen.Dashboard,
    Screen.Traffic,
    Screen.WifiHealth,
    Screen.Security,
)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MainScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    trafficViewModel: TrafficViewModel = viewModel(),
    wifiHealthViewModel: WifiHealthViewModel = viewModel(),
    securityViewModel: SecurityViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Dashboard.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                // Collect the live data from the ViewModel
                val devices by dashboardViewModel.devices.collectAsState()
                val isScanning by dashboardViewModel.isScanning.collectAsState()
                // Pass the live data AND the scan function to the DashboardScreen UI
                DashboardScreen(
                    devices = devices,
                    isScanning = isScanning,
                    onScanClick = { dashboardViewModel.startNetworkScan(context) }
                )
            }
            composable(Screen.Traffic.route) {
                TrafficScreen(trafficViewModel)
            }
            composable(Screen.WifiHealth.route) {
                WifiHealthScreen(wifiHealthViewModel)
            }
            composable(Screen.Security.route) {
                SecurityScreen(securityViewModel)
            }
        }
    }
}


//val items = listOf(
//    Screen.Dashboard,
//    Screen.Traffic,
//    Screen.WifiHealth,
//    Screen.Security
//)
//@Composable
//fun MainScreen(
//    dashboardViewModel: DashboardViewModel = viewModel(),
//    trafficViewModel: TrafficViewModel = viewModel(),
//    wifiHealthViewModel: WifiHealthViewModel = viewModel(),
//    securityViewModel: SecurityViewModel = viewModel()
//) {
//    val navController = rememberNavController()
//    val context = LocalContext.current
//
//    // This is the critical change: It tells the ViewModels to start their respective
//    // scans and monitors automatically once when the app's main screen is first displayed.
//    LaunchedEffect(key1 = true) {
//        dashboardViewModel.startNetworkScan(context)
//        // The other ViewModels (Traffic, WifiHealth, Security) already start their
//        // processes in their `init` blocks, so we only need to trigger the dashboard scan.
//    }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//                val navBackStackEntry by navController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry?.destination
//                items.forEach { screen ->
//                    NavigationBarItem(
//                        icon = { Icon(screen.icon, contentDescription = null) },
//                        label = { Text(screen.label) },
//                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
//                        onClick = {
//                            navController.navigate(screen.route) {
//                                popUpTo(navController.graph.findStartDestination().id) {
//                                    saveState = true
//                                }
//                                launchSingleTop = true
//                                restoreState = true
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    ) { innerPadding ->
//        NavHost(
//            navController,
//            startDestination = Screen.Dashboard.route,
//            Modifier.padding(innerPadding)
//        ) {
//            composable(Screen.Dashboard.route) {
//                // Collect the live data from the ViewModel
//                val devices by dashboardViewModel.devices.collectAsState()
//                val isScanning by dashboardViewModel.isScanning.collectAsState()
//                // Pass the live data to the DashboardScreen UI
//                DashboardScreen(devices = devices, isScanning = isScanning)
//            }
//            composable(Screen.Traffic.route) {
//                TrafficScreen(trafficViewModel)
//            }
//            composable(Screen.WifiHealth.route) {
//                WifiHealthScreen(wifiHealthViewModel)
//            }
//            composable(Screen.Security.route) {
//                SecurityScreen(securityViewModel)
//            }
//        }
//    }
//}
//
//
