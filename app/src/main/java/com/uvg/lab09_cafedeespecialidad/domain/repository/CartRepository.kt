package com.uvg.lab09_cafedeespecialidad.domain.repository

import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun observeItems(): Flow<List<OrderItem>>

    suspend fun saveItem(item: OrderItem)

    suspend fun removeItem(productId: String)

    suspend fun clear()
}