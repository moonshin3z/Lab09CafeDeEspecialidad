package com.uvg.lab09_cafedeespecialidad.catalog

import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder
import com.uvg.lab09_cafedeespecialidad.model.Product

fun sortCatalog(
    products: List<Product>,
    sortOrder: CatalogSortOrder
): List<Product> {
    return when (sortOrder) {
        CatalogSortOrder.NAME -> products.sortedBy { product ->
            product.name.lowercase()
        }

        CatalogSortOrder.PRICE -> products.sortedBy { product ->
            product.price
        }
    }
}
