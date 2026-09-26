package com.uvg.lab09_cafedeespecialidad.model

data class OrderReceipt(
    val folio: String,
    val customerName: String,
    val phone: String,
    val billingType: BillingType,
    val nit: String?,
    val businessName: String?,
    val paymentMethod: PaymentMethod,
    val total: Double
)