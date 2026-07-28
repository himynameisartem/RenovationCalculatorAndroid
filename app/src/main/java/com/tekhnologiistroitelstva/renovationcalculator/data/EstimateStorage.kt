package com.tekhnologiistroitelstva.renovationcalculator.data

import android.content.Context
import com.tekhnologiistroitelstva.renovationcalculator.ui.models.RoomDraftInput
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object EstimateStorage {
    private const val FOLDER_NAME = "saved_estimates"

    private fun directory(context: Context): File {
        val folder = File(context.filesDir, FOLDER_NAME)
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun save(
        context: Context,
        total: Int,
        rooms: List<RoomDraftInput>,
        selectedItems: Map<String, Double>,
        lines: List<SavedEstimateLine>,
        id: String = UUID.randomUUID().toString(),
        createdAt: Long = System.currentTimeMillis(),
    ): SavedEstimate {
        val estimate = SavedEstimate(
            id = id,
            createdAt = createdAt,
            total = total,
            rooms = rooms,
            selectedItems = selectedItems,
            lines = lines
        )

        File(directory(context), "$id.json").writeText(estimate.toJson().toString(2))
        return estimate
    }

    fun loadAll(context: Context): List<SavedEstimate> {
        val files = directory(context).listFiles { file ->
            file.extension.equals("json", ignoreCase = true)
        } ?: emptyArray()

        return files.mapNotNull { file ->
            runCatching {
                SavedEstimate.fromJson(JSONObject(file.readText()))
            }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    fun delete(context: Context, id: String) {
        val file = File(directory(context), "$id.json")
        if (file.exists()) file.delete()
    }

    fun deleteAll(context: Context) {
        directory(context).listFiles { file ->
            file.extension.equals("json", ignoreCase = true)
        }?.forEach { it.delete() }
    }
}

private fun SavedEstimate.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("createdAt", createdAt)
    put("total", total)
    put("rooms", JSONArray().apply {
        rooms.forEach { room ->
            put(JSONObject().apply {
                put("name", room.name)
                put("area", room.area)
            })
        }
    })
    put("selectedItems", JSONObject(selectedItems))
    put("lines", JSONArray().apply {
        lines.forEach { line ->
            put(JSONObject().apply {
                put("id", line.id)
                put("title", line.title)
                put("quantity", line.quantity)
                put("unit", line.unit)
                put("unitPrice", line.unitPrice)
                put("subtotal", line.subtotal)
            })
        }
    })
}

private fun SavedEstimate.Companion.fromJson(json: JSONObject): SavedEstimate {
    val rooms = buildList {
        val arr = json.optJSONArray("rooms") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            add(
                RoomDraftInput(
                    name = obj.optString("name"),
                    area = obj.optDouble("area")
                )
            )
        }
    }

    val selectedItems = buildMap {
        val obj = json.optJSONObject("selectedItems") ?: JSONObject()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            put(key, obj.optDouble(key))
        }
    }

    val lines = buildList {
        val arr = json.optJSONArray("lines") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            add(
                SavedEstimateLine(
                    id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                    title = obj.optString("title"),
                    quantity = obj.optDouble("quantity"),
                    unit = obj.optString("unit"),
                    unitPrice = obj.optDouble("unitPrice"),
                    subtotal = obj.optDouble("subtotal")
                )
            )
        }
    }

    return SavedEstimate(
        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        total = json.optInt("total"),
        rooms = rooms,
        selectedItems = selectedItems,
        lines = lines
    )
}
