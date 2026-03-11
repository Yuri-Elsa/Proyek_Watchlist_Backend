package org.delcom.watchlist.user.table
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
object UsersTable : Table("users") {
    val id = varchar("id", 36); val name = varchar("name", 100)
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 255); val about = text("about").nullable()
    val photo = varchar("photo", 10).nullable()
    val createdAt = datetime("created_at"); val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}