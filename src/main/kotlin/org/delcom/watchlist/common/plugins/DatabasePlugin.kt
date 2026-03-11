package org.delcom.watchlist.common.plugins
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.delcom.watchlist.auth.table.AuthTokensTable
import org.delcom.watchlist.common.config.loadAppConfig
import org.delcom.watchlist.todo.table.TodosTable
import org.delcom.watchlist.user.table.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
fun Application.configureDatabases() {
    val cfg = loadAppConfig().db
    val ds = HikariDataSource(HikariConfig().apply {
        jdbcUrl = cfg.url; driverClassName = cfg.driver; username = cfg.user; password = cfg.password
        maximumPoolSize = cfg.maxPool; isAutoCommit = false; transactionIsolation = "TRANSACTION_REPEATABLE_READ"; validate()
    })
    Database.connect(ds)
    transaction { SchemaUtils.createMissingTablesAndColumns(UsersTable, AuthTokensTable, TodosTable) }
    log.info("Database ready.")
}