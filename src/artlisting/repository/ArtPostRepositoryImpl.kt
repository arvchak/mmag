package com.mymomartgallery.artlisting.repository

import artlisting.repository.Arts
import com.google.gson.Gson
import com.mymomartgallery.DatabaseFactory.dbQuery
import com.mymomartgallery.artlisting.data.Art
import com.mymomartgallery.artlisting.data.Image
import com.mymomartgallery.artlisting.data.dto.ImageDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.util.*


class ArtPostRepositoryImpl : ArtPostRepository {
    override suspend fun addArtPost(title: String, description: String, price: Double): Art? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Arts.insert { art ->
                art[Arts.title] = title
                art[Arts.description] = description
                art[Arts.price] = price
            }
        }
        return rowToPost(statement?.resultedValues?.firstOrNull())
    }

    override suspend fun addImageArt(
        fileId: String,
        artId: Int,
        name: String,
        url: String,
        thumnailUrl: String
    ): Image? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Images.insert { image ->
                image[Images.artId] = artId
                image[id] = fileId
                image[Images.name] = name
                image[Images.url] = url
                image[Images.thumnailUrl] = thumnailUrl
            }
        }
        return rowToImage(statement?.resultedValues?.firstOrNull())
    }

    private fun rowToPost(row: ResultRow?): Art? {
        if (row == null) return null
        return Art(
            id = row[Arts.id],
            title = row[Arts.title],
            description = row[Arts.description],
            price = row[Arts.price]
        )
    }

    private fun rowToImage(row: ResultRow?): Image? {
        if (row == null) return null
        return Image(
            fileId = row[Images.id],
            artId = row[Images.artId],
            name = row[Images.name],
            url = row[Images.url],
            thumbnailUrl = row[Images.thumnailUrl]
        )
    }


    private fun rowToPostNonNull(row: ResultRow): Art {
        return Art(
            id = row[Arts.id],
            title = row[Arts.title],
            description = row[Arts.description],
            price = row[Arts.price]
        )
    }

    override suspend fun findArtPostById(id: Int): Art? = dbQuery {
        Arts.select { Arts.id.eq(id) }.map { rowToPost(it) }.singleOrNull()
    }

    override suspend fun getArtPosts(offset: Int, maxArtPerPage: Int): List<Art> {
        if (offset == 0 && maxArtPerPage == 0) {
            return dbQuery {
                Arts.selectAll().map { rowToPostNonNull(it) }
            }
        }
        return dbQuery {
            Arts.selectAll().limit(maxArtPerPage, offset).map { rowToPostNonNull(it) }
        }

    }

    @InternalAPI
    override suspend fun uploadImage(file: ByteArray, filename: String, imagekitPrivatekey: String): ImageDto? {
        val client = HttpClient(CIO)
        val fileBinary = Base64.getMimeEncoder().encode(file)
        val response: String? = client.post("https://upload.imagekit.io/api/v1/files/upload") {
            header(
                "Authorization",
                "Basic " + imagekitPrivatekey.encodeBase64()
            )
            body = MultiPartFormDataContent(
                formData {
                    append(FormPart("file", String(fileBinary)))
                    append(FormPart("fileName", filename))
                }
            )
        }
        response?.let {
            val gson = Gson()
            return gson.fromJson(it, ImageDto::class.java)
        }
        return null
    }
}