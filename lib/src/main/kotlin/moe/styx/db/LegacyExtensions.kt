package moe.styx.db

import moe.styx.types.LegacyAnimeInfo
import moe.styx.types.LegacyEpisodeInfo
import moe.styx.types.toBoolean
import java.net.URLEncoder

fun StyxDBClient.save(episodeInfo: LegacyEpisodeInfo, newID: String?): Boolean {
    val edit = objectExists(episodeInfo.GUID, "Episodes", "PermID")
    val query = if (edit)
        "UPDATE Episodes SET GUID=?, PermID=?, Date=?, Name=?, EP=?, ep_name_en=?, ep_name_de=?, summary_en=?, summary_de=?, FilePath=?, FileSize=?, PrevName=? WHERE PermID=?;"
    else
        "INSERT INTO Episodes (GUID, PermID, Date, Name, EP, ep_name_en, ep_name_de, summary_en, summary_de, FilePath, FileSize, PrevName) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) episodeInfo.permID else newID)
        setString(2, if (newID.isNullOrBlank()) episodeInfo.permID else newID)
        setString(3, episodeInfo.date)
        setString(4, episodeInfo.name)
        setString(5, episodeInfo.ep)
        setString(6, episodeInfo.ep_name_en)
        setString(7, episodeInfo.ep_name_de)
        setString(8, episodeInfo.summary_en)
        setString(9, episodeInfo.summary_de)
        setString(10, episodeInfo.filePath)
        setString(11, "" + episodeInfo.fileSize)
        setString(12, episodeInfo.prevName)

        if (edit)
            setString(13, episodeInfo.permID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getLegacyEpisodes(conditions: Map<String, Any>? = null): List<LegacyEpisodeInfo> {
    val episodes = mutableListOf<LegacyEpisodeInfo>()
    openResultSet("SELECT * FROM Episodes;", conditions) {
        while (next())
            episodes.add(
                LegacyEpisodeInfo(
                    getString("GUID"),
                    getString("PermID"),
                    getString("Date"),
                    getString("Name"),
                    getString("EP"),
                    getString("PrevName"),
                    getString("ep_name_en"),
                    getString("ep_name_de"),
                    getString("summary_en"),
                    getString("summary_de"),
                    getString("FilePath"),
                    getDouble("FileSize")
                )
            )
    }
    return episodes.toList()
}

fun StyxDBClient.delete(episodeInfo: LegacyEpisodeInfo) = genericDelete(episodeInfo.permID, "Episodes", "PermID")

// LegacyAnimeInfo

fun StyxDBClient.save(animeInfo: LegacyAnimeInfo, newID: String?): Boolean {
    val edit = objectExists(animeInfo.GUID, "Anime")
    val query = if (edit)
        "UPDATE Anime SET GUID=?, Name=?, SeasonName=?, ListLink=?, CoverURL=?, CoverMD5=?, English=?, Romaji=?, Schedule=?, Synopsis=?, DbMapping=? WHERE GUID=?;"
    else
        "INSERT INTO Anime (GUID, Name, SeasonName, ListLink, CoverURL, CoverMD5, English, Romaji, Schedule, Synopsis, DbMapping) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) animeInfo.GUID else newID)
        setString(2, animeInfo.name)
        setString(3, animeInfo.season)
        setString(4, "")
        setString(5, animeInfo.coverURL)
        setString(6, animeInfo.coverMD5)
        setString(7, animeInfo.English)
        setString(8, animeInfo.Romaji)
        setString(9, animeInfo.Schedule)
        setString(10, URLEncoder.encode(animeInfo.Synopsis, "UTF-8"))
        setString(11, animeInfo.DbMapping)

        if (edit)
            setString(12, animeInfo.GUID)

    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getLegacyAnime(conditions: Map<String, Any>? = null): List<LegacyAnimeInfo> {
    val anime = mutableListOf<LegacyAnimeInfo>()
    openResultSet("SELECT * FROM Anime;", conditions) {
        while (next())
            anime.add(
                LegacyAnimeInfo(
                    getString("GUID"),
                    getString("Name"),
                    getString("SeasonName"),
                    getString("CoverURL"),
                    getString("CoverMD5"),
                    getString("English"),
                    getString("Romaji"),
                    getString("Schedule"),
                    getString("Synopsis"),
                    null,
                    getString("DbMapping")
                )
            )
    }
    return anime.toList()
}

fun StyxDBClient.delete(animeInfo: LegacyAnimeInfo) = genericDelete(animeInfo.GUID, "Anime")