package org.fnives.android.qrcodetransfer.read

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.copyToClipboard
import org.fnives.android.qrcodetransfer.isMaybeLink
import org.fnives.android.qrcodetransfer.openLink

@Composable
fun ActionRow(readState: ReadState?) {
    val context = LocalContext.current

    Row(Modifier.alpha(if (readState?.smallestMissingIndex == null) 1f else 0f)) {
        Spacer(Modifier.weight(1f))
        Button(onClick = { context.copyToClipboard(readState?.currentText.orEmpty()) }) {
            Text(stringResource(id = R.string.copy))
        }
        if (readState?.currentText?.isMaybeLink() == true) {
            Spacer(Modifier.width(16.dp))
            Button(onClick = { context.openLink(readState.currentText.orEmpty()) }) {
                Text(stringResource(id = R.string.open))
            }
        }
    }
}