package com.uvg.lab09_cafedeespecialidad.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.OutOfStockException
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.Product
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> {
        return flow {
            authRepository
                .ensureSignedIn()
                .getOrThrow()

            emitAll(observeFirestoreProducts())
        }
    }

    override suspend fun reserveUnits(
        productId: String,
        quantity: Int
    ): Result<Unit> {
        if (quantity <= 0) {
            return Result.failure(
                IllegalArgumentException("La cantidad debe ser mayor que cero.")
            )
        }

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
        if (quantity <= 0) {
            return Result.failure(
                IllegalArgumentException("La cantidad debe ser mayor que cero.")
            )
        }

        return updateStock(productId) { currentStock ->
            currentStock + quantity
        }
    }

    /**
     * Lee las existencias en el servidor y escribe el nuevo valor dentro de
     * una transacción. Si otro cliente cambia el producto entre la lectura y
     * la escritura, Firestore rechaza el commit y vuelve a ejecutar la
     * función con el valor actualizado, así que nunca se descuenta una unidad
     * que ya no existe. Sin conexión la transacción falla en lugar de
     * quedarse en cola, por eso el resultado siempre es el del servidor.
     *
     * calculateNewStock devuelve null cuando el cambio no se puede hacer.
     */
    private suspend fun updateStock(
        productId: String,
        calculateNewStock: (Long) -> Long?
    ): Result<Unit> {
        return try {
            authRepository
                .ensureSignedIn()
                .getOrThrow()

            val productReference = firestore
                .collection(PRODUCTS_COLLECTION)
                .document(productId)

            val isUpdated = withTimeoutOrNull(STOCK_UPDATE_TIMEOUT_MILLIS) {
                firestore.runTransaction { transaction ->
                    val currentStock = transaction
                        .get(productReference)
                        .getLong(STOCK_FIELD)
                        ?: throw IllegalStateException(
                            "El producto $productId no tiene existencias registradas."
                        )

                    val newStock = calculateNewStock(currentStock)

                    if (newStock != null) {
                        transaction.update(
                            productReference,
                            STOCK_FIELD,
                            newStock
                        )
                    }

                    newStock != null
                }.await()
            }

            when (isUpdated) {
                null -> Result.failure(
                    TimeoutException(
                        "No se confirmó el cambio de existencias de $productId."
                    )
                )

                true -> Result.success(Unit)

                false -> Result.failure(
                    OutOfStockException(productId = productId)
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun observeFirestoreProducts(): Flow<List<Product>> {
        return callbackFlow {
            val registration: ListenerRegistration =
                firestore
                    .collection(PRODUCTS_COLLECTION)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            return@addSnapshotListener
                        }

                        val productsResult = runCatching {
                            snapshot.documents.map { document ->
                                val productDocument =
                                    document.toObject(
                                        ProductDocument::class.java
                                    ) ?: error(
                                        "No se pudo leer el producto ${document.id}."
                                    )

                                productDocument.toDomain(
                                    productId = document.id
                                )
                            }
                        }

                        productsResult.fold(
                            onSuccess = { products ->
                                trySend(products)
                            },
                            onFailure = { mappingError ->
                                close(mappingError)
                            }
                        )
                    }

            awaitClose {
                registration.remove()
            }
        }
    }

    private companion object {
        const val PRODUCTS_COLLECTION = "products"
        const val STOCK_FIELD = "stock"

        // Sin conexión, runTransaction reintenta unos segundos antes de fallar;
        // este límite evita que la pantalla se quede reservando sin fin.
        const val STOCK_UPDATE_TIMEOUT_MILLIS = 15_000L
    }
}
