package com.uvg.lab09_cafedeespecialidad

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uvg.lab09_cafedeespecialidad.data.local.StoreDatabase
import com.uvg.lab09_cafedeespecialidad.data.local.storePreferencesDataStore
import com.uvg.lab09_cafedeespecialidad.ui.theme.Lab09CafeDeEspecialidadTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container =
            (application as StoreApplication).container

        val database =
            StoreDatabase.getInstance(applicationContext)

        val preferencesDataStore =
            applicationContext.storePreferencesDataStore

        setContent {
            Lab09CafeDeEspecialidadTheme {
                StoreApp(
                    onSeedCatalog = { products ->
                        lifecycleScope.launch {
                            val authenticationResult =
                                container.authRepository.ensureSignedIn()

                            if (authenticationResult.isFailure) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No se pudo iniciar la sesión anónima.",
                                    Toast.LENGTH_LONG
                                ).show()

                                return@launch
                            }

                            val seedResult =
                                container.catalogSeeder.seed(products)

                            val message = seedResult.fold(
                                onSuccess = { productCount ->
                                    "Se cargaron $productCount productos."
                                },
                                onFailure = { error ->
                                    error.message
                                        ?: "No se pudo cargar el catálogo."
                                }
                            )

                            Toast.makeText(
                                this@MainActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    viewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                StoreViewModel(
                                    storeDao = database.storeDao(),
                                    preferencesDataStore =
                                        preferencesDataStore
                                )
                            }
                        }
                    )
                )
            }
        }
    }
}