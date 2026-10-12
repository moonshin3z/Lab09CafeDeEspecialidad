package com.uvg.lab09_cafedeespecialidad.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.lab09_cafedeespecialidad.remote.OrderApi
import com.uvg.lab09_cafedeespecialidad.remote.OrderApiClient
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class OrdersViewModel(
    private val orderApi: OrderApi = OrderApiClient.orderApi
) : ViewModel() {

    private val _ordersUiState =
        MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)

    val ordersUiState: StateFlow<OrdersUiState> =
        _ordersUiState.asStateFlow()

    private val _orderDetailUiState =
        MutableStateFlow<OrderDetailUiState>(
            OrderDetailUiState.Loading
        )

    val orderDetailUiState: StateFlow<OrderDetailUiState> =
        _orderDetailUiState.asStateFlow()

    fun loadOrders() {
        _ordersUiState.value = OrdersUiState.Loading

        viewModelScope.launch {
            try {
                val orders = orderApi.getOrders()

                _ordersUiState.value = if (orders.isEmpty()) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Success(orders = orders)
                }
            } catch (_: IOException) {
                _ordersUiState.value = OrdersUiState.Error(
                    message = NETWORK_ERROR_MESSAGE
                )
            } catch (_: HttpException) {
                _ordersUiState.value = OrdersUiState.Error(
                    message = SERVER_ERROR_MESSAGE
                )
            } catch (_: SerializationException) {
                _ordersUiState.value = OrdersUiState.Error(
                    message = DATA_ERROR_MESSAGE
                )
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        _orderDetailUiState.value = OrderDetailUiState.Loading

        viewModelScope.launch {
            try {
                val order = orderApi.getOrderDetail(
                    orderId = orderId
                )

                _orderDetailUiState.value =
                    OrderDetailUiState.Success(order = order)
            } catch (error: HttpException) {
                _orderDetailUiState.value =
                    if (error.code() == HTTP_NOT_FOUND) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            message = SERVER_ERROR_MESSAGE
                        )
                    }
            } catch (_: IOException) {
                _orderDetailUiState.value =
                    OrderDetailUiState.Error(
                        message = NETWORK_ERROR_MESSAGE
                    )
            } catch (_: SerializationException) {
                _orderDetailUiState.value =
                    OrderDetailUiState.Error(
                        message = DATA_ERROR_MESSAGE
                    )
            }
        }
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404

        const val NETWORK_ERROR_MESSAGE =
            "No se pudo conectar con el servidor. Revisa tu conexión."

        const val SERVER_ERROR_MESSAGE =
            "El servidor no pudo completar la solicitud."

        const val DATA_ERROR_MESSAGE =
            "La respuesta del servidor no tiene el formato esperado."
    }
}