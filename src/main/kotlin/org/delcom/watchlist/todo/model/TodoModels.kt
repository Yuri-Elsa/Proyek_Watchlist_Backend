package org.delcom.watchlist.todo.model
import kotlinx.serialization.Serializable
@Serializable data class TodoDto(val id: String, val userId: String, val title: String, val description: String, val isDone: Boolean, val urgency: String, val cover: String?, val createdAt: String, val updatedAt: String)
@Serializable data class TodoResponse(val todo: TodoDto)
@Serializable data class PaginationDto(val currentPage: Int, val perPage: Int, val total: Long, val totalPages: Int, val hasNextPage: Boolean, val hasPrevPage: Boolean)
@Serializable data class TodoListResponse(val todos: List<TodoDto>, val pagination: PaginationDto)
@Serializable data class StatsDto(val total: Long, val done: Long, val pending: Long)
@Serializable data class StatsResponse(val stats: StatsDto)
@Serializable data class CreateTodoRequest(val title: String, val description: String = "", val isDone: Boolean = false, val urgency: String = "medium")
@Serializable data class UpdateTodoRequest(val title: String, val description: String = "", val isDone: Boolean = false, val urgency: String = "medium")
@Serializable data class CreateTodoResponse(val todoId: String)