package com.jqk.wifitest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.udptest.udp.AppConstants
import com.example.udptest.udp.ByteUtil
import com.jqk.wifitest.databinding.FragmentTcpBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TCPFragment : Fragment() {
    lateinit var binding: FragmentTcpBinding

    lateinit var manager: WifiP2pManager

    lateinit var lock: WifiManager.MulticastLock

    var mChannel: WifiP2pManager.Channel? = null

    var selectedDevice: WifiP2pDevice? = null

    var isWifiP2pEnabled = false
    var retryChannel = false

    var wifiP2pInfo: WifiP2pInfo? = null

    var isClient = false

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    when (state) {
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                            // Wifi P2P is enabled
                            isWifiP2pEnabled = true
                            Log.d("jiqingke", "Wifi P2P is enabled")
                        }
                        else -> {
                            // Wi-Fi P2P is not enabled
                            isWifiP2pEnabled = false
                            Log.d("jiqingke", "Wi-Fi P2P is not enabled")
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(mChannel) { peers: WifiP2pDeviceList? ->
                        // Handle peers list
                        // 刷新recyclerview
                        updateRecyclerView(peers!!)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    if (manager == null) {
                        return
                    }

                    val networkInfo = intent
                        .getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

                    if (networkInfo.isConnected) {

                        // we are connected with the other device, request connection
                        // info to find group owner IP

                        manager.requestConnectionInfo(mChannel, object : WifiP2pManager.ConnectionInfoListener {
                            override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
                                // 建立连接
                                TCPManager.getInstance().closeAll()
                                L.d("建立连接 ")

                                if (info.groupFormed && info.isGroupOwner) {

                                } else if (info.groupFormed) {
                                    // The other device acts as the client. In this case, we enable the
                                    // get file button.
                                }

                                if (isClient) {
                                    TCPManager.getInstance().openClient(info.groupOwnerAddress.hostAddress, AppConstants.SOCKET_SERVER_PORT)
                                } else {
                                    TCPManager.getInstance().openServer(info.groupOwnerAddress.hostAddress, AppConstants.SOCKET_SERVER_PORT)
                                }

//                                val intent = Intent()
//                                intent.setClass(context!!, WifiService().javaClass)
//                                if (isClient) {
//                                    intent.putExtra("type", "client")
//                                } else {
//                                    intent.putExtra("type", "server")
//                                }
//
//                                intent.putExtra("info", info)
//                                context?.startService(intent)

                                wifiP2pInfo = info
                            }
                        })
                    } else {
                        // It's a disconnect
                        Log.d("jiqingke", "It's a disconnect")
                        TCPManager.getInstance().closeAll()
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val wifiP2pDevice =
                        intent.getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice
                    setMyDeviceInfo(wifiP2pDevice)

                    manager.requestGroupInfo(mChannel, object : WifiP2pManager.GroupInfoListener {
                        override fun onGroupInfoAvailable(p0: WifiP2pGroup?) {
                            L.d("WifiP2pGroup = " + p0.toString())
                        }
                    })
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentTcpBinding>(inflater, R.layout.fragment_tcp, container, false)
        binding.view = this

        UDPManager.getInstance().setContext(context)

        manager = context?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

        mChannel = manager.initialize(context, context?.mainLooper, object : WifiP2pManager.ChannelListener {
            override fun onChannelDisconnected() {
                if (!retryChannel) {
                    Toast.makeText(context, "Channel lost. Trying again", Toast.LENGTH_LONG).show()
                    retryChannel = true
                    manager.initialize(context, context?.mainLooper, this)
                } else {
                    Toast.makeText(
                        context,
                        "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

        val manager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = manager.createMulticastLock("test wifi")

        lock.acquire()

        binding.switchBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if (p1) {
                    isClient = true
                } else {
                    isClient = false
                }
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        context?.registerReceiver(broadcastReceiver, intentFilter)

        EventBus.getDefault().register(this)

        if (isClient) {
            TCPManager.getInstance().openClient(wifiP2pInfo?.groupOwnerAddress?.hostAddress, AppConstants.SOCKET_SERVER_PORT)
        } else {
            TCPManager.getInstance().openServer(wifiP2pInfo?.groupOwnerAddress?.hostAddress, AppConstants.SOCKET_SERVER_PORT)
        }
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(broadcastReceiver)
        EventBus.getDefault().unregister(this)

        TCPManager.getInstance().closeAll()

    }

    override fun onDestroy() {
        super.onDestroy()

        lock.release()
        TCPManager.getInstance().closeAll()
        disconnect(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventBusMessage) {
        Toast.makeText(context, event.type, Toast.LENGTH_SHORT).show()
    }

    fun setMyDeviceInfo(wifiP2pDevice: WifiP2pDevice) {
        binding.name.text = wifiP2pDevice.deviceName
        binding.status.text = Util.getDeviceStatus(wifiP2pDevice.status)
    }

    fun updateSelectDeviceInfo(wifiP2pDevice: WifiP2pDevice) {
        selectedDevice = wifiP2pDevice
        binding.selectName.text = wifiP2pDevice.deviceName
        binding.selectStatus.text = Util.getDeviceStatus(wifiP2pDevice.status)
    }

    fun updateRecyclerView(peers: WifiP2pDeviceList) {
        val datas = arrayListOf<WifiP2pDevice>()
        datas.clear()
        datas.addAll(peers.deviceList)
        val deviceListAdapter = DeviceListAdapter(context!!, datas)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerview.layoutManager = linearLayoutManager
        binding.recyclerview.adapter = deviceListAdapter

        deviceListAdapter.onClickListener = object : DeviceListAdapter.OnClickListener {
            override fun onClick(wifiP2pDevice: WifiP2pDevice) {
                updateSelectDeviceInfo(wifiP2pDevice)
            }
        }
    }

    fun discover(view: View) {
        if (!isWifiP2pEnabled) {
            Log.d("jiqingke", "wifiP2p不能用")
            return
        }
        manager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d("jiqingke", "开始发现")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("jiqingke", "discoverPeerss失败" + reasonCode)
            }
        })
    }

    fun connect(view: View) {
        selectedDevice?.let {
            val config = WifiP2pConfig()
            config.deviceAddress = it.deviceAddress
            config.wps.setup = WpsInfo.PBC
            manager.connect(mChannel, config, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(
                        context, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    fun disconnect(view: View?) {
        manager.removeGroup(mChannel, object : WifiP2pManager.ActionListener {

            override fun onFailure(reasonCode: Int) {
                L.d("Disconnect failed. Reason :$reasonCode")

            }

            override fun onSuccess() {
                L.d("disconnect success")

                TCPManager.getInstance().closeAll()
            }

        })
    }

    fun send(view: View) {
        L.d("wifiP2pInfo?.groupOwnerAddress?.hostAddress = " + wifiP2pInfo?.groupOwnerAddress?.hostAddress)
        if (isClient) {
            TCPManager.getInstance().send2Server(ByteUtil.int2Bytes(1))
        } else {
            TCPManager.getInstance().send2Client(ByteUtil.int2Bytes(2))
        }
    }

    fun createGroup(view: View) {
        manager.createGroup(mChannel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                L.d("createGroup success")
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                    context, "Connect failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}