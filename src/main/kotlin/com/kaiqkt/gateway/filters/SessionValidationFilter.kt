package com.kaiqkt.gateway.filters

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kaiqkt.gateway.entities.Error
import com.kaiqkt.gateway.entities.Type
import com.kaiqkt.gateway.exceptions.UnauthorisedException
import com.kaiqkt.gateway.ext.AUTHORIZATION_BEARER_PREFIX
import com.kaiqkt.gateway.ext.REFRESH_TOKEN_HEADER
import com.kaiqkt.gateway.resources.AuthorizationRegistryClient
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SessionValidationFilter(
    private val authorizationRegistryClient: AuthorizationRegistryClient
) : AbstractGatewayFilterFactory<SessionValidationFilter.Config>(Config::class.java) {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request

            val accessToken = request.headers[HttpHeaders.AUTHORIZATION]?.first() ?: throw UnauthorisedException()
            val refreshToken = request.headers[REFRESH_TOKEN_HEADER]?.first() ?: throw UnauthorisedException()

            authorizationRegistryClient.validateSession(accessToken)?.run {
                handle(this, exchange, accessToken, refreshToken)
            }

            chain.filter(exchange)
        }
    }

    private fun handle(
        error: Error,
        exchange: ServerWebExchange,
        accessToken: String,
        refreshToken: String
    ) {
        when (error.type) {
            Type.ACCESS_TOKEN_EXPIRED -> refreshAuthentication(exchange, accessToken, refreshToken)
            Type.SESSION_NOT_FOUND -> throw UnauthorisedException()
        }
    }

    private fun refreshAuthentication(
        exchange: ServerWebExchange,
        accessToken: String,
        refreshToken: String
    ): ServerWebExchange {
        val authentication = authorizationRegistryClient.refresh(accessToken, refreshToken)

        val request = exchange.request.mutate()
            .header(HttpHeaders.AUTHORIZATION, "$AUTHORIZATION_BEARER_PREFIX${authentication.accessToken}").build()

        val response = exchange.response.apply {
            this.headers[HttpHeaders.AUTHORIZATION] = "$AUTHORIZATION_BEARER_PREFIX${authentication.accessToken}"
            this.headers[REFRESH_TOKEN_HEADER] = authentication.refreshToken
        }

        exchange.mutate().response(response).build()
        exchange.mutate().request(request).build()

        return exchange
    }

    private fun ServerWebExchange.error(
        error: Error
    ): Mono<Void> {
        this.response.statusCode = HttpStatus.UNAUTHORIZED
        val dataBufferFactory: DataBufferFactory = this.response.bufferFactory()
        val body = jacksonObjectMapper().writeValueAsBytes(error)
        this.response.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        return this.response.writeWith(Mono.just(body).map { r -> dataBufferFactory.wrap(r) })
    }

    class Config
}
