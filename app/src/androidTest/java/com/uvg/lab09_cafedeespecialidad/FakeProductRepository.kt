package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

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