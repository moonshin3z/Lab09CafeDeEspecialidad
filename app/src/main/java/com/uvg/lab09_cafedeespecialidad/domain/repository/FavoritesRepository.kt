package com.uvg.lab09_cafedeespecialidad.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun observeFavoriteIds(): Flow<Set<String>>

    suspend fun setFavorite(
        productId: String,
        isFavorite: Boolean
    )
}