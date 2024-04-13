package moe.styx.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class DBClient(private val connectionString: String, private val driver: String, private val user: String, private val pass: String) {
    val databaseConnection: Database

    init {
        val config = HikariConfig().apply {
            jdbcUrl = connectionString
            driverClassName = driver
            username = user
            password = pass
        }
        databaseConnection = Database.connect(HikariDataSource(config))
    }

    suspend fun <T> asyncQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

    fun <T> query(block: () -> T): T = transaction {
        block()
    }
}