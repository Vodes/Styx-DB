package moe.styx.db

import moe.styx.types.LegacyAnimeInfo
import moe.styx.types.LegacyEpisodeInfo
import moe.styx.types.LegacyMovieInfo
import moe.styx.types.toBoolean
import java.net.URLDecoder
import java.net.URLEncoder

fun StyxDBClient.save(episodeInfo: LegacyEpisodeInfo, newID: String? = null): Boolean {
    val edit = objectExists(episodeInfo.permID, "Episodes", "PermID")
    val query = if (edit)
        "UPDATE Episodes SET GUID=?, PermID=?, Date=?, Name=?, EP=?, ep_name_en=?, ep_name_de=?, summary_en=?, summary_de=?, FilePath=?, FileSize=?, PrevName=? WHERE PermID=?;"
    else
        "INSERT INTO Episodes (GUID, PermID, Date, Name, EP, ep_name_en, ep_name_de, summary_en, summary_de, FilePath, FileSize, PrevName) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"

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
    openResultSet("SELECT * FROM Episodes ORDER BY EP;", conditions) {
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

fun StyxDBClient.save(animeInfo: LegacyAnimeInfo, newID: String? = null): Boolean {
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
        setString(10, URLEncoder.encode(animeInfo.Synopsis, Charsets.UTF_8))
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
                    URLDecoder.decode(getString("Synopsis"), Charsets.UTF_8),
                    null,
                    getString("DbMapping")
                )
            )
    }
    return anime.toList()
}

fun StyxDBClient.delete(animeInfo: LegacyAnimeInfo) = genericDelete(animeInfo.GUID, "Anime")

// LegacyMovieInfo

fun StyxDBClient.save(movieInfo: LegacyMovieInfo, newID: String? = null): Boolean {
    val edit = objectExists(movieInfo.permID, "Movies", "permID")
    val query = if (edit)
        "UPDATE Movies SET GUID=?, PermID=?, Date=?, Name=?, ListLink=?, CoverURL=?, CoverMD5=?, English=?, Romaji=?, Synopsis=?, FilePath=?, FileSize=?, Category=? WHERE PermID=?;"
    else
        "INSERT INTO Movies (GUID, PermID, Date, Name, ListLink, CoverURL, CoverMD5, English, Romaji, Synopsis, FilePath, FileSize, Category) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) movieInfo.permID else newID)
        setString(2, if (newID.isNullOrBlank()) movieInfo.permID else newID)
        setString(3, movieInfo.date)
        setString(4, movieInfo.name)
        setString(5, movieInfo.listLink)
        setString(6, movieInfo.coverURL)
        setString(7, movieInfo.coverMD5)
        setString(8, movieInfo.English)
        setString(9, movieInfo.Romaji)
        setString(10, URLEncoder.encode(movieInfo.synopsis, Charsets.UTF_8))
        setString(11, movieInfo.filePath)
        setDouble(12, movieInfo.fileSize)
        setString(13, movieInfo.category)
        if (edit)
            setString(14, movieInfo.permID)

    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getLegacyMovieList(conditions: Map<String, Any>? = null): List<LegacyMovieInfo> {
    val movies = mutableListOf<LegacyMovieInfo>()
    openResultSet("SELECT * FROM Movies;", conditions) {
        while (next())
            movies.add(
                LegacyMovieInfo(
                    getString("PermID"),
                    getString("Date"),
                    getString("Name"),
                    getString("ListLink"),
                    getString("CoverURL"),
                    getString("CoverMD5"),
                    getString("English"),
                    getString("Romaji"),
                    URLDecoder.decode(getString("Synopsis"), Charsets.UTF_8),
                    getString("FilePath"),
                    getDouble("FileSize"),
                    getString("Category")
                )
            )
    }
    return movies.toList()
}

fun StyxDBClient.delete(movieInfo: LegacyMovieInfo) = genericDelete(movieInfo.permID, "Movies", "PermID")