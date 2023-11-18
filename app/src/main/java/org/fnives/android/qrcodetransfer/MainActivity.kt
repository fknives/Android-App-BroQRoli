package org.fnives.android.qrcodetransfer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.fnives.android.qrcodetransfer.create.CreateQRCode
import org.fnives.android.qrcodetransfer.intent.LocalIntentTextProvider
import org.fnives.android.qrcodetransfer.read.ReadQRCode
import org.fnives.android.qrcodetransfer.ui.theme.QRCodeTransferTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalIntentTextProvider(intent) {
                QRCodeTransferTheme {
                    var writerSelected by rememberSaveable { mutableStateOf(true) }
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background,
                    ) {
                        Scaffold(bottomBar = {
                            NavBar(
                                writerSelected = writerSelected,
                                setWriterSelected = { writerSelected = it })
                        }) {
                            Box(Modifier.padding(it)) {
                                AnimatedContent(targetState = writerSelected) { showWriter ->
                                    if (showWriter) {
                                        CreateQRCode()
                                    } else {
                                        ReadQRCode()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
            Toast.makeText(this, "onNewIntent: url", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun NavBar(writerSelected: Boolean, setWriterSelected: (Boolean) -> Unit) {
    BottomNavigation(Modifier.fillMaxWidth()) {
        BottomNavigationItem(
            selected = writerSelected,
            onClick = { setWriterSelected(true) },
            icon = { Icon(Icons.Filled.Create, contentDescription = null) },
            label = { Text(stringResource(id = R.string.create_qr_code)) },
        )

        BottomNavigationItem(
            selected = !writerSelected,
            onClick = { setWriterSelected(false) },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text(stringResource(id = R.string.read_qr_code)) },
        )
    }
}