package com.example.fu

import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux

val app = application(WebApplicationType.REACTIVE) {
    webFlux {
        router {
            val repository = ref<CustomerRepository>()
            GET("/customers") { ok().body(repository.all()) }
            GET("/greetings/{name}") { ok().bodyValue("Hello, ${it.pathVariable("name")}!") }
        }
        codecs {
            string()
            jackson { indentOutput = true }
        }
    }
    listener<ApplicationReadyEvent> {
        val repository = ref<CustomerRepository>()
        Flux
                .just("A", "B", "C")
                .map { Customer(null, it) }
                .flatMap { repository.insert(it) }
                .subscribe { println("saving ${it.id} having name ${it.name}") }
    }
    reactiveMongodb()
    beans {
        bean<CustomerRepository>()
    }
}

class CustomerRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) {
    fun all(): Flux<Customer> = this.reactiveMongoTemplate.findAll()
    fun insert(c: Customer) = this.reactiveMongoTemplate.save(c)
}

data class Customer(val id: String? = null, val name: String)

fun main(args: Array<String>) {
    app.run(args)
}