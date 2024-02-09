package moe.styx.db

import moe.styx.common.extension.toBoolean
import moe.styx.common.extension.toInt
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class StyxDBClient(driver: String, connectionString: String, user: String? = null, pass: String? = null) {
    private val connection: Connection

    init {
        Class.forName(driver)
        connection = if (user.isNullOrBlank() || pass.isNullOrBlank()) {
            DriverManager.getConnection(connectionString)
        } else {
            DriverManager.getConnection(connectionString, user, pass)
        }
    }

    fun openStatement(query: String): PreparedStatement = connection.prepareStatement(query)
    inline fun openStatement(query: String, func: PreparedStatement.() -> Unit): PreparedStatement {
        val statement = openStatement(query)
        func(statement)
        return statement
    }

    /**
     * Creates a PreparedStatement for the query and calls the inlined function on the ResultSet.
     * This automatically closes the statement and the ResultSet after.
     *
     * You can define additional WHERE conditions with a map like this:
     * `mapOf("column" to "value")` where value can be any primitive type or string.
     */
    inline fun openResultSet(query: String, conditions: Map<String, Any>? = null, func: ResultSet.() -> Unit) {
        var finalQuery = query
        val keys = conditions?.keys?.toList() ?: listOf()
        if (conditions != null) {
            if (query.endsWith(";"))
                finalQuery = finalQuery.removeSuffix(";")
            val conds = keys.joinToString(" AND ", postfix = "") { "$it=?" }
            if (finalQuery.contains("ORDER BY", true)) {
                val (before, after) = finalQuery.split("ORDER BY", ignoreCase = true).map { it.trim() }
                finalQuery = "$before WHERE $conds ORDER BY $after;"
            } else
                finalQuery += " WHERE $conds;"
        }
        val stat = openStatement(finalQuery)
        if (conditions != null) {
            keys.forEachIndexed { i, key ->
                when (val value = conditions[key]) {
                    is String -> stat.setString(i + 1, value)
                    is Long -> stat.setLong(i + 1, value)
                    is Int -> stat.setInt(i + 1, value)
                    is Boolean -> stat.setInt(i + 1, value.toInt())
                    is Number -> stat.setDouble(i + 1, value.toDouble())
                    else -> stat.setString(i + 1, value.toString())
                }
            }
        }
        val rs = stat.executeQuery()
        func(rs)
        rs.close()
        stat.close()
    }

    fun closeConnection() = connection.close()

    /**
     * Generic function to find out if an object exists with a single identifier.
     */
    fun objectExists(GUID: String, table: String, identifier: String = "GUID"): Boolean {
        val stat = openStatement("SELECT $identifier FROM $table WHERE $identifier=?;")
        stat.setString(1, GUID)
        val results = stat.executeQuery()
        val exists = results.next()
        stat.close()
        return exists
    }

    /**
     * Generic function to find out if an object exists identified by two columns.
     */
    fun objectExistsTwo(ID: String, ID2: String, content: String, content2: String, table: String): Boolean {
        val stat = openStatement("SELECT * FROM $table WHERE $ID=? AND $ID2=?;")
        stat.setString(1, content)
        stat.setString(2, content2)
        val results = stat.executeQuery()
        val exists = results.next()
        stat.close()
        return exists
    }

    /**
     * Generic Delete function to delete and object with a certain identifier on a certain table.
     * Returns `true` if something was deleted and `false` otherwise.
     */
    fun genericDelete(GUID: String, table: String, identifier: String = "GUID"): Boolean {
        val stat = openStatement("DELETE FROM $table WHERE $identifier=?;")
        stat.setString(1, GUID)
        val i = stat.executeUpdate()
        stat.close()
        return i.toBoolean()
    }

    fun genericCount(table: String, conditions: Map<String, Any>? = null): Int {
        var count = 0
        openResultSet("SELECT COUNT(*) FROM $table;", conditions) {
            if (next())
                count = getInt(1)
        }
        return count
    }

    inline fun execute(func: StyxDBClient.() -> Unit) {
        func()
    }

    inline fun executeAndClose(func: StyxDBClient.() -> Unit) {
        execute(func)
        this.closeConnection()
    }

    inline fun <T> executeGet(close: Boolean = true, func: StyxDBClient.() -> T): T {
        val result = func()
        if (close)
            this.closeConnection()
        return result
    }
}