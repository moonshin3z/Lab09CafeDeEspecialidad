package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.domain.repository.OutOfStockException
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.Product
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeProductRepository(
    initialProducts: List<Product> = testProducts(),
    initialError: Throwable? = null
) : ProductRepository {

    private val products =
        MutableStateFlow(initialProducts)

    private var currentError: Throwable? =
        initialError

    var observationCount: Int = 0
        private set

    // Simula una red caída: las reservas y devoluciones fallan.
    var isOffline: Boolean = false

    // Si no es null, las reservas esperan a que la prueba lo complete.
    var reservationGate: CompletableDeferred<Unit>? = null

    // Hace lo mismo que la transacción: revisa y descuenta sin que otra
    // reserva se meta entre la lectura y la escritura.
    private val stockMutex = Mutex()

    override fun observeProducts(): Flow<List<Product>> {
        observationCount += 1

        val error = currentError

        return if (error == null) {
            products
        } else {
            flow {
                throw error
            }
        }
    }

    override suspend fun reserveUnits(
        productId: String,
        quantity: Int
    ): Result<Unit> {
        reservationGate?.await()

        return updateStock(productId) { currentStock ->
            if (currentStock >= quantity) {
                currentStock - quantity
            } else {
                null
            }
        }
    }

    override suspend fun releaseUnits(
        productId: String,
        quantity: Int
    ): Result<Unit> {
        return updateStock(productId) { currentStock ->
            currentStock + quantity
        }
    }

    fun stockOf(productId: String): Int {
        return products.value.first { product ->
            product.id == productId
        }.stock
    }

    private suspend fun updateStock(
        productId: String,
        calculateNewStock: (Int) -> Int?
    ): Result<Unit> {
        if (isOffline) {
            return Result.failure(IOException("Sin conexión"))
        }

        return stockMutex.withLock {
            val newStock = calculateNewStock(stockOf(productId))

            if (newStock == null) {
                Result.failure(OutOfStockException(productId = productId))
            } else {
                products.value = products.value.map { product ->
                    if (product.id == productId) {
                        product.copy(stock = newStock)
                    } else {
                        product
                    }
                }
                Result.success(Unit)
            }
        }
    }

    fun emitProducts(updatedProducts: List<Product>) {
        products.value = updatedProducts
    }

    fun failWith(error: Throwable) {
        currentError = error
    }

    fun recover(
        updatedProducts: List<Product> = testProducts()
    ) {
        currentError = null
        products.value = updatedProducts
    }
}

fun testProducts(): List<Product> {
    return listOf(
        Product(
            id = "cafe-geisha",
            name = "Geisha de Huehuetenango",
            description = "Café floral de prueba.",
            price = 145.00,
            stock = 3,
            imageUrl = "https://example.com/geisha.jpg",
            profileId = "finca-la-esperanza",
            technicalSheet = "Ficha técnica Geisha."
        ),
        Product(
            id = "cafe-bourbon",
            name = "Bourbon de Antigua",
            description = "Café balanceado de prueba.",
            price = 98.00,
            stock = 8,
            imageUrl = "https://example.com/bourbon.jpg",
            profileId = "finca-la-esperanza",
            technicalSheet = "Ficha técnica Bourbon."
        ),
        Product(
            id = "cafe-caturra",
            name = "Caturra de Cobán",
            description = "Café suave de prueba.",
            price = 85.00,
            stock = 0,
            imageUrl = "https://example.com/caturra.jpg",
            profileId = "cooperativa-chicoj",
            technicalSheet = "Ficha técnica Caturra."
        )
    )
}