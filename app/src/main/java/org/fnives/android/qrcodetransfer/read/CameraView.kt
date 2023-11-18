package org.fnives.android.qrcodetransfer.read

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.WorkerThread
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun interface PreviewProcessor {

    @WorkerThread
    fun process(image: Bitmap)
}

@Composable
fun CameraView(
    interval: Duration,
    processImage: PreviewProcessor,
    backgroundColor: Color = Color.Black,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val bitmapReaderScope = rememberCoroutineScope()
    val bitmapStream = MutableStateFlow<Bitmap?>(null)

    ImageProcessingEffect(bitmapStream, processImage)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    setBackgroundColor(backgroundColor.toArgb())
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE


                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)

                    bitmapReaderScope.launch {
                        while (isActive) {
                            delay(interval.toMillis())
                            bitmapStream.value = bitmap
                        }
                    }
                }
            },
            onRelease = {
                cameraController.unbind()
            },
            update = {
                it.setBackgroundColor(backgroundColor.toArgb())
            }
        )
    }
}

@Composable
fun ImageProcessingEffect(
    bitmapStream: Flow<Bitmap?>,
    processImage: PreviewProcessor
) {
    DisposableEffect(processImage) {
        val processScope = CoroutineScope(Dispatchers.IO)
        processScope.launch {
            bitmapStream
                .filterNotNull()
                .collectLatest {
                    processImage.process(it)
                }
        }

        onDispose {
            processScope.cancel()
        }
    }
}