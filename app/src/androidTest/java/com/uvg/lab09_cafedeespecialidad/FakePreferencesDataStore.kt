package com.uvg.lab09_cafedeespecialidad

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesDataStore : DataStore<Preferences> {

    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences
    ): Preferences {
        val updatedPreferences = transform(preferences.value)
        preferences.value = updatedPreferences
        return updatedPreferences
    }
}
