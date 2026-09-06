package com.uvg.lab09_cafedeespecialidad

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val profileId: String,
    val technicalSheet: String
)

data class Profile(
    val id: String,
    val name: String,
    val role: String,
    val location: String,
    val description: String
)

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)
