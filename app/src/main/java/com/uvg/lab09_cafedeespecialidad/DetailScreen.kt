package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.uvg.lab09_cafedeespecialidad.model.ConcurrencyTestState
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.ui.components.FavoriteIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    product: Product,
    isFavorite: Boolean,
    orderQuantity: Int,
    orderMessage: String?,
    isReserving: Boolean,
    concurrencyTestState: ConcurrencyTestState,
    onToggleFavorite: () -> Unit,
    onAddToOrder: () -> Unit,
    onConcurrencyTest: () -> Unit,
    onProfileClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTechnicalSheetExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
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
                .verticalScroll(rememberScrollState())
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

            // Las existencias son las de Firestore: lo que está en el pedido
            // ya se descontó al reservarlo.
            Text(
                text = if (product.stock == 0) {
                    "Agotado · $orderQuantity en el pedido"
                } else {
                    "${product.stock} disponibles · $orderQuantity en el pedido"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            // Sigue habilitado sin existencias para que el ViewModel explique
            // por qué no se puede agregar.
            Button(
                onClick = onAddToOrder,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isReserving
            ) {
                if (isReserving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservando unidad…")
                } else {
                    Text("Agregar al pedido")
                }
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

            HorizontalDivider()

            ConcurrencyTestSection(
                productId = product.id,
                state = concurrencyTestState,
                onConcurrencyTest = onConcurrencyTest
            )
        }
    }
}

/** Herramienta de prueba del laboratorio: no cambia el pedido en curso. */
@Composable
private fun ConcurrencyTestSection(
    productId: String,
    state: ConcurrencyTestState,
    onConcurrencyTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = state is ConcurrencyTestState.Running

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onConcurrencyTest,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            Text("Prueba de concurrencia · 20 intentos")
        }

        val resultText = when (state) {
            is ConcurrencyTestState.Running -> if (state.productId == productId) {
                "Enviando ${state.attempts} reservas a la vez…"
            } else {
                null
            }

            is ConcurrencyTestState.Finished -> if (state.productId == productId) {
                "Éxitos: ${state.successes} · Rechazos: ${state.rejections}"
            } else {
                null
            }

            ConcurrencyTestState.Idle -> null
        }

        if (resultText != null) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
