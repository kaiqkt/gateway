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
import org.springframework.http.HttpMethod

@Configuration
class AuthRegistryRoutesServiceConfig(
    @Value("\${services.authorization-registry-service-url}")
    private val serviceUrl: String
) {
    @Bean
    fun authRegistryRoutes(
        builder: RouteLocatorBuilder,
        sessionValidationFilter: SessionValidationFilter,
        setHeadersFilter: SetHeadersFilter
    ): RouteLocator? {
        return builder.routes()
            //session
            .route { r: PredicateSpec ->
                r.path("/session")
                r.filters {
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            //authentication
            .route { r: PredicateSpec ->
                r.path("/auth/login")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_auth_login_v1+json")))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/auth/refresh")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_auth_refresh_v1+json")))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/auth/logout")
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/auth/logout/all")
                r.filters {
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/auth/logout/**")
                r.filters {
                    this.rewritePath("/auth/logout/?<sessionId>.*", "/auth/logout/\${sessionId}")
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            //user
            .route { r: PredicateSpec ->
                r.path("/user/address")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_address_v1+json")))
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user/phone")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_phone_v1+json")))
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user/update-password")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_password_v1+json")))
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user/redefine-password")
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_redefine_password_v1+json")))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user/redefine-password/**")
                r.filters {
                    this.rewritePath("/user/redefine-password/?<code>.*", "/user/redefine-password/\${code}")
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_redefine_password_v1+json")))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user")
                r.method(HttpMethod.POST)
                r.filters {
                    this.filter(setHeadersFilter.apply(SetHeadersFilter.Config("application/vnd.kaiqkt_user_v1+json")))
                }
                r.uri(serviceUrl)
            }
            .route { r: PredicateSpec ->
                r.path("/user")
                r.filters {
                    this.filter(sessionValidationFilter.apply(SessionValidationFilter.Config()))
                }
                r.uri(serviceUrl)
            }
            .build()
    }
}