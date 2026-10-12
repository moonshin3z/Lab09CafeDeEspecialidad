package com.uvg.lab09_cafedeespecialidad.remote

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

@Serializable
data class NewOrderDto(
    val createdAt: Long,
    val lines: List<OrderLineDto>,
    val total: Double,
    val billingType: String,
    val paymentMethod: String
)

@Serializable
data class OrderDto(
    val createdAt: Long,
    val lines: List<OrderLineDto>,
    val total: Double,
    val billingType: String,
    val paymentMethod: String,
    val id: String
)