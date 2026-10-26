package com.uvg.lab09_cafedeespecialidad.data.firebase

import com.uvg.lab09_cafedeespecialidad.model.Product

data class ProductDocument(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Long = 0L,
    val imageUrl: String = "",
    val profileId: String = "",
    val technicalSheet: String = ""
)

fun ProductDocument.toDomain(productId: String): Product {
    return Product(
        id = productId,
        name = name,
        description = description,
        price = price,
        stock = stock.toInt(),
        imageUrl = imageUrl,
        profileId = profileId,
        technicalSheet = technicalSheet
    )
}

fun Product.toDocument(): ProductDocument {
    return ProductDocument(
        name = name,
        description = description,
        price = price,
        stock = stock.toLong(),
        imageUrl = imageUrl,
        profileId = profileId,
        technicalSheet = technicalSheet
    )
}