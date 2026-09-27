package com.uvg.lab09_cafedeespecialidad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.CheckoutUiState
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    orderUnits: Int,
    orderTotal: Double,
    isConfirmEnabled: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Checkout")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumen del pedido",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "$orderUnits unidades",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Total: Q ${"%.2f".format(orderTotal)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre completo *")
                },
                placeholder = {
                    Text("Ej. María Morales")
                },
                singleLine = true,
                isError = uiState.isFullNameTouched &&
                        uiState.fullNameError != null,
                supportingText = {
                    if (
                        uiState.isFullNameTouched &&
                        uiState.fullNameError != null
                    ) {
                        Text(uiState.fullNameError.orEmpty())
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                )
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Teléfono / WhatsApp *")
                },
                placeholder = {
                    Text("Ej. 55123456")
                },
                singleLine = true,
                isError = uiState.isPhoneTouched &&
                        uiState.phoneError != null,
                supportingText = {
                    if (
                        uiState.isPhoneTouched &&
                        uiState.phoneError != null
                    ) {
                        Text(uiState.phoneError.orEmpty())
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = if (
                        uiState.billingType == BillingType.NIT
                    ) {
                        ImeAction.Next
                    } else {
                        ImeAction.Done
                    }
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (uiState.billingType == BillingType.NIT) {
                            nitFocusRequester.requestFocus()
                        }
                    },
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Facturación *",
                    style = MaterialTheme.typography.titleMedium
                )

                listOf(
                    BillingType.CF to "Consumidor Final (CF)",
                    BillingType.NIT to "Factura con NIT"
                ).forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = uiState.billingType == type,
                                role = Role.RadioButton,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onBillingTypeChange(type)
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.billingType == type,
                            onClick = null
                        )

                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.billingType == BillingType.NIT
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Datos de facturación fiscal",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = uiState.nit,
                        onValueChange = onNitChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nitFocusRequester),
                        label = {
                            Text("NIT *")
                        },
                        singleLine = true,
                        isError = uiState.isNitTouched &&
                                uiState.nitError != null,
                        supportingText = {
                            if (
                                uiState.isNitTouched &&
                                uiState.nitError != null
                            ) {
                                Text(uiState.nitError.orEmpty())
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Next)
                            }
                        )
                    )

                    OutlinedTextField(
                        value = uiState.businessName,
                        onValueChange = onBusinessNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Razón Social / Nombre fiscal *")
                        },
                        placeholder = {
                            Text("Ej. Café Guatemalteco S.A.")
                        },
                        singleLine = true,
                        isError = uiState.isBusinessNameTouched &&
                                uiState.businessNameError != null,
                        supportingText = {
                            if (
                                uiState.isBusinessNameTouched &&
                                uiState.businessNameError != null
                            ) {
                                Text(
                                    uiState.businessNameError.orEmpty()
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Método de pago *",
                    style = MaterialTheme.typography.titleMedium
                )

                listOf(
                    PaymentMethod.CASH_ON_DELIVERY to
                            "Efectivo contra entrega",
                    PaymentMethod.BANK_TRANSFER to
                            "Transferencia bancaria"
                ).forEach { (method, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = uiState.paymentMethod == method,
                                role = Role.RadioButton,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onPaymentMethodChange(method)
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.paymentMethod == method,
                            onClick = null
                        )

                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onConfirmOrder,
                modifier = Modifier.fillMaxWidth(),
                enabled = isConfirmEnabled
            ) {
                Text(
                    text = "Confirmar pedido (Total Q ${
                        "%.2f".format(orderTotal)
                    })"
                )
            }

            if (!isConfirmEnabled) {
                Text(
                    text = "Completa los campos obligatorios para continuar.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}