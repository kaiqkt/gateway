package com.kaiqkt.gateway.entities

data class Error(
    val type: Type,
    val message: String
)

enum class Type{
    SESSION_NOT_FOUND, ACCESS_TOKEN_EXPIRED
}
