package com.uvg.lab09_cafedeespecialidad.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.uvg.lab09_cafedeespecialidad.domain.repository.CatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreCatalogPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : CatalogPreferencesRepository {

    override fun observeSortOrder(): Flow<CatalogSortOrder> {
        return dataStore.data.map { preferences ->
            CatalogSortOrder.fromStorageValue(
                preferences[StorePreferencesKeys.CATALOG_SORT_ORDER]
                    ?: CatalogSortOrder.NAME.storageValue
            )
        }
    }

    override suspend fun setSortOrder(
        sortOrder: CatalogSortOrder
    ) {
        dataStore.edit { preferences ->
            preferences[StorePreferencesKeys.CATALOG_SORT_ORDER] =
                sortOrder.storageValue
        }
    }
}