package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.model.Profile

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column {
        Text("Catálogo")

        products.forEach { product ->
            Button(
                onClick = { onProductClick(product.id) }
            ) {
                Text(
                    "${product.name}${
                        if (product.id in favoriteIds) " ★" else ""
                    }"
                )
            }

            Button(
                onClick = { onToggleFavorite(product.id) }
            ) {
                Text("Favorito")
            }
        }
    }
}

@Composable
fun DetailScreen(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onProfileClick: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        Text("Detalle: ${product.name}")
        Text(if (isFavorite) "Favorito activo" else "Sin favorito")

        Button(onClick = onToggleFavorite) {
            Text("Alternar favorito")
        }

        Button(onClick = onProfileClick) {
            Text("Ver productor")
        }

        Button(onClick = onBack) {
            Text("Regresar")
        }
    }
}

@Composable
fun ProfileScreen(
    profile: Profile,
    onBack: () -> Unit
) {
    Column {
        Text("Productor: ${profile.name}")

        Button(onClick = onBack) {
            Text("Regresar")
        }
    }
}