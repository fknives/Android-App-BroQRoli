package org.fnives.android.qrcodetransfer.read

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.toOrdinal

@Composable
fun PageInfo(readState: ReadState?) {
    val missingIndex = readState?.smallestMissingIndex
    if (missingIndex != null) {
        Text(
            text = stringResource(
                id = R.string.next_qr_code,
                (missingIndex + 1).toOrdinal()
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}