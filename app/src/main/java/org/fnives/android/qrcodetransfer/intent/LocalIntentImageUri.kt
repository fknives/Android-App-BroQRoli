package org.fnives.android.qrcodetransfer.intent

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.core.content.IntentCompat

val LocalIntentImageUri = compositionLocalOf<Uri?> {
    error("CompositionLocal LocalIntentImageUri not present")
}

@Composable
fun LocalIntentImageUriProvider(intent: Intent?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIntentImageUri provides intent?.uri, content = content)
}

private val Intent.uri: Uri?
    get() {
        return try {
            IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java)
        } catch (ignore: Throwable) {
            null
        }
    }