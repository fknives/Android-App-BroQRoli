package org.fnives.android.qrcodetransfer.create

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.SequenceProtocol
import org.fnives.android.qrcodetransfer.intent.LocalIntentText
import org.fnives.android.qrcodetransfer.storage.LocalAppPreferences
import org.fnives.android.qrcodetransfer.toBitmap


@Composable
fun CreateQRCode() {
    var bitmaps by remember { mutableStateOf(listOf<Bitmap>()) }
    var bitmapIndex by remember(bitmaps) { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Box(Modifier.weight(1f)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            QRCodeCarousel(
                bitmaps = bitmaps,
                bitmapIndex = bitmapIndex,
                setBitmapIndex = { bitmapIndex = it },
            )
        }
        QRCodeContentInput(
            bitmaps = bitmaps,
            setBitmaps = {
                bitmaps.forEach { it.recycle() }
                bitmaps = it
            },
            setLoading = { loading = it }
        )
    }
}

@Composable
fun QRCodeCarousel(
    bitmaps: List<Bitmap>,
    bitmapIndex: Int,
    setBitmapIndex: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        val imageBitmap = remember(bitmaps, bitmapIndex) {
            if (bitmapIndex < bitmaps.size) {
                bitmaps[bitmapIndex].asImageBitmap()
            } else {
                null
            }
        }

        if (imageBitmap != null) {
            Image(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                bitmap = imageBitmap,
                contentDescription = "",
                contentScale = ContentScale.Fit
            )

            if (bitmaps.size > 1) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isAfterFirst = bitmapIndex > 0
                    val isBeforeLast = bitmapIndex + 1 < bitmaps.size
                    Button(
                        modifier = Modifier.alpha(if (isAfterFirst) 1f else 0f),
                        onClick = {
                            if (isAfterFirst) {
                                setBitmapIndex(bitmapIndex - 1)
                            }
                        }) {
                        Text(stringResource(id = R.string.previous))
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${bitmapIndex + 1} / ${bitmaps.size}")
                    Spacer(Modifier.weight(1f))
                    Button(
                        modifier = Modifier.alpha(if (isBeforeLast) 1f else 0f),
                        onClick = {
                            if (isBeforeLast) {
                                setBitmapIndex(bitmapIndex + 1)
                            }
                        }) {
                        Text(stringResource(id = R.string.next))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

class JobHolder(var job: Job?) {
    fun cancel() = job?.cancel()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QRCodeContentInput(
    bitmaps: List<Bitmap>,
    setLoading: (Boolean) -> Unit,
    setBitmaps: (List<Bitmap>) -> Unit,
) {
    val messageFromIntent = LocalIntentText.current
    var content by rememberSaveable(messageFromIntent) { mutableStateOf(messageFromIntent.orEmpty()) }
    val appPreferences = LocalAppPreferences.current
    val number by appPreferences.versionCode.collectAsState(initial = SequenceProtocol.versionCode)
    SequenceProtocol.versionCode = number
    val encodeBase64 by appPreferences.encodeBase64.collectAsState(initial = SequenceProtocol.encodeBase64)
    SequenceProtocol.encodeBase64 = encodeBase64
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }
    val holder = remember(coroutineScope) { JobHolder(null) }

    val createBitmaps = remember(bitmaps, content) {
        return@remember fun() {
            if (content.isBlank()) return
            keyboardController?.hide()
            setLoading(true)
            holder.cancel()
            holder.job = coroutineScope.launch {
                val matrix = SequenceProtocol.createBitMatrix(content)
                val newBitmaps = matrix.map { it.toBitmap() }
                withContext(Dispatchers.Main) {
                    setBitmaps(newBitmaps)
                    setLoading(false)
                }
            }
        }
    }
    val inputScrollState = rememberScrollState()

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxHeight = 128.dp)
            .verticalScroll(inputScrollState),
        label = { Text(stringResource(id = R.string.qr_code_content)) },
        value = content,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { createBitmaps() }),
        onValueChange = {
            setBitmaps(emptyList())
            content = it
        })
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            QRCodeVersionNumberDropdown(number, setVersionNumber = {
                // protocol does additional checks so we follow it's lead
                SequenceProtocol.versionCode = it
                appPreferences.setVersionCode(SequenceProtocol.versionCode)
                createBitmaps()
            })
            Base64EncodeCheckbox(encode = encodeBase64, setEncode = {
                SequenceProtocol.encodeBase64 = it
                appPreferences.setEncodeBase64(SequenceProtocol.encodeBase64)
                createBitmaps()
            })
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = createBitmaps) {
            Text(stringResource(id = R.string.create))
        }
    }
}
