package com.sally.zbrowser.data

data class BrowserTab(
    val id: String,
    val url: String = "about:blank",
    val title: String = "Tab baru",
    val incognito: Boolean = false,
    val groupId: String? = null,
    val loading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
)

data class TabGroup(
    val id: String,
    val name: String,
)

data class HistoryEntry(
    val url: String,
    val title: String,
    val time: Long,
)
