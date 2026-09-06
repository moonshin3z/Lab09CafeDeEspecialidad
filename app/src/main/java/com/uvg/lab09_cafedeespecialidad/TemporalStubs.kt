package com.uvg.lab09_cafedeespecialidad


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Product(val id: String, val name: String, val profileId: String)
data class Profile(val id: String, val name: String)

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)

class StoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = listOf(
                Product("p1", "Geisha Huehuetenango", "f1"),
                Product("p2", "Bourbon Antigua", "f1"),
                Product("p3", "Caturra Cobán", "f2")
            ),
            profiles = listOf(
                Profile("f1", "Finca La Esperanza"),
                Profile("f2", "Cooperativa Chicoj")
            )
        )
    )
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { s ->
            s.copy(
                favoriteIds = if (productId in s.favoriteIds) s.favoriteIds - productId
                else s.favoriteIds + productId
            )
        }
    }
}

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column {
        Text("Catálogo")
        products.forEach { p ->
            Button(onClick = { onProductClick(p.id) }) {
                Text("${p.name}${if (p.id in favoriteIds) " ★" else ""}")
            }
            Button(onClick = { onToggleFavorite(p.id) }) { Text("Favorito") }
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
        Button(onClick = onToggleFavorite) { Text("Alternar favorito") }
        Button(onClick = onProfileClick) { Text("Ver productor") }
        Button(onClick = onBack) { Text("Regresar") }
    }
}

@Composable
fun ProfileScreen(profile: Profile, onBack: () -> Unit) {
    Column {
        Text("Productor: ${profile.name}")
        Button(onClick = onBack) { Text("Regresar") }
    }
}