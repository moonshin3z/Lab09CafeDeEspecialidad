package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.OrderSubmissionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderConfirmationViewModelTest {

    @Test
    fun invalidFormDoesNotSendOrClearOrder() {
        val orderApi = FakeOrderApi()

        val viewModel = createTestStoreViewModel(
            storeDao = FakeStoreDao(),
            preferencesDataStore = FakePreferencesDataStore(),
            orderApi = orderApi
        )

        viewModel.addToOrder("cafe-geisha")
        viewModel.awaitOrderUnits(1)

        viewModel.confirmOrder()

        assertTrue(orderApi.sentOrders.isEmpty())
        assertEquals(
            OrderSubmissionState.Idle,
            viewModel.orderSubmissionState.value
        )
        assertEquals(
            1,
            viewModel.uiState.value.orderItems.sumOf { item ->
                item.quantity
            }
        )
    }

    @Test
    fun successfulSubmissionSendsOrderAndClearsCart() {
        val orderApi = FakeOrderApi()

        val viewModel = createTestStoreViewModel(
            storeDao = FakeStoreDao(),
            preferencesDataStore = FakePreferencesDataStore(),
            orderApi = orderApi
        )

        viewModel.addToOrder("cafe-geisha")
        viewModel.awaitOrderUnits(1)
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        viewModel.confirmOrder()

        waitForSubmission(viewModel) { state ->
            state is OrderSubmissionState.Success
        }

        assertEquals(1, orderApi.sentOrders.size)
        assertEquals("CF", orderApi.sentOrders.single().billingType)
        assertEquals(145.00, orderApi.sentOrders.single().total, 0.001)

        viewModel.awaitOrderUnits(0)

        assertTrue(viewModel.uiState.value.orderItems.isEmpty())
        assertEquals("", viewModel.checkoutUiState.value.fullName)
        assertEquals("", viewModel.checkoutUiState.value.phone)
        assertTrue(viewModel.orderReceipt.value != null)
    }

    @Test
    fun failedSubmissionKeepsCartAndFormForRetry() {
        val orderApi = FakeOrderApi(
            shouldFailOnCreate = true
        )

        val viewModel = createTestStoreViewModel(
            storeDao = FakeStoreDao(),
            preferencesDataStore = FakePreferencesDataStore(),
            orderApi = orderApi
        )

        viewModel.addToOrder("cafe-geisha")
        viewModel.awaitOrderUnits(1)
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        viewModel.confirmOrder()

        val result = waitForSubmission(viewModel) { state ->
            state is OrderSubmissionState.Error
        }

        assertTrue(result is OrderSubmissionState.Error)
        assertEquals(1, orderApi.sentOrders.size)
        assertNull(viewModel.orderReceipt.value)

        viewModel.awaitOrderUnits(1)

        assertEquals(
            1,
            viewModel.uiState.value.orderItems.sumOf { item ->
                item.quantity
            }
        )
        assertEquals(
            "María Morales",
            viewModel.checkoutUiState.value.fullName
        )
        assertEquals(
            "55123456",
            viewModel.checkoutUiState.value.phone
        )
    }

    private fun waitForSubmission(
        viewModel: StoreViewModel,
        condition: (OrderSubmissionState) -> Boolean
    ): OrderSubmissionState {
        return runBlocking {
            withTimeout(1_000) {
                viewModel.orderSubmissionState.first(condition)
            }
        }
    }
}
