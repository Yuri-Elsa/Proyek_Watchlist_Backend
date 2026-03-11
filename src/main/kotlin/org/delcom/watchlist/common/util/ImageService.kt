package org.delcom.watchlist.common.util
import io.ktor.http.content.*
import java.io.File
object ImageService {
    private val allowed = setOf("jpg", "jpeg", "png", "webp")
    fun saveImage(part: PartData.FileItem, baseDir: String, sub: String, id: String, maxMb: Long = 5L): String {
        val ext = (part.originalFileName ?: "file").substringAfterLast('.', "jpg").lowercase()
        if (ext !in allowed) throw IllegalArgumentException("Format tidak didukung: $ext")
        val bytes = part.streamProvider().readBytes()
        if (bytes.size > maxMb * 1024 * 1024) throw IllegalArgumentException("File terlalu besar. Max ${maxMb}MB.")
        val dir = File("$baseDir/$sub").also { it.mkdirs() }
        allowed.forEach { File(dir, "$id.$it").takeIf { f -> f.exists() }?.delete() }
        File(dir, "$id.$ext").writeBytes(bytes)
        return ext
    }
    fun findImage(baseDir: String, sub: String, id: String): File? =
        allowed.map { File("$baseDir/$sub/$id.$it") }.firstOrNull { it.exists() }
    fun deleteImage(baseDir: String, sub: String, id: String) =
        allowed.forEach { File("$baseDir/$sub/$id.$it").takeIf { f -> f.exists() }?.delete() }
}