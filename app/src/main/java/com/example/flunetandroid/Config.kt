package com.example.flunetandroid

object AppConfig {
    // This is the single source of truth for your backend URL.
    // Use this URL for the Android Emulator with the `adb reverse` command.
    const val BACKEND_URL = "ws://localhost:8080"

    // To test with a physical phone, comment out the line above and uncomment the line below.
    // Make sure to replace "192.168.1.15" with your computer's actual Wi-Fi IP address.
    // const val BACKEND_URL = "ws://192.168.1.15:8080"
}
