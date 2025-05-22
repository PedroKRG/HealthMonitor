package com.projeto.healthmonitor.api

import retrofit2.http.GET

interface TimeApiService {
    @GET("timezone/America/Sao_Paulo")
    suspend fun getCurrentTime(): TimeResponse
}