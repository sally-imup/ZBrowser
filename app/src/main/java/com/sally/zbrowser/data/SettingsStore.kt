package com.sally.zbrowser.data

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var searchEngine: SearchEngine
        get() = runCatching {
            SearchEngine.valueOf(prefs.getString(KEY_ENGINE, SearchEngine.DUCKDUCKGO.name)!!)
        }.getOrDefault(SearchEngine.DUCKDUCKGO)
        set(value) = prefs.edit().putString(KEY_ENGINE, value.name).apply()

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_ENGINE = "search_engine"
    }
}
