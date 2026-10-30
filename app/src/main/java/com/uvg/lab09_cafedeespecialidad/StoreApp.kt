package com.uvg.lab09_cafedeespecialidad

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.uvg.lab09_cafedeespecialidad.navigation.StoreNavKey
import com.uvg.lab09_cafedeespecialidad.navigation.MyOrders
import com.uvg.lab09_cafedeespecialidad.navigation.OrderDetail
import com.uvg.lab09_cafedeespecialidad.orders.OrderDetailScreen
import com.uvg.lab09_cafedeespecialidad.orders.OrdersScreen
import com.uvg.lab09_cafedeespecialidad.orders.OrdersViewModel
import com.uvg.lab09_cafedeespecialidad.model.OrderSubmissionState

@Composable
fun StoreApp(
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel,
    ordersViewModel: OrdersViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val orderSubmissionState by
    viewModel.orderSubmissionState.collectAsStateWithLifecycle()

    val concurrencyTestState by
    viewModel.concurrencyTestState.collectAsStateWithLifecycle()

    val ordersUiState by
    ordersViewModel.ordersUiState.collectAsStateWithLifecycle()

    val orderDetailUiState by
    ordersViewModel.orderDetailUiState.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(
        StoreNavKey.Catalog
    )

    val catalogGridState = rememberLazyGridState()
    var previousQuery by rememberSaveable {
        mutableStateOf(uiState.query)
    }

    LaunchedEffect(uiState.query) {
        if (uiState.query != previousQuery) {
            catalogGridState.scrollToItem(0)
            previousQuery = uiState.query
        }
    }

    val orderUnitCount = uiState.orderItems.sumOf { item ->
        item.quantity
    }

    // Deja solo el catálogo en el historial para que Atrás no reabra
    // el checkout ni la confirmación.
    fun returnToCatalog() {
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    LaunchedEffect(orderSubmissionState) {
        if (orderSubmissionState == OrderSubmissionState.Success) {
            returnToCatalog()
            backStack.add(StoreNavKey.OrderConfirmation)
            viewModel.resetOrderSubmissionState()
        }
    }

    BackHandler(
        enabled = backStack.size > 1
    ) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
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
                    sortOrder = uiState.sortOrder,
                    catalogLoadState = uiState.catalogLoadState,
                    orderUnitCount = orderUnitCount,
                    onQueryChange = viewModel::onQueryChange,
                    onRetryCatalog = viewModel::retryCatalog,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onOrderClick = {
                        backStack.add(StoreNavKey.Order)
                    },
                    onMyOrdersClick = {
                        ordersViewModel.loadOrders()
                        backStack.add(MyOrders)
                    },
                    gridState = catalogGridState,
                    onProductClick = { productId ->
                        viewModel.clearOrderMessage()
                        backStack.add(
                            StoreNavKey.Detail(productId)
                        )
                    },
                    onToggleFavorite = { productId ->
                        viewModel.toggleFavorite(productId)
                    }
                )
            }

            entry<MyOrders> {
                OrdersScreen(
                    uiState = ordersUiState,
                    onRetry = {
                        ordersViewModel.loadOrders()
                    },
                    onOrderClick = { orderId ->
                        ordersViewModel.loadOrderDetail(orderId)
                        backStack.add(
                            OrderDetail(orderId = orderId)
                        )
                    },
                    onCatalogClick = {
                        returnToCatalog()
                    }
                )
            }

            entry<OrderDetail> { key ->
                OrderDetailScreen(
                    uiState = orderDetailUiState,
                    onRetry = {
                        ordersViewModel.loadOrderDetail(key.orderId)
                    },
                    onBack = {
                        backStack.removeLastOrNull()
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
                        isReserving = product.id in uiState.reservingProductIds,
                        concurrencyTestState = concurrencyTestState,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(product.id)
                        },
                        onAddToOrder = {
                            viewModel.addToOrder(product.id)
                        },
                        onConcurrencyTest = {
                            viewModel.onConcurrencyTest(product.id)
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
                    reservingProductIds = uiState.reservingProductIds,
                    releasingProductIds = uiState.releasingProductIds,
                    onIncrease = { productId ->
                        viewModel.addToOrder(productId)
                    },
                    onDecrease = { productId ->
                        viewModel.decreaseOrderItem(productId)
                    },
                    onRemove = { productId ->
                        viewModel.removeOrderItem(productId)
                    },
                    onContinueToCheckout = {
                        backStack.add(StoreNavKey.Checkout)
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<StoreNavKey.Checkout> {
                val checkoutState =
                    viewModel.checkoutUiState.collectAsStateWithLifecycle()
                val storeState =
                    viewModel.uiState.collectAsStateWithLifecycle()

                val isConfirmEnabled by remember(checkoutState, storeState) {
                    derivedStateOf {
                        val units = storeState.value.orderItems.sumOf { item ->
                            item.quantity
                        }

                        checkoutState.value.isFormValid && units > 0
                    }
                }

                CheckoutScreen(
                    uiState = checkoutState.value,
                    orderUnits = orderUnitCount,
                    orderTotal = viewModel.orderTotal(),
                    isConfirmEnabled = isConfirmEnabled,
                    submissionState = orderSubmissionState,
                    onFullNameChange = viewModel::onFullNameChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onBillingTypeChange = viewModel::onBillingTypeChange,
                    onNitChange = viewModel::onNitChange,
                    onBusinessNameChange = viewModel::onBusinessNameChange,
                    onPaymentMethodChange = viewModel::onPaymentMethodChange,
                    onConfirmOrder = viewModel::confirmOrder,
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<StoreNavKey.OrderConfirmation> {
                val receipt by viewModel.orderReceipt.collectAsStateWithLifecycle()
                val currentReceipt = receipt

                if (currentReceipt == null) {
                    LaunchedEffect(Unit) {
                        returnToCatalog()
                    }
                } else {
                    OrderConfirmationScreen(
                        receipt = currentReceipt,
                        onBackToCatalog = {
                            returnToCatalog()
                        }
                    )
                }
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
