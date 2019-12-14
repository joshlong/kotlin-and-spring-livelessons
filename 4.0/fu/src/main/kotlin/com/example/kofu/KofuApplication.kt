package com.example.fu


import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.string
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

val app = application(WebApplicationType.REACTIVE) {
    beans {
        bean<CustomerRepository>()
    }
    webFlux {
        router {
            val repo = ref<CustomerRepository>()
            GET("/customers") { ok().body(repo.all()) }
            GET("/") { ok().syncBody("Hello world!") }
        }
        codecs {
            string()
            jackson { indentOutput = true }
        }

    }
    reactiveMongodb()
    listener<ApplicationReadyEvent> {
        val repo = ref<CustomerRepository>()
        Flux
                .just("A", "B", "C")
                .map { Customer(name = it) }
                .flatMap { repo.insert(it) }
                .subscribe { println("saving ${it.id}") }
    }
}

class CustomerRepository(private val rxTemplate: ReactiveMongoTemplate) {
    fun all(): Flux<Customer> = this.rxTemplate.findAll()
    fun insert(c: Customer) = this.rxTemplate.save(c)
}

data class Customer(val id: String? = null, val name: String)

fun main() {
    app.run()
}
