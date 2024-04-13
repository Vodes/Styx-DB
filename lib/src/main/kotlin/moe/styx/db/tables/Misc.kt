package moe.styx.db.tables

import moe.styx.common.data.Changes
import moe.styx.common.data.Log
import moe.styx.common.data.LogType
import moe.styx.common.extension.currentUnixSeconds
import org.jetbrains.exposed.sql.*

object LogTable : Table("Logs") {
    val userID = varchar("userID", 36).references(UserTable.GUID, onDelete = ReferenceOption.CASCADE)
    val deviceID = varchar("deviceID", 36).references(DeviceTable.GUID)
    val type = text("type")
    val content = largeText("content").nullable()
    val time = long("time")

    fun insertItem(item: Log) = insert {
        it[userID] = item.userID
        it[deviceID] = item.deviceID
        it[type] = item.type.name
        it[content] = item.content
        it[time] = item.time
    }

    fun query(block: LogTable.() -> List<ResultRow>): List<Log> {
        return block(this).map {
            Log(
                it[userID],
                it[deviceID],
                LogType.valueOf(it[type]),
                it[content],
                it[time]
            )
        }
    }
}

object ChangesTable : Table("Changes") {
    val id = integer("id").default(0)
    val lastEntryChange = long("lastEntryChange")
    val lastMediaChange = long("lastMediaChange")

    override val primaryKey = PrimaryKey(id)

    fun setToNow(media: Boolean, entry: Boolean) {
        val now = currentUnixSeconds()
        val current = getCurrent()
        val new = (current ?: Changes(0, 0))
            .copy(media = if (media) now else current?.media ?: 0, entry = if (entry) now else current?.entry ?: 0)
        if (current != null) {
            update { it[lastMediaChange] = new.media; it[lastEntryChange] = new.entry }
        } else {
            insert { it[lastMediaChange] = new.media; it[lastEntryChange] = new.entry }
        }
    }

    fun getCurrent(): Changes? {
        val current = selectAll().toList().firstOrNull()
        return current?.let {
            Changes(
                current[lastMediaChange],
                current[lastEntryChange]
            )
        }
    }
}