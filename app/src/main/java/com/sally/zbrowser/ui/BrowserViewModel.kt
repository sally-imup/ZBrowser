package com.sally.zbrowser.ui

import android.app.Application
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.AndroidViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.sally.zbrowser.data.BrowserTab
import com.sally.zbrowser.data.HistoryEntry
import com.sally.zbrowser.data.HistoryStore
import com.sally.zbrowser.data.SearchEngine
import com.sally.zbrowser.data.SettingsStore
import com.sally.zbrowser.data.TabGroup
import com.sally.zbrowser.data.ThemeMode
import com.sally.zbrowser.data.resolveInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsStore(application)
    private val historyStore = HistoryStore(application)

    private val webViews = HashMap<String, WebView>()
    private var counter = 0
    private var darkWeb = false

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

        val wv = createWebView(id, incognito)
        webViews[id] = wv
        syncPause()
        if (url != null) wv.loadUrl(url)
        return id
    }

    fun webViewFor(id: String): WebView? = webViews[id]

    fun selectTab(id: String) {
        _activeId.value = id
        syncPause()
    }

    fun closeTab(id: String) {
        webViews.remove(id)?.let { wv ->
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.stopLoading()
            wv.destroy()
        }
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
        syncPause()
    }

    fun navigate(input: String) {
        val id = _activeId.value ?: return
        webViews[id]?.loadUrl(resolveInput(input, _engine.value))
    }

    fun back() {
        _activeId.value?.let { webViews[it]?.goBack() }
    }

    fun forward() {
        _activeId.value?.let { webViews[it]?.goForward() }
    }

    fun reload() {
        _activeId.value?.let { webViews[it]?.reload() }
    }

    // ---- Grup tab ----

    fun createGroup(name: String) {
        val id = "grp-${++counter}"
        _groups.update { it + TabGroup(id, name) }
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

    fun applyWebDarkMode(dark: Boolean) {
        darkWeb = dark
        webViews.values.forEach { applyForceDark(it) }
    }

    // ---- Internal ----

    private fun createWebView(id: String, incognito: Boolean): WebView {
        return WebView(getApplication()).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.cacheMode = if (incognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            applyForceDark(this)

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    updateTab(id) {
                        it.copy(
                            url = url ?: it.url,
                            loading = true,
                            canGoBack = view?.canGoBack() == true,
                            canGoForward = view?.canGoForward() == true,
                        )
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    updateTab(id) {
                        it.copy(
                            url = url ?: it.url,
                            loading = false,
                            canGoBack = view?.canGoBack() == true,
                            canGoForward = view?.canGoForward() == true,
                        )
                    }
                    val tab = _tabs.value.firstOrNull { it.id == id } ?: return
                    if (!tab.incognito && url != null && url != "about:blank") {
                        recordHistory(HistoryEntry(url, tab.title, System.currentTimeMillis()))
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    updateTab(id) { it.copy(title = title ?: it.url) }
                }
            }
        }
    }

    private fun applyForceDark(wv: WebView) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(
                wv.settings,
                if (darkWeb) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF,
            )
        }
    }

    /** Hanya WebView tab aktif yang berjalan, sisanya dijeda supaya hemat baterai dan RAM. */
    private fun syncPause() {
        val active = _activeId.value
        webViews.forEach { (id, wv) ->
            if (id == active) wv.onResume() else wv.onPause()
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
        webViews.values.forEach { it.destroy() }
        webViews.clear()
    }

    private companion object {
        const val MAX_HISTORY = 500
    }
}
