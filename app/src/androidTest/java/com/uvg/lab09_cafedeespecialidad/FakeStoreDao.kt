package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.data.local.StoreDao
import com.uvg.lab09_cafedeespecialidad.data.local.entity.FavoriteEntity
import com.uvg.lab09_cafedeespecialidad.data.local.entity.OrderLineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStoreDao : StoreDao {

    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private val orderLines = MutableStateFlow<Map<String, Int>>(emptyMap())

    override fun observeFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteIds.map { ids ->
            ids.sorted().map { productId ->
                FavoriteEntity(productId = productId)
            }
        }
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteIds.value = favoriteIds.value + favorite.productId
    }

    override suspend fun deleteFavorite(productId: String) {
        favoriteIds.value = favoriteIds.value - productId
    }

    override fun observeOrderLines(): Flow<List<OrderLineEntity>> {
        return orderLines.map { lines ->
            lines.entries
                .sortedBy { it.key }
                .map { entry ->
                    OrderLineEntity(
                        productId = entry.key,
                        quantity = entry.value
                    )
                }
        }
    }

    override suspend fun upsertOrderLine(orderLine: OrderLineEntity) {
        orderLines.value = orderLines.value + (
            orderLine.productId to orderLine.quantity
        )
    }

    override suspend fun deleteOrderLine(productId: String) {
        orderLines.value = orderLines.value - productId
    }

    override suspend fun clearOrderLines() {
        orderLines.value = emptyMap()
    }
}