package com.uvg.lab09_cafedeespecialidad.model

data class OrderLine(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

data class NewOrder(
    val createdAt: Long,
    val lines: List<OrderLine>,
    val total: Double,
    val billingType: BillingType,
    val paymentMethod: PaymentMethod
)

data class Order(
    val id: String,
    val createdAt: Long,
    val lines: List<OrderLine>,
    val total: Double,
    val billingType: BillingType,
    val paymentMethod: PaymentMethod
)