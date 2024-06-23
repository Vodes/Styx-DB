package moe.styx.db.tables

import kotlinx.serialization.encodeToString
import moe.styx.common.data.*
import moe.styx.common.json
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.json.json as jsonCol

object MediaTable : Table("media") {
    val GUID = varchar("GUID", 36)
    val name = mediumText("name")
    val nameEN = mediumText("nameEN").nullable()
    val nameJP = mediumText("nameJP").nullable()
    val synopsisEN = largeText("synopsisEN").nullable()
    val synopsisDE = largeText("synopsisDE").nullable()
    val thumbID = reference("thumbID", ImageTable.GUID, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE).nullable()
    val bannerID = reference("bannerID", ImageTable.GUID, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE).nullable()
    val categoryID = reference("categoryID", CategoryTable.GUID, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE).nullable()
    val prequel = varchar("prequel", 36).nullable()
    val sequel = varchar("sequel", 36).nullable()
    val genres = mediumText("genres").nullable()
    val tags = mediumText("tags").nullable()
    val metadataMap = jsonCol<MappingCollection>("metadataMap", json).nullable()
    val isSeries = integer("isSeries")
    val added = long("added")

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: Media) = upsert {
        it[GUID] = item.GUID
        it[name] = item.name
        it[nameEN] = item.nameEN
        it[nameJP] = item.nameJP
        it[synopsisEN] = item.synopsisEN
        it[synopsisDE] = item.synopsisDE
        it[thumbID] = if (item.thumbID.isNullOrBlank()) null else item.thumbID
        it[bannerID] = if (item.bannerID.isNullOrBlank()) null else item.bannerID
        it[categoryID] = if (item.categoryID.isNullOrBlank()) null else item.categoryID
        it[prequel] = item.prequel
        it[sequel] = item.sequel
        it[genres] = item.genres
        it[tags] = item.tags
        it[metadataMap] = item.metadataMap?.let { kotlin.runCatching { json.decodeFromString<MappingCollection>(it) }.getOrNull() }
        it[isSeries] = item.isSeries
        it[added] = item.added
    }

    fun query(block: MediaTable.() -> List<ResultRow>): List<Media> {
        return block(this).map {
            Media(
                it[GUID],
                it[name],
                it[nameJP],
                it[nameEN],
                it[synopsisEN],
                it[synopsisDE],
                it[thumbID],
                it[bannerID],
                it[categoryID],
                it[prequel],
                it[sequel],
                it[genres],
                it[tags],
                it[metadataMap]?.let { runCatching { json.encodeToString(it) }.getOrNull() },
                it[isSeries],
                it[added]
            )
        }
    }
}

object ImageTable : Table("images") {
    val GUID = varchar("GUID", 36)
    val hasWEBP = integer("hasWEBP").nullable()
    val hasJPG = integer("hasJPG").nullable()
    val hasPNG = integer("hasPNG").nullable()
    val externalURL = mediumText("externalURL").nullable()
    val type = integer("type")

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: Image) = upsert {
        it[GUID] = item.GUID
        it[hasWEBP] = item.hasWEBP
        it[hasJPG] = item.hasJPG
        it[hasPNG] = item.hasPNG
        it[externalURL] = item.externalURL
        it[type] = item.type
    }

    fun query(block: ImageTable.() -> List<ResultRow>): List<Image> {
        return block(this).map {
            Image(
                it[GUID],
                it[hasWEBP],
                it[hasJPG],
                it[hasPNG],
                it[externalURL],
                it[type]
            )
        }
    }
}

object MediaScheduleTable : Table("media_schedule") {
    val mediaID = reference("mediaID", MediaTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val day = text("day")
    val hour = integer("hour")
    val minute = integer("minute")
    val isEstimated = integer("isEstimated")
    val finalEpisodeCount = integer("finalEpisodeCount").nullable()

    override val primaryKey = PrimaryKey(mediaID)

    fun upsertItem(item: MediaSchedule) = upsert {
        it[mediaID] = item.mediaID
        it[day] = item.day.name
        it[hour] = item.hour
        it[minute] = item.minute
        it[isEstimated] = item.isEstimated
        it[finalEpisodeCount] = item.finalEpisodeCount
    }

    fun query(block: MediaScheduleTable.() -> List<ResultRow>): List<MediaSchedule> {
        return block(this).map {
            MediaSchedule(
                it[mediaID],
                ScheduleWeekday.valueOf(it[day]),
                it[hour],
                it[minute],
                it[isEstimated],
                it[finalEpisodeCount] ?: 0
            )
        }
    }
}

object CategoryTable : Table("media_category") {
    val GUID = varchar("GUID", 36)
    val sort = integer("sort")
    val isSeries = integer("isSeries")
    val isVisible = integer("isVisible")
    val name = mediumText("name")

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: Category) = upsert {
        it[GUID] = item.GUID
        it[sort] = item.sort
        it[isSeries] = item.isSeries
        it[isVisible] = item.isVisible
        it[name] = item.name
    }

    fun query(block: CategoryTable.() -> List<ResultRow>): List<Category> {
        return block(this).map {
            Category(
                it[GUID],
                it[sort],
                it[isSeries],
                it[isVisible],
                it[name]
            )
        }
    }
}