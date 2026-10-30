package com.uvg.lab09_cafedeespecialidad.data.local

import com.uvg.lab09_cafedeespecialidad.data.local.entity.FavoriteEntity
import com.uvg.lab09_cafedeespecialidad.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(
    private val storeDao: StoreDao
) : FavoritesRepository {

    override fun observeFavoriteIds(): Flow<Set<String>> {
        return storeDao.observeFavorites().map { favorites ->
            favorites.map { favorite ->
                favorite.productId
            }.toSet()
        }
    }

    override suspend fun setFavorite(
        productId: String,
        isFavorite: Boolean
    ) {
        if (isFavorite) {
            storeDao.insertFavorite(
                FavoriteEntity(productId = productId)
            )
        } else {
            storeDao.deleteFavorite(productId)
        }
    }
}