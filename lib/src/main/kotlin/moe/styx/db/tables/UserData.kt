package moe.styx.db.tables

import moe.styx.common.data.Favourite
import moe.styx.common.data.MediaPreferences
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.UserMediaPreferences
import moe.styx.common.json
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.json.json as jsonCol

object FavouriteTable : Table("user_favourite") {
    val mediaID = reference("mediaID", MediaTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val userID = reference("userID", UserTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
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

object MediaWatchedTable : Table("user_media_watched") {
    val entryID = reference("entryID", MediaEntryTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val userID = reference("userID", UserTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val lastWatched = long("lastWatched")
    val progress = long("progress")
    val progressPercent = float("progressPercent")
    val maxProgress = float("maxProgress")

    override val primaryKey = PrimaryKey(entryID, userID)

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

object UserMediaPreferencesTable : Table("user_media_preferences") {
    val mediaID = reference("mediaID", MediaTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val userID = reference("userID", UserTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val mediaPreference = jsonCol<MediaPreferences>("mediaPreferences", json)

    override val primaryKey = PrimaryKey(mediaID, userID)

    fun upsertItem(item: UserMediaPreferences) = upsert {
        it[mediaID] = item.mediaID
        it[userID] = item.userID
        it[mediaPreference] = item.mediaPreferences
    }

    fun query(block: UserMediaPreferencesTable.() -> List<ResultRow>): List<UserMediaPreferences> {
        return block(this).map {
            UserMediaPreferences(
                it[userID],
                it[mediaID],
                it[mediaPreference]
            )
        }
    }
}