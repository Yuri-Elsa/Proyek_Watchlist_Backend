package org.delcom.watchlist.common.plugins
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.watchlist.common.config.loadAppConfig
fun Application.configureAuthentication() {
    val cfg = loadAppConfig().jwt
    install(Authentication) {
        jwt("auth-jwt") {
            realm = cfg.realm
            verifier(JWT.require(Algorithm.HMAC256(cfg.secret)).withIssuer(cfg.issuer).withAudience(cfg.audience).withClaimPresence("userId").withClaim("type", "auth").build())
            validate { cred -> val uid = cred.payload.getClaim("userId").asString(); if (uid.isNullOrBlank()) null else JWTPrincipal(cred.payload) }
        }
    }
}
fun Application.generateAuthToken(userId: String): String {
    val cfg = loadAppConfig().jwt
    return JWT.create().withIssuer(cfg.issuer).withAudience(cfg.audience).withClaim("userId", userId).withClaim("type", "auth")
        .withExpiresAt(java.util.Date(System.currentTimeMillis() + cfg.expireDays * 86_400_000L)).sign(Algorithm.HMAC256(cfg.secret))
}
fun Application.generateRefreshToken(userId: String): String {
    val cfg = loadAppConfig().jwt
    return JWT.create().withIssuer(cfg.issuer).withAudience(cfg.audience).withClaim("userId", userId).withClaim("type", "refresh")
        .withExpiresAt(java.util.Date(System.currentTimeMillis() + cfg.refreshExpireDays * 86_400_000L)).sign(Algorithm.HMAC256(cfg.secret))
}
fun Application.verifyRefreshToken(token: String): String? = try {
    val cfg = loadAppConfig().jwt
    JWT.require(Algorithm.HMAC256(cfg.secret)).withIssuer(cfg.issuer).withAudience(cfg.audience).withClaim("type", "refresh").build().verify(token).getClaim("userId").asString()
} catch (e: Exception) { null }