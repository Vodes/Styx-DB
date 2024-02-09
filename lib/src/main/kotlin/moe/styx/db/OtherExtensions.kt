package moe.styx.db

import moe.styx.common.data.Category
import moe.styx.common.data.Favourite
import moe.styx.common.data.Image
import moe.styx.common.extension.toBoolean

// Image

fun StyxDBClient.save(image: Image, newID: String? = null): Boolean {
    val edit = objectExists(image.GUID, "Image")
    val query = if (edit)
        "UPDATE Image SET GUID=?, hasWEBP=?, hasPNG=?, hasJPG=?, externalURL=?, type=? WHERE GUID=?;"
    else
        "INSERT INTO Image (GUID, hasWEBP, hasPNG, hasJPG, externalURL, type) VALUES(?, ?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) image.GUID else newID)
        setInt(2, image.hasWEBP ?: 0)
        setInt(3, image.hasPNG ?: 0)
        setInt(4, image.hasJPG ?: 0)
        setString(5, image.externalURL)
        setInt(6, image.type)
        if (edit)
            setString(7, image.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(image: Image) = genericDelete(image.GUID, "Image")

fun StyxDBClient.getImages(conditions: Map<String, Any>? = null): List<Image> {
    val images = mutableListOf<Image>()
    openResultSet("SELECT * FROM Image;", conditions) {
        while (next()) {
            images.add(
                Image(
                    getString("GUID"),
                    getInt("hasWEBP"),
                    getInt("hasPNG"),
                    getInt("hasJPG"),
                    getString("externalURL"),
                    getInt("type")
                )
            )
        }
    }
    return images.toList()
}

// Category

fun StyxDBClient.save(category: Category, newID: String? = null): Boolean {
    val edit = objectExists(category.GUID, "Category")
    val query = if (edit)
        "UPDATE Category SET GUID=?, sort=?, isSeries=?, isVisible=?, name=? WHERE GUID=?;"
    else
        "INSERT INTO Category (GUID, sort, isSeries, isVisible, name) VALUES(?, ?, ?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, if (newID.isNullOrBlank()) category.GUID else newID)
        setInt(2, category.sort)
        setInt(3, category.isSeries)
        setInt(4, category.isVisible)
        setString(5, category.name)
        if (edit)
            setString(6, category.GUID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(category: Category) = genericDelete(category.GUID, "Category")

fun StyxDBClient.getCategories(conditions: Map<String, Any>? = null): List<Category> {
    val categories = mutableListOf<Category>()
    openResultSet("SELECT * FROM Category ORDER BY isSeries DESC, sort DESC;", conditions) {
        while (next()) {
            categories.add(
                Category(
                    getString("GUID"), getInt("sort"), getInt("isSeries"),
                    getInt("isVisible"), getString("name")
                )
            )
        }
    }
    return categories.toList()
}

// Favourite

fun StyxDBClient.save(fav: Favourite): Boolean {
    val edit = objectExistsTwo("mediaID", "userID", fav.mediaID, fav.userID, "Favourites")
    val query = if (edit)
        "UPDATE Favourites SET mediaID=?, userID=?, added=? WHERE mediaID=? AND userID=?;"
    else
        "INSERT INTO Favourites (mediaID, userID, added) VALUES(?, ?, ?);"

    val stat = openStatement(query) {
        setString(1, fav.mediaID)
        setString(2, fav.userID)
        setLong(3, fav.added)
        if (edit) {
            setString(4, fav.mediaID)
            setString(5, fav.userID)
        }
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.delete(fav: Favourite): Boolean {
    val stat = openStatement("DELETE FROM Favourites WHERE mediaID=? AND userID=?;") {
        setString(1, fav.mediaID)
        setString(2, fav.userID)
    }
    return stat.executeUpdate().toBoolean().also { stat.close() }
}

fun StyxDBClient.getFavourites(conditions: Map<String, Any>? = null): List<Favourite> {
    val favs = mutableListOf<Favourite>()
    openResultSet("SELECT * FROM Favourites;", conditions) {
        while (next())
            favs.add(Favourite(getString("mediaID"), getString("userID"), getLong("added")))
    }
    return favs.toList()
}