package com.uvg.lab09_cafedeespecialidad.model

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)