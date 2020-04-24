package com.mymomartgallery.artlisting.repository

import artlisting.repository.Arts
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Images : Table() {
    val id: Column<String> = Images.varchar("id", 256)
    val artId: Column<Int> = Images.integer("artId").references(Arts.id)
    val name: Column<String> = Images.varchar("name", 256)
    val url: Column<String> = Images.varchar("url", 512)
    val thumnailUrl: Column<String> = Images.varchar("thumbnail", 512)

}