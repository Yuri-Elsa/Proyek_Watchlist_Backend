package org.delcom.watchlist.common.plugins
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.delcom.watchlist.common.util.fail
class AppException(val status: HttpStatusCode, message: String) : Exception(message)
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AppException> { call, ex -> call.respond(ex.status, fail(ex.message ?: "Error")) }
        exception<IllegalArgumentException> { call, ex -> call.respond(HttpStatusCode.BadRequest, fail(ex.message ?: "Request tidak valid")) }
        exception<Throwable> { call, ex -> call.application.log.error("Error", ex); call.respond(HttpStatusCode.InternalServerError, fail("Kesalahan server")) }
        status(HttpStatusCode.Unauthorized) { call, _ -> call.respond(HttpStatusCode.Unauthorized, fail("Unauthorized")) }
        status(HttpStatusCode.NotFound) { call, _ -> call.respond(HttpStatusCode.NotFound, fail("Tidak ditemukan")) }
    }
}