package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.CatalogLoadState
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogViewModelTest {

    @Test
    fun productsFromRepositoryProduceSuccessState() {
        runBlocking {
            val productRepository =
                FakeProductRepository()

            val viewModel = createTestStoreViewModel(
                productRepository = productRepository,
                waitForCatalog = false
            )

            val state = withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.catalogLoadState ==
                            CatalogLoadState.Success
                }
            }

            assertEquals(3, state.products.size)
            assertEquals(
                setOf(
                    "cafe-geisha",
                    "cafe-bourbon",
                    "cafe-caturra"
                ),
                state.products
                    .map { product -> product.id }
                    .toSet()
            )
            assertEquals(1, productRepository.observationCount)
        }
    }

    @Test
    fun repositoryFailureProducesErrorState() {
        runBlocking {
            val productRepository =
                FakeProductRepository(
                    initialError =
                        IOException("Fallo simulado")
                )

            val viewModel = createTestStoreViewModel(
                productRepository = productRepository,
                waitForCatalog = false
            )

            val state = withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.catalogLoadState is CatalogLoadState.Error
                }
            }

            assertTrue(
                state.catalogLoadState
                        is CatalogLoadState.Error
            )
            assertTrue(state.products.isEmpty())
            assertEquals(1, productRepository.observationCount)
        }
    }

    @Test
    fun retryObservesRepositoryAgainAfterFailure() {
        runBlocking {
            val productRepository =
                FakeProductRepository(
                    initialError =
                        IOException("Fallo simulado")
                )

            val viewModel = createTestStoreViewModel(
                productRepository = productRepository,
                waitForCatalog = false
            )

            withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.catalogLoadState is CatalogLoadState.Error
                }
            }

            productRepository.recover()
            viewModel.retryCatalog()

            val recoveredState = withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.catalogLoadState ==
                            CatalogLoadState.Success
                }
            }

            assertEquals(3, recoveredState.products.size)
            assertEquals(2, productRepository.observationCount)
        }
    }

    @Test
    fun searchAndSortDoNotObserveRepositoryAgain() {
        runBlocking {
            val productRepository =
                FakeProductRepository()

            val viewModel = createTestStoreViewModel(
                productRepository = productRepository,
                waitForCatalog = false
            )

            withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.catalogLoadState ==
                            CatalogLoadState.Success
                }
            }

            val observationsAfterLoad =
                productRepository.observationCount

            viewModel.onQueryChange("Geisha")

            withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.query == "Geisha"
                }
            }

            viewModel.onSortOrderChange(
                CatalogSortOrder.PRICE
            )

            withTimeout(1_000) {
                viewModel.uiState.first { currentState ->
                    currentState.sortOrder ==
                            CatalogSortOrder.PRICE
                }
            }

            assertEquals(
                observationsAfterLoad,
                productRepository.observationCount
            )
        }
    }
}