package moe.styx.db.tables

import moe.styx.common.data.WebLogin
import moe.styx.common.data.WebTempLink
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.upsert

object WebTempLinkTable : Table("web_temp_links") {
    val urlSegment = text("urlSegment").uniqueIndex()
    val createdAt = long("createdAt")
    val expiresAt = long("expiresAt")
    val userID = reference("userID", UserTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val file = text("file")

    override val primaryKey = PrimaryKey(urlSegment)

    fun upsertItem(item: WebTempLink) = upsert {
        it[urlSegment] = item.urlSegment
        it[createdAt] = item.createdAt
        it[expiresAt] = item.expiresAt
        it[userID] = item.userID
        it[file] = item.file
    }

    fun query(block: WebTempLinkTable.() -> List<ResultRow>): List<WebTempLink> {
        return block(this).map {
            WebTempLink(
                it[urlSegment],
                it[createdAt],
                it[expiresAt],
                it[userID],
                it[file]
            )
        }
    }
}

object WebLoginTable : Table("web_login") {
    val userID = reference("userID", UserTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val createdAt = long("createdAt")
    val expiresAt = long("expiresAt")
    val token = varchar("token", 36).uniqueIndex()

    override val primaryKey = PrimaryKey(userID, token)

    fun upsertItem(item: WebLogin) = upsert {
        it[userID] = item.userID
        it[token] = item.token
        it[createdAt] = item.createdAt
        it[expiresAt] = item.expiresAt
    }

    fun query(block: WebLoginTable.() -> List<ResultRow>): List<WebLogin> {
        return block(this).map {
            WebLogin(
                it[userID],
                it[createdAt],
                it[expiresAt],
                it[token]
            )
        }
    }
}
