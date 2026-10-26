package com.uvg.lab09_cafedeespecialidad

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.uvg.lab09_cafedeespecialidad.data.local.StoreDao
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.CatalogLoadState
import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import com.uvg.lab09_cafedeespecialidad.remote.OrderApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun createTestStoreViewModel(
    storeDao: StoreDao = FakeStoreDao(),
    preferencesDataStore: DataStore<Preferences> =
        FakePreferencesDataStore(),
    productRepository: ProductRepository =
        FakeProductRepository(),
    orderApi: OrderApi = FakeOrderApi(),
    waitForCatalog: Boolean = true
): StoreViewModel {
    val viewModel = StoreViewModel(
        storeDao = storeDao,
        preferencesDataStore = preferencesDataStore,
        productRepository = productRepository,
        orderApi = orderApi
    )

    if (waitForCatalog) {
        viewModel.awaitCatalogLoaded()
    }

    return viewModel
}

fun StoreViewModel.awaitCatalogLoaded(): StoreUiState {
    return runBlocking {
        withTimeout(1_000) {
            uiState.first { state ->
                state.catalogLoadState ==
                        CatalogLoadState.Success
            }
        }
    }
}

fun StoreViewModel.awaitOrderUnits(
    expectedUnits: Int
): StoreUiState {
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