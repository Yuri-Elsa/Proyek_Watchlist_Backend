package org.delcom.watchlist.todo.repository
import org.delcom.watchlist.common.util.newId
import org.delcom.watchlist.todo.model.*
import org.delcom.watchlist.todo.table.TodosTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object TodoRepository {
    private fun ResultRow.toDto() = TodoDto(this[TodosTable.id], this[TodosTable.userId], this[TodosTable.title], this[TodosTable.description], this[TodosTable.isDone], this[TodosTable.urgency], this[TodosTable.cover], this[TodosTable.createdAt].toString(), this[TodosTable.updatedAt].toString())

    fun create(userId: String, title: String, desc: String, isDone: Boolean, urgency: String): String = transaction {
        val id = newId(); val now = LocalDateTime.now()
        TodosTable.insert { it[TodosTable.id]=id; it[TodosTable.userId]=userId; it[TodosTable.title]=title; it[description]=desc; it[TodosTable.isDone]=isDone; it[TodosTable.urgency]=urgency; it[createdAt]=now; it[updatedAt]=now }
        id
    }

    fun findAll(userId: String, search: String?=null, page: Int=1, perPage: Int=10, isDone: Boolean?=null, urgency: String?=null): Pair<List<TodoDto>, PaginationDto> = transaction {
        val q = TodosTable.selectAll().where { TodosTable.userId eq userId }
        search?.takeIf { it.isNotBlank() }?.let { s ->
            q.andWhere { TodosTable.title.lowerCase() like "%${s.lowercase()}%" }
        }
        isDone?.let { d -> q.andWhere { TodosTable.isDone eq d } }
        urgency?.takeIf { it.isNotBlank() }?.let { u -> q.andWhere { TodosTable.urgency eq u } }

        val total = q.count()
        val totalPages = maxOf(1, ((total + perPage - 1) / perPage).toInt())
        val safePage = page.coerceIn(1, totalPages)
        val offset = ((safePage - 1) * perPage).toLong()

        val items = q
            .orderBy(TodosTable.updatedAt, SortOrder.DESC)
            .limit(perPage, offset)
            .map { it.toDto() }

        val hasNext = safePage < totalPages
        val hasPrev = safePage > 1
        items to PaginationDto(safePage, perPage, total, totalPages, hasNext, hasPrev)
    }

    fun findById(id: String, userId: String): TodoDto? = transaction {
        TodosTable.selectAll().where { (TodosTable.id eq id) and (TodosTable.userId eq userId) }.singleOrNull()?.toDto()
    }

    fun update(id: String, userId: String, title: String, desc: String, isDone: Boolean, urgency: String): Boolean = transaction {
        TodosTable.update({ (TodosTable.id eq id) and (TodosTable.userId eq userId) }) { it[TodosTable.title]=title; it[description]=desc; it[TodosTable.isDone]=isDone; it[TodosTable.urgency]=urgency; it[updatedAt]=LocalDateTime.now() } > 0
    }

    fun updateCover(id: String, userId: String, ext: String): Boolean = transaction {
        TodosTable.update({ (TodosTable.id eq id) and (TodosTable.userId eq userId) }) { it[cover]=ext; it[updatedAt]=LocalDateTime.now() } > 0
    }

    fun delete(id: String, userId: String): Boolean = transaction {
        TodosTable.deleteWhere { (TodosTable.id eq id) and (TodosTable.userId eq userId) } > 0
    }

    fun getStats(userId: String): StatsDto = transaction {
        val total = TodosTable.selectAll().where { TodosTable.userId eq userId }.count()
        val done = TodosTable.selectAll().where { (TodosTable.userId eq userId) and (TodosTable.isDone eq true) }.count()
        StatsDto(total, done, total - done)
    }
}