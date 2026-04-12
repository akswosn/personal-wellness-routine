package com.forlks.personal_wellness_routine.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wellflow_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_CHARACTER_TYPE = stringPreferencesKey("character_type")   // CAT, DOG, PLANT, FAIRY, DRAGON, RABBIT
        val KEY_CHARACTER_NAME = stringPreferencesKey("character_name")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_ADMOB_APP_ID = stringPreferencesKey("admob_app_id")
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_FIRST_LAUNCH] ?: true
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val characterType: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CHARACTER_TYPE] ?: "CAT"
    }

    val characterName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CHARACTER_NAME] ?: "솔이"
    }

    val isOnboardingDone: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { prefs -> prefs[KEY_IS_FIRST_LAUNCH] = false }
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_NAME] = name }
    }

    suspend fun saveCharacter(type: String, name: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CHARACTER_TYPE] = type
            prefs[KEY_CHARACTER_NAME] = name
        }
    }

    suspend fun setOnboardingDone() {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = true
            prefs[KEY_IS_FIRST_LAUNCH] = false
        }
    }
}
