package org.delcom.watchlist.auth.table
import org.delcom.watchlist.user.table.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
object AuthTokensTable : Table("auth_tokens") {
    val id = varchar("id", 36); val userId = varchar("user_id", 36).references(UsersTable.id)
    val authToken = text("auth_token").uniqueIndex(); val refreshToken = text("refresh_token").uniqueIndex()
    val createdAt = datetime("created_at"); val expiresAt = datetime("expires_at")
    override val primaryKey = PrimaryKey(id)
}