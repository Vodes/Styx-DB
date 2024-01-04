package moe.styx.db

import kotlinx.serialization.encodeToString
import moe.styx.types.*

// User

fun StyxDBClient.save(user: User, newID: String? = null): Boolean {
    val edit = objectExists(user.GUID, "User")
    val query = if (edit)
        "UPDATE User SET GUID=?, name=?, discordID=?, added=?, lastLogin=?, permissions=? WHERE GUID=?;"
    else
        "INSERT INTO User (GUID, name, discordID, added, lastLogin, permissions) VALUES(?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) user.GUID else newID)
        setString(2, user.name)
        setString(3, user.discordID)
        setLong(4, user.added)
        setLong(5, user.lastLogin)
        setInt(6, user.permissions)
        if (edit)
            setString(7, user.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(user: User): Boolean = genericDelete(user.GUID, "User")

fun StyxDBClient.getUsers(conditions: Map<String, Any>? = null): List<User> {
    val users = mutableListOf<User>()
    openResultSet("SELECT * FROM User;", conditions) {
        while (next()) {
            val user = User(
                getString("GUID"), getString("name"),
                getString("discordID"), getLong("added"),
                getLong("lastLogin"), getInt("permissions")
            )
            users.add(user)
        }
    }
    return users.toList()
}

// Devices

fun StyxDBClient.save(device: Device, newID: String? = null): Boolean {
    val edit = objectExists(device.GUID, "UserDevices")
    val query = if (edit)
        "UPDATE UserDevices SET GUID=?, userID=?, deviceName=?, deviceInfo=?, lastUsed=?, accessToken=?, watchToken=?, refreshToken=?, tokenExpiry=? WHERE GUID=?;"
    else
        "INSERT INTO UserDevices (GUID, userID, deviceName, deviceInfo, lastUsed, accessToken, watchToken, refreshToken, tokenExpiry) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) device.GUID else newID)
        setString(2, device.userID)
        setString(3, device.name)
        setString(4, json.encodeToString<DeviceInfo>(device.deviceInfo))
        setLong(5, device.lastUsed)
        setString(6, device.accessToken)
        setString(7, device.watchToken)
        setString(8, device.refreshToken)
        setLong(9, device.tokenExpiry)
        if (edit)
            setString(10, device.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(device: Device): Boolean = genericDelete(device.GUID, "UserDevices")

fun StyxDBClient.getDevices(conditions: Map<String, Any>? = null): List<Device> {
    val devices = mutableListOf<Device>()
    openResultSet("SELECT * FROM UserDevices;", conditions) {
        while (next()) {
            devices.add(
                Device(
                    getString("GUID"),
                    getString("userID"),
                    getString("deviceName"),
                    json.decodeFromString(getString("deviceInfo")),
                    getLong("lastUsed"),
                    getString("accessToken"),
                    getString("watchToken"),
                    getString("refreshToken"),
                    getLong("tokenExpiry")
                )
            )
        }
    }
    return devices.toList()
}

// Unregistered Devices

fun StyxDBClient.save(device: UnregisteredDevice): Boolean {
    val edit = objectExists(device.GUID, "UnregisteredDevices")
    val query = if (edit)
        "UPDATE UnregisteredDevices SET GUID=?, deviceInfo=?, codeExpiry=?, code=? WHERE GUID=?;"
    else
        "INSERT INTO UnregisteredDevices (GUID, deviceInfo, codeExpiry, code) VALUES(?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, device.GUID)
        setString(2, json.encodeToString<DeviceInfo>(device.deviceInfo))
        setLong(3, device.codeExpiry)
        setInt(4, device.code)
        if (edit)
            setString(5, device.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(device: UnregisteredDevice): Boolean = genericDelete(device.GUID, "UnregisteredDevices")

fun StyxDBClient.getUnregisteredDevices(conditions: Map<String, Any>? = null): List<UnregisteredDevice> {
    val devices = mutableListOf<UnregisteredDevice>()
    openResultSet("SELECT * FROM UnregisteredDevices;", conditions) {
        while (next()) {
            devices.add(
                UnregisteredDevice(
                    getString("GUID"), json.decodeFromString(getString("deviceInfo")),
                    getLong("codeExpiry"), getInt("code")
                )
            )
        }
    }
    return devices.toList()
}