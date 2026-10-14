package com.uvg.lab09_cafedeespecialidad.remote

import com.uvg.lab09_cafedeespecialidad.model.CheckoutUiState
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.Product
import java.math.BigDecimal
import java.math.RoundingMode

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
            unitPrice = product.price.roundToCents(),
            quantity = orderItem.quantity,
            subtotal = (
                product.price * orderItem.quantity
            ).roundToCents()
        )
    }

    return NewOrderDto(
        createdAt = createdAt,
        lines = lines,
        total = lines.sumOf { line -> line.subtotal }.roundToCents(),
        billingType = checkout.billingType.name,
        paymentMethod = checkout.paymentMethod.name
    )
}
private fun Double.roundToCents(): Double {
    return BigDecimal.valueOf(this)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
}