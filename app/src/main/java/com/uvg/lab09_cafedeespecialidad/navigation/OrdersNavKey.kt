package com.uvg.lab09_cafedeespecialidad.navigation

import kotlinx.serialization.Serializable

@Serializable
data object MyOrders : StoreNavKey

@Serializable
data class OrderDetail(
    val orderId: String
) : StoreNavKey