package com.tekhnologiistroitelstva.renovationcalculator.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CatalogRepository(
    private val url: String = "https://sk-family.ru/db.json",
) {
    fun loadCatalog(): List<CatalogCategory> {
        val json = request(url)
        return parseCategories(JSONObject(json))
    }

    private fun request(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/json")
        connection.connect()
        val code = connection.responseCode
        if (code !in 200..299) throw IllegalStateException("HTTP $code")
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseCategories(root: JSONObject): List<CatalogCategory> {
        val categoriesJson = root.optJSONArray("categories") ?: JSONArray()
        return buildList {
            for (i in 0 until categoriesJson.length()) {
                val c = categoriesJson.optJSONObject(i) ?: continue
                add(
                    CatalogCategory(
                        id = c.optString("id"),
                        title = c.optString("title"),
                        sections = parseSections(c.optJSONArray("sections") ?: JSONArray())
                    )
                )
            }
        }
    }

    private fun parseSections(arr: JSONArray): List<CatalogSection> = buildList {
        for (i in 0 until arr.length()) {
            val s = arr.optJSONObject(i) ?: continue
            add(
                CatalogSection(
                    id = s.optString("id"),
                    title = s.optString("title"),
                    items = parseItems(s.optJSONArray("items") ?: JSONArray())
                )
            )
        }
    }

    private fun parseItems(arr: JSONArray): List<CatalogItem> = buildList {
        for (i in 0 until arr.length()) {
            val it = arr.optJSONObject(i) ?: continue
            val photos = mutableListOf<String>()
            val photosArr = it.optJSONArray("photos") ?: JSONArray()
            for (p in 0 until photosArr.length()) {
                photos += photosArr.optString(p)
            }
            add(
                CatalogItem(
                    id = it.optString("id"),
                    title = it.optString("title"),
                    unit = it.optString("unit"),
                    price = it.optDouble("price"),
                    description = it.optString("description").takeIf { d -> d.isNotBlank() && d != "null" },
                    photos = photos
                )
            )
        }
    }
}

