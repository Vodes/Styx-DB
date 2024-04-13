package moe.styx.db.tables

import moe.styx.common.data.ActiveUser
import moe.styx.common.data.MediaActivity
import moe.styx.common.data.User
import moe.styx.common.extension.eqI
import moe.styx.common.json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.json as jsonCol


object ActiveUserTable : Table("ActiveUsers") {
    val userID = varchar("userID", 36).references(UserTable.GUID, onDelete = ReferenceOption.CASCADE)
    val deviceID = varchar("deviceID", 36).uniqueIndex().references(DeviceTable.GUID, onDelete = ReferenceOption.CASCADE)
    val deviceType = text("deviceType")
    val lastPing = long("lastPing").nullable()
    val mediaActivity = jsonCol<MediaActivity>("mediaActivity", json).nullable()
    val listeningTo = varchar("listeningTo", 36).nullable()

    override val primaryKey = PrimaryKey(userID, deviceID)

    fun upsertItem(item: ActiveUser) = upsert {
        it[userID] = item.user.GUID
        it[deviceID] = item.deviceID
        it[deviceType] = item.deviceType
        it[lastPing] = item.lastPing
        it[mediaActivity] = item.mediaActivity
        it[listeningTo] = item.listeningTo
    }

    fun query(block: ActiveUserTable.() -> List<ResultRow>): List<ActiveUser> {
        // TODO: Could use a join here?
        val users = UserTable.query { this.selectAll().toList() }
        return block(this).mapNotNull {
            users.find { user -> user.GUID eqI it[userID] }
                ?.let { user -> ActiveUser(user, it[deviceID], it[deviceType], it[lastPing], it[mediaActivity], it[listeningTo]) }
        }
    }
}

object UserTable : Table("User") {
    val GUID = varchar("GUID", 36)
    val name = mediumText("name")
    val discordID = mediumText("discordID")
    val added = long("added")
    val lastLogin = long("lastLogin")
    val permissions = integer("permissions")

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: User) = upsert {
        it[GUID] = item.GUID
        it[name] = item.name
        it[discordID] = item.discordID
        it[added] = item.added
        it[lastLogin] = item.lastLogin
        it[permissions] = item.permissions
    }

    fun query(block: UserTable.() -> List<ResultRow>) =
        block(this).map { User(it[GUID], it[name], it[discordID], it[added], it[lastLogin], it[permissions]) }
}