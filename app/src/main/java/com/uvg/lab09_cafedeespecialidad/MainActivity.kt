package com.uvg.lab09_cafedeespecialidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uvg.lab09_cafedeespecialidad.ui.theme.Lab09CafeDeEspecialidadTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uvg.lab09_cafedeespecialidad.data.local.StoreDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = StoreDatabase.getInstance(applicationContext)

        setContent {
            Lab09CafeDeEspecialidadTheme {
                StoreApp(
                    viewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                StoreViewModel(
                                    storeDao = database.storeDao()
                                )
                            }
                        }
                    )
                )
            }
        }
    }
}