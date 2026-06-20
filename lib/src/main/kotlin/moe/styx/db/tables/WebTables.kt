package moe.styx.db.tables

import moe.styx.common.data.ShowVoting
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

object ShowVotingTable : Table("show_voting") {
    val title = text("title")
    val anilistID = integer("anilistID")
    val votes = integer("votes")
    val hasVeto = bool("hasVeto")
    val serverID = long("serverID")
    val channelID = long("channelID")
    val messageID = long("messageID")

    override val primaryKey = PrimaryKey(anilistID)

    fun upsertItem(item: ShowVoting) = upsert {
        it[title] = item.title
        it[anilistID] = item.anilistID
        it[votes] = item.votes
        it[hasVeto] = item.hasVeto
        it[serverID] = item.serverID
        it[channelID] = item.channelID
        it[messageID] = item.messageID
    }

    fun query(block: ShowVotingTable.() -> List<ResultRow>): List<ShowVoting> {
        return block(this).map {
            ShowVoting(
                it[title],
                it[anilistID],
                it[votes],
                it[hasVeto],
                it[serverID],
                it[channelID],
                it[messageID]
            )
        }
    }
}
