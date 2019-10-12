package com.jqk.wifitest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.net.Proxy.getPort
import java.lang.reflect.Array.getLength
import java.net.DatagramPacket
import java.net.DatagramSocket


class ServerService : Service() {

    override fun onCreate() {
        super.onCreate()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}