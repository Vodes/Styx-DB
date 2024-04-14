import moe.styx.common.extension.eqI
import moe.styx.db.*
import moe.styx.db.tables.*

fun getDBClient(database: String = "Styx2"): StyxDBClient {
    return StyxDBClient(
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://redacted/$database?" +
                "useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin",
        "redacted",
        "redacted"
    )
}

val dbClient by lazy {
    DBClient("jdbc:postgresql://redacted/Styx", "org.postgresql.Driver", "styxuser", "redacted")
}

fun main() {
    dbClient.createTables()

    val oldDB = getDBClient()

    dbClient.transaction { oldDB.getCategories().forEach { CategoryTable.upsertItem(it) } }
    dbClient.transaction { oldDB.getImages().forEach { ImageTable.upsertItem(it) } }
    dbClient.transaction {
        oldDB.getUsers().forEach { UserTable.upsertItem(it) }
        oldDB.getDevices().forEach { DeviceTable.upsertItem(it) }
        val media = oldDB.getMedia()
        val entries = oldDB.getEntries()
        media.forEach { m ->
            runCatching {
                MediaTable.upsertItem(m)
            }.onFailure {
                it.printStackTrace()
            }
        }

        entries.forEach { MediaEntryTable.upsertItem(it) }
        oldDB.getMediaInfo().forEach {
            if (entries.find { ent -> ent.GUID eqI it.entryID } != null)
                MediaInfoTable.upsertItem(it)
        }
        oldDB.getSchedules().forEach { MediaScheduleTable.upsertItem(it) }
        oldDB.getMediaWatched().forEach {
            if (entries.find { ent -> ent.GUID eqI it.entryID } != null)
                MediaWatchedTable.upsertItem(it)
        }
        oldDB.getFavourites().forEach { FavouriteTable.upsertItem(it) }
    }
    dbClient.transaction {
        oldDB.getTargets().forEach { DownloaderTargetsTable.upsertItem(it) }
        oldDB.getLogs().forEach { LogTable.upsertItem(it) }
    }
}