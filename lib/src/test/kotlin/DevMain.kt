import moe.styx.common.util.launchGlobal
import moe.styx.db.DBClient
import moe.styx.db.tables.*
import org.jetbrains.exposed.sql.SchemaUtils

val dbClient by lazy {
    DBClient("jdbc:postgresql://192.168.0.224:1337/Styx", "org.postgresql.Driver", "styxuser", "testpass")
}

fun main() {
    //ActiveUserTable.selectAll().tol
    dbClient.query {
        SchemaUtils.create(
            DeviceGraveyard,
            CategoryTable,
            ImageTable,
            UserTable,
            DeviceTable,
            ActiveUserTable,
            MediaTable,
            MediaEntryTable,
            MediaWatchedTable,
            FavouriteTable,
            MediaInfoTable,
            MediaScheduleTable,
            UnregisteredDeviceTable,
            DeviceTrafficTable,
            LogTable,
            ChangesTable,
        )
    }

    launchGlobal {
//        val media =
//            dbClient.asyncQuery {
//
//            }
    }
}