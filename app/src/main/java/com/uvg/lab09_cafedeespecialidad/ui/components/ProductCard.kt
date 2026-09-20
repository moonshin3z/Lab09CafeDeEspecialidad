package com.uvg.lab09_cafedeespecialidad.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.Product

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(product.id) {
        Log.d(
            "CatalogProbe",
            "ENTER id=${product.id}"
        )

        onDispose {
            Log.d(
                "CatalogProbe",
                "EXIT id=${product.id}"
            )
        }
    }

    Card(
        modifier = modifier
            .height(288.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            ProductImage(
                imageUrl = product.imageUrl
            )

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Q ${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (product.stock == 0) {
                        "Agotado"
                    } else {
                        "${product.stock} disponibles"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                FavoriteIconButton(
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}
