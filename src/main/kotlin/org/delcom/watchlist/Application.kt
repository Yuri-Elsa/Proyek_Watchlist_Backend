package org.delcom.watchlist
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.delcom.watchlist.common.plugins.*
fun main(args: Array<String>) = EngineMain.main(args)
fun Application.module() {
    configureDatabases(); configureSerialization(); configureAuthentication()
    configureCors(); configureStatusPages(); configureCallLogging(); configureRouting()
}