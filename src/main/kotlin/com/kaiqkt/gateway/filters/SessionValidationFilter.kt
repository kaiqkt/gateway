package com.kaiqkt.gateway.filters

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kaiqkt.gateway.entities.Error
import com.kaiqkt.gateway.entities.Type
import com.kaiqkt.gateway.exceptions.UnauthorisedException
import com.kaiqkt.gateway.ext.AUTHORIZATION_BEARER_PREFIX
import com.kaiqkt.gateway.ext.REFRESH_TOKEN_HEADER
import com.kaiqkt.gateway.resources.AuthorizationRegistryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class SessionValidationFilter: AbstractGatewayFilterFactory<SessionValidationFilter.Config>(Config::class.java) {

    @Autowired
    private lateinit var authorizationRegistryClient: AuthorizationRegistryClient

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request

            logger.info("Request to path ${request.path} of ${request.remoteAddress}")

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

    class Config
}
