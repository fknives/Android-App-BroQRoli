package org.fnives.android.qrcodetransfer.storage

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppPreferences(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val Context.store: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore get() = context.store
    private val versionCodeKey = intPreferencesKey("versionCode")
    val versionCode: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[versionCodeKey] ?: 4
        }

    fun setVersionCode(versionCode: Int) {
        scope.launch {
            dataStore.edit {
                it[versionCodeKey] = versionCode
            }
        }
    }

    private val encodeBase64Key = booleanPreferencesKey("encodeBase64")
    val encodeBase64: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[encodeBase64Key] ?: false
        }

    fun setEncodeBase64(encodeBase64: Boolean) {
        scope.launch {
            dataStore.edit {
                it[encodeBase64Key] = encodeBase64
            }
        }
    }
}

val LocalAppPreferences = compositionLocalOf<AppPreferences> {
    error("CompositionLocal LocalIntentText not present")
}

@SuppressLint("StaticFieldLeak")
private var appPreferences: AppPreferences? = null

@Composable
fun LocalAppPreferencesProvider(context: Context, content: @Composable () -> Unit) {
    val preferences = appPreferences ?: AppPreferences(context.applicationContext).also { appPreferences = it }
    CompositionLocalProvider(LocalAppPreferences provides preferences, content = content)
}