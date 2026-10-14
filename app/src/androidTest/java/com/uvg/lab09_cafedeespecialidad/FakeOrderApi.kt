package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.remote.NewOrderDto
import com.uvg.lab09_cafedeespecialidad.remote.OrderApi
import com.uvg.lab09_cafedeespecialidad.remote.OrderDto
import java.io.IOException

class FakeOrderApi(
    private val shouldFailOnCreate: Boolean = false
) : OrderApi {

    val sentOrders = mutableListOf<NewOrderDto>()

    override suspend fun getOrders(
        sortBy: String,
        order: String
    ): List<OrderDto> {
        return emptyList()
    }

    override suspend fun getOrderDetail(
        orderId: String
    ): OrderDto {
        throw UnsupportedOperationException(
            "Esta prueba no usa el detalle de pedidos."
        )
    }

    override suspend fun createOrder(
        order: NewOrderDto
    ): OrderDto {
        sentOrders += order

        if (shouldFailOnCreate) {
            throw IOException("Fallo simulado de red")
        }

        return OrderDto(
            createdAt = order.createdAt,
            lines = order.lines,
            total = order.total,
            billingType = order.billingType,
            paymentMethod = order.paymentMethod,
            id = "test-order-1"
        )
    }
}