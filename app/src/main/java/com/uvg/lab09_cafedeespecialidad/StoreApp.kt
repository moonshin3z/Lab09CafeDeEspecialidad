package com.uvg.lab09_cafedeespecialidad

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
fun StoreApp(
    viewModel: StoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(
        StoreNavKey.Catalog
    )

    val catalogGridState = rememberLazyGridState()
    val orderUnitCount = uiState.orderItems.sumOf { item ->
        item.quantity
    }

    BackHandler(
        enabled = backStack.size > 1
    ) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> width },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width -> -width },
                animationSpec = tween(300)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> -width },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width -> width },
                animationSpec = tween(300)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> -width },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width -> width },
                animationSpec = tween(300)
            )
        },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteIds = uiState.favoriteIds,
                    query = uiState.query,
                    orderUnitCount = orderUnitCount,
                    onQueryChange = viewModel::onQueryChange,
                    onOrderClick = {
                        backStack.add(StoreNavKey.Order)
                    },
                    gridState = catalogGridState,
                    onProductClick = { productId ->
                        backStack.add(
                            StoreNavKey.Detail(productId)
                        )
                    },
                    onToggleFavorite = { productId ->
                        viewModel.toggleFavorite(productId)
                    }
                )
            }

            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.firstOrNull { currentProduct ->
                    currentProduct.id == key.productId
                }

                if (product == null) {
                    LaunchedEffect(key) {
                        backStack.removeLastOrNull()
                    }
                } else {
                    val orderQuantity = uiState.orderItems
                        .firstOrNull { item ->
                            item.productId == product.id
                        }
                        ?.quantity
                        ?: 0

                    DetailScreen(
                        product = product,
                        isFavorite = product.id in uiState.favoriteIds,
                        orderQuantity = orderQuantity,
                        orderMessage = uiState.orderMessage,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(product.id)
                        },
                        onAddToOrder = {
                            viewModel.addToOrder(product.id)
                        },
                        onProfileClick = {
                            backStack.add(
                                StoreNavKey.Profile(product.profileId)
                            )
                        },
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }

            entry<StoreNavKey.Order> {
                val lineSubtotals = uiState.orderItems.associate { item ->
                    item.productId to viewModel.orderSubtotal(item.productId)
                }

                OrderScreen(
                    products = uiState.products,
                    orderItems = uiState.orderItems,
                    lineSubtotals = lineSubtotals,
                    orderTotal = viewModel.orderTotal(),
                    orderMessage = uiState.orderMessage,
                    onIncrease = { productId ->
                        viewModel.addToOrder(productId)
                    },
                    onDecrease = { productId ->
                        viewModel.decreaseOrderItem(productId)
                    },
                    onRemove = { productId ->
                        viewModel.removeOrderItem(productId)
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<StoreNavKey.Profile> { key ->
                val profile = uiState.profiles.firstOrNull { currentProfile ->
                    currentProfile.id == key.profileId
                }

                if (profile == null) {
                    LaunchedEffect(key) {
                        backStack.removeLastOrNull()
                    }
                } else {
                    ProfileScreen(
                        profile = profile,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        }
    )
}
