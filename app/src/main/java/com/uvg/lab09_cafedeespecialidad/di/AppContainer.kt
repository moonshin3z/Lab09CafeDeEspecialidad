package com.uvg.lab09_cafedeespecialidad.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.lab09_cafedeespecialidad.data.firebase.FirebaseAuthRepository
import com.uvg.lab09_cafedeespecialidad.data.firebase.FirestoreProductRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository
import com.uvg.lab09_cafedeespecialidad.domain.repository.ProductRepository

class AppContainer(
    @Suppress("UNUSED_PARAMETER")
    context: Context
) {
    private val firebaseAuth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    val authRepository: AuthRepository =
        FirebaseAuthRepository(
            firebaseAuth = firebaseAuth
        )

    val productRepository: ProductRepository =
        FirestoreProductRepository(
            firestore = firestore,
            authRepository = authRepository
        )
}