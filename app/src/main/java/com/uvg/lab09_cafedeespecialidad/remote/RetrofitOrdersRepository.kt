package com.uvg.lab09_cafedeespecialidad.remote

import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderError
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderResult
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrdersRepository
import com.uvg.lab09_cafedeespecialidad.model.NewOrder
import com.uvg.lab09_cafedeespecialidad.model.Order
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class RetrofitOrdersRepository(
    private val orderApi: OrderApi
) : OrdersRepository {

    override suspend fun createOrder(
        order: NewOrder
    ): OrderResult<Order> = request {
        orderApi.createOrder(order.toDto()).toDomain()
    }

    override suspend fun getOrders(): OrderResult<List<Order>> =
        request {
            orderApi.getOrders(
                sortBy = "createdAt",
                order = "desc"
            ).map { order ->
                order.toDomain()
            }
        }

    override suspend fun getOrderDetail(
        orderId: String
    ): OrderResult<Order> = request {
        orderApi.getOrderDetail(
            orderId = orderId
        ).toDomain()
    }

    private suspend fun <T> request(
        block: suspend () -> T
    ): OrderResult<T> {
        return try {
            OrderResult.Success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            OrderResult.Failure(OrderError.NETWORK)
        } catch (error: HttpException) {
            OrderResult.Failure(
                if (error.code() == 404) {
                    OrderError.NOT_FOUND
                } else {
                    OrderError.SERVER
                }
            )
        } catch (_: SerializationException) {
            OrderResult.Failure(OrderError.INVALID_DATA)
        } catch (_: IllegalArgumentException) {
            OrderResult.Failure(OrderError.INVALID_DATA)
        } catch (_: Exception) {
            OrderResult.Failure(OrderError.UNKNOWN)
        }
    }
}