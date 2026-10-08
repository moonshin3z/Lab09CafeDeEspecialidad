package com.uvg.lab09_cafedeespecialidad

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.lab09_cafedeespecialidad.catalog.sortCatalog
import com.uvg.lab09_cafedeespecialidad.data.local.StoreDao
import com.uvg.lab09_cafedeespecialidad.data.local.StorePreferencesKeys
import com.uvg.lab09_cafedeespecialidad.data.local.entity.FavoriteEntity
import com.uvg.lab09_cafedeespecialidad.data.local.entity.OrderLineEntity
import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import com.uvg.lab09_cafedeespecialidad.model.CheckoutUiState
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.OrderReceipt
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.model.Profile
import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import com.uvg.lab09_cafedeespecialidad.order.OrderUpdateResult
import com.uvg.lab09_cafedeespecialidad.order.addToOrder as addToOrderRule
import com.uvg.lab09_cafedeespecialidad.order.calculateLineSubtotal
import com.uvg.lab09_cafedeespecialidad.order.calculateOrderTotal
import com.uvg.lab09_cafedeespecialidad.order.decreaseOrderItem as decreaseOrderItemRule
import com.uvg.lab09_cafedeespecialidad.validation.validateBusinessName
import com.uvg.lab09_cafedeespecialidad.validation.validateFullName
import com.uvg.lab09_cafedeespecialidad.validation.validateNit
import com.uvg.lab09_cafedeespecialidad.validation.validatePhone
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val storeDao: StoreDao,
    private val preferencesDataStore: DataStore<Preferences>
) : ViewModel() {
    private val originalProducts = listOf(
        Product(
            id = "cafe-geisha",
            name = "Geisha de Huehuetenango",
            description = "Taza floral y delicada, con notas a jazmín, bergamota y un dulzor tipo panela.",
            price = 145.00,
            stock = 3,
            imageUrl = "https://picsum.photos/seed/cafe-geisha/400/400",
            profileId = "finca-la-esperanza",
            technicalSheet = "Altitud: 1,850 msnm · Variedad: Geisha · Proceso: Lavado · Secado: Patio y sombra, 12 días."
        ),
        Product(
            id = "cafe-bourbon",
            name = "Bourbon de Antigua",
            description = "Cuerpo medio con acidez cítrica equilibrada, notas de chocolate y almendra tostada.",
            price = 98.00,
            stock = 8,
            imageUrl = "https://picsum.photos/seed/cafe-bourbon/400/400",
            profileId = "finca-la-esperanza",
            technicalSheet = "Altitud: 1,500 msnm · Variedad: Bourbon Rojo · Proceso: Honey · Secado: Camas africanas, 9 días."
        ),
        Product(
            id = "cafe-caturra",
            name = "Caturra de Cobán",
            description = "Perfil suave y balanceado, con notas a caramelo, nuez y final limpio achocolatado.",
            price = 85.00,
            stock = 0,
            imageUrl = "https://picsum.photos/seed/cafe-caturra/400/400",
            profileId = "cooperativa-chicoj",
            technicalSheet = "Altitud: 1,300 msnm · Variedad: Caturra · Proceso: Natural · Secado: Marquesina, 15 días."
        )
    )

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

    private val products = generateCatalog(
        originals = originalProducts,
        seed = CATALOG_SEED
    )

    private val orderMessage = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")

    private val catalogSortOrder: Flow<CatalogSortOrder> =
        preferencesDataStore.data.map { preferences ->
            CatalogSortOrder.fromStorageValue(
                preferences[StorePreferencesKeys.CATALOG_SORT_ORDER]
                    ?: CatalogSortOrder.NAME.storageValue
            )
        }

    val uiState: StateFlow<StoreUiState> = combine(
        storeDao.observeFavorites(),
        storeDao.observeOrderLines(),
        catalogSortOrder,
        query,
        orderMessage
    ) { favorites, orderLines, sortOrder, currentQuery, currentOrderMessage ->
        StoreUiState(
            products = sortCatalog(
                products = products,
                sortOrder = sortOrder
            ),
            profiles = originalProfiles,
            favoriteIds = favorites.map { it.productId }.toSet(),
            query = currentQuery,
            orderItems = orderLines.map { line ->
                OrderItem(
                    productId = line.productId,
                    quantity = line.quantity
                )
            },
            orderMessage = currentOrderMessage,
            sortOrder = sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(
            products = sortCatalog(
                products = products,
                sortOrder = CatalogSortOrder.NAME
            ),
            profiles = originalProfiles,
            sortOrder = CatalogSortOrder.NAME
        )
    )

    private val _checkoutUiState = MutableStateFlow(
        CheckoutUiState()
    )

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState.asStateFlow()

    private val _orderReceipt =
        MutableStateFlow<OrderReceipt?>(null)

    val orderReceipt: StateFlow<OrderReceipt?> =
        _orderReceipt.asStateFlow()

    private var nextOrderNumber = INITIAL_ORDER_NUMBER

    init {
        check(uiState.value.products.size == 500)
        check(
            uiState.value.products
                .map { product -> product.id }
                .distinct()
                .size == 500
        )
    }

    fun toggleFavorite(productId: String) {
        val isFavorite = productId in uiState.value.favoriteIds

        viewModelScope.launch {
            if (isFavorite) {
                storeDao.deleteFavorite(productId)
            } else {
                storeDao.insertFavorite(
                    FavoriteEntity(productId = productId)
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        this.query.value = query
    }

    fun onSortOrderChange(sortOrder: CatalogSortOrder) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[StorePreferencesKeys.CATALOG_SORT_ORDER] =
                    sortOrder.storageValue
            }
        }
    }

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
                val unitLabel = if (increment == 1) {
                    "unidad"
                } else {
                    "unidades"
                }

                orderMessage.value =
                    "Se agregó $increment $unitLabel al pedido."

                val updatedLine = result.orderItems.first {
                    it.productId == productId
                }

                viewModelScope.launch {
                    storeDao.upsertOrderLine(
                        OrderLineEntity(
                            productId = updatedLine.productId,
                            quantity = updatedLine.quantity
                        )
                    )
                }
            }

            is OrderUpdateResult.Rejected -> {
                orderMessage.value = result.reason
            }
        }
    }

    fun decreaseOrderItem(productId: String) {
        val updatedItems = decreaseOrderItemRule(
            orderItems = uiState.value.orderItems,
            productId = productId
        )

        orderMessage.value = null

        viewModelScope.launch {
            val updatedLine = updatedItems.firstOrNull {
                it.productId == productId
            }

            if (updatedLine == null) {
                storeDao.deleteOrderLine(productId)
            } else {
                storeDao.upsertOrderLine(
                    OrderLineEntity(
                        productId = updatedLine.productId,
                        quantity = updatedLine.quantity
                    )
                )
            }
        }
    }

    fun removeOrderItem(productId: String) {
        orderMessage.value = null

        viewModelScope.launch {
            storeDao.deleteOrderLine(productId)
        }
    }

    fun orderSubtotal(productId: String): Double {
        val current = uiState.value

        val product = current.products.firstOrNull { item ->
            item.id == productId
        } ?: return 0.0

        val orderItem = current.orderItems.firstOrNull { item ->
            item.productId == productId
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
        return uiState.value.orderItems.sumOf { orderItem ->
            orderItem.quantity
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

    fun confirmOrder(): Boolean {
        val validatedCheckout =
            _checkoutUiState.value.withRecalculatedValidation()

        _checkoutUiState.value = validatedCheckout

        val currentStore = uiState.value
        val units = currentStore.orderItems.sumOf { orderItem ->
            orderItem.quantity
        }

        if (!validatedCheckout.isFormValid || units <= 0) {
            return false
        }

        val total = calculateOrderTotal(
            products = currentStore.products,
            orderItems = currentStore.orderItems
        )

        val receipt = OrderReceipt(
            folio = formatFolio(nextOrderNumber),
            customerName = validatedCheckout.fullName.trim(),
            phone = validatedCheckout.phone.trim(),
            billingType = validatedCheckout.billingType,
            nit = if (
                validatedCheckout.billingType == BillingType.NIT
            ) {
                validatedCheckout.nit.trim()
            } else {
                null
            },
            businessName = if (
                validatedCheckout.billingType == BillingType.NIT
            ) {
                validatedCheckout.businessName.trim()
            } else {
                null
            },
            paymentMethod = validatedCheckout.paymentMethod,
            total = total
        )

        _orderReceipt.value = receipt

        orderMessage.value = null

        viewModelScope.launch {
            storeDao.clearOrderLines()
        }

        _checkoutUiState.value = CheckoutUiState()
        nextOrderNumber += 1

        return true
    }

    private fun CheckoutUiState.withRecalculatedValidation():
            CheckoutUiState {
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
        const val CATALOG_SEED = 2026
        const val INITIAL_ORDER_NUMBER = 1
    }
}