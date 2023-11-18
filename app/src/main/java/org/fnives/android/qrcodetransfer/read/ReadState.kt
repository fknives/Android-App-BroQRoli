package org.fnives.android.qrcodetransfer.read

data class ReadState(
    val length: Int,
    val parts: Map<Int, String>,
)

val ReadState.currentText: String
    get() = (0 until length).map {
        parts.getOrDefault(it, " ")
    }.joinToString(separator = "")

val ReadState.smallestMissingIndex: Int?
    get() =
        (0 until length).firstOrNull { parts[it] == null }