package com.uvg.lab09_cafedeespecialidad.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    @GET("orders")
    suspend fun getOrders(): List<OrderDto>

    @GET("orders/{orderId}")
    suspend fun getOrderDetail(
        @Path("orderId") orderId: String
    ): OrderDto

    @POST("orders")
    suspend fun createOrder(
        @Body order: NewOrderDto
    ): OrderDto
}