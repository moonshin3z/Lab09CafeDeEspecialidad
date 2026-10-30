package com.uvg.lab09_cafedeespecialidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uvg.lab09_cafedeespecialidad.ui.theme.Lab09CafeDeEspecialidadTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container =
            (application as StoreApplication).container

        setContent {
            Lab09CafeDeEspecialidadTheme {
                StoreApp(
                    viewModel = viewModel(
                        factory = container.storeViewModelFactory
                    ),
                    ordersViewModel = viewModel(
                        factory = container.ordersViewModelFactory
                    )
                )
            }
        }
    }
}