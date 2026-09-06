package com.uvg.lab09_cafedeespecialidad.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val profileId: String
)