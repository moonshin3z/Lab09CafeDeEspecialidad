package com.uvg.lab09_cafedeespecialidad.domain.repository

import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun observeProducts(): Flow<List<Product>>
}