package com.uvg.lab09_cafedeespecialidad.di

import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.lab09_cafedeespecialidad.data.firebase.FirebaseAuthRepository
import com.uvg.lab09_cafedeespecialidad.data.firebase.FirestoreProductRepository
import com.uvg.lab09_cafedeespecialidad.data.local.DataStoreCatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.data.local.RoomCartRepository
import com.uvg.lab09_cafedeespecialidad.data.local.RoomFavoritesRepository
import com.uvg.lab09_cafedeespecialidad.data.local.StoreDatabase
import com.uvg.lab09_cafedeespecialidad.data.local.storePreferencesDataStore
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.CartRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.CatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.FavoritesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrdersRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.remote.OrderApiClient
import com.uvg.lab09_cafedeespecialidad.remote.RetrofitOrdersRepository

class AppContainer(
    context: Context
) {
    private val applicationContext = context.applicationContext

    private val firebaseAuth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    private val storeDao =
        StoreDatabase.getInstance(applicationContext).storeDao()

    val authRepository: AuthRepository =
        FirebaseAuthRepository(
            firebaseAuth = firebaseAuth
        )

    val productRepository: ProductRepository =
        FirestoreProductRepository(
            firestore = firestore,
            authRepository = authRepository
        )

    val favoritesRepository: FavoritesRepository =
        RoomFavoritesRepository(
            storeDao = storeDao
        )

    val cartRepository: CartRepository =
        RoomCartRepository(
            storeDao = storeDao
        )

    val catalogPreferencesRepository: CatalogPreferencesRepository =
        DataStoreCatalogPreferencesRepository(
            dataStore = applicationContext.storePreferencesDataStore
        )

    val ordersRepository: OrdersRepository =
        RetrofitOrdersRepository(
            orderApi = OrderApiClient.orderApi
        )
    val ordersViewModelFactory: ViewModelProvider.Factory =
        ordersViewModelFactory(
            ordersRepository = ordersRepository
        )
    val storeViewModelFactory: ViewModelProvider.Factory =
        storeViewModelFactory(
            favoritesRepository = favoritesRepository,
            cartRepository = cartRepository,
            catalogPreferencesRepository =
                catalogPreferencesRepository,
            productRepository = productRepository,
            ordersRepository = ordersRepository
        )
}