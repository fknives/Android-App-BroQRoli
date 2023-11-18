package org.fnives.android.qrcodetransfer.create

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.SequenceProtocol

@Composable
fun QRCodeVersionNumberDropdown(versionNumber: Int, setVersionNumber: (Int) -> Unit) {
    val validCodes = SequenceProtocol.validVersionCodes
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier) {
        Text(
            stringResource(R.string.version_number, versionNumber),
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable(onClick = { expanded = true })
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
        DropdownMenu(
            modifier = Modifier.sizeIn(maxHeight = 256.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            validCodes.forEach { value ->
                DropdownMenuItem(onClick = {
                    setVersionNumber(value)
                    expanded = false
                }) {
                    val text = if (versionNumber == value) {
                        stringResource(id = R.string.selected_version_number, value)
                    } else if (value == 13) {
                        stringResource(id = R.string.recommended_max, value)
                    } else {
                        "$value"
                    }
                    Text(text = text)
                }
            }
        }
    }
}