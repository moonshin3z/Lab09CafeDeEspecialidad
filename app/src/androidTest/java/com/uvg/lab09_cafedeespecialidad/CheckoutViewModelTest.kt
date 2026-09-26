package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutViewModelTest {

    @Test
    fun checkoutStartsInCfWithoutVisibleErrors() {
        val viewModel = StoreViewModel()
        val state = viewModel.checkoutUiState.value

        assertEquals(BillingType.CF, state.billingType)
        assertEquals(
            PaymentMethod.CASH_ON_DELIVERY,
            state.paymentMethod
        )

        assertEquals("", state.fullName)
        assertEquals("", state.phone)
        assertEquals("", state.nit)
        assertEquals("", state.businessName)

        assertNull(state.fullNameError)
        assertNull(state.phoneError)
        assertNull(state.nitError)
        assertNull(state.businessNameError)

        assertFalse(state.isFullNameTouched)
        assertFalse(state.isPhoneTouched)
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertFalse(state.isFormValid)
    }

    @Test
    fun editingFullNameMarksFieldAsTouched() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Al")

        val state = viewModel.checkoutUiState.value

        assertEquals("Al", state.fullName)
        assertTrue(state.isFullNameTouched)
        assertEquals(
            "Ingrese un nombre con al menos 3 letras.",
            state.fullNameError
        )
        assertFalse(state.isFormValid)
    }

    @Test
    fun correctingFullNameRemovesItsError() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Al")
        viewModel.onFullNameChange("Alberto Guzmán")

        val state = viewModel.checkoutUiState.value

        assertTrue(state.isFullNameTouched)
        assertNull(state.fullNameError)
    }

    @Test
    fun validNameAndPhoneMakeCfFormValid() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        val state = viewModel.checkoutUiState.value

        assertTrue(state.isFormValid)
        assertNull(state.fullNameError)
        assertNull(state.phoneError)
        assertNull(state.nitError)
        assertNull(state.businessNameError)
    }

    @Test
    fun selectingNitValidatesFiscalFieldsWithoutTouchingThem() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")
        viewModel.onBillingTypeChange(BillingType.NIT)

        val state = viewModel.checkoutUiState.value

        assertEquals(BillingType.NIT, state.billingType)
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            state.nitError
        )
        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            state.businessNameError
        )
        assertFalse(state.isFormValid)
    }

    @Test
    fun editingInvalidNitMarksItAsTouched() {
        val viewModel = StoreViewModel()

        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("4512")

        val state = viewModel.checkoutUiState.value

        assertTrue(state.isNitTouched)
        assertEquals(
            "Ingrese al menos 5 dígitos.",
            state.nitError
        )
    }

    @Test
    fun validFiscalFieldsMakeNitFormValid() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Alberto Guzmán")
        viewModel.onPhoneChange("55442211")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("12345")
        viewModel.onBusinessNameChange(
            "Guzmán Inversiones S.A."
        )

        val state = viewModel.checkoutUiState.value

        assertTrue(state.isFormValid)
        assertNull(state.fullNameError)
        assertNull(state.phoneError)
        assertNull(state.nitError)
        assertNull(state.businessNameError)
    }

    @Test
    fun changingFromNitToCfClearsFiscalErrorsAndTouchedState() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Alberto Guzmán")
        viewModel.onPhoneChange("55442211")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("4512")
        viewModel.onBusinessNameChange("AB")

        viewModel.onBillingTypeChange(BillingType.CF)

        val state = viewModel.checkoutUiState.value

        assertEquals(BillingType.CF, state.billingType)
        assertNull(state.nitError)
        assertNull(state.businessNameError)
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertTrue(state.isFormValid)
    }

    @Test
    fun changingToCfPreservesFiscalText() {
        val viewModel = StoreViewModel()

        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("4512")
        viewModel.onBusinessNameChange("AB")

        viewModel.onBillingTypeChange(BillingType.CF)

        val state = viewModel.checkoutUiState.value

        assertEquals("4512", state.nit)
        assertEquals("AB", state.businessName)
    }

    @Test
    fun returningToNitRevalidatesFiscalFieldsWithoutTouchingThem() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("Alberto Guzmán")
        viewModel.onPhoneChange("55442211")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("4512")
        viewModel.onBusinessNameChange("AB")
        viewModel.onBillingTypeChange(BillingType.CF)

        viewModel.onBillingTypeChange(BillingType.NIT)

        val state = viewModel.checkoutUiState.value

        assertEquals(BillingType.NIT, state.billingType)
        assertEquals(
            "Ingrese al menos 5 dígitos.",
            state.nitError
        )
        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            state.businessNameError
        )
        assertFalse(state.isNitTouched)
        assertFalse(state.isBusinessNameTouched)
        assertFalse(state.isFormValid)
    }

    @Test
    fun paymentMethodCanBeChangedWithoutLosingFormData() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        viewModel.onPaymentMethodChange(
            PaymentMethod.BANK_TRANSFER
        )

        val state = viewModel.checkoutUiState.value

        assertEquals(
            PaymentMethod.BANK_TRANSFER,
            state.paymentMethod
        )
        assertEquals("María Morales", state.fullName)
        assertEquals("55123456", state.phone)
        assertTrue(state.isFormValid)
    }
}
