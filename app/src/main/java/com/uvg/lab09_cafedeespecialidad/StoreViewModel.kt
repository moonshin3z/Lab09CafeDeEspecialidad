package com.uvg.lab09_cafedeespecialidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.lab09_cafedeespecialidad.catalog.sortCatalog
import com.uvg.lab09_cafedeespecialidad.domain.repository.CartRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.CatalogPreferencesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.FavoritesRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderError
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrderResult
import com.uvg.lab09_cafedeespecialidad.domain.repository.OrdersRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.OutOfStockException
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository
import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.CatalogLoadState
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import com.uvg.lab09_cafedeespecialidad.model.CheckoutUiState
import com.uvg.lab09_cafedeespecialidad.model.ConcurrencyTestState
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.OrderReceipt
import com.uvg.lab09_cafedeespecialidad.model.OrderSubmissionState
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.model.Profile
import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import com.uvg.lab09_cafedeespecialidad.order.OUT_OF_STOCK_MESSAGE
import com.uvg.lab09_cafedeespecialidad.order.OrderUpdateResult
import com.uvg.lab09_cafedeespecialidad.order.addToOrder as addToOrderRule
import com.uvg.lab09_cafedeespecialidad.order.buildNewOrder
import com.uvg.lab09_cafedeespecialidad.order.calculateLineSubtotal
import com.uvg.lab09_cafedeespecialidad.order.calculateOrderTotal
import com.uvg.lab09_cafedeespecialidad.order.decreaseOrderItem as decreaseOrderItemRule
import com.uvg.lab09_cafedeespecialidad.order.removeOrderItem as removeOrderItemRule
import com.uvg.lab09_cafedeespecialidad.validation.validateBusinessName
import com.uvg.lab09_cafedeespecialidad.validation.validateFullName
import com.uvg.lab09_cafedeespecialidad.validation.validateNit
import com.uvg.lab09_cafedeespecialidad.validation.validatePhone
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StoreViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val cartRepository: CartRepository,
    private val catalogPreferencesRepository: CatalogPreferencesRepository,
    private val productRepository: ProductRepository,
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val originalProfiles = listOf(
        Profile(
            id = "finca-la-esperanza",
            name = "Finca La Esperanza",
            role = "Finca productora",
            location = "Huehuetenango, Guatemala",
            description = "Finca familiar de tercera generación dedicada al cultivo de variedades de altura bajo sombra."
        ),
        Profile(
            id = "cooperativa-chicoj",
            name = "Cooperativa Chicoj",
            role = "Cooperativa de productores",
            location = "Cobán, Alta Verapaz, Guatemala",
            description = "Agrupa a más de 40 familias caficultoras y comercializa bajo un modelo de comercio justo."
        )
    )

    private val catalogSnapshot =
        MutableStateFlow(CatalogSnapshot())

    private var catalogJob: Job? = null

    private val orderMessage = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val stockOperations = MutableStateFlow(StockOperations())

    // Las reservas y devoluciones del pedido se hacen de una en una: así cada
    // una calcula la cantidad con el pedido que dejó la anterior.
    private val stockOperationMutex = Mutex()

    private val interactionState: Flow<InteractionState> =
        combine(
            query,
            orderMessage,
            stockOperations
        ) { currentQuery, currentOrderMessage, operations ->
            InteractionState(
                query = currentQuery,
                orderMessage = currentOrderMessage,
                stockOperations = operations
            )
        }

    private val catalogSortOrder: Flow<CatalogSortOrder> =
        catalogPreferencesRepository.observeSortOrder()

    private val sortedCatalog: Flow<SortedCatalog> =
        combine(
            catalogSnapshot,
            catalogSortOrder
        ) { snapshot, sortOrder ->
            SortedCatalog(
                products = sortCatalog(
                    products = snapshot.products,
                    sortOrder = sortOrder
                ),
                loadState = snapshot.loadState,
                sortOrder = sortOrder
            )
        }

    val uiState: StateFlow<StoreUiState> = combine(
        favoritesRepository.observeFavoriteIds(),
        cartRepository.observeItems(),
        sortedCatalog,
        interactionState
    ) { favorites, orderItems, catalog, interaction ->
        StoreUiState(
            products = catalog.products,
            profiles = originalProfiles,
            favoriteIds = favorites,
            query = interaction.query,
            orderItems = orderItems,
            orderMessage = interaction.orderMessage,
            sortOrder = catalog.sortOrder,
            catalogLoadState = catalog.loadState,
            reservingProductIds =
                interaction.stockOperations.reservingProductIds.toSet(),
            releasingProductIds =
                interaction.stockOperations.releasingProductIds.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(
            products = emptyList(),
            profiles = originalProfiles,
            sortOrder = CatalogSortOrder.NAME,
            catalogLoadState = CatalogLoadState.Loading
        )
    )

    private val _checkoutUiState =
        MutableStateFlow(CheckoutUiState())

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState.asStateFlow()

    private val _orderReceipt =
        MutableStateFlow<OrderReceipt?>(null)

    val orderReceipt: StateFlow<OrderReceipt?> =
        _orderReceipt.asStateFlow()

    private val _orderSubmissionState =
        MutableStateFlow<OrderSubmissionState>(
            OrderSubmissionState.Idle
        )

    val orderSubmissionState: StateFlow<OrderSubmissionState> =
        _orderSubmissionState.asStateFlow()

    private val _concurrencyTestState =
        MutableStateFlow<ConcurrencyTestState>(
            ConcurrencyTestState.Idle
        )

    val concurrencyTestState: StateFlow<ConcurrencyTestState> =
        _concurrencyTestState.asStateFlow()

    private var nextOrderNumber = INITIAL_ORDER_NUMBER

    init {
        observeCatalog()
    }

    fun toggleFavorite(productId: String) {
        val isFavorite = productId in uiState.value.favoriteIds

        viewModelScope.launch {
            favoritesRepository.setFavorite(
                productId = productId,
                isFavorite = !isFavorite
            )
        }
    }

    fun onQueryChange(query: String) {
        this.query.value = query
    }

    fun retryCatalog() {
        observeCatalog()
    }

    private fun observeCatalog() {
        catalogJob?.cancel()

        catalogSnapshot.value = CatalogSnapshot(
            loadState = CatalogLoadState.Loading
        )

        catalogJob = viewModelScope.launch {
            productRepository
                .observeProducts()
                .catch {
                    catalogSnapshot.value = CatalogSnapshot(
                        loadState = CatalogLoadState.Error(
                            message = "No se pudo cargar el catálogo."
                        )
                    )
                }
                .collect { products ->
                    catalogSnapshot.value = CatalogSnapshot(
                        products = products,
                        loadState = CatalogLoadState.Success
                    )
                }
        }
    }

    fun onSortOrderChange(sortOrder: CatalogSortOrder) {
        viewModelScope.launch {
            catalogPreferencesRepository.setSortOrder(sortOrder)
        }
    }

    /**
     * Agregar al pedido reserva la unidad en Firestore. El pedido en curso
     * solo cambia si la reserva se confirmó; si falla, queda igual.
     */
    fun addToOrder(
        productId: String,
        increment: Int = 1
    ) {
        val current = uiState.value

        when (
            val result = addToOrderRule(
                products = current.products,
                orderItems = current.orderItems,
                productId = productId,
                increment = increment
            )
        ) {
            is OrderUpdateResult.Success -> {
                reserveAndAddToOrder(
                    productId = productId,
                    increment = increment
                )
            }

            is OrderUpdateResult.Rejected -> {
                orderMessage.value = result.reason
            }
        }
    }

    /** Quitar una unidad la devuelve a Firestore antes de cambiar el pedido. */
    fun decreaseOrderItem(productId: String) {
        releaseAndUpdateOrder(productId = productId) { orderItems ->
            decreaseOrderItemRule(
                orderItems = orderItems,
                productId = productId
            )
        }
    }

    /** Eliminar la línea devuelve todas sus unidades a Firestore. */
    fun removeOrderItem(productId: String) {
        releaseAndUpdateOrder(productId = productId) { orderItems ->
            removeOrderItemRule(
                orderItems = orderItems,
                productId = productId
            )
        }
    }

    /**
     * Herramienta de prueba: lanza varias reservas a la vez sin tocar el
     * pedido en curso. Con un mecanismo seguro, los éxitos nunca superan
     * las existencias que había.
     */
    fun onConcurrencyTest(
        productId: String,
        attempts: Int = CONCURRENCY_TEST_ATTEMPTS
    ) {
        if (_concurrencyTestState.value is ConcurrencyTestState.Running) {
            return
        }

        _concurrencyTestState.value = ConcurrencyTestState.Running(
            productId = productId,
            attempts = attempts
        )

        viewModelScope.launch {
            val results = List(attempts) {
                async {
                    productRepository.reserveUnits(productId)
                }
            }.awaitAll()

            val successes = results.count { result ->
                result.isSuccess
            }

            _concurrencyTestState.value = ConcurrencyTestState.Finished(
                productId = productId,
                successes = successes,
                rejections = attempts - successes
            )
        }
    }

    private fun reserveAndAddToOrder(
        productId: String,
        increment: Int
    ) {
        orderMessage.value = null
        stockOperations.update { operations ->
            operations.copy(
                reservingProductIds =
                    operations.reservingProductIds + productId
            )
        }

        viewModelScope.launch {
            try {
                stockOperationMutex.withLock {
                    reserveAndSaveLine(
                        productId = productId,
                        increment = increment
                    )
                }
            } finally {
                stockOperations.update { operations ->
                    operations.copy(
                        reservingProductIds =
                            operations.reservingProductIds - productId
                    )
                }
            }
        }
    }

    private suspend fun reserveAndSaveLine(
        productId: String,
        increment: Int
    ) {
        val reservation = productRepository.reserveUnits(
            productId = productId,
            quantity = increment
        )

        if (reservation.isFailure) {
            orderMessage.value = reservationErrorMessage(
                error = reservation.exceptionOrNull()
            )
            return
        }

        // Se lee el pedido guardado y no uiState, que puede no reflejar
        // todavía la última reserva.
        val currentQuantity = cartRepository
            .observeItems()
            .first()
            .quantityOf(productId)

        cartRepository.saveItem(
            OrderItem(
                productId = productId,
                quantity = currentQuantity + increment
            )
        )

        val unitLabel = if (increment == 1) {
            "unidad"
        } else {
            "unidades"
        }

        orderMessage.value = "Se agregó $increment $unitLabel al pedido."
    }

    private fun releaseAndUpdateOrder(
        productId: String,
        updateItems: (List<OrderItem>) -> List<OrderItem>
    ) {
        orderMessage.value = null
        stockOperations.update { operations ->
            operations.copy(
                releasingProductIds =
                    operations.releasingProductIds + productId
            )
        }

        viewModelScope.launch {
            try {
                stockOperationMutex.withLock {
                    releaseAndSaveLine(
                        productId = productId,
                        updateItems = updateItems
                    )
                }
            } finally {
                stockOperations.update { operations ->
                    operations.copy(
                        releasingProductIds =
                            operations.releasingProductIds - productId
                    )
                }
            }
        }
    }

    private suspend fun releaseAndSaveLine(
        productId: String,
        updateItems: (List<OrderItem>) -> List<OrderItem>
    ) {
        val currentItems = cartRepository.observeItems().first()
        val updatedItems = updateItems(currentItems)
        val releasedUnits = currentItems.quantityOf(productId) -
            updatedItems.quantityOf(productId)

        if (releasedUnits <= 0) {
            return
        }

        val release = productRepository.releaseUnits(
            productId = productId,
            quantity = releasedUnits
        )

        if (release.isFailure) {
            orderMessage.value =
                "No se pudieron devolver las unidades. Revisa tu conexión e intenta de nuevo."
            return
        }

        val updatedLine = updatedItems.firstOrNull { item ->
            item.productId == productId
        }

        if (updatedLine == null) {
            cartRepository.removeItem(productId)
        } else {
            cartRepository.saveItem(updatedLine)
        }
    }

    private fun reservationErrorMessage(error: Throwable?): String {
        return if (error is OutOfStockException) {
            OUT_OF_STOCK_MESSAGE
        } else {
            "No se pudo reservar la unidad. Revisa tu conexión e intenta de nuevo."
        }
    }

    fun orderSubtotal(productId: String): Double {
        val current = uiState.value

        val product = current.products.firstOrNull {
            it.id == productId
        } ?: return 0.0

        val orderItem = current.orderItems.firstOrNull {
            it.productId == productId
        } ?: return 0.0

        return calculateLineSubtotal(
            product = product,
            orderItem = orderItem
        )
    }

    fun orderTotal(): Double {
        val current = uiState.value

        return calculateOrderTotal(
            products = current.products,
            orderItems = current.orderItems
        )
    }

    fun orderUnits(): Int {
        return uiState.value.orderItems.sumOf {
            it.quantity
        }
    }

    fun clearOrderMessage() {
        orderMessage.value = null
    }

    fun onFullNameChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                fullName = value,
                isFullNameTouched = true
            ).withRecalculatedValidation()
        }
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                phone = value,
                isPhoneTouched = true
            ).withRecalculatedValidation()
        }
    }

    fun onNitChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                nit = value,
                isNitTouched = current.billingType == BillingType.NIT
            ).withRecalculatedValidation()
        }
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.update { current ->
            current.copy(
                businessName = value,
                isBusinessNameTouched =
                    current.billingType == BillingType.NIT
            ).withRecalculatedValidation()
        }
    }

    fun onBillingTypeChange(billingType: BillingType) {
        _checkoutUiState.update { current ->
            if (billingType == current.billingType) {
                current.withRecalculatedValidation()
            } else {
                when (billingType) {
                    BillingType.CF -> {
                        current.copy(
                            billingType = BillingType.CF,
                            nitError = null,
                            businessNameError = null,
                            isNitTouched = false,
                            isBusinessNameTouched = false
                        ).withRecalculatedValidation()
                    }

                    BillingType.NIT -> {
                        current.copy(
                            billingType = BillingType.NIT,
                            isNitTouched = false,
                            isBusinessNameTouched = false
                        ).withRecalculatedValidation()
                    }
                }
            }
        }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        _checkoutUiState.update { current ->
            current.copy(
                paymentMethod = paymentMethod
            ).withRecalculatedValidation()
        }
    }

    fun confirmOrder() {
        if (_orderSubmissionState.value is OrderSubmissionState.Sending) {
            return
        }

        val checkout =
            _checkoutUiState.value.withRecalculatedValidation()

        _checkoutUiState.value = checkout

        val currentStore = uiState.value
        val units = currentStore.orderItems.sumOf {
            it.quantity
        }

        if (!checkout.isFormValid || units <= 0) {
            return
        }

        val newOrder = buildNewOrder(
            products = currentStore.products,
            orderItems = currentStore.orderItems,
            billingType = checkout.billingType,
            paymentMethod = checkout.paymentMethod,
            createdAt = System.currentTimeMillis()
        )

        _orderSubmissionState.value = OrderSubmissionState.Sending

        viewModelScope.launch {
            when (
                val result = ordersRepository.createOrder(newOrder)
            ) {
                is OrderResult.Success -> {
                    _orderReceipt.value = OrderReceipt(
                        folio = formatFolio(nextOrderNumber),
                        serverOrderId = result.value.id,
                        customerName = checkout.fullName.trim(),
                        phone = checkout.phone.trim(),
                        billingType = checkout.billingType,
                        nit = if (checkout.billingType == BillingType.NIT) {
                            checkout.nit.trim()
                        } else {
                            null
                        },
                        businessName = if (
                            checkout.billingType == BillingType.NIT
                        ) {
                            checkout.businessName.trim()
                        } else {
                            null
                        },
                        paymentMethod = checkout.paymentMethod,
                        total = calculateOrderTotal(
                            products = currentStore.products,
                            orderItems = currentStore.orderItems
                        )
                    )

                    cartRepository.clear()
                    _checkoutUiState.value = CheckoutUiState()
                    nextOrderNumber += 1

                    _orderSubmissionState.value =
                        OrderSubmissionState.Success
                }

                is OrderResult.Failure -> {
                    _orderSubmissionState.value =
                        OrderSubmissionState.Error(
                            message = when (result.error) {
                                OrderError.NETWORK ->
                                    "No se pudo conectar con el servidor. Intenta de nuevo."

                                OrderError.INVALID_DATA ->
                                    "La respuesta del servidor no tiene el formato esperado."

                                else ->
                                    "El servidor no pudo guardar el pedido. Intenta de nuevo."
                            }
                        )
                }
            }
        }
    }

    fun resetOrderSubmissionState() {
        _orderSubmissionState.value = OrderSubmissionState.Idle
    }

    private fun CheckoutUiState.withRecalculatedValidation(): CheckoutUiState {
        val updatedFullNameError = validateFullName(fullName)
        val updatedPhoneError = validatePhone(phone)

        val updatedNitError = if (billingType == BillingType.NIT) {
            validateNit(nit)
        } else {
            null
        }

        val updatedBusinessNameError =
            if (billingType == BillingType.NIT) {
                validateBusinessName(businessName)
            } else {
                null
            }

        val updatedIsFormValid =
            updatedFullNameError == null &&
                updatedPhoneError == null &&
                updatedNitError == null &&
                updatedBusinessNameError == null

        return copy(
            fullNameError = updatedFullNameError,
            phoneError = updatedPhoneError,
            nitError = updatedNitError,
            businessNameError = updatedBusinessNameError,
            isFormValid = updatedIsFormValid
        )
    }

    private fun formatFolio(orderNumber: Int): String {
        return String.format(
            Locale.US,
            "#ORD-%05d",
            orderNumber
        )
    }

    private companion object {
        const val INITIAL_ORDER_NUMBER = 1
        const val CONCURRENCY_TEST_ATTEMPTS = 20
    }
}

private fun List<OrderItem>.quantityOf(productId: String): Int {
    return firstOrNull { item ->
        item.productId == productId
    }?.quantity ?: 0
}

// Listas y no conjuntos: si se tocan dos veces, el aviso sigue hasta que
// terminen las dos operaciones.
private data class StockOperations(
    val reservingProductIds: List<String> = emptyList(),
    val releasingProductIds: List<String> = emptyList()
)

private data class InteractionState(
    val query: String,
    val orderMessage: String?,
    val stockOperations: StockOperations
)

private data class CatalogSnapshot(
    val products: List<Product> = emptyList(),
    val loadState: CatalogLoadState = CatalogLoadState.Loading
)

private data class SortedCatalog(
    val products: List<Product>,
    val loadState: CatalogLoadState,
    val sortOrder: CatalogSortOrder
)