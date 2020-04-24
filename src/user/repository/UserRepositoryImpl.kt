package com.mymomartgallery.user.repository

import com.mymomartgallery.DatabaseFactory.dbQuery
import com.mymomartgallery.user.data.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement

class UserRepositoryImpl : UserRepository {
    override suspend fun addUser(email: String, displayName: String, passwordHash: String): User? {
        var statement: InsertStatement<Number>? = null
        dbQuery{
            statement = Users.insert{
                it[Users.email] = email
                it[Users.displayName] = displayName
                it[Users.passwordHash] = passwordHash
            }
        }
        return rowToUser(statement?.resultedValues?.firstOrNull())
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }
        return User(
                    userid = row[Users.userId],
                    email = row[Users.email],
                    displayName = row[Users.displayName],
                    passwordHash = row[Users.passwordHash])
        }


    override suspend fun findUserById(userId: Int): User? = dbQuery {
        Users.select{Users.userId.eq(userId)}.map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String): User? = dbQuery {
        Users.select{Users.email.eq(email)}.map { rowToUser(it) }.singleOrNull()
    }

}