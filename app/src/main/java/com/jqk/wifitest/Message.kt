package com.jqk.wifitest

data class Message(
    val head: Byte,
    val property: ByteArray,
    val deviceID: ByteArray,
    val mesID: Byte,
    val mes: ByteArray,
    var check: Byte
) {
    fun getMessage(): ByteArray {
        val data = mutableListOf<Byte>()
        data.add(head)
        data.addAll(property.toList())
        data.addAll(deviceID.toList())
        data.add(mesID)
        data.addAll(mes.toList())
        data.add(check)

        return data.toByteArray()
    }

    fun getMessageBody(): ByteArray {
        val data = mutableListOf<Byte>()
        data.addAll(property.toList())
        data.addAll(deviceID.toList())
        data.add(mesID)
        data.addAll(mes.toList())

        return data.toByteArray()
    }
}

data class Mes0x01(
    val check: Byte,
    val carBrandColor: Byte,
    val carBrand: ByteArray,
    val flow: ByteArray,
    val softwareVersionLength: Byte,
    val softwareVersion: ByteArray
) {
    fun getMes0x01(): ByteArray {
        val data = mutableListOf<Byte>()
        data.add(check)
        data.add(carBrandColor)
        data.addAll(carBrand.toList())
        data.addAll(flow.toList())
        data.add(softwareVersionLength)
        data.addAll(softwareVersion.toList())
        return data.toByteArray()
    }
}

data class Mes0x03(val datas: List<Mes0x03Child>) {
    fun getMes0x03(): ByteArray {
        val data = mutableListOf<Byte>()
        data.add(datas.size.toByte())
        for (i in datas) {
            data.addAll(i.getChild())
        }
        return data.toByteArray()
    }
}

data class Mes0x03Child(val id: Byte, val length: ByteArray, val value: ByteArray) {
    fun getChild(): List<Byte> {
        val data = mutableListOf<Byte>()
        data.add(id)
        data.addAll(length.toList())
        data.addAll(value.toList())
        return data
    }
}