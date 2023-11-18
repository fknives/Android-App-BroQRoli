package org.fnives.android.qrcodetransfer

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.CharacterSetECI
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.max
import kotlin.math.min
import com.google.zxing.Result as QRCodeResult

// Since zxing only supports reading structured append and I cannot be bothered to modify its encoding
// I simply add header to the "content" of the QR code
// Format: starts with "S://[C][L]" where C is the current index and L is length
// C & L are binary numbers aka character A means 65 (ASCII)
// is this naive? yes, but it seems to be enough for my use case
object SequenceProtocol {

    private val writer by lazy { QRCodeWriter() }
    private val reader by lazy { QRCodeReader() }
    private val maxSizeMap = mutableMapOf<Int, Int>()
    private val maxSize get() = maxSizeMap[versionCode] ?: findMaxSize().also { maxSizeMap[versionCode] = it }
    private val formatCurrent = 'C'
    private val formatLength = 'L'
    private val formatPrefix = "S://"
    private val format = "${formatPrefix}${formatCurrent}${formatLength}"
    var versionCode: Int = 4
        set(value) {
            field = max(min(value, validVersionCodes.last), validVersionCodes.first)
        }
    val validVersionCodes = 2 until 40
    var encodeBase64: Boolean = false

    @OptIn(ExperimentalEncodingApi::class)
    @Throws
    fun createBitMatrix(text: String): List<BitMatrix> {
        val message = base64Encode(text)
        if (message.length < maxSize) {
            return listOf(encode(message))
        }

        val contentThatFits = (maxSize - format.length)
        val chunks = message.chunked(contentThatFits)
        val formatWithLength = format.replace(formatLength, chunks.size.toChar())
        val messages = chunks.mapIndexed { index, s ->
            val prefix = formatWithLength.replace(formatCurrent, index.toChar())
            "${prefix}${s}".also {
                println("MYLOG${index} $it")
                println("MYLOG$it")
            }
        }
        return messages.map {
            encode(it)
        }
    }

    @Throws
    fun read(binaryBitmap: BinaryBitmap): ReadResult? {
        val result = decode(binaryBitmap) ?: return null

        if (!result.text.startsWith(formatPrefix)) {
            return ReadResult(SequenceInfo.NotSequence(base64Decode(result.text)), result)
        }

        val remaining = result.text.drop(formatPrefix.length)
        val current = remaining[0]
        val length = remaining[1]
        val content = base64Decode(remaining.drop(2))
        return ReadResult(
            SequenceInfo.SequenceElement(
                current = current.code,
                length = length.code,
                content = content,
            ),
            result
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64Decode(text: String) =
        if (encodeBase64) {
            Base64.decode(text).toString(Charsets.UTF_16)
        } else {
            text
        }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64Encode(text: String) =
        if (encodeBase64) {
            Base64.encode(text.toByteArray(Charsets.UTF_16))
        } else {
            text
        }

    private fun decode(binaryBitmap: BinaryBitmap) =
        try {
            reader.decode(binaryBitmap)
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            null
        }

    private fun encode(message: String): BitMatrix {
        return writer.encode(
            message,
            BarcodeFormat.QR_CODE,
            256,
            256,
            mapOf(
                EncodeHintType.CHARACTER_SET to CharacterSetECI.ASCII,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.QR_VERSION to versionCode,
                EncodeHintType.MARGIN to max(versionCode / 2, 3)
            )
        )
    }

    /**
     * Naive method to find the max size we can fit into our encoding ¯\_(ツ)_/¯
     */
    fun findMaxSize(): Int {
        val msg = StringBuilder("a")
        var maxLength: Int? = null
        while (maxLength == null) {
            try {
                encode(msg.toString())
            } catch (e: Throwable) {
                maxLength = msg.length
            }
            msg.append("a")
        }
        return maxLength - 1
    }

    data class ReadResult(
        val sequenceInfo: SequenceInfo,
        val underlyingResult: QRCodeResult
    )

    // whether we are dealing with normal QR Code or "sequenced" one
    sealed interface SequenceInfo {

        abstract val content: String

        data class NotSequence(override val content: String) : SequenceInfo
        data class SequenceElement(
            val current: Int,
            val length: Int,
            override val content: String
        ) : SequenceInfo
    }
}