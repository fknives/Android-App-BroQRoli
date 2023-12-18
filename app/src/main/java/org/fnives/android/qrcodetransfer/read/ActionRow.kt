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
import org.fnives.android.qrcodetransfer.read.parsed.DataFormatter

@Composable
fun ActionRow(readState: ReadState?) =
    ActionRow(
        parsedContent = readState?.currentText.orEmpty(),
        show = readState?.smallestMissingIndex == null
    )

@Composable
fun ActionRow(parsedContent: String, show: Boolean = true) {
    val context = LocalContext.current
    val formatted = DataFormatter.receivedDataFormatter(parsedContent)

    Row(Modifier.alpha(if (show) 1f else 0f)) {
        Spacer(Modifier.weight(1f))
        Button(onClick = { context.copyToClipboard(formatted) }) {
            Text(stringResource(id = R.string.copy))
        }
        if (formatted != parsedContent) {
            Spacer(Modifier.width(16.dp))
            Button(onClick = { context.copyToClipboard(parsedContent) }) {
                Text(stringResource(id = R.string.copy_raw))
            }
        }
        if (formatted.isMaybeLink()) {
            Spacer(Modifier.width(16.dp))
            Button(onClick = { context.openLink(formatted) }) {
                Text(stringResource(id = R.string.open))
            }
        }
    }
}