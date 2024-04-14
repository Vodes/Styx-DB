package moe.styx.db.tables

import moe.styx.common.data.DownloadableOption
import moe.styx.common.data.DownloaderTarget
import moe.styx.common.json
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.json.json as jsonCol

object DownloaderTargetsTable : Table("media_download_target") {
    val mediaID = reference("mediaID", MediaTable.GUID, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val options = jsonCol<List<DownloadableOption>>("options", json)
    val namingTemplate = text("namingTemplate")
    val titleTemplate = text("titleTemplate")
    val outputDir = text("outputDir")

    override val primaryKey = PrimaryKey(mediaID)

    fun upsertItem(item: DownloaderTarget) = upsert {
        it[mediaID] = item.mediaID
        it[options] = item.options
        it[namingTemplate] = item.namingTemplate
        it[titleTemplate] = item.titleTemplate
        it[outputDir] = item.outputDir
    }

    fun query(block: DownloaderTargetsTable.() -> List<ResultRow>): List<DownloaderTarget> {
        return block(this).map {
            DownloaderTarget(
                it[mediaID],
                it[options].toMutableList(),
                it[namingTemplate],
                it[titleTemplate],
                it[outputDir]
            )
        }
    }
}