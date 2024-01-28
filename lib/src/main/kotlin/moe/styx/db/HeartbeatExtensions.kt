package moe.styx.db

import kotlinx.serialization.encodeToString
import moe.styx.types.ActiveUser
import moe.styx.types.json
import moe.styx.types.toBoolean

fun StyxDBClient.save(activeUser: ActiveUser): Boolean {
    val exists = objectExistsTwo("userID", "deviceID", activeUser.user.GUID, activeUser.deviceID, "ActiveUsers")
    val query = if (exists)
        "UPDATE ActiveUsers SET userID=?, deviceID=?, deviceType=?, lastPing=?, mediaActivity=?, listeningTo=? WHERE userID=? AND deviceID=?;"
    else
        "INSERT INTO ActiveUsers (userID, deviceID, deviceType, lastPing, mediaActivity, listeningTo) " +
                "VALUES(?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, activeUser.user.GUID)
        setString(2, activeUser.deviceID)
        setString(3, activeUser.deviceType)
        setLong(4, activeUser.lastPing ?: -1)
        setString(5, activeUser.mediaActivity?.let { json.encodeToString(it) })
        setString(6, activeUser.listeningTo)
        if (exists) {
            setString(7, activeUser.user.GUID)
            setString(8, activeUser.deviceID)
        }
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(activeUser: ActiveUser): Boolean {
    val stat = openStatement("DELETE FROM ActiveUsers WHERE WHERE deviceID=? AND userID=?;") {
        setString(1, activeUser.deviceID)
        setString(2, activeUser.user.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getActiveUsers(conditions: Map<String, Any>? = null): List<ActiveUser> {
    val users = mutableListOf<ActiveUser>()
    openResultSet("SELECT * FROM ActiveUsers;", conditions) {
        while (next()) {
            val resolvedUser = getUsers(mapOf("GUID" to getString("userID"))).firstOrNull() ?: continue
            val resolvedDevice = getDevices(
                mapOf(
                    "GUID" to getString("deviceID"),
                    "userID" to getString("userID")
                )
            ).firstOrNull() ?: continue
            users.add(
                ActiveUser(
                    resolvedUser,
                    resolvedDevice.GUID,
                    resolvedDevice.deviceInfo.type,
                    getLong("lastPing"),
                    getString("mediaActivity")?.let { json.decodeFromString(it) },
                    getString("listeningTo")
                )
            )
        }
    }
    return users.toList()
}