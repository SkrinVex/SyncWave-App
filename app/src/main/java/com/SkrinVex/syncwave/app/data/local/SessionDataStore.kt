package com.SkrinVex.syncwave.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.SkrinVex.syncwave.app.domain.model.AuthSession
import com.SkrinVex.syncwave.app.domain.model.User
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "syncwave_session")

class SessionDataStore(private val context: Context, private val gson: Gson = Gson()) {

    companion object {
        val KEY_TOKEN = stringPreferencesKey("jwt_token")
        val KEY_USER_JSON = stringPreferencesKey("user_json")
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_AUDIO_FOCUS = booleanPreferencesKey("audio_focus_enabled")
        const val DEFAULT_SERVER_URL = "https://syncwave.skrinvex.com"
    }

    @Volatile
    private var _cachedServerUrl: String = DEFAULT_SERVER_URL

    @Volatile
    private var _cachedToken: String? = null

    @Volatile
    private var _cachedUser: User? = null

    @Volatile
    private var _cachedAudioFocus: Boolean = true

    init {
        // Preload memory cache from DataStore asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.dataStore.data.first()
                prefs[KEY_SERVER_URL]?.takeIf { it.isNotBlank() }?.let { _cachedServerUrl = it }
                prefs[KEY_TOKEN]?.takeIf { it.isNotBlank() }?.let { _cachedToken = it }
                prefs[KEY_AUDIO_FOCUS]?.let { _cachedAudioFocus = it }
                prefs[KEY_USER_JSON]?.takeIf { it.isNotBlank() }?.let {
                    _cachedUser = gson.fromJson(it, User::class.java)
                }
            } catch (_: Exception) {}
        }
    }

    val serverUrlFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val url = preferences[KEY_SERVER_URL]?.takeIf { it.isNotBlank() } ?: DEFAULT_SERVER_URL
            _cachedServerUrl = url
            url
        }

    val tokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val token = preferences[KEY_TOKEN]?.takeIf { it.isNotBlank() }
            _cachedToken = token
            token
        }

    val audioFocusEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val enabled = preferences[KEY_AUDIO_FOCUS] ?: true
            _cachedAudioFocus = enabled
            enabled
        }

    val sessionFlow: Flow<AuthSession?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val token = preferences[KEY_TOKEN] ?: return@map null
            val userJson = preferences[KEY_USER_JSON] ?: return@map null
            val serverUrl = preferences[KEY_SERVER_URL]?.takeIf { it.isNotBlank() } ?: DEFAULT_SERVER_URL

            try {
                val user = gson.fromJson(userJson, User::class.java)
                _cachedToken = token
                _cachedUser = user
                _cachedServerUrl = serverUrl
                AuthSession(token = token, user = user, serverUrl = serverUrl)
            } catch (e: Exception) {
                null
            }
        }

    fun getServerUrlCached(): String = _cachedServerUrl

    fun getTokenCached(): String? = _cachedToken

    fun isAudioFocusEnabledCached(): Boolean = _cachedAudioFocus

    fun getSessionCached(): AuthSession? {
        val token = _cachedToken ?: return null
        val user = _cachedUser ?: return null
        return AuthSession(token = token, user = user, serverUrl = _cachedServerUrl)
    }

    suspend fun getServerUrl(): String {
        return try {
            serverUrlFlow.first()
        } catch (_: Exception) {
            _cachedServerUrl
        }
    }

    suspend fun saveServerUrl(url: String) {
        val cleanUrl = url.trim().trimEnd('/')
        _cachedServerUrl = cleanUrl
        context.dataStore.edit { preferences ->
            preferences[KEY_SERVER_URL] = cleanUrl
        }
    }

    suspend fun setAudioFocusEnabled(enabled: Boolean) {
        _cachedAudioFocus = enabled
        context.dataStore.edit { preferences ->
            preferences[KEY_AUDIO_FOCUS] = enabled
        }
    }

    suspend fun saveSession(token: String, user: User) {
        _cachedToken = token
        _cachedUser = user
        val userJson = gson.toJson(user)
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            preferences[KEY_USER_JSON] = userJson
        }
    }

    suspend fun getSavedSession(): AuthSession? {
        return try {
            sessionFlow.first()
        } catch (_: Exception) {
            getSessionCached()
        }
    }

    suspend fun clearSession() {
        _cachedToken = null
        _cachedUser = null
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_JSON)
        }
    }
}
