package moe.styx.db

import moe.styx.common.data.Log
import moe.styx.common.data.LogType
import moe.styx.common.extension.toBoolean

fun StyxDBClient.save(log: Log): Boolean {
    val edit = objectExistsTwo("userID", "time", log.userID, log.time.toString(), "Logs")

    val query = if (edit)
        "UPDATE Logs SET userID=?, deviceID=?, type=?, content=?, time=? WHERE userID=? AND time=?;"
    else
        "INSERT INTO Logs (userID, deviceID, type, content, time) VALUES(?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, log.userID)
        setString(2, log.deviceID)
        setString(3, log.type.toString())
        setString(4, log.content)
        setLong(5, log.time)
        if (edit) {
            setString(6, log.userID)
            setLong(7, log.time)
        }
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(log: Log): Boolean {
    val stat = openStatement("DELETE FROM Logs WHERE userID=? AND time=?;") {
        setString(1, log.userID)
        setLong(2, log.time)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}


fun StyxDBClient.getLogs(conditions: Map<String, Any>? = null): List<Log> {
    val logs = mutableListOf<Log>()
    openResultSet("SELECT * FROM Logs;", conditions) {
        while (next()) {
            logs.add(
                Log(
                    getString("userID"),
                    getString("deviceID"),
                    LogType.valueOf(getString("type")),
                    getString("content"),
                    getLong("time")
                )
            )
        }
    }
    return logs.toList()
}
