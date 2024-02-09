package moe.styx.db

import moe.styx.common.data.*
import moe.styx.common.extension.toBoolean

// MediaInfo

fun StyxDBClient.save(info: MediaInfo): Boolean {
    val edit = objectExists(info.entryID, "MediaInfo", "entryID")
    val query = if (edit)
        "UPDATE MediaInfo SET entryID=?, videoCodec=?, videoBitdepth=?, videoRes=?, hasEnglishDub=?, hasGermanDub=?, hasGermanSub=? WHERE entryID=?;"
    else
        "INSERT INTO MediaInfo (entryID, videoCodec, videoBitdepth, videoRes, hasEnglishDub, hasGermanDub, hasGermanSub) VALUES(?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, info.entryID)
        setString(2, info.videoCodec)
        setInt(3, info.videoBitdepth)
        setString(4, info.videoRes)
        setInt(5, info.hasEnglishDub)
        setInt(6, info.hasGermanDub)
        setInt(7, info.hasGermanSub)
        if (edit)
            setString(8, info.entryID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(info: MediaInfo) = genericDelete(info.entryID, "MediaInfo", "entryID")

fun StyxDBClient.getMediaInfo(conditions: Map<String, Any>? = null): List<MediaInfo> {
    val mediainfos = mutableListOf<MediaInfo>()
    openResultSet("SELECT * FROM MediaInfo;", conditions) {
        while (next())
            mediainfos.add(
                MediaInfo(
                    getString("entryID"),
                    getString("videoCodec"),
                    getInt("videoBitdepth"),
                    getString("videoRes"),
                    getInt("hasEnglishDub"),
                    getInt("hasGermanDub"),
                    getInt("hasGermanSub")
                )
            )
    }
    return mediainfos.toList()
}

// MediaWatched

fun StyxDBClient.save(watched: MediaWatched): Boolean {
    val edit = objectExistsTwo("entryID", "userID", watched.entryID, watched.userID, "MediaWatched")
    val query = if (edit)
        "UPDATE MediaWatched SET entryID=?, userID=?, lastWatched=?, progress=?, progressPercent=?, maxProgress=? WHERE entryID=? AND userID=?;"
    else
        "INSERT INTO MediaWatched (entryID, userID, lastWatched, progress, progressPercent, maxProgress) VALUES(?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, watched.entryID)
        setString(2, watched.userID)
        setLong(3, watched.lastWatched)
        setLong(4, watched.progress)
        setFloat(5, watched.progressPercent)
        setFloat(6, watched.maxProgress)
        if (edit) {
            setString(7, watched.entryID)
            setString(8, watched.userID)
        }
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(watched: MediaWatched): Boolean {
    val stat = openStatement("DELETE FROM MediaWatched WHERE entryID=? AND userID=?;")
    stat.setString(1, watched.entryID)
    stat.setString(2, watched.userID)
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getMediaWatched(conditions: Map<String, Any>? = null): List<MediaWatched> {
    val watcheds = mutableListOf<MediaWatched>()
    openResultSet("SELECT * FROM MediaWatched;", conditions) {
        while (next())
            watcheds.add(
                MediaWatched(
                    getString("entryID"),
                    getString("userID"),
                    getLong("lastWatched"),
                    getLong("progress"),
                    getFloat("progressPercent"),
                    getFloat("maxProgress")
                )
            )
    }
    return watcheds.toList()
}

// MediaEntry

fun StyxDBClient.save(entry: MediaEntry, newID: String? = null): Boolean {
    val edit = objectExists(entry.GUID, "MediaEntry")
    val query = if (edit)
        "UPDATE MediaEntry SET GUID=?, mediaID=?, timestamp=?, entryNumber=?, nameEN=?, nameDE=?, synopsisEN=?, synopsisDE=?, thumbID=?, filePath=?, fileSize=?, originalName=? WHERE GUID=?;"
    else
        "INSERT INTO MediaEntry (GUID, mediaID, timestamp, entryNumber, nameEN, nameDE, synopsisEN, synopsisDE, thumbID, filePath, fileSize, originalName) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) entry.GUID else newID)
        setString(2, entry.mediaID)
        setLong(3, entry.timestamp)
        setString(4, entry.entryNumber)
        setString(5, entry.nameEN)
        setString(6, entry.nameDE)
        setString(7, entry.synopsisEN)
        setString(8, entry.synopsisDE)
        setString(9, entry.thumbID)
        setString(10, entry.filePath)
        setLong(11, entry.fileSize)
        setString(12, entry.originalName)
        if (edit)
            setString(13, entry.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(entry: MediaEntry) = genericDelete(entry.GUID, "MediaEntry")

fun StyxDBClient.getEntries(conditions: Map<String, Any>? = null): List<MediaEntry> {
    val entries = mutableListOf<MediaEntry>()
    openResultSet("SELECT * FROM MediaEntry;", conditions) {
        while (next())
            entries.add(
                MediaEntry(
                    getString("GUID"), getString("mediaID"),
                    getLong("timestamp"), getString("entryNumber"),
                    getString("nameEN"), getString("nameDE"),
                    getString("synopsisEN"), getString("synopsisDE"),
                    getString("thumbID"), getString("filePath"),
                    getLong("fileSize"), getString("originalName")
                )
            )
    }
    return entries.toList()
}

// Media

fun StyxDBClient.save(media: Media, newID: String? = null): Boolean {
    val edit = objectExists(media.GUID, "Media")
    val query = if (edit)
        "UPDATE Media SET GUID=?, name=?, nameJP=?, nameEN=?, synopsisEN=?, synopsisDE=?, thumbID=?, bannerID=?, categoryID=?, prequel=?, sequel=?, genres=?, tags=?, metadataMap=?, isSeries=?, added=? WHERE GUID=?;"
    else
        "INSERT INTO Media (GUID, name, nameJP, nameEN, synopsisEN, synopsisDE, thumbID, bannerID, categoryID, prequel, sequel, genres, tags, metadataMap, isSeries, added) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) media.GUID else newID)
        setString(2, media.name)
        setString(3, media.nameJP)
        setString(4, media.nameEN)
        setString(5, media.synopsisEN)
        setString(6, media.synopsisDE)
        setString(7, media.thumbID)
        setString(8, media.bannerID)
        setString(9, media.categoryID)
        setString(10, media.prequel)
        setString(11, media.sequel)
        setString(12, media.genres)
        setString(13, media.tags)
        setString(14, media.metadataMap)
        setInt(15, media.isSeries)
        setLong(16, media.added)
        if (edit)
            setString(17, media.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(media: Media) = genericDelete(media.GUID, "Media")

fun StyxDBClient.getMedia(conditions: Map<String, Any>? = null): List<Media> {
    val media = mutableListOf<Media>()
    openResultSet("SELECT * FROM Media;", conditions) {
        while (next())
            media.add(
                Media(
                    getString("GUID"), getString("name"),
                    getString("nameJP"), getString("nameEN"),
                    getString("synopsisEN"), getString("synopsisDE"),
                    getString("thumbID"), getString("bannerID"),
                    getString("categoryID"), getString("prequel"),
                    getString("sequel"), getString("genres"),
                    getString("tags"), getString("metadataMap"),
                    getInt("isSeries"), getLong("added")
                )
            )
    }
    return media.toList()
}

// MediaSchedule

fun StyxDBClient.save(schedule: MediaSchedule): Boolean {
    val edit = objectExists(schedule.mediaID, "MediaSchedule", "mediaID")
    val query = if (edit)
        "UPDATE MediaSchedule SET mediaID=?, day=?, hour=?, minute=?, isEstimated=?, finalEpisodeCount=? WHERE mediaID=?;"
    else
        "INSERT INTO MediaSchedule (mediaID, day, hour, minute, isEstimated, finalEpisodeCount) VALUES (?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, schedule.mediaID)
        setString(2, schedule.day.name)
        setInt(3, schedule.hour)
        setInt(4, schedule.minute)
        setInt(5, schedule.isEstimated)
        setInt(6, schedule.finalEpisodeCount)
        if (edit)
            setString(7, schedule.mediaID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(schedule: MediaSchedule): Boolean = genericDelete(schedule.mediaID, "MediaSchedule", "mediaID")

fun StyxDBClient.getSchedules(conditions: Map<String, Any>? = null): List<MediaSchedule> {
    val schedules = mutableListOf<MediaSchedule>()
    openResultSet("SELECT * FROM MediaSchedule;", conditions) {
        while (next())
            schedules.add(
                MediaSchedule(
                    getString("mediaID"),
                    ScheduleWeekday.valueOf(getString("day")),
                    getInt("hour"),
                    getInt("minute"),
                    getInt("isEstimated"),
                    getInt("finalEpisodeCount")
                )
            )
    }
    return schedules.toList()
}