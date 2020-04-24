package artlisting.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Arts: Table() {
    val id : Column<Int> = integer("id").autoIncrement().primaryKey()
    val title = varchar("title" , 256)
    val description = varchar("desc", 512)
    val price = double("price")
    val last_modified = long("last_modified")
    val created = long("created")
}

