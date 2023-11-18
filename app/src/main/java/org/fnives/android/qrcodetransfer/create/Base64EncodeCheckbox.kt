package org.fnives.android.qrcodetransfer.create

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Base64
import org.fnives.android.qrcodetransfer.R

@Composable
fun Base64EncodeCheckbox(encode: Boolean, setEncode: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = encode, onCheckedChange = setEncode)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(id = R.string.encode_base64))
    } 
}