package com.jqk.wifitest

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log

object Util {
    fun getDeviceStatus(deviceStatus: Int): String {
        when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> return "Available"
            WifiP2pDevice.INVITED -> return "Invited"
            WifiP2pDevice.CONNECTED -> return "Connected"
            WifiP2pDevice.FAILED -> return "Failed"
            WifiP2pDevice.UNAVAILABLE -> return "Unavailable"
            else -> return "Unknown"
        }
    }
}