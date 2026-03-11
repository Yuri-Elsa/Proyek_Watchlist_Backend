package org.delcom.watchlist.todo.route
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
import org.delcom.watchlist.todo.model.*
import org.delcom.watchlist.todo.repository.TodoRepository
fun Routing.todoRoutes() {
    authenticate("auth-jwt") {
        route("/todos") {
            get("/stats") { call.respond(HttpStatusCode.OK, success("Berhasil", StatsResponse(TodoRepository.getStats(call.uid())))) }
            get {
                val uid = call.uid(); val p = call.request.queryParameters
                val (todos, pag) = TodoRepository.findAll(uid, p["search"], p["page"]?.toIntOrNull() ?: 1, p["perPage"]?.toIntOrNull()?.coerceIn(1,100) ?: 10, p["isDone"]?.toBooleanStrictOrNull(), p["urgency"])
                call.respond(HttpStatusCode.OK, success("Berhasil", TodoListResponse(todos, pag)))
            }
            post {
                val uid = call.uid(); val b = call.receive<CreateTodoRequest>()
                if (b.title.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Judul tidak boleh kosong")
                if (b.urgency !in listOf("low","medium","high")) throw AppException(HttpStatusCode.BadRequest, "Urgency harus: low, medium, atau high")
                val id = TodoRepository.create(uid, b.title, b.description, b.isDone, b.urgency)
                call.respond(HttpStatusCode.Created, success("Film ditambahkan", CreateTodoResponse(id)))
            }
            get("/{id}") {
                val uid = call.uid(); val id = call.parameters["id"]!!
                val todo = TodoRepository.findById(id, uid) ?: throw AppException(HttpStatusCode.NotFound, "Film tidak ditemukan")
                call.respond(HttpStatusCode.OK, success("Berhasil", TodoResponse(todo)))
            }
            put("/{id}") {
                val uid = call.uid(); val id = call.parameters["id"]!!; val b = call.receive<UpdateTodoRequest>()
                if (b.title.isBlank()) throw AppException(HttpStatusCode.BadRequest, "Judul tidak boleh kosong")
                if (b.urgency !in listOf("low","medium","high")) throw AppException(HttpStatusCode.BadRequest, "Urgency harus: low, medium, atau high")
                if (!TodoRepository.update(id, uid, b.title, b.description, b.isDone, b.urgency)) throw AppException(HttpStatusCode.NotFound, "Film tidak ditemukan")
                call.respond(HttpStatusCode.OK, success("Film diperbarui", TodoResponse(TodoRepository.findById(id, uid)!!)))
            }
            delete("/{id}") {
                val uid = call.uid(); val id = call.parameters["id"]!!
                if (!TodoRepository.delete(id, uid)) throw AppException(HttpStatusCode.NotFound, "Film tidak ditemukan")
                ImageService.deleteImage(call.application.loadAppConfig().upload.dir, "todos", id)
                call.respond(HttpStatusCode.OK, success<Unit>("Film dihapus"))
            }
            put("/{id}/cover") {
                val uid = call.uid(); val id = call.parameters["id"]!!
                TodoRepository.findById(id, uid) ?: throw AppException(HttpStatusCode.NotFound, "Film tidak ditemukan")
                val cfg = call.application.loadAppConfig().upload; var ext: String? = null
                call.receiveMultipart().forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "file") ext = ImageService.saveImage(part, cfg.dir, "todos", id, cfg.maxSizeMb)
                    part.dispose()
                }
                ext ?: throw AppException(HttpStatusCode.BadRequest, "File gambar tidak ditemukan")
                TodoRepository.updateCover(id, uid, ext!!)
                call.respond(HttpStatusCode.OK, success<Unit>("Poster diperbarui"))
            }
        }
    }
    get("/images/todos/{id}") {
        val id = call.parameters["id"]!!
        val file = ImageService.findImage(call.application.loadAppConfig().upload.dir, "todos", id)
            ?: throw AppException(HttpStatusCode.NotFound, "Poster tidak ditemukan")
        call.respondFile(file)
    }
}
private fun ApplicationCall.uid() = principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()