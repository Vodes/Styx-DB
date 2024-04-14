package moe.styx.db.tables

import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaInfo
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.upsert

object MediaEntryTable : Table("media_entries") {
    val GUID = varchar("GUID", 36)
    val mediaID = reference("mediaID", MediaTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")
    val entryNumber = varchar("entryNumber", 4)
    val nameEN = mediumText("nameEN").nullable()
    val nameDE = mediumText("nameDE").nullable()
    val synopsisEN = mediumText("synopsisEN").nullable()
    val synopsisDE = mediumText("synopsisDE").nullable()
    val thumbID = reference("thumbID", ImageTable.GUID, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE).nullable()
    val filePath = mediumText("filePath")
    val fileSize = long("fileSize").nullable()
    val originalName = mediumText("originalName").nullable()

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: MediaEntry) = upsert {
        it[GUID] = item.GUID
        it[mediaID] = item.mediaID
        it[timestamp] = item.timestamp
        it[entryNumber] = item.entryNumber
        it[nameEN] = item.nameEN
        it[nameDE] = item.nameDE
        it[synopsisEN] = item.synopsisEN
        it[synopsisDE] = item.synopsisDE
        it[thumbID] = if (item.thumbID.isNullOrBlank()) null else item.thumbID
        it[filePath] = item.filePath
        it[fileSize] = item.fileSize
        it[originalName] = item.originalName
    }

    fun query(block: MediaEntryTable.() -> List<ResultRow>): List<MediaEntry> {
        return block(this).map {
            MediaEntry(
                it[GUID],
                it[mediaID],
                it[timestamp],
                it[entryNumber],
                it[nameEN],
                it[nameDE],
                it[synopsisEN],
                it[synopsisDE],
                it[thumbID],
                it[filePath],
                it[fileSize] ?: 0,
                it[originalName]
            )
        }
    }
}

object MediaInfoTable : Table("media_info") {
    val entryID = reference("entryID", MediaEntryTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val videoCodec = text("videoCodec")
    val videoBitdepth = integer("videoBitdepth")
    val videoRes = text("videoRes")
    val hasEnglishDub = integer("hasEnglishDub")
    val hasGermanDub = integer("hasGermanDub")
    val hasGermanSub = integer("hasGermanSub")

    override val primaryKey = PrimaryKey(entryID)

    fun upsertItem(item: MediaInfo) = upsert {
        it[entryID] = item.entryID
        it[videoCodec] = item.videoCodec
        it[videoBitdepth] = item.videoBitdepth
        it[videoRes] = item.videoRes
        it[hasEnglishDub] = item.hasEnglishDub
        it[hasGermanDub] = item.hasGermanDub
        it[hasGermanSub] = item.hasGermanSub
    }

    fun query(block: MediaInfoTable.() -> List<ResultRow>): List<MediaInfo> {
        return block(this).map {
            MediaInfo(
                it[entryID],
                it[videoCodec],
                it[videoBitdepth],
                it[videoRes],
                it[hasEnglishDub],
                it[hasGermanDub],
                it[hasGermanSub]
            )
        }
    }
}