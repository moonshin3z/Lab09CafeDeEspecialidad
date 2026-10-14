package com.uvg.lab09_cafedeespecialidad.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object OrderApiClient {

    private const val BASE_URL =
        "https://6ac7d4df75a4ce3fe7224823.mockapi.io/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val orderApi: OrderApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(OrderApi::class.java)
    }
}