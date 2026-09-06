package com.uvg.lab09_cafedeespecialidad

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.uvg.lab09_cafedeespecialidad.navigation.StoreNavKey

@Composable
fun StoreApp(viewModel: StoreViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteIds = uiState.favoriteIds,
                    onProductClick = { id -> backStack.add(StoreNavKey.Detail(id)) },
                    onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
                )
            }

            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.firstOrNull { it.id == key.productId }
                if (product == null) {
                    LaunchedEffect(key) { backStack.removeLastOrNull() }
                } else {
                    DetailScreen(
                        product = product,
                        isFavorite = product.id in uiState.favoriteIds,
                        onToggleFavorite = { viewModel.toggleFavorite(product.id) },
                        onProfileClick = { backStack.add(StoreNavKey.Profile(product.profileId)) },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }

            entry<StoreNavKey.Profile> { key ->
                val profile = uiState.profiles.firstOrNull { it.id == key.profileId }
                if (profile == null) {
                    LaunchedEffect(key) { backStack.removeLastOrNull() }
                } else {
                    ProfileScreen(
                        profile = profile,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    )
}