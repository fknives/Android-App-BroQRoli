package org.fnives.android.qrcodetransfer.intent

import android.content.Intent
import androidx.compose.runtime.Composable

@Composable
fun LocalIntentProvider(intent: Intent?, content: @Composable () -> Unit) {
    LocalIntentImageUriProvider(intent) {
        LocalIntentTextProvider(intent, content)
    }
}