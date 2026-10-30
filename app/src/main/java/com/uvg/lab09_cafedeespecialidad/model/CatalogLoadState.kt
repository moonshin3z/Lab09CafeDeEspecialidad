package com.uvg.lab09_cafedeespecialidad.model

sealed interface CatalogLoadState {

    data object Loading : CatalogLoadState

    data object Success : CatalogLoadState

    data class Error(
        val message: String
    ) : CatalogLoadState
}