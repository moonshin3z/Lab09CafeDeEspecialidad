package com.uvg.lab09_cafedeespecialidad.orders

import com.uvg.lab09_cafedeespecialidad.model.Order

sealed interface OrdersUiState {

    data object Loading : OrdersUiState

    data object Empty : OrdersUiState

    data class Success(
        val orders: List<Order>
    ) : OrdersUiState

    data class Error(
        val message: String
    ) : OrdersUiState
}

sealed interface OrderDetailUiState {

    data object Loading : OrderDetailUiState

    data class Success(
        val order: Order
    ) : OrderDetailUiState

    data object NotFound : OrderDetailUiState

    data class Error(
        val message: String
    ) : OrderDetailUiState
}