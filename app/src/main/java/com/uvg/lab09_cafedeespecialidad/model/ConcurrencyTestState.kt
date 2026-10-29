package com.uvg.lab09_cafedeespecialidad.model

sealed interface ConcurrencyTestState {

    data object Idle : ConcurrencyTestState

    data class Running(
        val productId: String,
        val attempts: Int
    ) : ConcurrencyTestState

    data class Finished(
        val productId: String,
        val successes: Int,
        val rejections: Int
    ) : ConcurrencyTestState
}
