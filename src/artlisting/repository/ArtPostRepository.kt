package com.mymomartgallery.artlisting.repository

import com.mymomartgallery.artlisting.data.Art
import com.mymomartgallery.artlisting.data.Image
import com.mymomartgallery.artlisting.data.dto.ImageDto

interface ArtPostRepository {
    suspend fun addArtPost(title: String, description: String, price: Double): Art?
    suspend fun addImageArt(fileId: String, artId: Int, name: String, url: String, thumnailUrl: String): Image?
    suspend fun findArtPostById(id: Int): Art?
    suspend fun getArtPosts(page: Int = -1, maxArtPerPage: Int = 20): List<Art>
    suspend fun uploadImage(file: ByteArray, filename: String, imagekitPrivatekey: String): ImageDto?
}