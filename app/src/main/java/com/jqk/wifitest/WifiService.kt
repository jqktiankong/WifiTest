package com.jqk.wifitest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.net.Proxy.getPort
import android.net.wifi.p2p.WifiP2pInfo
import com.example.udptest.udp.AppConstants
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Array.getLength
import java.net.DatagramPacket
import java.net.DatagramSocket


class WifiService : Service() {

    var type = ""
    lateinit var wifiP2pInfo: WifiP2pInfo

    override fun onCreate() {
//        EventBus.getDefault().register(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        type = intent?.getStringExtra("type") as String
        wifiP2pInfo = intent?.getParcelableExtra("info") as WifiP2pInfo
        if (type.equals("server")) {
            TCPManager.getInstance()
                .openServer(wifiP2pInfo.groupOwnerAddress.hostAddress, AppConstants.SOCKET_SERVER_PORT)
        } else {
            TCPManager.getInstance()
                .openClient(wifiP2pInfo.groupOwnerAddress.hostAddress, AppConstants.SOCKET_SERVER_PORT)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
//        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}