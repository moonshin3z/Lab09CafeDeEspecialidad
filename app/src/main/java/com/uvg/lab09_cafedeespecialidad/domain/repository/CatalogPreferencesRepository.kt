package com.uvg.lab09_cafedeespecialidad.domain.repository

import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import kotlinx.coroutines.flow.Flow

interface CatalogPreferencesRepository {

    fun observeSortOrder(): Flow<CatalogSortOrder>

    suspend fun setSortOrder(
        sortOrder: CatalogSortOrder
    )
}