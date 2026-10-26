package com.uvg.lab09_cafedeespecialidad.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.uvg.lab09_cafedeespecialidad.data.firebase.FirebaseAuthRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository

class AppContainer(
    @Suppress("UNUSED_PARAMETER")
    context: Context
) {
    val authRepository: AuthRepository =
        FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance()
        )
}