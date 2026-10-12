package com.uvg.lab09_cafedeespecialidad.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.remote.OrderDto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onRetry: () -> Unit,
    onOrderClick: (String) -> Unit,
    onCatalogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Mis pedidos")
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                OrdersUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                OrdersUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aún no has enviado pedidos",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onCatalogClick) {
                            Text(text = "Ir al catálogo")
                        }
                    }
                }

                is OrdersUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onRetry) {
                            Text(text = "Reintentar")
                        }
                    }
                }

                is OrdersUiState.Success -> {
                    OrdersList(
                        orders = uiState.orders,
                        onOrderClick = onOrderClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<OrderDto>,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = orders,
            key = { order -> order.id }
        ) { order ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOrderClick(order.id)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pedido #${order.id}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = formatOrderDate(order.createdAt),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = formatOrderAmount(order.total),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

private fun formatOrderDate(createdAt: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(createdAt))
}

private fun formatOrderAmount(amount: Double): String {
    return NumberFormat
        .getCurrencyInstance(Locale.forLanguageTag("es-GT"))
        .format(amount)
}