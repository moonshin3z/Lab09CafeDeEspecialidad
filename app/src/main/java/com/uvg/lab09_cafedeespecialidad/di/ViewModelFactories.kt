package com.uvg.lab09_cafedeespecialidad.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uvg.lab09_cafedeespecialidad.StoreViewModel
import com.uvg.lab09_cafedeespecialidad.domain.repository.CartRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.CatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.FavoritesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrdersRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.orders.OrdersViewModel

fun ordersViewModelFactory(
    ordersRepository: OrdersRepository
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            OrdersViewModel(
                ordersRepository = ordersRepository
            )
        }
    }
}

fun storeViewModelFactory(
    favoritesRepository: FavoritesRepository,
    cartRepository: CartRepository,
    catalogPreferencesRepository: CatalogPreferencesRepository,
    productRepository: ProductRepository,
    ordersRepository: OrdersRepository
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            StoreViewModel(
                favoritesRepository = favoritesRepository,
                cartRepository = cartRepository,
                catalogPreferencesRepository =
                    catalogPreferencesRepository,
                productRepository = productRepository,
                ordersRepository = ordersRepository
            )
        }
    }
}