package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

// uiState usa WhileSubscribed y las escrituras en Room son asíncronas.
// Mientras se espera, la suscripción mantiene activo el combine, y la
// función regresa cuando el ViewModel ya refleja lo que quedó guardado.
fun StoreViewModel.awaitOrderUnits(expectedUnits: Int): StoreUiState {
    return runBlocking {
        withTimeout(1_000) {
            uiState.first { state ->
                state.orderItems.sumOf { item ->
                    item.quantity
                } == expectedUnits
            }
        }
    }
}
