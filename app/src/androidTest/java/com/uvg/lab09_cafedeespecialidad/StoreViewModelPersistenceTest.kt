package com.uvg.lab09_cafedeespecialidad

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelPersistenceTest {

    @Test
    fun toggleFavoriteInsertsAndDeletesFavorite() {
        runBlocking {
            val dao = FakeStoreDao()
            val viewModel = StoreViewModel(
                storeDao = dao,
                preferencesDataStore = FakePreferencesDataStore()
            )

            viewModel.uiState.first()

            viewModel.toggleFavorite("cafe-geisha")

            val favoritesAfterInsert = withTimeout(1_000) {
                dao.observeFavorites().first { favorites ->
                    favorites.any { it.productId == "cafe-geisha" }
                }
            }

            assertTrue(
                favoritesAfterInsert.any {
                    it.productId == "cafe-geisha"
                }
            )

            withTimeout(1_000) {
                viewModel.uiState.first { state ->
                    "cafe-geisha" in state.favoriteIds
                }
            }

            viewModel.toggleFavorite("cafe-geisha")

            val favoritesAfterDelete = withTimeout(1_000) {
                dao.observeFavorites().first { favorites ->
                    favorites.isEmpty()
                }
            }

            assertTrue(favoritesAfterDelete.isEmpty())
        }
    }

    @Test
    fun zeroQuantityAndConfirmationDeleteOrderLines() {
        runBlocking {
            val dao = FakeStoreDao()
            val viewModel = StoreViewModel(
                storeDao = dao,
                preferencesDataStore = FakePreferencesDataStore()
            )

            viewModel.uiState.first()

            viewModel.addToOrder("cafe-geisha")

            withTimeout(1_000) {
                dao.observeOrderLines().first { lines ->
                    lines.singleOrNull()?.quantity == 1
                }
            }
            viewModel.awaitOrderUnits(1)

            viewModel.decreaseOrderItem("cafe-geisha")

            withTimeout(1_000) {
                dao.observeOrderLines().first { lines ->
                    lines.isEmpty()
                }
            }
            viewModel.awaitOrderUnits(0)

            viewModel.addToOrder("cafe-bourbon")

            withTimeout(1_000) {
                dao.observeOrderLines().first { lines ->
                    lines.isNotEmpty()
                }
            }
            withTimeout(1_000) {
            viewModel.uiState.first { state ->
                state.orderItems.any { item ->
                    item.productId == "cafe-bourbon"
                }
            }
        }

            viewModel.onFullNameChange("María Morales")
            viewModel.onPhoneChange("55123456")

            assertTrue(viewModel.confirmOrder())

            withTimeout(1_000) {
                dao.observeOrderLines().first { lines ->
                    lines.isEmpty()
                }
            }
        }
    }

    @Test
    fun rejectedOrderUpdateDoesNotReachDatabase() {
        runBlocking {
            val dao = FakeStoreDao()
            val viewModel = StoreViewModel(
                storeDao = dao,
                preferencesDataStore = FakePreferencesDataStore()
            )

            viewModel.addToOrder(
                productId = "cafe-geisha",
                increment = 3
            )
            viewModel.awaitOrderUnits(3)

            viewModel.addToOrder("cafe-geisha")

            val rejectedState = withTimeout(1_000) {
                viewModel.uiState.first { state ->
                    state.orderMessage ==
                        "No hay existencias suficientes. Máximo disponible: 3."
                }
            }
            val savedLines = dao.observeOrderLines().first()

            assertEquals(3, rejectedState.orderItems.single().quantity)
            assertEquals(1, savedLines.size)
            assertEquals(3, savedLines.single().quantity)
        }
    }
}
