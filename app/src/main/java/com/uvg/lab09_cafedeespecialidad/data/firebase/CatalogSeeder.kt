package com.uvg.lab09_cafedeespecialidad.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class CatalogSeeder(
    private val firestore: FirebaseFirestore
) {

    suspend fun seed(products: List<Product>): Result<Int> {
        val validationError = validateProducts(products)

        if (validationError != null) {
            return Result.failure(
                IllegalArgumentException(validationError)
            )
        }

        return try {
            val productsCollection =
                firestore.collection(PRODUCTS_COLLECTION)

            val existingProducts = productsCollection
                .limit(1)
                .get()
                .await()

            if (!existingProducts.isEmpty) {
                return Result.failure(
                    IllegalStateException(
                        "La colección products ya contiene documentos."
                    )
                )
            }

            products
                .chunked(BATCH_SIZE)
                .forEach { productBatch ->
                    val writeBatch = firestore.batch()

                    productBatch.forEach { product ->
                        val documentReference =
                            productsCollection.document(product.id)

                        writeBatch.set(
                            documentReference,
                            product.toDocument()
                        )
                    }

                    writeBatch.commit().await()
                }

            Result.success(products.size)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun validateProducts(
        products: List<Product>
    ): String? {
        if (products.size != EXPECTED_PRODUCT_COUNT) {
            return "Se esperaban $EXPECTED_PRODUCT_COUNT productos."
        }

        val uniqueIds = products
            .map { product -> product.id }
            .distinct()
            .size

        if (uniqueIds != EXPECTED_PRODUCT_COUNT) {
            return "Los productos deben tener IDs únicos."
        }

        if (products.any { product -> product.stock < 0 }) {
            return "Las existencias no pueden ser negativas."
        }

        return null
    }

    private companion object {
        const val PRODUCTS_COLLECTION = "products"
        const val EXPECTED_PRODUCT_COUNT = 500
        const val BATCH_SIZE = 100
    }
}