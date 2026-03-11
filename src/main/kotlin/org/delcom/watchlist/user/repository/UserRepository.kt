package org.delcom.watchlist.user.repository
import at.favre.lib.crypto.bcrypt.BCrypt
import org.delcom.watchlist.user.model.UserDto
import org.delcom.watchlist.user.table.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
object UserRepository {
    private fun ResultRow.toDto() = UserDto(this[UsersTable.id], this[UsersTable.name], this[UsersTable.username], this[UsersTable.about], this[UsersTable.createdAt].toString(), this[UsersTable.updatedAt].toString())
    fun findById(id: String): UserDto? = transaction { UsersTable.select { UsersTable.id eq id }.singleOrNull()?.toDto() }
    fun usernameExistsExcept(username: String, excludeId: String) = transaction { UsersTable.select { (UsersTable.username eq username) and (UsersTable.id neq excludeId) }.count() > 0 }
    fun updateProfile(id: String, name: String, username: String) = transaction { UsersTable.update({ UsersTable.id eq id }) { it[UsersTable.name]=name; it[UsersTable.username]=username; it[updatedAt]=LocalDateTime.now() } }
    fun getPassword(id: String): String? = transaction { UsersTable.select { UsersTable.id eq id }.singleOrNull()?.get(UsersTable.password) }
    fun verifyPassword(plain: String, hashed: String) = BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified
    fun updatePassword(id: String, newPwd: String) = transaction { val h = BCrypt.withDefaults().hashToString(12, newPwd.toCharArray()); UsersTable.update({ UsersTable.id eq id }) { it[password]=h; it[updatedAt]=LocalDateTime.now() } }
    fun updateAbout(id: String, about: String) = transaction { UsersTable.update({ UsersTable.id eq id }) { it[UsersTable.about]=about; it[updatedAt]=LocalDateTime.now() } }
    fun updatePhoto(id: String, ext: String) = transaction { UsersTable.update({ UsersTable.id eq id }) { it[photo]=ext; it[updatedAt]=LocalDateTime.now() } }
}