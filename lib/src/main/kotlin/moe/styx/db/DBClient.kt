package moe.styx.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import moe.styx.db.tables.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager

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

    fun <T> transaction(
        transactionIsolation: Int? = databaseConnection.transactionManager.defaultIsolationLevel,
        readOnly: Boolean? = databaseConnection.transactionManager.defaultReadOnly,
        block: JdbcTransaction.() -> T
    ): T = transaction(databaseConnection, transactionIsolation, readOnly) {
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
            DownloaderTargetsTable,
            APIStateTable,
            ProxyServerTable,
            UserMediaPreferencesTable,
            WebTempLinkTable,
            WebLoginTable
        )
    }
}