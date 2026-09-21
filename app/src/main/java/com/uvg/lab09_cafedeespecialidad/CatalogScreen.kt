package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.ui.components.ProductCard
import com.uvg.lab09_cafedeespecialidad.ui.components.ScrollToTopButton
import com.uvg.lab09_cafedeespecialidad.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    query: String,
    orderUnitCount: Int,
    gridState: LazyGridState,
    onQueryChange: (String) -> Unit,
    onOrderClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val normalizedQuery = query.trim()

    val filteredProducts = remember(products, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(
                    other = normalizedQuery,
                    ignoreCase = true
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Café de Especialidad")
                },
                actions = {
                    TextButton(onClick = onOrderClick) {
                        Text("Pedido · $orderUnitCount")
                    }
                }
            )
        },
        floatingActionButton = {
            ScrollToTopButton(
                gridState = gridState
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 96.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                SearchBar(
                    query = query,
                    resultCount = filteredProducts.size,
                    onQueryChange = onQueryChange
                )
            }

            if (filteredProducts.isEmpty()) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = "No encontramos productos.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Prueba con otra búsqueda o usa Limpiar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(
                    items = filteredProducts,
                    key = { product ->
                        product.id
                    }
                ) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = product.id in favoriteIds,
                        onClick = {
                            onProductClick(product.id)
                        },
                        onToggleFavorite = {
                            onToggleFavorite(product.id)
                        }
                    )
                }
            }
        }
    }
}
