package com.mymomartgallery

import auth.JwtService
import com.fasterxml.jackson.databind.SerializationFeature
import com.mymomartgallery.artlisting.posts
import com.mymomartgallery.artlisting.repository.ArtPostRepositoryImpl
import com.mymomartgallery.auth.MMAGSession
import com.mymomartgallery.auth.hash
import com.mymomartgallery.user.repository.UserRepositoryImpl
import com.mymomartgallery.user.routes.users
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.routing.Route
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import kotlin.collections.set

    fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DatabaseFactory.init()
    val userRepo = UserRepositoryImpl()
    val artPostRepo = ArtPostRepositoryImpl()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }
    val imagekitPrivateKey = System.getenv("IMAGE_KIT_PRIVATE_KEY")


    install(Locations) {
    }

    install(Sessions) {
        cookie<MMAGSession>("MMAGSession") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt") { //1
            verifier(jwtService.verifier) // 2
            realm = "Todo Server"
            validate { // 3
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = userRepo.findUserById(claimString) // 4
                user
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    routing {
        users(userRepo, jwtService, hashFunction)
        posts(artPostRepo , userRepo, imagekitPrivateKey)
    }

}

const val API_VERSION = "/v1"



