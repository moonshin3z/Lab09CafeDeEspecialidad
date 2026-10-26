package com.uvg.lab09_cafedeespecialidad

import android.app.Application
import com.uvg.lab09_cafedeespecialidad.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StoreApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(applicationContext)

        applicationScope.launch {
            container.authRepository.ensureSignedIn()
        }
    }
}