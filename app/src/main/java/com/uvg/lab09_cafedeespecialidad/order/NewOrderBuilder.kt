package com.uvg.lab09_cafedeespecialidad.order

import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.NewOrder
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.OrderLine
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import com.uvg.lab09_cafedeespecialidad.model.Product
import java.math.BigDecimal
import java.math.RoundingMode

fun buildNewOrder(
    products: List<Product>,
    orderItems: List<OrderItem>,
    billingType: BillingType,
    paymentMethod: PaymentMethod,
    createdAt: Long
): NewOrder {
    val productsById = products.associateBy { product ->
        product.id
    }

    val lines = orderItems.mapNotNull { item ->
        val product = productsById[item.productId]
            ?: return@mapNotNull null

        OrderLine(
            productId = product.id,
            productName = product.name,
            unitPrice = product.price.roundToCents(),
            quantity = item.quantity,
            subtotal = (
                product.price * item.quantity
            ).roundToCents()
        )
    }

    return NewOrder(
        createdAt = createdAt,
        lines = lines,
        total = lines.sumOf { line ->
            line.subtotal
        }.roundToCents(),
        billingType = billingType,
        paymentMethod = paymentMethod
    )
}

private fun Double.roundToCents(): Double {
    return BigDecimal.valueOf(this)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
}