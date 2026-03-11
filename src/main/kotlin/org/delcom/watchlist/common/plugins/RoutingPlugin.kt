package org.delcom.watchlist.common.plugins
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.watchlist.auth.route.authRoutes
import org.delcom.watchlist.todo.route.todoRoutes
import org.delcom.watchlist.user.route.userRoutes
fun Application.configureRouting() {
    routing {
        get("/") { call.respond(mapOf("status" to "ok", "app" to "WatchList API", "version" to "1.0.0")) }
        authRoutes(); userRoutes(); todoRoutes()
    }
}