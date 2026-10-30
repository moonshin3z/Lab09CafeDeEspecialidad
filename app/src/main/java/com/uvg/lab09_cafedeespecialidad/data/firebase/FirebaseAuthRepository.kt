package com.uvg.lab09_cafedeespecialidad.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.uvg.lab09_cafedeespecialidad.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun ensureSignedIn(): Result<Unit> {
        if (firebaseAuth.currentUser != null) {
            return Result.success(Unit)
        }

        return try {
            val result = firebaseAuth
                .signInAnonymously()
                .await()

            if (result.user == null) {
                Result.failure(
                    IllegalStateException(
                        "Firebase no devolvió un usuario autenticado."
                    )
                )
            } else {
                Result.success(Unit)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override fun currentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}