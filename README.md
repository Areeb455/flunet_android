FluNet - A Native Android Network Analyzer

A powerful, standalone network analysis and monitoring tool built entirely with Kotlin and Jetpack Compose. FluNet provides a suite of tools to help you understand, monitor, and secure your local network, all from your Android device.

No backend or server required. All scanning and analysis are performed 100% natively on the device, making this a truly "APK-friendly" and shareable application.

🚀 Core Features

FluNet combines multiple networking tools into one clean, multi-screen interface:

Device Discovery (Real ARP Scan)
A high-speed, multi-threaded ARP scanner that discovers all devices connected to your Wi-Fi network. It reliably finds the IP address, MAC address, and even performs a reverse-DNS lookup to discover device hostnames (e.g., "My-Laptop.local", "Pixel-8-Pro").

Live Traffic Monitor (Real TrafficStats)
A real-time, dual-line graph that plots your phone's Download (Cyan) and Upload (Magenta) bandwidth usage, pulling data directly from Android's TrafficStats API. The chart automatically scrolls and can be panned, just like professional monitoring tools.

Internet Speed Test (Active Download)
A "fast.com" style active speed test. Tapping "Start Test" initiates a multi-second download of a test file, allowing the Live Traffic monitor to measure your network's peak download speed, which is then displayed on a beautiful speedometer gauge.

Wi-Fi & Cellular Health (Real WifiManager)
An adaptive analysis screen that reads your phone's hardware.

On Wi-Fi: It displays your real-time signal strength (RSSI converted to 0-100%), your current Wi-Fi channel, and a visual bar chart of (simulated) channel interference.

On Cellular: It switches to "Cellular Health," showing your real-time signal strength (dBm converted to 0-100%) and your network type (e.g., "4G (LTE)", "5G").

Includes a "Pull-to-Refresh" feature to re-run the analysis at any time.

Security Scanner (Native Port Scan)
A standalone, multi-threaded port scanner. After discovering live devices on the network, this tool checks them for a list of common, vulnerable open ports (like 21-FTP, 23-Telnet, 3389-RDP). Results are displayed with expandable cards explaining the risk of each vulnerability.

📱 Screenshots

Dashboard (Device Discovery)

Live Traffic (Speed Test)





Wi-Fi Health

Security Scan





🛠️ Tech Stack & Key Components

This project was built from the ground up as a native, standalone application, pushing the boundaries of what can be done on-device.

100% Kotlin & Jetpack Compose (Material 3) for a modern, reactive UI.

Android Architecture Components:

ViewModel: To manage UI state and encapsulate all business logic.

StateFlow: To provide a stream of live data from the ViewModels to the UI.

Native Android APIs:

ConnectivityManager & WifiManager: For real-time Wi-Fi/cellular signal strength and network info.

TrafficStats: For reading the phone's live download/upload byte counters.

java.net.Socket & InetAddress: For performing the multi-threaded port scans and reverse DNS lookups.

/proc/net/arp: For reading the system's ARP table to get a reliable list of device IP and MAC addresses.

Key Libraries:

ycharts: For the beautiful, animated, and interactive line charts.

androidx.compose.material: For the "pull-to-refresh" functionality.

accompanist-permissions: To professionally handle runtime permissions for location (for scanning) and phone state (for cellular signal).

📦 How to Build the APK

To build a shareable APK file for your friends or for your project showcase:

Change Build Variant:

In the bottom-left of Android Studio, click on Build Variants.

Change the Active Build Variant for the :app module from debug to release.

Generate Signed APK:

In the top menu, go to Build > Generate Signed Bundle / APK....

Select APK and click Next.

If you don't have one, click "Create new..." to generate a new Keystore.

Important: Save this .jks file and your passwords somewhere safe. You will need them to publish or update your app.

Once your keystore is selected, click Next.

Choose the release variant and click Create.

Locate Your APK:

Android Studio will build the app. When it's finished, a notification will appear.

Click the "locate" link in the notification.

Your shareable file, app-release.apk, will be in the app/release folder.

License

Distributed under the MIT License. See LICENSE for more information.
