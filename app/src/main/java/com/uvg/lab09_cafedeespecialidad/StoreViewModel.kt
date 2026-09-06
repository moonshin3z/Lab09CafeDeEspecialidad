package com.uvg.lab09_cafedeespecialidad

import androidx.lifecycle.ViewModel
import com.uvg.lab09_cafedeespecialidad.model.Product
import com.uvg.lab09_cafedeespecialidad.model.Profile
import com.uvg.lab09_cafedeespecialidad.model.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = listOf(
                Product(
                    id = "cafe-geisha",
                    name = "Geisha de Huehuetenango",
                    description = "Café de aroma floral, acidez brillante y notas de jazmín y durazno.",
                    price = 145.00,
                    profileId = "finca-la-esperanza"
                ),
                Product(
                    id = "cafe-bourbon",
                    name = "Bourbon de Antigua",
                    description = "Café balanceado con notas de chocolate, caramelo y frutos rojos.",
                    price = 110.00,
                    profileId = "finca-la-esperanza"
                ),
                Product(
                    id = "cafe-caturra",
                    name = "Caturra de Cobán",
                    description = "Café de cuerpo cremoso con notas de cacao, nuez y naranja.",
                    price = 95.00,
                    profileId = "cooperativa-chicoj"
                )
            ),
            profiles = listOf(
                Profile(
                    id = "finca-la-esperanza",
                    name = "Finca La Esperanza",
                    role = "Productor de café de especialidad",
                    location = "Huehuetenango, Guatemala",
                    description = "Finca familiar dedicada al cultivo y procesamiento de café de altura."
                ),
                Profile(
                    id = "cooperativa-chicoj",
                    name = "Cooperativa Chicoj",
                    role = "Cooperativa de productores",
                    location = "Cobán, Alta Verapaz, Guatemala",
                    description = "Cooperativa que reúne a pequeños productores y promueve prácticas sostenibles."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            val updatedFavoriteIds =
                if (productId in currentState.favoriteIds) {
                    currentState.favoriteIds - productId
                } else {
                    currentState.favoriteIds + productId
                }

            currentState.copy(
                favoriteIds = updatedFavoriteIds
            )
        }
    }
}