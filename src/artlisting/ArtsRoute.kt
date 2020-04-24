package com.mymomartgallery.artlisting


import com.mymomartgallery.API_VERSION
import com.mymomartgallery.artlisting.data.Image
import com.mymomartgallery.artlisting.data.dto.ImageDto
import com.mymomartgallery.artlisting.repository.ArtPostRepository
import com.mymomartgallery.auth.MMAGSession
import com.mymomartgallery.user.repository.UserRepository
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.*
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import java.io.File

const val ART_POST = "$API_VERSION/post"
const val ART_POSTS = "$API_VERSION/posts/[id]"

@KtorExperimentalLocationsAPI
@Location(ART_POSTS)
class PostsRoute

@KtorExperimentalLocationsAPI
@Location(ART_POST)
class PostRoute

@KtorExperimentalLocationsAPI
fun Route.posts(
    artPostRepo: ArtPostRepository,
    userRepo: UserRepository,
    imagekitPrivatekey: String
) {
    authenticate("jwt") {
        // 1
        post<PostRoute> {
            val user = call.sessions.get<MMAGSession>()?.let {
                userRepo.findUserById(it.userid)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "User not found"
                )
                return@post
            }
            // 2
            val post = call.receiveMultipart()
            var title: String? = null
            var desc: String? = null
            var price: String? = null
            var response: ImageDto? = null
            post.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when {
                            part.name == "title" -> title = part.value
                            part.name == "description" -> desc = part.value
                            part.name == "price" -> price = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        val ext = File(part.originalFileName).extension
                        val uniqueFilename =
                            "upload-${System.currentTimeMillis()}-${user.userid.hashCode()}-${title.hashCode()}.$ext"
                        part.streamProvider().use { input ->
                            val file = input.readAllBytes()
                            response = artPostRepo.uploadImage(file, uniqueFilename,imagekitPrivatekey)
                        }

                    }
                }

            }

            val artTitle = title ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing parameter title")
            val artDesc = desc ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing parameter description")
            val artPrice = price ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing parameter price")
            val artImage = response ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing parameter image")

            try {
                val newPost = artPostRepo.addArtPost(artTitle, artDesc, artPrice.toDouble())
                var newImage: Image? = null
                newPost?.let {
                    newImage = artPostRepo.addImageArt(artImage.fileId , newPost.id, artImage.name, artImage.url, artImage.thumbnailUrl)
                }
                newImage?.let {
                    call.respondText(
                        "Post successfully created",
                        status = HttpStatusCode.Created
                    )
                }

            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving post")
            }
            // 3
        }

        get<PostsRoute> {
            val postId = call.parameters["id"]
            try {
                if (postId != null && postId.isNotEmpty()) {
                    val post = artPostRepo.findArtPostById(postId.toInt()) ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        "Post not found"
                    )
                    call.respond(post)
                } else {
                    val posts = artPostRepo.getArtPosts()
                    call.respond(posts)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to get posts", e)
                call.respond(HttpStatusCode.GatewayTimeout, "Problems getting posts")
            }
        }
    }
}



