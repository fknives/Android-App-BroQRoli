package org.fnives.android.qrcodetransfer.read.parsed

import org.fnives.android.qrcodetransfer.BuildConfig

object WiFiInfoFormatter {

    private const val PREFIX = "WIFI:"
    private const val EXTRA_KEY = "EXTRA"
    private const val NAME_KEY = "S"
    private const val PASSWORD_KEY = "P"
    private const val SECURITY_KEY = "T"
    private const val HIDDEN_KEY = "H"

    fun tryToParse(data: String): WifiInfo? {
        if (data.startsWith(PREFIX)) {
            try {
                val result = data.drop(PREFIX.length).split(";").map {
                    if (it.contains(":")) {
                        val (key, value) = it.split(":")
                        key to value
                    } else {
                        EXTRA_KEY to it
                    }
                }.toMap()

                return WifiInfo(
                    name = result[NAME_KEY]
                        ?: throw IllegalArgumentException("Could not find name"),
                    password = result[PASSWORD_KEY]
                        ?: throw IllegalArgumentException("Could not find password"),
                    security = result[SECURITY_KEY].orEmpty(),
                    extra = result[EXTRA_KEY].orEmpty(),
                    hidden = result[HIDDEN_KEY] == "true",
                )
            } catch (ignored: Throwable) {
                if (BuildConfig.DEBUG) {
                    ignored.printStackTrace()
                }
                return null
            }
        } else {
            return null
        }
    }
}

data class WifiInfo(
    val name: String,
    val security: String,
    val password: String,
    val hidden: Boolean,
    val extra: String,
)

fun WifiInfo.format(): String =
    """
name: $name
password: $password
hidden: $hidden
security: $security
extra: $extra
    """.trimIndent()