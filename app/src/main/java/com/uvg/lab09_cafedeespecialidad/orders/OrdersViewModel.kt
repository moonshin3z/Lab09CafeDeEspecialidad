package com.uvg.lab09_cafedeespecialidad.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderError
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderResult
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _ordersUiState =
        MutableStateFlow<OrdersUiState>(
            OrdersUiState.Loading
        )

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
            _ordersUiState.value = when (
                val result = ordersRepository.getOrders()
            ) {
                is OrderResult.Success -> {
                    if (result.value.isEmpty()) {
                        OrdersUiState.Empty
                    } else {
                        OrdersUiState.Success(
                            orders = result.value
                        )
                    }
                }

                is OrderResult.Failure -> {
                    OrdersUiState.Error(
                        message = result.error.toMessage()
                    )
                }
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        _orderDetailUiState.value =
            OrderDetailUiState.Loading

        viewModelScope.launch {
            _orderDetailUiState.value = when (
                val result = ordersRepository.getOrderDetail(
                    orderId = orderId
                )
            ) {
                is OrderResult.Success -> {
                    OrderDetailUiState.Success(
                        order = result.value
                    )
                }

                is OrderResult.Failure -> {
                    if (result.error == OrderError.NOT_FOUND) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            message = result.error.toMessage()
                        )
                    }
                }
            }
        }
    }

    private fun OrderError.toMessage(): String {
        return when (this) {
            OrderError.NETWORK -> NETWORK_ERROR_MESSAGE
            OrderError.INVALID_DATA -> DATA_ERROR_MESSAGE
            else -> SERVER_ERROR_MESSAGE
        }
    }

    private companion object {
        const val NETWORK_ERROR_MESSAGE =
            "No se pudo conectar con el servidor. Revisa tu conexión."

        const val SERVER_ERROR_MESSAGE =
            "El servidor no pudo completar la solicitud."

        const val DATA_ERROR_MESSAGE =
            "La respuesta del servidor no tiene el formato esperado."
    }
}