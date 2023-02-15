package com.kaiqkt.gateway.routes

import com.kaiqkt.gateway.filters.SessionValidationFilter
import com.kaiqkt.gateway.filters.SetHeadersFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommunicationServiceRoutesConfig(
    @Value("\${services.communication-service-url}")
    private val serviceUrl: String
) {
    @Bean
    fun communicationRoutes(
        builder: RouteLocatorBuilder,
        sessionValidationFilter: SessionValidationFilter,
        setHeadersFilter: SetHeadersFilter
    ): RouteLocator? {
        return builder.routes()
            .route { r: PredicateSpec ->
                r.path("/ws")
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/notification")
                r.filters {
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }.build()
    }
}
