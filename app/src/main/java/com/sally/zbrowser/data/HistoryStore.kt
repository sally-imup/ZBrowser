package com.sally.zbrowser.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class HistoryStore(context: Context) {

    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)

    fun load(): List<HistoryEntry> {
        return try {
            val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                HistoryEntry(o.getString("url"), o.optString("title"), o.getLong("time"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(entries: List<HistoryEntry>) {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(
                JSONObject()
                    .put("url", e.url)
                    .put("title", e.title)
                    .put("time", e.time)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private companion object {
        const val KEY = "entries"
    }
}
