package com.mymomartgallery.user.routes

import auth.JwtService
import com.mymomartgallery.API_VERSION
import com.mymomartgallery.auth.MMAGSession
import com.mymomartgallery.user.repository.UserRepository
import com.mymomartgallery.user.repository.UserRepositoryImpl
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set

const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute


@KtorExperimentalLocationsAPI
// 1
fun Route.users(
    userRepo:  UserRepository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    post<UserCreateRoute> { // 2
        val signupParameters = call.receive<Parameters>() // 3
        val password = signupParameters["password"] // 4
            ?: return@post call.respond(
                HttpStatusCode.BadRequest, "Missing Fields")
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(
                HttpStatusCode.BadRequest, "Missing Fields")
        val email = signupParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.BadRequest, "Missing Fields")
        val hash = hashFunction(password) // 5
        try {
            val newUser = userRepo.addUser(email, displayName, hash) // 6
            newUser?.userid?.let {
                call.sessions.set(MMAGSession(it))
                call.respondText(
                    jwtService.generateToken(newUser),
                    status = HttpStatusCode.Created
                )
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }

    post<UserLoginRoute>{
        val credentials = call.receive<Parameters>()
        val user = credentials["email"]?:return@post call.respond(
            HttpStatusCode.BadRequest, "Missing Fields")
        val password = credentials["password"]?:return@post call.respond(
            HttpStatusCode.BadRequest, "Missing Fields")
        val hash = hashFunction(password)

        try {
            val currentUser = userRepo.findUserByEmail(user) // 2
            currentUser?.userid?.let {
                if (currentUser.passwordHash == hash) { // 3
                    call.sessions.set(MMAGSession(it)) // 4
                    call.respondText(jwtService.generateToken(currentUser)) // 5
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Wrong username / password") // 6
                }
            }
        } catch (e: Throwable) {
            application.log.error("Wrong email/password", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}