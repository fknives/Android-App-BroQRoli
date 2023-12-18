package org.fnives.android.qrcodetransfer.read.image

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.fnives.android.qrcodetransfer.BuildConfig
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.SequenceProtocol
import org.fnives.android.qrcodetransfer.create.Base64EncodeCheckbox
import org.fnives.android.qrcodetransfer.read.ActionRow
import org.fnives.android.qrcodetransfer.read.parsed.DataFormatter
import org.fnives.android.qrcodetransfer.storage.LocalAppPreferences
import org.fnives.android.qrcodetransfer.toBinaryBitmap


@Composable
fun ImageReadQRCode(imageUri: Uri, onErrorLoadingFile: () -> Unit) {
    val imageBitmap = imageUri.toBitmap()
    LaunchedEffect(imageBitmap) {
        if (imageBitmap == null) {
            onErrorLoadingFile()
        }
    }
    if (imageBitmap == null) {
        return
    }

    val appPreferences = LocalAppPreferences.current
    val encodeBase64 by appPreferences.encodeBase64.collectAsState(initial = SequenceProtocol.encodeBase64)
    val imageParsedContent = remember(imageUri, encodeBase64) {
        try {
            SequenceProtocol.read(imageBitmap.toBinaryBitmap())?.sequenceInfo?.content
        } catch (ignored: Throwable) {
            if (BuildConfig.DEBUG) {
                ignored.printStackTrace()
            }
            null
        }
    }
    val textScrollState = rememberScrollState()

    Column {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = imageBitmap.asImageBitmap(),
                contentScale = ContentScale.Fit,
                contentDescription = null
            )
        }
        Column {
            Base64EncodeCheckbox(encode = encodeBase64, setEncode = {
                SequenceProtocol.encodeBase64 = it
                appPreferences.setEncodeBase64(SequenceProtocol.encodeBase64)
            })
            Column(
                Modifier.padding(24.dp)
            ) {
                if (imageParsedContent == null) {
                    Text(
                        text = stringResource(id = R.string.could_not_read_content),
                        style = LocalTextStyle.current.copy(fontSize = TextUnit(16f, TextUnitType.Sp))
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.content_read),
                        style = LocalTextStyle.current.copy(fontSize = TextUnit(12f, TextUnitType.Sp))
                    )

                    Spacer(Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .heightIn(max = 128.dp)
                                .verticalScroll(textScrollState),
                            text = DataFormatter.receivedDataFormatter(imageParsedContent),
                        )
                    }

                    ActionRow(imageParsedContent)
                }
            }
        }
    }
}