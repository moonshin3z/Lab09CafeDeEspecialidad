package com.uvg.lab09_cafedeespecialidad.remote

import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.NewOrder
import com.uvg.lab09_cafedeespecialidad.model.Order
import com.uvg.lab09_cafedeespecialidad.model.OrderLine
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod

fun NewOrder.toDto(): NewOrderDto {
    return NewOrderDto(
        createdAt = createdAt,
        lines = lines.map { line ->
            OrderLineDto(
                productId = line.productId,
                productName = line.productName,
                unitPrice = line.unitPrice,
                quantity = line.quantity,
                subtotal = line.subtotal
            )
        },
        total = total,
        billingType = billingType.name,
        paymentMethod = paymentMethod.name
    )
}

fun OrderDto.toDomain(): Order {
    return Order(
        id = id,
        createdAt = createdAt,
        lines = lines.map { line ->
            OrderLine(
                productId = line.productId,
                productName = line.productName,
                unitPrice = line.unitPrice,
                quantity = line.quantity,
                subtotal = line.subtotal
            )
        },
        total = total,
        billingType = BillingType.valueOf(billingType),
        paymentMethod = PaymentMethod.valueOf(paymentMethod)
    )
}