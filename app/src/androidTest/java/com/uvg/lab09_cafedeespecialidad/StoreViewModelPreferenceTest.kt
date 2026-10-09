package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.data.local.StorePreferencesKeys
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelPreferenceTest {

    @Test
    fun catalogStartsSortedByNameByDefault() {
        runBlocking {
            val viewModel = StoreViewModel(
                storeDao = FakeStoreDao(),
                preferencesDataStore = FakePreferencesDataStore()
            )

            val state = withTimeout(1_000) {
                viewModel.uiState.first()
            }

            assertEquals(CatalogSortOrder.NAME, state.sortOrder)
            assertTrue(
                state.products.zipWithNext().all { (current, next) ->
                    current.name.lowercase() <= next.name.lowercase()
                }
            )
        }
    }

    @Test
    fun sortOrderIsSavedAndAppliedWhenViewModelIsCreatedAgain() {
        runBlocking {
            val preferencesDataStore = FakePreferencesDataStore()
            val viewModel = StoreViewModel(
                storeDao = FakeStoreDao(),
                preferencesDataStore = preferencesDataStore
            )

            viewModel.onSortOrderChange(CatalogSortOrder.PRICE)

            withTimeout(1_000) {
                viewModel.uiState.first { state ->
                    state.sortOrder == CatalogSortOrder.PRICE
                }
            }

            val savedValue = preferencesDataStore.data.first()[
                StorePreferencesKeys.CATALOG_SORT_ORDER
            ]

            assertEquals("price", savedValue)

            // Un ViewModel nuevo con el mismo DataStore simula reabrir la app.
            val reopenedViewModel = StoreViewModel(
                storeDao = FakeStoreDao(),
                preferencesDataStore = preferencesDataStore
            )

            val reopenedState = withTimeout(1_000) {
                reopenedViewModel.uiState.first { state ->
                    state.sortOrder == CatalogSortOrder.PRICE
                }
            }

            assertTrue(
                reopenedState.products.zipWithNext().all { (current, next) ->
                    current.price <= next.price
                }
            )
        }
    }
}
