package moe.styx.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import moe.styx.db.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class DBClient(
    private val connectionString: String,
    private val driver: String,
    private val user: String,
    private val pass: String,
    private val maxConnections: Int = 15
) {
    val databaseConnection: Database

    init {
        val config = HikariConfig().apply {
            jdbcUrl = connectionString
            driverClassName = driver
            username = user
            password = pass
            maximumPoolSize = maxConnections
        }
        databaseConnection = Database.connect(HikariDataSource(config))
        transaction { createTables() }
    }

    suspend fun <T> asyncTransaction(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, databaseConnection) {
        block()
    }

    fun <T> transaction(block: () -> T): T = transaction(databaseConnection) {
        block()
    }

    fun createTables() {
        SchemaUtils.createMissingTablesAndColumns(
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
            DownloaderTargetsTable
        )
    }
}