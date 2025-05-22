package com.projeto.healthmonitor.api

import com.squareup.moshi.Json

data class TimeResponse(
    @Json(name = "datetime")
    val datetime: String
)
