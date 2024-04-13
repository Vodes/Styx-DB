import moe.styx.db.DBClient
import org.jetbrains.exposed.sql.SchemaUtils

val dbClient by lazy {
    DBClient("", "", "", "")
}

fun main() {
    //ActiveUserTable.selectAll().tol
    dbClient.query {
        SchemaUtils.createMissingTablesAndColumns()
//        SchemaUtils.create
    }
}