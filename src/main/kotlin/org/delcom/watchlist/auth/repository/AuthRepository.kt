package org.delcom.watchlist.auth.repository
import at.favre.lib.crypto.bcrypt.BCrypt
import org.delcom.watchlist.auth.table.AuthTokensTable
import org.delcom.watchlist.common.util.newId
import org.delcom.watchlist.user.table.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
object AuthRepository {
    data class UserCreds(val id: String, val hashedPassword: String)
    fun usernameExists(username: String): Boolean = transaction { UsersTable.select { UsersTable.username eq username }.count() > 0 }
    fun createUser(name: String, username: String, password: String): String = transaction {
        val id = newId(); val now = LocalDateTime.now()
        val hashed = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        UsersTable.insert { it[UsersTable.id]=id; it[UsersTable.name]=name; it[UsersTable.username]=username; it[UsersTable.password]=hashed; it[createdAt]=now; it[updatedAt]=now }
        id
    }
    fun findByUsername(username: String): UserCreds? = transaction {
        UsersTable.select { UsersTable.username eq username }.singleOrNull()?.let { UserCreds(it[UsersTable.id], it[UsersTable.password]) }
    }
    fun verifyPassword(plain: String, hashed: String) = BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified
    fun saveTokens(userId: String, auth: String, refresh: String) = transaction {
        val now = LocalDateTime.now()
        AuthTokensTable.insert { it[AuthTokensTable.id]=newId(); it[AuthTokensTable.userId]=userId; it[authToken]=auth; it[refreshToken]=refresh; it[createdAt]=now; it[expiresAt]=now.plusDays(30) }
    }
    fun deleteByAuthToken(token: String) = transaction { AuthTokensTable.deleteWhere { authToken eq token } }
    fun findUserIdByRefreshToken(token: String): String? = transaction { AuthTokensTable.select { AuthTokensTable.refreshToken eq token }.singleOrNull()?.get(AuthTokensTable.userId) }
    fun rotateTokens(oldRefresh: String, newAuth: String, newRefresh: String) = transaction {
        val uid = AuthTokensTable.select { AuthTokensTable.refreshToken eq oldRefresh }.singleOrNull()?.get(AuthTokensTable.userId) ?: return@transaction
        AuthTokensTable.deleteWhere { refreshToken eq oldRefresh }
        saveTokens(uid, newAuth, newRefresh)
    }
}