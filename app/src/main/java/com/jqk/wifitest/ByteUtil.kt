package com.example.udptest.udp

import kotlin.experimental.and
import kotlin.experimental.xor

object ByteUtil {
    fun int2Bytes(integer: Int): ByteArray {
        val bytes = ByteArray(4)
        bytes[3] = (integer shr 24).toByte()
        bytes[2] = (integer shr 16).toByte()
        bytes[1] = (integer shr 8).toByte()
        bytes[0] = integer.toByte()
        return bytes
    }

    fun bytesToInt(bytes: ByteArray, off: Int): Int {
        val b0 = bytes[off].toInt() and 0xFF
        val b1 = bytes[off + 1].toInt() and 0xFF
        val b2 = bytes[off + 2].toInt() and 0xFF
        val b3 = bytes[off + 3].toInt() and 0xFF
        return b0 shl 24 or (b1 shl 16) or (b2 shl 8) or b3
    }

    fun byteToBit(value: Byte): ByteArray {
        var value = value
        val byteArr = ByteArray(8) //一个字节八位
        for (i in 7 downTo 1) {
            byteArr[i] = (value.toInt() and 1).toByte() //获取最低位
            value = (value.toInt() shr 1).toByte() //每次右移一位
        }
        return byteArr
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (i in bytes.indices) {
            val hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    fun str2Bcd(asc: String): ByteArray {
        var asc = asc
        var len = asc.length
        val mod = len % 2
        if (mod != 0) {
            asc = "0$asc"
            len = asc.length
        }
        var abt = ByteArray(len)
        if (len >= 2) {
            len = len / 2
        }
        val bbt = ByteArray(len)
        abt = asc.toByteArray()
        var j: Int
        var k: Int
        for (p in 0 until asc.length / 2) {
            if (abt[2 * p] >= '0'.toByte() && abt[2 * p] <= '9'.toByte()) {
                j = abt[2 * p] - '0'.toByte()
            } else if (abt[2 * p] >= 'a'.toByte() && abt[2 * p] <= 'z'.toByte()) {
                j = abt[2 * p] - 'a'.toByte() + 0x0a
            } else {
                j = abt[2 * p] - 'A'.toByte() + 0x0a
            }
            if (abt[2 * p + 1] >= '0'.toByte() && abt[2 * p + 1] <= '9'.toByte()) {
                k = abt[2 * p + 1] - '0'.toByte()
            } else if (abt[2 * p + 1] >= 'a'.toByte() && abt[2 * p + 1] <= 'z'.toByte()) {
                k = abt[2 * p + 1] - 'a'.toByte() + 0x0a
            } else {
                k = abt[2 * p + 1] - 'A'.toByte() + 0x0a
            }
            val a = (j shl 4) + k
            val b = a.toByte()
            bbt[p] = b
        }
        return bbt
    }

}