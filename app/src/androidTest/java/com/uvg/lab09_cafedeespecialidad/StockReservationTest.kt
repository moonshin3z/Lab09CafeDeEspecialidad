package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.ConcurrencyTestState
import com.uvg.lab09_cafedeespecialidad.model.OrderSubmissionState
import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockReservationTest {

    private val productRepository = FakeProductRepository()

    private val viewModel = createTestStoreViewModel(
        productRepository = productRepository
    )

    @Test
    fun addingReservesTheUnitBeforeChangingTheOrder() {
        viewModel.addToOrder("cafe-bourbon")

        val state = viewModel.awaitOrderUnits(1)

        assertEquals(7, productRepository.stockOf("cafe-bourbon"))
        assertEquals(1, state.quantityOf("cafe-bourbon"))
    }

    @Test
    fun decreasingAndRemovingReturnUnitsToTheStock() {
        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitOrderUnits(1)
        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitOrderUnits(2)
        assertEquals(6, productRepository.stockOf("cafe-bourbon"))

        viewModel.decreaseOrderItem("cafe-bourbon")
        viewModel.awaitOrderUnits(1)
        assertEquals(7, productRepository.stockOf("cafe-bourbon"))

        viewModel.removeOrderItem("cafe-bourbon")
        viewModel.awaitOrderUnits(0)
        assertEquals(8, productRepository.stockOf("cafe-bourbon"))
    }

    @Test
    fun outOfStockProductIsRejectedAndTheOrderDoesNotChange() {
        viewModel.addToOrder("cafe-caturra")

        val state = viewModel.awaitOrderMessage(
            "Agotado: no quedan existencias de este producto."
        )

        assertTrue(state.orderItems.isEmpty())
        assertEquals(0, productRepository.stockOf("cafe-caturra"))
    }

    @Test
    fun serverRejectsTheReservationEvenIfTheScreenShowedStock() {
        val gate = CompletableDeferred<Unit>()
        productRepository.reservationGate = gate

        viewModel.addToOrder("cafe-geisha")
        viewModel.awaitReserving("cafe-geisha")

        // Otro cliente se lleva las últimas unidades antes de que llegue la reserva.
        productRepository.emitProducts(
            testProducts().map { product ->
                if (product.id == "cafe-geisha") {
                    product.copy(stock = 0)
                } else {
                    product
                }
            }
        )
        gate.complete(Unit)

        val state = viewModel.awaitOrderMessage(
            "Agotado: no quedan existencias de este producto."
        )

        assertTrue(state.orderItems.isEmpty())
        assertEquals(0, productRepository.stockOf("cafe-geisha"))
    }

    @Test
    fun screenShowsTheReservationWhileItIsPending() {
        val gate = CompletableDeferred<Unit>()
        productRepository.reservationGate = gate

        viewModel.addToOrder("cafe-bourbon")
        val pendingState = viewModel.awaitReserving("cafe-bourbon")

        assertTrue(pendingState.orderItems.isEmpty())

        gate.complete(Unit)
        val finalState = viewModel.awaitOrderUnits(1)

        assertTrue(finalState.reservingProductIds.isEmpty())
    }

    @Test
    fun quickTapsAreReservedOneAfterAnotherWithoutLosingUnits() {
        val gate = CompletableDeferred<Unit>()
        productRepository.reservationGate = gate

        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitReserving("cafe-bourbon")
        viewModel.addToOrder("cafe-bourbon")
        viewModel.decreaseOrderItem("cafe-bourbon")
        gate.complete(Unit)

        val state = viewModel.awaitOrderUnits(1)

        assertEquals(1, state.quantityOf("cafe-bourbon"))
        assertEquals(7, productRepository.stockOf("cafe-bourbon"))
    }

    @Test
    fun failedReservationKeepsTheOrderAndTheStock() {
        productRepository.isOffline = true

        viewModel.addToOrder("cafe-bourbon")

        val state = viewModel.awaitOrderMessage(
            "No se pudo reservar la unidad. Revisa tu conexión e intenta de nuevo."
        )

        assertTrue(state.orderItems.isEmpty())
        assertEquals(8, productRepository.stockOf("cafe-bourbon"))
    }

    @Test
    fun failedReleaseKeepsTheLineInTheOrder() {
        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitOrderUnits(1)
        productRepository.isOffline = true

        viewModel.decreaseOrderItem("cafe-bourbon")

        val state = viewModel.awaitOrderMessage(
            "No se pudieron devolver las unidades. Revisa tu conexión e intenta de nuevo."
        )

        assertEquals(1, state.quantityOf("cafe-bourbon"))
        assertEquals(7, productRepository.stockOf("cafe-bourbon"))
    }

    @Test
    fun confirmingTheOrderDoesNotChangeTheStock() {
        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitOrderUnits(1)
        viewModel.addToOrder("cafe-bourbon")
        viewModel.awaitOrderUnits(2)
        viewModel.onFullNameChange("María Morales")
        viewModel.onPhoneChange("55123456")

        viewModel.confirmOrder()

        runBlocking {
            withTimeout(1_000) {
                viewModel.orderSubmissionState.first { state ->
                    state is OrderSubmissionState.Success
                }
            }
        }
        viewModel.awaitOrderUnits(0)

        assertEquals(6, productRepository.stockOf("cafe-bourbon"))
    }

    @Test
    fun concurrencyTestReservesOnlyTheUnitsThatExist() {
        val repository = FakeProductRepository(
            initialProducts = testProducts().map { product ->
                if (product.id == "cafe-bourbon") {
                    product.copy(stock = 5)
                } else {
                    product
                }
            }
        )
        val testViewModel = createTestStoreViewModel(
            productRepository = repository
        )

        testViewModel.onConcurrencyTest(
            productId = "cafe-bourbon",
            attempts = 20
        )

        val result = runBlocking {
            withTimeout(1_000) {
                testViewModel.concurrencyTestState.first { state ->
                    state is ConcurrencyTestState.Finished
                }
            }
        }

        assertEquals(
            ConcurrencyTestState.Finished(
                productId = "cafe-bourbon",
                successes = 5,
                rejections = 15
            ),
            result
        )
        assertEquals(0, repository.stockOf("cafe-bourbon"))
        assertTrue(testViewModel.uiState.value.orderItems.isEmpty())
    }

    private fun StoreUiState.quantityOf(productId: String): Int {
        return orderItems.firstOrNull { item ->
            item.productId == productId
        }?.quantity ?: 0
    }

    private fun StoreViewModel.awaitOrderMessage(message: String): StoreUiState {
        return runBlocking {
            withTimeout(1_000) {
                uiState.first { state ->
                    state.orderMessage == message &&
                        state.reservingProductIds.isEmpty() &&
                        state.releasingProductIds.isEmpty()
                }
            }
        }
    }

    private fun StoreViewModel.awaitReserving(productId: String): StoreUiState {
        return runBlocking {
            withTimeout(1_000) {
                uiState.first { state ->
                    productId in state.reservingProductIds
                }
            }
        }
    }
}
