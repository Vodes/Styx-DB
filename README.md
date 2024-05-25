# Styx-DB

A library for database interactions.<br>
This does not ship (depend on) a driver as you can choose that yourself.<br>
I currently use the [postgres docker](https://hub.docker.com/_/postgres) with the respective [driver](https://mvnrepository.com/artifact/org.postgresql/postgresql).

## Installation

- Add these [maven repos](https://repo.styx.moe/#/)

    ```kts
    repositories {
        maven { url = uri("https://repo.styx.moe/releases") }
        maven { url = uri("https://repo.styx.moe/snapshots") }
    }
    ```

- Grab the latest artifact [from here](https://repo.styx.moe/#/releases/moe/styx/styx-db).

As for the database itself, there are currently no fancy migrations or whatever.<br>
~~[This](https://bin.disroot.org/?8fd1a41a039aef2f#DqufRS7CpCdid9dtB2GE6fmNxbb3TiFLQAy2hVXYXWXA) is the table structure I'm currently using.~~<br>
As of 0.1.0: This will create missing tables/columns when the client first connects.

## Example Usage

##### Doesn't nearly cover everything but most of it should be self-explanatory.

### 0.1.0+
This is now much closer to writing actual SQL and you can simply reference the [exposed documentation](https://jetbrains.github.io/Exposed/home.html) for more details.
```kt
// Not needed or even a good idea anymore to create many clients.
// We now use HikariCP to have a connection pool that we don't have to worry about.
val dbClient by lazy {
    DBClient("jdbc:postgresql://127.0.0.1/Styx", "org.postgresql.Driver", "user", "pass")
}

fun main() {    
    // All table actions have to be wrapped in a transaction.
    // Every table has this query function to convert the rows to the styx dataclasses.
    val users = dbClient.transaction { UserTable.query { selectAll().toList() } }
  
    dbClient.transaction {
        // The filtering now works with type-checked where expressions
        val filtered = UserTable.query { selectAll().where { discordID eq "11111111111111" }.toList() }
        
        val media = Media("...")
        // For more advanced conditions check https://jetbrains.github.io/Exposed/deep-dive-into-dsl.html#where-expression
        val wasDeleted = MediaTable.deleteWhere { GUID eq media.GUID }.toBoolean()
        if (wasDeleted)
            MediaEntryTable.deleteWhere { mediaID eq media.GUID }
      
        val newMedia = Media("...")
        // Convenience function to insert or update (based on GUID) using a styx dataclass
        MediaTable.upsertItem(newMedia)
    }
}
```

### Pre 0.1.0 (pre Exposed migration & old database model)
```kt
import moe.styx.common.data.Media

fun getDBClient(database: String = "Styx2"): StyxDBClient {
    return StyxDBClient(
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://host/$database?" +
                "useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin",
        "user",
        "pass"
    )
}

fun main() {
    // To get the result of a single function and close it immediately after
    val user = getDBClient().executeGet { getUsers() }.find { it.permissions >= minPerms }

    // You can also use a map of conditions that will get inserted into the query
    val conditions = mapOf("discordID" to discordUser.id)
    val user = getDBClient().executeGet { getUsers(conditions) }


    val media = Media("...")

    //Do a bunch of stuff and close after
    getDBClient().executeAndClose {
        delete(media)
        val entries = getEntries(mapOf("mediaID" to media.GUID))
        entries.forEach { delete(it) }
    }
}

```