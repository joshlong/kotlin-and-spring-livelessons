package com.example.jdbc

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

@SpringBootApplication
class JdbcApplication {

    @Bean
    fun springTransactionManager(ds: DataSource) = SpringTransactionManager(ds)

    @Bean
    fun transactionTemplate(ptm: PlatformTransactionManager) = TransactionTemplate(ptm)
}


object Customers : Table("customer") {
    val id = integer("id").primaryKey()
    val name = varchar("name", 255)
}

@Service
@Transactional
class ExposedOrmCustomerService(val transactionTemplate: TransactionTemplate) : CustomerService, InitializingBean {

    override fun afterPropertiesSet() {
        this.transactionTemplate.execute { SchemaUtils.create(Customers) }
    }

    override fun all(): List<Customer> =
            Customers
                    .selectAll()
                    .map { Customer(it[Customers.id], it[Customers.name]) }

    override fun byId(id: Int): Customer? =
            Customers
                    .select { Customers.id.eq(id) }
                    .map { Customer(it[Customers.id], it[Customers.name]) }
                    .firstOrNull()
}

@Service
@Profile("jdbcTemplate")
class JdbcCustomerService(val jdbcTemplate: JdbcTemplate) : CustomerService {

    override fun all() = this.jdbcTemplate.query("select * from customer") { resultSet, _ ->
        Customer(resultSet.getInt("id"), resultSet.getString("name"))
    }

    override fun byId(id: Int): Customer? =
            this.jdbcTemplate.queryForObject("select * from customer where id = ?", id) { resultSet, _ ->
                Customer(resultSet.getInt("id"), resultSet.getString("name"))
            }
}

data class Customer(val id: Int, val name: String)

interface CustomerService {

    fun all(): List<Customer>

    fun byId(id: Int): Customer?
}

@Component
class Initializer(val customerService: CustomerService) {

    @EventListener(ApplicationReadyEvent::class)
    fun ready() {
        this.customerService.all().forEach {
            println(this.customerService.byId(it.id))
        }
    }
}


fun main(args: Array<String>) {
    runApplication<JdbcApplication>(*args)
}

