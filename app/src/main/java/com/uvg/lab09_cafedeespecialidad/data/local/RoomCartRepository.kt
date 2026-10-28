package com.uvg.lab09_cafedeespecialidad.data.local

import com.uvg.lab09_cafedeespecialidad.data.local.entity.OrderLineEntity
import com.uvg.lab09_cafedeespecialidad.domain.repository.CartRepository
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCartRepository(
    private val storeDao: StoreDao
) : CartRepository {

    override fun observeItems(): Flow<List<OrderItem>> {
        return storeDao.observeOrderLines().map { lines ->
            lines.map { line ->
                OrderItem(
                    productId = line.productId,
                    quantity = line.quantity
                )
            }
        }
    }

    override suspend fun saveItem(item: OrderItem) {
        if (item.quantity <= 0) {
            storeDao.deleteOrderLine(item.productId)
        } else {
            storeDao.upsertOrderLine(
                OrderLineEntity(
                    productId = item.productId,
                    quantity = item.quantity
                )
            )
        }
    }

    override suspend fun removeItem(productId: String) {
        storeDao.deleteOrderLine(productId)
    }

    override suspend fun clear() {
        storeDao.clearOrderLines()
    }
}