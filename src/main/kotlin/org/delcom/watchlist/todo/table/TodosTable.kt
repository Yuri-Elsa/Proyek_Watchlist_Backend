package org.delcom.watchlist.todo.table
import org.delcom.watchlist.user.table.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
object TodosTable : Table("todos") {
    val id = varchar("id", 36); val userId = varchar("user_id", 36).references(UsersTable.id)
    val title = varchar("title", 255); val description = text("description").default("")
    val isDone = bool("is_done").default(false); val urgency = varchar("urgency", 10).default("medium")
    val cover = varchar("cover", 10).nullable()
    val createdAt = datetime("created_at"); val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}