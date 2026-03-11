package org.delcom.watchlist.auth.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.watchlist.auth.model.*
import org.delcom.watchlist.auth.repository.AuthRepository
import org.delcom.watchlist.common.plugins.*
import org.delcom.watchlist.common.util.*
fun Routing.authRoutes() {
    route("/auth") {
        post("/register") {
            val b = call.receive<RegisterRequest>()
            if (b.name.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Nama tidak boleh kosong")
            if (b.username.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Username tidak boleh kosong")
            if (b.password.length < 6) throw AppException(HttpStatusCode.BadRequest, "Password minimal 6 karakter")
            if (AuthRepository.usernameExists(b.username)) throw AppException(HttpStatusCode.Conflict, "Username sudah digunakan")
            val id = AuthRepository.createUser(b.name, b.username, b.password)
            call.respond(HttpStatusCode.Created, success("Registrasi berhasil", RegisterResponse(id)))
        }
        post("/login") {
            val b = call.receive<LoginRequest>()
            val creds = AuthRepository.findByUsername(b.username) ?: throw AppException(HttpStatusCode.Unauthorized, "Username atau password salah")
            if (!AuthRepository.verifyPassword(b.password, creds.hashedPassword)) throw AppException(HttpStatusCode.Unauthorized, "Username atau password salah")
            val auth = call.application.generateAuthToken(creds.id)
            val refresh = call.application.generateRefreshToken(creds.id)
            AuthRepository.saveTokens(creds.id, auth, refresh)
            call.respond(HttpStatusCode.OK, success("Login berhasil", LoginResponse(auth, refresh)))
        }
        delete("/logout") {
            val b = call.receive<LogoutRequest>()
            AuthRepository.deleteByAuthToken(b.authToken)
            call.respond(HttpStatusCode.OK, success<Unit>("Logout berhasil"))
        }
        post("/refresh") {
            val b = call.receive<RefreshRequest>()
            val userId = call.application.verifyRefreshToken(b.refreshToken) ?: throw AppException(HttpStatusCode.Unauthorized, "Refresh token tidak valid")
            if (AuthRepository.findUserIdByRefreshToken(b.refreshToken) != userId) throw AppException(HttpStatusCode.Unauthorized, "Refresh token tidak ditemukan")
            val newAuth = call.application.generateAuthToken(userId)
            val newRefresh = call.application.generateRefreshToken(userId)
            AuthRepository.rotateTokens(b.refreshToken, newAuth, newRefresh)
            call.respond(HttpStatusCode.OK, success("Token diperbarui", LoginResponse(newAuth, newRefresh)))
        }
    }
}