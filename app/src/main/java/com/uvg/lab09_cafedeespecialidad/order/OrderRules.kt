package com.uvg.lab09_cafedeespecialidad.order

import com.uvg.lab09_cafedeespecialidad.model.OrderItem
import com.uvg.lab09_cafedeespecialidad.model.Product

sealed interface OrderUpdateResult {
    data class Success(
        val orderItems: List<OrderItem>
    ) : OrderUpdateResult

    data class Rejected(
        val reason: String
    ) : OrderUpdateResult
}

fun addToOrder(
    products: List<Product>,
    orderItems: List<OrderItem>,
    productId: String,
    increment: Int = 1
): OrderUpdateResult {
    if (increment <= 0) {
        return OrderUpdateResult.Rejected(
            reason = "La cantidad debe ser mayor que cero."
        )
    }

    val product = products.firstOrNull { currentProduct ->
        currentProduct.id == productId
    } ?: return OrderUpdateResult.Rejected(
        reason = "El producto solicitado no existe."
    )

    val currentItem = orderItems.firstOrNull { item ->
        item.productId == productId
    }
    val currentQuantity = currentItem?.quantity ?: 0
    val requestedQuantity = currentQuantity + increment

    if (requestedQuantity > product.stock) {
        val reason = if (product.stock == 0) {
            "No hay existencias disponibles para este producto."
        } else {
            "No hay existencias suficientes. Máximo disponible: ${product.stock}."
        }

        return OrderUpdateResult.Rejected(reason = reason)
    }

    val updatedItems = if (currentItem == null) {
        orderItems + OrderItem(
            productId = productId,
            quantity = increment
        )
    } else {
        orderItems.map { item ->
            if (item.productId == productId) {
                item.copy(quantity = requestedQuantity)
            } else {
                item
            }
        }
    }

    return OrderUpdateResult.Success(orderItems = updatedItems)
}

fun decreaseOrderItem(
    orderItems: List<OrderItem>,
    productId: String
): List<OrderItem> {
    val currentItem = orderItems.firstOrNull { item ->
        item.productId == productId
    } ?: return orderItems

    return if (currentItem.quantity <= 1) {
        removeOrderItem(
            orderItems = orderItems,
            productId = productId
        )
    } else {
        orderItems.map { item ->
            if (item.productId == productId) {
                item.copy(quantity = item.quantity - 1)
            } else {
                item
            }
        }
    }
}

fun removeOrderItem(
    orderItems: List<OrderItem>,
    productId: String
): List<OrderItem> {
    return orderItems.filterNot { item ->
        item.productId == productId
    }
}

fun calculateLineSubtotal(
    product: Product,
    orderItem: OrderItem
): Double {
    return product.price * orderItem.quantity
}

fun calculateOrderTotal(
    products: List<Product>,
    orderItems: List<OrderItem>
): Double {
    val productsById = products.associateBy { product ->
        product.id
    }

    return orderItems.sumOf { orderItem ->
        val product = productsById[orderItem.productId]
        if (product == null) {
            0.0
        } else {
            calculateLineSubtotal(
                product = product,
                orderItem = orderItem
            )
        }
    }
}
