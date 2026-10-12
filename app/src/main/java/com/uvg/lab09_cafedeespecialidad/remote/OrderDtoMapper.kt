package com.uvg.lab09_cafedeespecialidad.remote

import com.uvg.lab09_cafedeespecialidad.model.CheckoutUiState
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.Product

fun buildNewOrderDto(
    products: List<Product>,
    orderItems: List<OrderItem>,
    checkout: CheckoutUiState,
    createdAt: Long
): NewOrderDto {
    val productsById = products.associateBy { product ->
        product.id
    }

    val lines = orderItems.mapNotNull { orderItem ->
        val product = productsById[orderItem.productId]
            ?: return@mapNotNull null

        OrderLineDto(
            productId = product.id,
            productName = product.name,
            unitPrice = product.price,
            quantity = orderItem.quantity,
            subtotal = product.price * orderItem.quantity
        )
    }

    return NewOrderDto(
        createdAt = createdAt,
        lines = lines,
        total = lines.sumOf { line -> line.subtotal },
        billingType = checkout.billingType.name,
        paymentMethod = checkout.paymentMethod.name
    )
}