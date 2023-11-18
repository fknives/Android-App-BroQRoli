package org.fnives.android.qrcodetransfer.read

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.time.Duration
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.SequenceProtocol
import org.fnives.android.qrcodetransfer.create.Base64EncodeCheckbox
import org.fnives.android.qrcodetransfer.toBinaryBitmap


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadQRCode() {
    val permissionState = rememberPermissionState(permission = "android.permission.CAMERA")

    if (permissionState.status == PermissionStatus.Granted) {
        QRCodeReader()
    } else {
        PermissionRequester(permissionState)
    }
}

@Composable
fun QRCodeReader() {
    var readState by remember { mutableStateOf<ReadState?>(null) }
    var encodeBase64 by remember { mutableStateOf<Boolean>(SequenceProtocol.encodeBase64) }
    val textScrollState = rememberScrollState()

    Column {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            CameraView(interval = Duration.ofSeconds(1), processImage = {
                readState = processImage(it, readState)
            })
        }
        Column {
            Base64EncodeCheckbox(encode = encodeBase64, setEncode = {
                SequenceProtocol.encodeBase64 = it
                readState = null
                encodeBase64 = it
            })
            Column(
                Modifier
                    .padding(24.dp)
                    .alpha(if (readState == null) 0f else 1f)
            ) {
                PageInfo(readState = readState)

                Text(
                    text = stringResource(id = if (readState?.smallestMissingIndex == null) R.string.content_read else R.string.content_read_partial),
                    style = LocalTextStyle.current.copy(fontSize = TextUnit(12f, TextUnitType.Sp))
                )
                Spacer(Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .heightIn(max = 128.dp)
                            .verticalScroll(textScrollState),
                        text = readState?.currentText.orEmpty(),
                    )
                }

                ActionRow(readState)
            }
        }
    }
}

fun processImage(bitmap: Bitmap, readState: ReadState?): ReadState? {
    val readResult = SequenceProtocol.read(bitmap.toBinaryBitmap()) ?: return readState
    if (readResult.sequenceInfo is SequenceProtocol.SequenceInfo.NotSequence) {
        return ReadState(
            length = 1,
            parts = mapOf(0 to readResult.sequenceInfo.content)
        )
    }
    if (readResult.sequenceInfo is SequenceProtocol.SequenceInfo.SequenceElement) {
        val currentMap =
            mapOf(readResult.sequenceInfo.current to readResult.sequenceInfo.content)
        val parts =
            if (readResult.sequenceInfo.current == 0 || readState?.length != readResult.sequenceInfo.length) {
                currentMap
            } else {
                currentMap + readState.parts
            }
        return ReadState(
            length = readResult.sequenceInfo.length,
            parts = parts
        )
    }
    return readState
}