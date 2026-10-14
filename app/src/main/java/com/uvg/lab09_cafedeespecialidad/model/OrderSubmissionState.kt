package com.uvg.lab09_cafedeespecialidad.model
sealed interface OrderSubmissionState {

    data object Idle : OrderSubmissionState

    data object Sending : OrderSubmissionState

    data object Success : OrderSubmissionState

    data class Error(
        val message: String
    ) : OrderSubmissionState
}