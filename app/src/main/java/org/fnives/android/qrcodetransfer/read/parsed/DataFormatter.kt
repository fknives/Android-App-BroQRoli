package org.fnives.android.qrcodetransfer.read.parsed

object DataFormatter {

    fun receivedDataFormatter(data: String): String {
        return WiFiInfoFormatter.tryToParse(data)?.format() ?: data
    }
}