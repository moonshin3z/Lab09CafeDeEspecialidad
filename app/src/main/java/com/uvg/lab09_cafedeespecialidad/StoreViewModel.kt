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
                    description = "Taza floral y delicada, con notas a jazmín, bergamota y un dulzor tipo panela.",
                    price = 145.00,
                    profileId = "finca-la-esperanza",
                    technicalSheet = "Altitud: 1,850 msnm · Variedad: Geisha · Proceso: Lavado · Secado: Patio y sombra, 12 días."
                ),
                Product(
                    id = "cafe-bourbon",
                    name = "Bourbon de Antigua",
                    description = "Cuerpo medio con acidez cítrica equilibrada, notas de chocolate y almendra tostada.",
                    price = 98.00,
                    profileId = "finca-la-esperanza",
                    technicalSheet = "Altitud: 1,500 msnm · Variedad: Bourbon Rojo · Proceso: Honey · Secado: Camas africanas, 9 días."
                ),
                Product(
                    id = "cafe-caturra",
                    name = "Caturra de Cobán",
                    description = "Perfil suave y balanceado, con notas a caramelo, nuez y final limpio achocolatado.",
                    price = 85.00,
                    profileId = "cooperativa-chicoj",
                    technicalSheet = "Altitud: 1,300 msnm · Variedad: Caturra · Proceso: Natural · Secado: Marquesina, 15 días."
                )
            ),
            profiles = listOf(
                Profile(
                    id = "finca-la-esperanza",
                    name = "Finca La Esperanza",
                    role = "Finca productora",
                    location = "Huehuetenango, Guatemala",
                    description = "Finca familiar de tercera generación dedicada al cultivo de variedades de altura bajo sombra."
                ),
                Profile(
                    id = "cooperativa-chicoj",
                    name = "Cooperativa Chicoj",
                    role = "Cooperativa de productores",
                    location = "Cobán, Alta Verapaz, Guatemala",
                    description = "Agrupa a más de 40 familias caficultoras y comercializa bajo un modelo de comercio justo."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { current ->
            current.copy(
                favoriteIds = if (productId in current.favoriteIds) {
                    current.favoriteIds - productId
                } else {
                    current.favoriteIds + productId
                }
            )
        }
    }
}