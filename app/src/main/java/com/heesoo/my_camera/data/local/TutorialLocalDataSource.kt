package com.heesoo.my_camera.data.local

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private val Context.tutorialDataStore by preferencesDataStore(
    name = "onboarding",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

internal interface TutorialLocalDataSource {
    suspend fun isCompleted(): Boolean

    suspend fun markCompleted()
}

internal class TutorialLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TutorialLocalDataSource {
    override suspend fun isCompleted(): Boolean =
        context.tutorialDataStore.data.first()[COMPLETED_KEY] == true

    override suspend fun markCompleted() {
        context.tutorialDataStore.edit { preferences ->
            preferences[COMPLETED_KEY] = true
        }
    }

    private companion object {
        val COMPLETED_KEY = booleanPreferencesKey("tutorial_completed")
    }
}
