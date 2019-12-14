package com.example.reactive

import kotlinx.coroutines.reactive.asFlow
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Document
data class Customer(@Id val id: String, val name: String)

interface CustomerRepository : ReactiveCrudRepository<Customer, String>

@SpringBootApplication
class ReactiveApplication

fun main(args: Array<String>) {
    runApplication<ReactiveApplication>(*args) {
        val context = beans {
            bean {
                ref<RouteLocatorBuilder>().routes {
                    route {
                        host("*.spring.io") and path("/proxy")
                        filters {
                            setPath("/guides")
                            addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        }
                        uri("https://spring.io")
                    }
                }
            }
            bean {
                val customerRepository = ref<CustomerRepository>()
                coRouter {
                    GET("/customers") {
                        ServerResponse.ok().bodyAndAwait(customerRepository.findAll().asFlow())
                    }
                }
            }
        }
        addInitializers(context)
    }
}