package moe.styx.db.tables

import moe.styx.common.data.Favourite
import moe.styx.common.data.MediaWatched
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.upsert

object FavouriteTable : Table("Favourites") {
    val mediaID = varchar("mediaID", 36).references(MediaTable.GUID, onDelete = ReferenceOption.CASCADE)
    val userID = varchar("userID", 36).references(UserTable.GUID, onDelete = ReferenceOption.CASCADE)
    val added = long("added")

    override val primaryKey = PrimaryKey(mediaID, userID)

    fun upsertItem(item: Favourite) = upsert {
        it[mediaID] = item.mediaID
        it[userID] = item.userID
        it[added] = item.added
    }

    fun query(block: FavouriteTable.() -> List<ResultRow>): List<Favourite> {
        return block(this).map {
            Favourite(
                it[mediaID],
                it[userID],
                it[added]
            )
        }
    }
}

object MediaWatchedTable : Table("MediaWatched") {
    val entryID = varchar("entryID", 36).references(MediaEntryTable.GUID, onDelete = ReferenceOption.CASCADE)
    val userID = varchar("userID", 36).references(UserTable.GUID, onDelete = ReferenceOption.CASCADE)
    val lastWatched = long("lastWatched")
    val progress = long("progress")
    val progressPercent = float("progressPercent")
    val maxProgress = float("maxProgress")

    fun upsertItem(item: MediaWatched) = upsert {
        it[entryID] = item.entryID
        it[userID] = item.userID
        it[lastWatched] = item.lastWatched
        it[progress] = item.progress
        it[progressPercent] = item.progressPercent
        it[maxProgress] = item.maxProgress
    }

    fun query(block: MediaWatchedTable.() -> List<ResultRow>): List<MediaWatched> {
        return block(this).map {
            MediaWatched(
                it[entryID],
                it[userID],
                it[lastWatched],
                it[progress],
                it[progressPercent],
                it[maxProgress]
            )
        }
    }
}