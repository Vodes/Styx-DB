# Styx-DB

A library for database interactions.<br>
This does not ship (depend on) a driver as you can choose that yourself.<br>
I currently use a regular mariadb server and the [mysql driver](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j).

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
[This](https://bin.disroot.org/?8fd1a41a039aef2f#DqufRS7CpCdid9dtB2GE6fmNxbb3TiFLQAy2hVXYXWXA) is the table structure I'm currently using.

## Example Usage

##### Doesn't nearly cover everything but most of it should be self-explanatory.

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


    val media = Media(...)

    //Do a bunch of stuff and close after
    getDBClient().executeAndClose {
        delete(media)
        val entries = getEntries(mapOf("mediaID" to media.GUID))
        entries.forEach { delete(it) }
    }
}

```