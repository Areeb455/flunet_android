# FluNet - A Native Android Network Analyzer

A powerful, standalone network analysis and monitoring tool built entirely with Kotlin and Jetpack Compose. FluNet provides a suite of tools to help you understand, monitor, and secure your local network, all from your Android device.

<p align="center">
  <img src="https://github.com/user-attachments/assets/cde1545d-c4b8-4ab2-a59b-cfc4cf8cf13f" width="150" alt="Dashboard Screen">
  <img src="https://github.com/user-attachments/assets/6cceffc1-1699-4412-8010-dee1c2e8ab5" width="150" alt="Live Traffic Screen">
  <img src="https://github.com/user-attachments/assets/ebf75128-3fcb-4c25-857c-911312bf2bf2" width="150" alt="Wi-Fi Health Screen">
  <img src="https://github.com/user-attachments/assets/beb20089-898a-43df-a1af-16e84261574b" width="150" alt="Security Scan Screen">
</p>

## 🚀 Core Features

FluNet combines multiple networking tools into one clean, multi-screen interface:

  * **Device Discovery (Real ARP Scan)**
    A high-speed, multi-threaded ARP scanner that discovers all devices connected to your Wi-Fi network. It reliably finds the IP address, MAC address, and even performs a reverse-DNS lookup to discover device hostnames (e.g., "My-Laptop.local", "Pixel-8-Pro").

  * **Live Traffic Monitor (Real `TrafficStats`)**
    A real-time, dual-line graph that plots your phone's **Download (Cyan)** and **Upload (Magenta)** bandwidth usage, pulling data directly from Android's `TrafficStats` API. The chart automatically scrolls and can be panned, just like professional monitoring tools.

  * **Internet Speed Test (Active Download)**
    A "fast.com" style active speed test. Tapping "Start Test" initiates a multi-second download of a test file, allowing the Live Traffic monitor to measure your network's **peak download speed**, which is then displayed on a beautiful speedometer gauge.

  * **Wi-Fi & Cellular Health (Real `WifiManager`)**
    An adaptive analysis screen that reads your phone's hardware.

      * **On Wi-Fi:** It displays your real-time signal strength (RSSI converted to 0-100%), your current Wi-Fi channel, and a visual bar chart of (simulated) channel interference.

      * **On Cellular:** It switches to "Cellular Health," showing your real-time signal strength (dBm converted to 0-100%) and your network type (e.g., "4G (LTE)", "5G").

      * Includes a "Pull-to-Refresh" feature to re-run the analysis at any time.

  * **Security Scanner (Native Port Scan)**
    A standalone, multi-threaded port scanner. After discovering live devices on the network, this tool checks them for a list of common, vulnerable open ports (like 21-FTP, 23-Telnet, 3389-RDP). Results are displayed with expandable cards explaining the risk of each vulnerability.


## License

Distributed under the MIT License. See `LICENSE` for more information.
