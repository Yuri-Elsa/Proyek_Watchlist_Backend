package org.delcom.watchlist.common.config
import io.ktor.server.application.*
data class DatabaseConfig(val url: String, val user: String, val password: String, val driver: String, val maxPool: Int)
data class JwtConfig(val secret: String, val issuer: String, val audience: String, val realm: String, val expireDays: Long, val refreshExpireDays: Long)
data class UploadConfig(val dir: String, val maxSizeMb: Long)
data class AppConfig(val db: DatabaseConfig, val jwt: JwtConfig, val upload: UploadConfig)
fun Application.loadAppConfig() = AppConfig(
    db = DatabaseConfig(
        url = environment.config.property("database.url").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString(),
        driver = environment.config.property("database.driver").getString(),
        maxPool = environment.config.propertyOrNull("database.maxPool")?.getString()?.toInt() ?: 10
    ),
    jwt = JwtConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "WatchList API",
        expireDays = environment.config.propertyOrNull("jwt.expireDays")?.getString()?.toLong() ?: 7L,
        refreshExpireDays = environment.config.propertyOrNull("jwt.refreshExpireDays")?.getString()?.toLong() ?: 30L
    ),
    upload = UploadConfig(
        dir = environment.config.property("upload.dir").getString(),
        maxSizeMb = environment.config.propertyOrNull("upload.maxSizeMb")?.getString()?.toLong() ?: 5L
    )
)