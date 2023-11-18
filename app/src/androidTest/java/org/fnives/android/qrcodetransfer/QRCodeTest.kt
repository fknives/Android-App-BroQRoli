package org.fnives.android.qrcodetransfer

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.fnives.android.qrcodetransfer.read.ReadState
import org.fnives.android.qrcodetransfer.read.currentText
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class QRCodeTest {
    @Test
    fun simple() {
        val bitMatrix = SequenceProtocol.createBitMatrix("alma")
        val readResult = SequenceProtocol.read(bitMatrix[0].toBitmap().toBinaryBitmap())

        Assert.assertEquals(
            SequenceProtocol.SequenceInfo.NotSequence("alma"),
            readResult?.sequenceInfo
        )
        Assert.assertEquals(1, bitMatrix.size)
    }

    @Test
    fun emptyThrows() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            SequenceProtocol.createBitMatrix("")
        }
    }

    @Test
    fun sequence() {
        val text = StringBuilder("alma")
        repeat(20) {
            text.append("alma")
        }
        val bitMatrix = SequenceProtocol.createBitMatrix(text.toString())
        Assert.assertTrue(bitMatrix.size > 1)
        val readResults = bitMatrix.map {
            val bitmap = it.toBitmap()
            SequenceProtocol.read(bitmap.toBinaryBitmap())
        }

        val endResult = readResults.fold(ReadState(0, emptyMap())) { readState, it ->
            val sequenceInfo = (it?.sequenceInfo as SequenceProtocol.SequenceInfo.SequenceElement)
            readState.copy(
                length = sequenceInfo.length,
                parts = readState.parts + mapOf(sequenceInfo.current to sequenceInfo.content)
            )
        }

        Assert.assertEquals(text.toString(), endResult.currentText)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun everyASCIICharacterCanBeSend() {
        SequenceProtocol.encodeBase64 = true
        SequenceProtocol.versionCode = 4
        val inputs = (0 until 255).map { it.toChar() }
            .map { "$it" }

        val results = mutableMapOf<String, String?>()

        inputs.forEach {
            val bitmap = SequenceProtocol.createBitMatrix(it)[0].toBitmap()
            val read = SequenceProtocol.read(bitmap.toBinaryBitmap())

            results[it] = read?.sequenceInfo?.content
        }
        val notMatching = results.entries.filter { it.key != it.value }
        println(notMatching)

        results.forEach { (input, actual) ->
            Assert.assertEquals(input, actual)
        }
    }
}