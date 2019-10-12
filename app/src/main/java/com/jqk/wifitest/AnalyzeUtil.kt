package com.jqk.wifitest

import com.example.udptest.udp.AppConstants
import com.example.udptest.udp.ByteUtil
import java.nio.charset.Charset
import java.util.*
import org.greenrobot.eventbus.EventBus
import java.nio.ByteBuffer


class AnalyzeUtil private constructor() {
    companion object {
        val instance: AnalyzeUtil by lazy {
            AnalyzeUtil()
        }
    }

    fun analyze(data: ArrayList<Byte>) {
        when (analyzeID(data)) {
            AppConstants.MESID_ERROR -> {
                L.d("没有对应的消息ID")
            }
            AppConstants.MESID_0X01 -> {
                L.d("msgID = " + AppConstants.MESID_0X01)
                val message = data.subList(11, data.size)
                L.d("message = " + ByteUtil.bytesToHex(message.toByteArray()))
                when (message[0].toInt()) {
                    0 -> {
                        L.d("上传失败")
                    }
                    1 -> {
                        L.d("上传成功，开启心跳")
                        EventBus.getDefault().post(EventBusMessage("startHeartbeat"))
                    }
                    2 -> {
                        L.d("有升级")
                        analyze_0X01_0_2(message.subList(1, message.size))
                    }
                }
            }
        }
    }

    // 解析命令字ID
    fun analyzeID(data: ArrayList<Byte>): Int {
        // 7f0401303132333435360101320
        val head = data[0]

        if (head.toInt() != 0x7f) {
            L.d("标识头错误")
            return AppConstants.MESID_ERROR
        }
        val deviceID = data.subList(3, 10)
        L.d("deviceID = " + ByteUtil.bytesToHex(deviceID.toByteArray()))
        if (!ByteUtil.bytesToHex(deviceID.toByteArray()).equals(
                ByteUtil.bytesToHex(
                    MessageUtil.instance.getDeviceID().toByteArray(
                        Charset.forName("GBK")
                    )
                )
            )
        ) {
            L.d("终端ID错误")
            return AppConstants.MESID_ERROR
        }
        val messageID = data[10]

        return messageID.toInt()
    }

    // 解析升级信息
    fun analyze_0X01_0_2(data: List<Byte>) {
        var message = data

        val versionLength = message[0].toInt()
//        L.d("versionLength = " + versionLength)
        message = message.subList(1, message.size)
        val version = String(message.subList(0, versionLength).toByteArray())
//        L.d("version = " + version)

        message = message.subList(versionLength, message.size)
        val FTPUserNameLength = message[0].toInt()
//        L.d("FTPUserNameLength = " + FTPUserNameLength)
        message = message.subList(1, message.size)
        val FTPUserName = String(message.subList(0, FTPUserNameLength).toByteArray())
//        L.d("FTPUserName = " + FTPUserName)

        message = message.subList(FTPUserNameLength, message.size)
        val FTPPwdLength = message[0].toInt()
//        L.d("FTPPwdLength = " + FTPPwdLength)
        message = message.subList(1, message.size)
        val FTPPwd = String(message.subList(0, FTPPwdLength).toByteArray())
//        L.d("FTPPwd = " + FTPPwd)

        message = message.subList(FTPPwdLength, message.size)
        val FTPAddressLength = message[0].toInt()
//        L.d("FTPPwdLength = " + FTPAddressLength)
        message = message.subList(1, message.size)
        val FTPAddress = String(message.subList(0, FTPAddressLength).toByteArray())
//        L.d("FTPAddress = " + FTPAddress)

        message = message.subList(FTPAddressLength, message.size)
        val array = byteArrayOf(0, 0, 0, 0)
        array.set(0, 0)
        array.set(1, 0)
        array.set(2, message[0])
        array.set(3, message[1])
        val port = ByteUtil.bytesToInt(array, 0)
//        L.d("port = " + port)

        EventBus.getDefault().post(FTPInfo(version, FTPAddress, port, FTPUserName, FTPPwd))
    }
}