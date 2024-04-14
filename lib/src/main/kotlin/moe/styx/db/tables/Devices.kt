package moe.styx.db.tables

import moe.styx.common.data.Device
import moe.styx.common.data.DeviceInfo
import moe.styx.common.data.UnregisteredDevice
import moe.styx.common.extension.toBoolean
import moe.styx.common.json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.json as jsonCol

object DeviceTable : Table("device") {
    val GUID = varchar("GUID", 36)
    val userID = varchar("userID", 36).references(UserTable.GUID)
    val deviceName = text("deviceName")
    val deviceInfo = jsonCol<DeviceInfo>("deviceInfo", json)
    val lastUsed = long("lastUsed").nullable()
    val accessToken = varchar("accessToken", 36).nullable()
    val watchToken = varchar("watchToken", 36).nullable()
    val refreshToken = varchar("refreshToken", 36)
    val tokenExpiry = long("tokenExpiry").nullable()

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: Device) = upsert {
        it[GUID] = item.GUID
        it[userID] = item.userID
        it[deviceName] = item.name
        it[deviceInfo] = item.deviceInfo
        it[lastUsed] = item.lastUsed
        it[accessToken] = item.accessToken
        it[watchToken] = item.watchToken
        it[refreshToken] = item.refreshToken
        it[tokenExpiry] = item.tokenExpiry
    }

    fun query(block: DeviceTable.() -> List<ResultRow> = { selectAll().toList() }): List<Device> {
        return block(this).map {
            Device(
                it[GUID],
                it[userID],
                it[deviceName],
                it[deviceInfo],
                it[lastUsed] ?: 0,
                it[accessToken] ?: "",
                it[watchToken] ?: "",
                it[refreshToken],
                it[tokenExpiry] ?: 0
            )
        }
    }

    fun delete(item: Device): Boolean {
        val deleted = this.deleteWhere { GUID eq item.GUID }.toBoolean()
        if (deleted) {
            DeviceGraveyard.insert {
                it[GUID] = item.GUID
                it[userID] = item.userID
                it[deviceName] = item.name
                it[deviceInfo] = item.deviceInfo
                it[lastUsed] = item.lastUsed
                it[accessToken] = item.accessToken
                it[watchToken] = item.watchToken
                it[refreshToken] = item.refreshToken
                it[tokenExpiry] = item.tokenExpiry
            }
        }
        return deleted
    }
}

object DeviceGraveyard : Table("device_graveyard") {
    val GUID = varchar("GUID", 36)
    val userID = varchar("userID", 36).references(UserTable.GUID)
    val deviceName = text("deviceName")
    val deviceInfo = jsonCol<DeviceInfo>("deviceInfo", json)
    val lastUsed = long("lastUsed").nullable()
    val accessToken = varchar("accessToken", 36).nullable()
    val watchToken = varchar("watchToken", 36).nullable()
    val refreshToken = varchar("refreshToken", 36)
    val tokenExpiry = long("tokenExpiry").nullable()

    override val primaryKey = PrimaryKey(GUID)
}

object DeviceTrafficTable : Table("device_traffic") {
    val deviceID = varchar("deviceID", 36).references(DeviceTable.GUID)
    val year = integer("year")
    val month = integer("month")
    val day = integer("day")
    val bytes = long("bytes")

    override val primaryKey = PrimaryKey(deviceID, year, month, day)
}

object UnregisteredDeviceTable : Table("device_unregistered") {
    val GUID = varchar("GUID", 36)
    val deviceInfo = jsonCol<DeviceInfo>("deviceInfo", json)
    val codeExpiry = long("codeExpiry")
    val code = integer("code")

    override val primaryKey = PrimaryKey(GUID)

    fun upsertItem(item: UnregisteredDevice) = upsert {
        it[GUID] = item.GUID
        it[deviceInfo] = item.deviceInfo
        it[codeExpiry] = item.codeExpiry
        it[code] = item.code
    }

    fun query(block: UnregisteredDeviceTable.() -> List<ResultRow>): List<UnregisteredDevice> {
        return block(this).map {
            UnregisteredDevice(
                it[GUID],
                it[deviceInfo],
                it[codeExpiry],
                it[code]
            )
        }
    }
}