package org.delcom.watchlist.user.route
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.watchlist.common.config.loadAppConfig
import org.delcom.watchlist.common.plugins.AppException
import org.delcom.watchlist.common.util.*
import org.delcom.watchlist.user.model.*
import org.delcom.watchlist.user.repository.UserRepository
fun Routing.userRoutes() {
    authenticate("auth-jwt") {
        route("/users") {
            get("/me") {
                val uid = call.uid()
                val user = UserRepository.findById(uid) ?: throw AppException(HttpStatusCode.NotFound, "User tidak ditemukan")
                call.respond(HttpStatusCode.OK, success("Berhasil", UserResponse(user)))
            }
            put("/me") {
                val uid = call.uid(); val b = call.receive<UpdateProfileRequest>()
                if (b.name.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Nama tidak boleh kosong")
                if (b.username.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Username tidak boleh kosong")
                if (UserRepository.usernameExistsExcept(b.username, uid)) throw AppException(HttpStatusCode.Conflict, "Username sudah digunakan")
                UserRepository.updateProfile(uid, b.name, b.username)
                call.respond(HttpStatusCode.OK, success("Profil diperbarui", UserResponse(UserRepository.findById(uid)!!)))
            }
            put("/me/password") {
                val uid = call.uid(); val b = call.receive<UpdatePasswordRequest>()
                if (b.newPassword.length < 6) throw AppException(HttpStatusCode.BadRequest, "Password baru minimal 6 karakter")
                val hashed = UserRepository.getPassword(uid) ?: throw AppException(HttpStatusCode.NotFound, "User tidak ditemukan")
                if (!UserRepository.verifyPassword(b.password, hashed)) throw AppException(HttpStatusCode.Unauthorized, "Password saat ini salah")
                UserRepository.updatePassword(uid, b.newPassword)
                call.respond(HttpStatusCode.OK, success<Unit>("Password berhasil diubah"))
            }
            put("/me/about") {
                val uid = call.uid(); val b = call.receive<UpdateAboutRequest>()
                UserRepository.updateAbout(uid, b.about)
                call.respond(HttpStatusCode.OK, success<Unit>("Bio diperbarui"))
            }
            put("/me/photo") {
                val uid = call.uid(); val cfg = call.application.loadAppConfig().upload; var ext: String? = null
                call.receiveMultipart().forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "file") ext = ImageService.saveImage(part, cfg.dir, "users", uid, cfg.maxSizeMb)
                    part.dispose()
                }
                ext ?: throw AppException(HttpStatusCode.BadRequest, "File gambar tidak ditemukan")
                UserRepository.updatePhoto(uid, ext!!)
                call.respond(HttpStatusCode.OK, success<Unit>("Foto profil diperbarui"))
            }
        }
    }
    get("/images/users/{userId}") {
        val id = call.parameters["userId"] ?: throw AppException(HttpStatusCode.BadRequest, "userId diperlukan")
        val file = ImageService.findImage(call.application.loadAppConfig().upload.dir, "users", id)
            ?: throw AppException(HttpStatusCode.NotFound, "Foto tidak ditemukan")
        call.respondFile(file)
    }
}
private fun ApplicationCall.uid() = principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()