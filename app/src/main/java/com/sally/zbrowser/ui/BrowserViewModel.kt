package com.sally.zbrowser.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sally.zbrowser.data.BrowserTab
import com.sally.zbrowser.data.HistoryEntry
import com.sally.zbrowser.data.HistoryStore
import com.sally.zbrowser.data.SearchEngine
import com.sally.zbrowser.data.SettingsStore
import com.sally.zbrowser.data.TabGroup
import com.sally.zbrowser.data.ThemeMode
import com.sally.zbrowser.data.resolveInput
import com.sally.zbrowser.engine.GeckoEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mozilla.geckoview.GeckoSession

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsStore(application)
    private val historyStore = HistoryStore(application)

    private val sessions = HashMap<String, GeckoSession>()
    private var counter = 0

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _groups = MutableStateFlow<List<TabGroup>>(emptyList())
    val groups: StateFlow<List<TabGroup>> = _groups.asStateFlow()

    private val _activeId = MutableStateFlow<String?>(null)
    val activeId: StateFlow<String?> = _activeId.asStateFlow()

    private val _history = MutableStateFlow(historyStore.load())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _theme = MutableStateFlow(settings.themeMode)
    val themeMode: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _engine = MutableStateFlow(settings.searchEngine)
    val searchEngine: StateFlow<SearchEngine> = _engine.asStateFlow()

    init {
        newTab()
    }

    // ---- Tab ----

    fun newTab(incognito: Boolean = false, groupId: String? = null, url: String? = null): String {
        val id = "tab-${++counter}"
        _tabs.update { it + BrowserTab(id = id, incognito = incognito, groupId = groupId) }
        _activeId.value = id

        val session = GeckoEngine.newSession(getApplication(), incognito)
        attachDelegates(id, session)
        sessions[id] = session

        if (url != null) session.loadUri(url)
        return id
    }

    fun sessionFor(id: String): GeckoSession? = sessions[id]

    fun selectTab(id: String) {
        _activeId.value = id
    }

    fun closeTab(id: String) {
        sessions.remove(id)?.close()
        val remaining = _tabs.value.filterNot { it.id == id }
        if (remaining.isEmpty()) {
            _tabs.value = emptyList()
            newTab()
            return
        }
        _tabs.value = remaining
        if (_activeId.value == id) {
            _activeId.value = remaining.last().id
        }
    }

    fun navigate(input: String) {
        val id = _activeId.value ?: return
        sessions[id]?.loadUri(resolveInput(input, _engine.value))
    }

    fun back() {
        _activeId.value?.let { sessions[it]?.goBack() }
    }

    fun forward() {
        _activeId.value?.let { sessions[it]?.goForward() }
    }

    fun reload() {
        _activeId.value?.let { sessions[it]?.reload() }
    }

    // ---- Grup tab ----

    fun createGroup(name: String): String {
        val id = "grp-${++counter}"
        _groups.update { it + TabGroup(id, name) }
        return id
    }

    fun deleteGroup(groupId: String) {
        _groups.update { list -> list.filterNot { it.id == groupId } }
        _tabs.update { list ->
            list.map { if (it.groupId == groupId) it.copy(groupId = null) else it }
        }
    }

    fun moveTabToGroup(tabId: String, groupId: String?) {
        updateTab(tabId) { it.copy(groupId = groupId) }
    }

    // ---- Riwayat ----

    fun clearHistory() {
        historyStore.clear()
        _history.value = emptyList()
    }

    // ---- Pengaturan ----

    fun setTheme(mode: ThemeMode) {
        settings.themeMode = mode
        _theme.value = mode
    }

    fun setSearchEngine(engine: SearchEngine) {
        settings.searchEngine = engine
        _engine.value = engine
    }

    // ---- Internal ----

    private fun attachDelegates(id: String, session: GeckoSession) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                if (url != null) updateTab(id) { it.copy(url = url) }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                updateTab(id) { it.copy(canGoBack = canGoBack) }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                updateTab(id) { it.copy(canGoForward = canGoForward) }
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                updateTab(id) { it.copy(loading = true, url = url) }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                updateTab(id) { it.copy(loading = false) }
                val tab = _tabs.value.firstOrNull { it.id == id } ?: return
                // Tab incognito tidak dicatat ke riwayat.
                if (!tab.incognito && success && tab.url != "about:blank") {
                    recordHistory(HistoryEntry(tab.url, tab.title, System.currentTimeMillis()))
                }
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                updateTab(id) { it.copy(title = title ?: it.url) }
            }
        }
    }

    private fun updateTab(id: String, transform: (BrowserTab) -> BrowserTab) {
        _tabs.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    private fun recordHistory(entry: HistoryEntry) {
        val list = (listOf(entry) + _history.value.filterNot { it.url == entry.url }).take(MAX_HISTORY)
        historyStore.save(list)
        _history.value = list
    }

    override fun onCleared() {
        sessions.values.forEach { it.close() }
        sessions.clear()
    }

    private companion object {
        const val MAX_HISTORY = 500
    }
}
