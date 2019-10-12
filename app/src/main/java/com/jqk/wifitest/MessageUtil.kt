package com.jqk.wifitest

import com.example.udptest.udp.AppConstants
import com.example.udptest.udp.ByteUtil
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

class MessageUtil private constructor() {
    val headData = getHead()
    var property = byteArrayOf(0, 0)
    val deviceID = getDeviceID().toByteArray(Charset.forName("GBK"))
    var mesID = 0
    var mes = byteArrayOf()
    var check: Byte = 0

    companion object {
        val instance: MessageUtil by lazy {
            MessageUtil()
        }
    }

    fun getMessage(messageID: Int): ByteArray {
        mesID = messageID
        when (messageID) {
            AppConstants.MESID_0X01 -> {
                mes = get0x01()
                property = getProperty(mes.size)

                val message = Message(headData, property, deviceID, mesID.toByte(), mes, check)
                check = getCheck(message.getMessageBody())
                message.check = check

                return message.getMessage()
            }
            AppConstants.MESID_0X02 -> {
                mes = get0x02()
                property = getProperty(mes.size)

                val message = Message(headData, property, deviceID, mesID.toByte(), mes, check)
                check = getCheck(message.getMessageBody())
                message.check = check

                return message.getMessage()
            }
            AppConstants.MESID_0X03 -> {
                mes = get0x03()
                property = getProperty(mes.size)

                val message = Message(headData, property, deviceID, mesID.toByte(), mes, check)
                check = getCheck(message.getMessageBody())
                message.check = check

                return message.getMessage()
            }
            else -> {
                return Message(0, byteArrayOf(), byteArrayOf(), 0, byteArrayOf(), 0).getMessage()
            }
        }
    }


    fun get0x01(): ByteArray {
        val check = getCheck(getDeviceID().toByteArray(Charset.forName("GBK")))
        val carBrandColor = ByteUtil.int2Bytes(getCarBrandColor())[0]
        val carBrand = Arrays.copyOf(getCarBrand().toByteArray(Charset.forName("GBK")), 8)
        val flow = ByteUtil.int2Bytes(getFlow())
        val softwareVersion = getSoftwareVersion().toByteArray(Charset.forName("GBK"))
        val softwareVersionLength = softwareVersion.size.toByte()

        var mes0x01 = Mes0x01(check, carBrandColor, carBrand, flow, softwareVersionLength, softwareVersion)

        return mes0x01.getMes0x01()
    }

    fun get0x02(): ByteArray {
        var mes0x02 = byteArrayOf()
        return mes0x02
    }

    fun get0x03(): ByteArray {
        val data = mutableListOf<Mes0x03Child>()
        val data1 = Mes0x03Child(0x03, byteArrayOf("测2222".toByteArray(Charset.forName("GBK")).size.toByte()), "测2222".toByteArray(Charset.forName("GBK")))
        val data2 = Mes0x03Child(0x09, byteArrayOf(ByteUtil.str2Bcd("18888888888").size.toByte()), ByteUtil.str2Bcd("18888888888"))

        data.add(data1)
        data.add(data2)

        return Mes0x03(data).getMes0x03()
    }

    fun getCheck(list: ByteArray): Byte {
        var check = list[0]
        for (i in 1..list.size - 1) {
            check = check xor list[i]
        }
        return check
    }

    fun getProperty(length: Int): ByteArray {
        val lengthB = ByteUtil.int2Bytes(length)
        val property = byteArrayOf(0, 0)
        property.set(1, lengthB[0])
        var byte = lengthB[1] and 3
        property.set(0, byte)
//        L.d("翻转的数字 = " + ByteUtil.bytes2Int(property))

        byte = byte or (getDeviceMark() shl 2).toByte()
        property.set(0, byte)
        return property
    }

    fun getHead(): Byte {
        return 0x7F
    }

    fun getDeviceID(): String {
        return "0123456"
    }

    fun getCarBrand(): String {
        return "测123456"
    }

    fun getSoftwareVersion(): String {
        return "1.0.0113.160330"
    }

    fun getCarBrandColor(): Int {
        return 2
    }

    fun getFlow(): Int {
        return 0
    }

    fun getDeviceMark(): Int {
        return AppConstants.DEVICE_MARK
    }
}