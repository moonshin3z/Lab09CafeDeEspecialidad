package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.BillingType
import com.uvg.lab09_cafedeespecialidad.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderConfirmationViewModelTest {

    @Test
    fun invalidFormDoesNotConfirmOrClearOrder() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-geisha")

        val confirmed = viewModel.confirmOrder()

        assertFalse(confirmed)
        assertEquals(
            1,
            viewModel.uiState.value.orderItems.sumOf { item ->
                item.quantity
            }
        )
        assertNull(viewModel.orderReceipt.value)
    }

    @Test
    fun emptyOrderCannotBeConfirmedWithValidForm() {
        val viewModel = StoreViewModel()

        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        val confirmed = viewModel.confirmOrder()

        assertFalse(confirmed)
        assertTrue(viewModel.uiState.value.orderItems.isEmpty())
        assertNull(viewModel.orderReceipt.value)
    }

    @Test
    fun validCfOrderCreatesReceiptAndClearsOrder() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-geisha")
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        val confirmed = viewModel.confirmOrder()
        val receipt = viewModel.orderReceipt.value

        assertTrue(confirmed)
        requireNotNull(receipt)

        assertEquals("#ORD-00001", receipt.folio)
        assertEquals("María Morales", receipt.customerName)
        assertEquals("55123456", receipt.phone)
        assertEquals(BillingType.CF, receipt.billingType)
        assertEquals(
            PaymentMethod.CASH_ON_DELIVERY,
            receipt.paymentMethod
        )
        assertNull(receipt.nit)
        assertNull(receipt.businessName)
        assertEquals(145.00, receipt.total, 0.001)

        assertTrue(viewModel.uiState.value.orderItems.isEmpty())
        assertEquals(0, viewModel.orderUnits())
        assertEquals(0.0, viewModel.orderTotal(), 0.001)
    }

    @Test
    fun receiptKeepsTotalAfterOrderIsCleared() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder(
            productId = "cafe-geisha",
            increment = 2
        )
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        val totalBeforeConfirmation = viewModel.orderTotal()

        val confirmed = viewModel.confirmOrder()
        val receipt = viewModel.orderReceipt.value

        assertTrue(confirmed)
        requireNotNull(receipt)

        assertEquals(290.00, totalBeforeConfirmation, 0.001)
        assertEquals(290.00, receipt.total, 0.001)
        assertEquals(0.0, viewModel.orderTotal(), 0.001)
    }

    @Test
    fun successfulConfirmationResetsCheckoutForm() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-geisha")
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")
        viewModel.onPaymentMethodChange(
            PaymentMethod.BANK_TRANSFER
        )

        val confirmed = viewModel.confirmOrder()
        val checkout = viewModel.checkoutUiState.value

        assertTrue(confirmed)
        assertEquals("", checkout.fullName)
        assertEquals("", checkout.phone)
        assertEquals("", checkout.nit)
        assertEquals("", checkout.businessName)
        assertEquals(BillingType.CF, checkout.billingType)
        assertEquals(
            PaymentMethod.CASH_ON_DELIVERY,
            checkout.paymentMethod
        )
        assertFalse(checkout.isFullNameTouched)
        assertFalse(checkout.isPhoneTouched)
        assertFalse(checkout.isNitTouched)
        assertFalse(checkout.isBusinessNameTouched)
        assertFalse(checkout.isFormValid)
    }

    @Test
    fun nitOrderStoresFiscalInformationInReceipt() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-bourbon")
        viewModel.onFullNameChange("Alberto Guzmán")
        viewModel.onPhoneChange("55442211")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("12345")
        viewModel.onBusinessNameChange(
            "Guzmán Inversiones S.A."
        )
        viewModel.onPaymentMethodChange(
            PaymentMethod.BANK_TRANSFER
        )

        val confirmed = viewModel.confirmOrder()
        val receipt = viewModel.orderReceipt.value

        assertTrue(confirmed)
        requireNotNull(receipt)

        assertEquals(BillingType.NIT, receipt.billingType)
        assertEquals("12345", receipt.nit)
        assertEquals(
            "Guzmán Inversiones S.A.",
            receipt.businessName
        )
        assertEquals(
            PaymentMethod.BANK_TRANSFER,
            receipt.paymentMethod
        )
        assertEquals(98.00, receipt.total, 0.001)
    }

    @Test
    fun cfReceiptExcludesPreservedFiscalText() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-geisha")
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("12345")
        viewModel.onBusinessNameChange("Empresa Ejemplo")
        viewModel.onBillingTypeChange(BillingType.CF)

        val confirmed = viewModel.confirmOrder()
        val receipt = viewModel.orderReceipt.value

        assertTrue(confirmed)
        requireNotNull(receipt)

        assertEquals(BillingType.CF, receipt.billingType)
        assertNull(receipt.nit)
        assertNull(receipt.businessName)
    }

    @Test
    fun foliosIncreaseOnlyAfterSuccessfulConfirmation() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-geisha")

        val rejected = viewModel.confirmOrder()

        assertFalse(rejected)
        assertNull(viewModel.orderReceipt.value)

        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        val firstConfirmation = viewModel.confirmOrder()
        val firstFolio = viewModel.orderReceipt.value?.folio

        assertTrue(firstConfirmation)
        assertEquals("#ORD-00001", firstFolio)

        viewModel.addToOrder("cafe-bourbon")
        viewModel.onFullNameChange("Carlos Méndez")
        viewModel.onPhoneChange("55887766")

        val secondConfirmation = viewModel.confirmOrder()
        val secondFolio = viewModel.orderReceipt.value?.folio

        assertTrue(secondConfirmation)
        assertEquals("#ORD-00002", secondFolio)
    }

    @Test
    fun receiptTrimsContactAndFiscalValues() {
        val viewModel = StoreViewModel()

        viewModel.addToOrder("cafe-bourbon")
        viewModel.onFullNameChange("  Alberto Guzmán  ")
        viewModel.onPhoneChange("  55442211  ")
        viewModel.onBillingTypeChange(BillingType.NIT)
        viewModel.onNitChange("  12345  ")
        viewModel.onBusinessNameChange(
            "  Guzmán Inversiones S.A.  "
        )

        val confirmed = viewModel.confirmOrder()
        val receipt = viewModel.orderReceipt.value

        assertTrue(confirmed)
        requireNotNull(receipt)

        assertEquals("Alberto Guzmán", receipt.customerName)
        assertEquals("55442211", receipt.phone)
        assertEquals("12345", receipt.nit)
        assertEquals(
            "Guzmán Inversiones S.A.",
            receipt.businessName
        )
    }
}
