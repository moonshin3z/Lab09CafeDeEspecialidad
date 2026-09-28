package com.uvg.lab09_cafedeespecialidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.OrderReceipt
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import com.uvg.lab09_cafedeespecialidad.ui.theme.Lab09CafeDeEspecialidadTheme
import java.util.Locale

@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "¡Pedido confirmado!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Orden registrada exitosamente en su tienda.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReceiptRow(
                            label = "Folio",
                            value = receipt.folio
                        )
                        ReceiptRow(
                            label = "Cliente",
                            value = receipt.customerName
                        )
                        ReceiptRow(
                            label = "Teléfono",
                            value = receipt.phone
                        )
                        ReceiptRow(
                            label = "Facturación",
                            value = billingLabel(receipt.billingType)
                        )

                        receipt.nit?.let { nit ->
                            ReceiptRow(
                                label = "NIT",
                                value = nit
                            )
                        }

                        receipt.businessName?.let { businessName ->
                            ReceiptRow(
                                label = "Razón social",
                                value = businessName
                            )
                        }

                        ReceiptRow(
                            label = "Método de pago",
                            value = paymentMethodLabel(receipt.paymentMethod)
                        )

                        HorizontalDivider()

                        ReceiptRow(
                            label = "Total del pedido",
                            value = formatQuetzales(receipt.total),
                            isHighlighted = true
                        )
                    }
                }

                Button(
                    onClick = onBackToCatalog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al catálogo")
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            style = if (isHighlighted) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = if (isHighlighted) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

private fun billingLabel(billingType: BillingType): String {
    return when (billingType) {
        BillingType.CF -> "CF (Consumidor Final)"
        BillingType.NIT -> "Factura con NIT"
    }
}

private fun paymentMethodLabel(paymentMethod: PaymentMethod): String {
    return when (paymentMethod) {
        PaymentMethod.CASH_ON_DELIVERY -> "Efectivo contra entrega"
        PaymentMethod.BANK_TRANSFER -> "Transferencia bancaria"
    }
}

private fun formatQuetzales(amount: Double): String {
    return String.format(Locale.US, "Q %.2f", amount)
}

@Preview(showBackground = true)
@Composable
private fun OrderConfirmationScreenPreview() {
    Lab09CafeDeEspecialidadTheme {
        OrderConfirmationScreen(
            receipt = OrderReceipt(
                folio = "#ORD-00001",
                customerName = "Alberto Guzmán",
                phone = "55442211",
                billingType = BillingType.NIT,
                nit = "4512789",
                businessName = "Guzmán Inversiones S.A.",
                paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
                total = 243.00
            ),
            onBackToCatalog = {}
        )
    }
}
