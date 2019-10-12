package com.example.udptest.udp

object AppConstants {
    // 服务器地址
    const val SOCKET_HOST = "10.10.11.239"
    // 服务器端口
    const val SOCKET_SERVER_PORT = 8888
    // 本地端口
    const val SOCKET_CLIENT_PORT = 8000
    // 终端标识
    const val DEVICE_MARK = 1

    // 错误信息
    const val MESID_ERROR = -1
    // 终端上报设备信息
    const val MESID_0X01 = 0x01
    const val MESID_0X02 = 0x02
    const val MESID_0X03 = 0x03
    const val MESID_0X04 = 0x04
    const val MESID_0X05 = 0x05
    const val MESID_0X06 = 0x06
    const val MESID_0X07 = 0x07

    val ACTION_SYSTEM_UPDATE_ONLINE = "android.intent.action.SYSTEM_UPDATE_ONLINE"
    val ACTION_STOPMCUHEART = "android.action.stopmcuheart"
    val ACTION_MCU_UPDATE = "android.intent.action.MCU_UPDATE"
    val ACTION_MCU_UPDATE_ONLINE = "android.intent.action.MCU_UPDATE_ONLINE"
}