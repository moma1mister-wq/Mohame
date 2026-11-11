package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_THEME = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val KEY_RINGTONE_URI = stringPreferencesKey("custom_ringtone_uri")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_LAST_CONTENT_ID = stringPreferencesKey("last_content_id")
        val KEY_LAST_CONTENT_TITLE = stringPreferencesKey("last_content_title")
        val KEY_LAST_CONTENT_SUBJ = stringPreferencesKey("last_content_subj")
        val KEY_LAST_CONTENT_IS_VIDEO = booleanPreferencesKey("last_content_is_video")
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { it[KEY_USERNAME] ?: "طالب الأكاديمية" }
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "light" }
    val ringtoneUriFlow: Flow<String?> = context.dataStore.data.map { it[KEY_RINGTONE_URI] }
    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    val lastContentFlow: Flow<Triple<String, String, Boolean>?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_LAST_CONTENT_ID]
        val title = prefs[KEY_LAST_CONTENT_TITLE]
        val isVideo = prefs[KEY_LAST_CONTENT_IS_VIDEO] ?: true
        if (id != null && title != null) Triple(id, title, isVideo) else null
    }

    suspend fun setLogin(username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun setRingtoneUri(uriString: String?) {
        context.dataStore.edit { prefs ->
            if (uriString != null) {
                prefs[KEY_RINGTONE_URI] = uriString
            } else {
                prefs.remove(KEY_RINGTONE_URI)
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setLastContent(id: String, title: String, isVideo: Boolean, subjTitle: String = "") {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_CONTENT_ID] = id
            prefs[KEY_LAST_CONTENT_TITLE] = title
            prefs[KEY_LAST_CONTENT_IS_VIDEO] = isVideo
            prefs[KEY_LAST_CONTENT_SUBJ] = subjTitle
        }
    }
}
