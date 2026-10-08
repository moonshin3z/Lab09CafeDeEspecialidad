package com.uvg.lab09_cafedeespecialidad.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Única instancia de Preferences DataStore para toda la app.
val Context.storePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "store_preferences"
)

object StorePreferencesKeys {
    val CATALOG_SORT_ORDER = stringPreferencesKey("catalog_sort_order")
}
