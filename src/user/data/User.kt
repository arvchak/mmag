package com.mymomartgallery.user.data

import io.ktor.auth.Principal
import java.io.Serializable

data class User(val userid: Int, val email: String, val displayName: String, val passwordHash: String) : Serializable,
    Principal