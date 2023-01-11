package com.kaiqkt.gateway.entities

data class Authentication(
    val accessToken: String,
    val refreshToken: String
)
