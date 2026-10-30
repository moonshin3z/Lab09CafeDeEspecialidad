package com.uvg.lab09_cafedeespecialidad.model

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val query: String = "",
    val orderItems: List<OrderItem> = emptyList(),
    val orderMessage: String? = null,
    val sortOrder: CatalogSortOrder = CatalogSortOrder.NAME,
    val catalogLoadState: CatalogLoadState =
        CatalogLoadState.Loading,
    // Productos con una reserva o una devolución esperando a Firestore.
    val reservingProductIds: Set<String> = emptySet(),
    val releasingProductIds: Set<String> = emptySet()
)
