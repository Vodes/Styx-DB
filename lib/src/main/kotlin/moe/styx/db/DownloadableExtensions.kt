package moe.styx.db

import kotlinx.serialization.encodeToString
import moe.styx.types.DownloaderTarget
import moe.styx.types.json
import moe.styx.types.toBoolean

fun StyxDBClient.save(target: DownloaderTarget, newID: String? = null): Boolean {
    val edit = objectExists(target.mediaID, "DownloaderTargets", "mediaID")
    val query = if (edit)
        "UPDATE DownloaderTargets SET mediaID=?, options=?, namingTemplate=?, titleTemplate=?, outputDir=? WHERE mediaID=?;"
    else
        "INSERT INTO DownloaderTargets (mediaID, options, namingTemplate, titleTemplate, outputDir) VALUES(?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) target.mediaID else newID)
        setString(2, json.encodeToString(target.options))
        setString(3, target.namingTemplate)
        setString(4, target.titleTemplate)
        setString(5, target.outputDir)
        if (edit)
            setString(6, target.mediaID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(target: DownloaderTarget) = genericDelete(target.mediaID, "DownloaderTargets", "mediaID")

fun StyxDBClient.getTargets(conditions: Map<String, Any>? = null): List<DownloaderTarget> {
    val targets = mutableListOf<DownloaderTarget>()
    openResultSet("SELECT * FROM DownloaderTargets;", conditions) {
        while (next())
            targets.add(
                DownloaderTarget(
                    getString("mediaID"),
                    json.decodeFromString(getString("options")),
                    getString("namingTemplate"),
                    getString("titleTemplate"),
                    getString("outputDir")
                )
            )
    }
    return targets.toList()
}