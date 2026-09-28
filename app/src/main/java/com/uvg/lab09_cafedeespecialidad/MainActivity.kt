package com.uvg.lab09_cafedeespecialidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uvg.lab09_cafedeespecialidad.ui.theme.Lab09CafeDeEspecialidadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab09CafeDeEspecialidadTheme {
                StoreApp()
            }
        }
    }
}
