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
import com.uvg.lab09_cafedeespecialidad.data.local.DataStoreCatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.data.local.RoomCartRepository
import com.uvg.lab09_cafedeespecialidad.data.local.RoomFavoritesRepository
import com.uvg.lab09_cafedeespecialidad.remote.RetrofitOrdersRepository
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
        favoritesRepository = RoomFavoritesRepository(
            storeDao = storeDao
        ),
        cartRepository = RoomCartRepository(
            storeDao = storeDao
        ),
        catalogPreferencesRepository =
            DataStoreCatalogPreferencesRepository(
                dataStore = preferencesDataStore
            ),
        productRepository = productRepository,
        ordersRepository = RetrofitOrdersRepository(
            orderApi = orderApi
        )
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

// También espera a que no quede una reserva o devolución en curso, porque
// mientras tanto el ViewModel ignora otra operación sobre el mismo producto.
fun StoreViewModel.awaitOrderUnits(
    expectedUnits: Int
): StoreUiState {
    return runBlocking {
        withTimeout(1_000) {
            uiState.first { state ->
                state.orderItems.sumOf { item ->
                    item.quantity
                } == expectedUnits &&
                    state.reservingProductIds.isEmpty() &&
                    state.releasingProductIds.isEmpty()
            }
        }
    }
}