package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.ui.components.FavoriteIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    product: Product,
    isFavorite: Boolean,
    orderQuantity: Int,
    orderMessage: String?,
    onToggleFavorite: () -> Unit,
    onAddToOrder: () -> Unit,
    onProfileClick: () -> Unit,
    onBack: () -> Unit
) {
    var isTechnicalSheetExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q ${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                FavoriteIconButton(
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite
                )
            }

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "${product.stock} disponibles · $orderQuantity en el pedido",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onAddToOrder,
                modifier = Modifier.fillMaxWidth(),
                enabled = product.stock > 0 && orderQuantity < product.stock
            ) {
                Text("Agregar al pedido")
            }

            if (orderMessage != null) {
                Text(
                    text = orderMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider()

            TextButton(onClick = { isTechnicalSheetExpanded = !isTechnicalSheetExpanded }) {
                Text(if (isTechnicalSheetExpanded) "Ocultar ficha técnica" else "Ver ficha técnica")
                Icon(
                    imageVector = if (isTechnicalSheetExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            if (isTechnicalSheetExpanded) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = product.technicalSheet,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedButton(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver productor")
            }
        }
    }
}
