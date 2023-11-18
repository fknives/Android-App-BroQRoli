package org.fnives.android.qrcodetransfer.intent

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalIntentText = compositionLocalOf<String?> {
    error("CompositionLocal LocalIntentText not present")
}

@Composable
fun LocalIntentTextProvider(intent: Intent?, content: @Composable () -> Unit) {
    val message= intent?.getStringExtra(Intent.EXTRA_TEXT)
    CompositionLocalProvider(LocalIntentText provides message, content = content)
}