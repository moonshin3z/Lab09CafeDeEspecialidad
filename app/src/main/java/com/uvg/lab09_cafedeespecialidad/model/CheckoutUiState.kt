package com.uvg.lab09_cafedeespecialidad.model

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,

    val fullNameError: String? = null,
    val phoneError: String? = null,
    val nitError: String? = null,
    val businessNameError: String? = null,

    val isFullNameTouched: Boolean = false,
    val isPhoneTouched: Boolean = false,
    val isNitTouched: Boolean = false,
    val isBusinessNameTouched: Boolean = false,

    val isFormValid: Boolean = false
)