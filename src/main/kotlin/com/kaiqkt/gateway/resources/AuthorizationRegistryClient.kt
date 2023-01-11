package com.kaiqkt.gateway.resources

import com.kaiqkt.gateway.entities.Authentication
import com.kaiqkt.gateway.entities.Error
import com.kaiqkt.gateway.exceptions.RefreshTokenException
import com.kaiqkt.gateway.exceptions.UnauthorisedException
import com.kaiqkt.gateway.ext.mapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component


@Component
class AuthorizationRegistryClient(
    @Value("\${services.authorization-registry-service-url}")
    private val serviceUrl: String
) {

    fun validateSession(accessToken: String): Error? {
        Fuel.get("$serviceUrl/session/validate")
            .header(
                mapOf(Headers.AUTHORIZATION to accessToken)
            ).response().let { (_, response) ->
                if (response.statusCode == HttpStatus.UNAUTHORIZED.value()) {
                    return jacksonObjectMapper().readValue(response.body().toByteArray(), Error::class.java)
                }
                return null
            }
    }

    fun refresh(accessToken: String, refreshToken: String): Authentication {
        Fuel.post("$serviceUrl/auth/refresh")
            .header(
                mapOf(
                    Headers.AUTHORIZATION to "Bearer $accessToken",
                    Headers.CONTENT_TYPE to "application/vnd.kaiqkt_auth_refresh_v1+json",
                    "Refresh-Token" to refreshToken
                )
            ).response().let { (_, response) ->
                when (response.statusCode) {
                    HttpStatus.OK.value() -> return mapper().readValue(
                        response.body().toByteArray(),
                        Authentication::class.java
                    )
                    HttpStatus.UNAUTHORIZED.value() -> {
                        val error = jacksonObjectMapper().readValue(response.body().toByteArray(), Error::class.java)
                        throw RefreshTokenException(error)
                    }

                    else -> {
                        throw UnauthorisedException()
                    }
                }
            }
    }
}