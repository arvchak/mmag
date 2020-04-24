package com.mymomartgallery.user.repository

import com.mymomartgallery.user.data.User

interface UserRepository {
    suspend fun addUser(email:String, displayName: String, passwordHash: String): User?
    suspend fun findUserById(userId: Int): User?
    suspend fun findUserByEmail(email: String): User?
}