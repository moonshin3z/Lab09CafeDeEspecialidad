package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    products: List<Product>,
    orderItems: List<OrderItem>,
    lineSubtotals: Map<String, Double>,
    orderTotal: Double,
    orderMessage: String?,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit
) {
    val productsById = remember(products) {
        products.associateBy { product ->
            product.id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi pedido")
                },
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
        if (orderItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tu pedido está vacío.",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Agrega productos desde su detalle.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (orderMessage != null) {
                    item {
                        Text(
                            text = orderMessage,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(
                    items = orderItems,
                    key = { orderItem ->
                        orderItem.productId
                    }
                ) { orderItem ->
                    val product = productsById[orderItem.productId]

                    if (product != null) {
                        OrderLine(
                            product = product,
                            orderItem = orderItem,
                            subtotal = lineSubtotals[orderItem.productId] ?: 0.0,
                            onIncrease = {
                                onIncrease(orderItem.productId)
                            },
                            onDecrease = {
                                onDecrease(orderItem.productId)
                            },
                            onRemove = {
                                onRemove(orderItem.productId)
                            }
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Q ${"%.2f".format(orderTotal)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderLine(
    product: Product,
    orderItem: OrderItem,
    subtotal: Double,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Q ${"%.2f".format(product.price)} por unidad",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Subtotal: Q ${"%.2f".format(subtotal)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrease) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Disminuir cantidad"
                    )
                }

                Text(
                    text = orderItem.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onIncrease) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Aumentar cantidad"
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar producto del pedido"
                )
            }
        }

        HorizontalDivider()
    }
}
