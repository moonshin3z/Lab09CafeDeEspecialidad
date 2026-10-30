package com.uvg.lab09_cafedeespecialidad.domain.repository

import com.uvg.lab09_cafedeespecialidad.model.NewOrder
import com.uvg.lab09_cafedeespecialidad.model.Order

interface OrdersRepository {

    suspend fun createOrder(
        order: NewOrder
    ): OrderResult<Order>

    suspend fun getOrders(): OrderResult<List<Order>>

    suspend fun getOrderDetail(
        orderId: String
    ): OrderResult<Order>
}

sealed interface OrderResult<out T> {

    data class Success<T>(
        val value: T
    ) : OrderResult<T>

    data class Failure(
        val error: OrderError
    ) : OrderResult<Nothing>
}

enum class OrderError {
    NETWORK,
    SERVER,
    INVALID_DATA,
    NOT_FOUND,
    UNKNOWN
}