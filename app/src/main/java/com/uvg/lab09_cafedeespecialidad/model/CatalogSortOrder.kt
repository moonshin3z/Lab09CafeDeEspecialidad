package com.uvg.lab09_cafedeespecialidad.model

enum class CatalogSortOrder(
    val storageValue: String
) {
    NAME(storageValue = "name"),
    PRICE(storageValue = "price");

    companion object {
        fun fromStorageValue(value: String): CatalogSortOrder {
            return entries.firstOrNull { sortOrder ->
                sortOrder.storageValue == value
            } ?: NAME
        }
    }
}
