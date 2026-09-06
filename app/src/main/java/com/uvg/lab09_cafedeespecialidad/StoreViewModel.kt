package com.uvg.lab09_cafedeespecialidad
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = listOf(
                Product(
                    id = "p1",
                    name = "Geisha Huehuetenango",
                    description = "Taza floral y delicada, con notas a jazmín, " +
                        "bergamota y un dulzor tipo panela. Proceso lavado.",
                    price = 145.00,
                    profileId = "f1",
                    technicalSheet = "Altitud: 1,850 msnm · Variedad: Geisha · " +
                        "Proceso: Lavado · Secado: Patio y sombra, 12 días · " +
                        "Cosecha: Diciembre–Febrero."
                ),
                Product(
                    id = "p2",
                    name = "Bourbon Antigua",
                    description = "Cuerpo medio con acidez cítrica equilibrada, " +
                        "notas de chocolate con leche y almendra tostada.",
                    price = 98.00,
                    profileId = "f1",
                    technicalSheet = "Altitud: 1,500 msnm · Variedad: Bourbon Rojo · " +
                        "Proceso: Honey · Secado: Camas africanas, 9 días · " +
                        "Cosecha: Enero–Marzo."
                ),
                Product(
                    id = "p3",
                    name = "Caturra Cobán",
                    description = "Perfil suave y balanceado, con notas a caramelo, " +
                        "nuez y un final limpio ligeramente achocolatado.",
                    price = 85.00,
                    profileId = "f2",
                    technicalSheet = "Altitud: 1,300 msnm · Variedad: Caturra · " +
                        "Proceso: Natural · Secado: Marquesina, 15 días · " +
                        "Cosecha: Noviembre–Enero."
                )
            ),
            profiles = listOf(
                Profile(
                    id = "f1",
                    name = "Finca La Esperanza",
                    role = "Finca productora",
                    location = "Huehuetenango, Guatemala",
                    description = "Finca familiar de tercera generación dedicada al " +
                        "cultivo de variedades de altura bajo sombra, con enfoque " +
                        "en prácticas de agricultura sostenible."
                ),
                Profile(
                    id = "f2",
                    name = "Cooperativa Chicoj",
                    role = "Cooperativa de productores",
                    location = "Cobán, Alta Verapaz, Guatemala",
                    description = "Agrupa a más de 40 familias caficultoras de la " +
                        "región y comercializa bajo un modelo de comercio justo " +
                        "que reinvierte en la comunidad."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    /** Alterna el favorito de un producto y produce un nuevo estado inmutable. */
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
