package com.kaiqkt.gateway.filters

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class SetHeadersFilter : AbstractGatewayFilterFactory<SetHeadersFilter.Config>(Config::class.java) {
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request.mutate().header("Content-Type", config.contentType).build()

            val mutatedExchange: ServerWebExchange = exchange.mutate().request(request).build()

            chain.filter(mutatedExchange)
        }
    }

    class Config(val contentType: String)
}

