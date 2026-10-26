package com.uvg.lab09_cafedeespecialidad.domain.repository

interface AuthRepository {

    suspend fun ensureSignedIn(): Result<Unit>

    fun currentUserId(): String?
}