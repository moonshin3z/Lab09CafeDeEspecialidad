package com.uvg.lab09_cafedeespecialidad.domain.repository

import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun observeProducts(): Flow<List<Product>>

    /**
     * Reserva unidades para el pedido en curso: las existencias guardadas en
     * el servidor bajan solo si en ese momento alcanzan. Si no alcanzan,
     * devuelve un fallo con [OutOfStockException].
     */
    suspend fun reserveUnits(
        productId: String,
        quantity: Int = 1
    ): Result<Unit>

    /** Devuelve a las existencias unidades que estaban reservadas. */
    suspend fun releaseUnits(
        productId: String,
        quantity: Int
    ): Result<Unit>
}

class OutOfStockException(
    val productId: String
) : Exception("No quedan existencias suficientes de $productId.")
