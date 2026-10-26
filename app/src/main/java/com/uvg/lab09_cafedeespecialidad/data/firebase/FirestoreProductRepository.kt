package com.uvg.lab09_cafedeespecialidad.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

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
    }
}