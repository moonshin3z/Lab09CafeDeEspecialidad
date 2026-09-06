package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.ui.components.ProductCard
import com.uvg.lab09_cafedeespecialidad.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Café de Especialidad") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selecciona un café para ver los detalles",
                style = MaterialTheme.typography.bodyMedium
            )
            products.forEach { product ->
                ProductCard(
                    product = product,
                    isFavorite = product.id in favoriteIds,
                    onClick = { onProductClick(product.id) },
                    onToggleFavorite = { onToggleFavorite(product.id) }
                )
            }
        }
    }
}

