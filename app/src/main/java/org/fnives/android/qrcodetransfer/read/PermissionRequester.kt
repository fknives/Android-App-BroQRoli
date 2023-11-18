package org.fnives.android.qrcodetransfer.read

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.fnives.android.qrcodetransfer.R
import org.fnives.android.qrcodetransfer.openAppSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequester(permissionState: PermissionState) {
    var wasRequested by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Text(stringResource(id = R.string.camera_required))
        Button(onClick = {
            if (wasRequested && !permissionState.status.shouldShowRationale) {
                context.openAppSettings()
            } else {
                permissionState.launchPermissionRequest()
                wasRequested = true
            }
        }) {
            if (wasRequested && !permissionState.status.shouldShowRationale) {
                Text(stringResource(id = R.string.open_settings))
            } else {
                Text(stringResource(id = R.string.allow))
            }
        }
        Spacer(Modifier.weight(1f))
    }
}