@file:OptIn(ExperimentalMaterial3Api::class)

package com.sally.zbrowser.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.LocaleListCompat
import com.sally.zbrowser.R
import com.sally.zbrowser.data.BrowserTab
import com.sally.zbrowser.data.HistoryEntry
import com.sally.zbrowser.data.SearchEngine
import com.sally.zbrowser.data.TabGroup
import com.sally.zbrowser.data.ThemeMode
import android.content.Intent
import android.provider.Settings as AndroidSettings

@Composable
fun BrowserScreen(vm: BrowserViewModel, dark: Boolean, onExit: () -> Unit) {
    val tabs by vm.tabs.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val groups by vm.groups.collectAsState()
    val history by vm.history.collectAsState()
    val active = tabs.firstOrNull { it.id == activeId }

    var showTabs by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(dark) { vm.applyWebDarkMode(dark) }

    BackHandler {
        if (active?.canGoBack == true) vm.back() else onExit()
    }

    Scaffold(
        topBar = {
            UrlBar(
                url = active?.url ?: "about:blank",
                incognito = active?.incognito == true,
                loading = active?.loading == true,
                onSubmit = { vm.navigate(it) },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.back() },
                    enabled = active?.canGoBack == true,
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back)) },
                    label = { Text(stringResource(R.string.nav_back)) },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.forward() },
                    enabled = active?.canGoForward == true,
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.nav_forward)) },
                    label = { Text(stringResource(R.string.nav_forward)) },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.reload() },
                    icon = { Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.nav_reload)) },
                    label = { Text(stringResource(R.string.nav_reload)) },
                )
                NavigationBarItem(
                    selected = showTabs,
                    onClick = { showTabs = true },
                    icon = { Icon(Icons.Filled.Tab, contentDescription = stringResource(R.string.nav_tabs)) },
                    label = { Text(stringResource(R.string.tabs_count, tabs.size)) },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showMenu = true },
                    icon = {
                        Box {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.nav_menu))
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.new_tab)) },
                                    leadingIcon = { Icon(Icons.Filled.Add, null) },
                                    onClick = { showMenu = false; vm.newTab() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.incognito_tab)) },
                                    leadingIcon = { Icon(Icons.Filled.VisibilityOff, null) },
                                    onClick = { showMenu = false; vm.newTab(incognito = true) },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.history)) },
                                    leadingIcon = { Icon(Icons.Filled.History, null) },
                                    onClick = { showMenu = false; showHistory = true },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.settings)) },
                                    leadingIcon = { Icon(Icons.Filled.Settings, null) },
                                    onClick = { showMenu = false; showSettings = true },
                                )
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.nav_menu)) },
                )
            }
        },
    ) { padding ->
        WebContainer(vm = vm, activeId = activeId, modifier = Modifier.padding(padding).fillMaxSize())
    }

    if (showTabs) {
        TabsSheet(tabs = tabs, groups = groups, activeId = activeId, vm = vm, onDismiss = { showTabs = false })
    }
    if (showHistory) {
        HistorySheet(history = history, vm = vm, onDismiss = { showHistory = false })
    }
    if (showSettings) {
        SettingsSheet(vm = vm, onDismiss = { showSettings = false })
    }
}

/** Container tempat WebView tab aktif ditampilkan. */
@Composable
private fun WebContainer(vm: BrowserViewModel, activeId: String?, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> FrameLayout(ctx) },
        update = { frame ->
            val wv = activeId?.let { vm.webViewFor(it) } ?: return@AndroidView
            if (frame.childCount == 1 && frame.getChildAt(0) === wv) return@AndroidView
            (wv.parent as? ViewGroup)?.removeView(wv)
            frame.removeAllViews()
            frame.addView(wv, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        },
    )
}

@Composable
private fun UrlBar(url: String, incognito: Boolean, loading: Boolean, onSubmit: (String) -> Unit) {
    val blank = stringResource(R.string.blank_page)
    var text by remember(url) { mutableStateOf(if (url == "about:blank") "" else url) }
    val focus = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text(stringResource(R.string.url_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = if (incognito) Icons.Filled.VisibilityOff else if (url.startsWith("https")) Icons.Filled.Lock else Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                onSubmit(text)
                focus.clearFocus()
            }),
        )
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TabsSheet(
    tabs: List<BrowserTab>,
    groups: List<TabGroup>,
    activeId: String?,
    vm: BrowserViewModel,
    onDismiss: () -> Unit,
) {
    var showNewGroup by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = { vm.newTab(); onDismiss() }) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.new_tab))
                    }
                    OutlinedButton(onClick = { vm.newTab(incognito = true); onDismiss() }) {
                        Icon(Icons.Filled.VisibilityOff, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.incognito_tab))
                    }
                    OutlinedButton(onClick = { showNewGroup = true }) {
                        Icon(Icons.Filled.Folder, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.new_group))
                    }
                }
            }

            groups.forEach { group ->
                item(key = "group-${group.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Folder, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { vm.deleteGroup(group.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_group))
                        }
                    }
                }
                items(tabs.filter { it.groupId == group.id }, key = { it.id }) { tab ->
                    TabRow(tab, groups, tab.id == activeId, vm, onDismiss)
                }
            }

            val ungrouped = tabs.filter { tab -> groups.none { it.id == tab.groupId } }
            if (ungrouped.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ungrouped),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                    )
                }
                items(ungrouped, key = { it.id }) { tab ->
                    TabRow(tab, groups, tab.id == activeId, vm, onDismiss)
                }
            }
        }
    }

    if (showNewGroup) {
        NewGroupDialog(
            onConfirm = { name ->
                vm.createGroup(name)
                showNewGroup = false
            },
            onDismiss = { showNewGroup = false },
        )
    }
}

@Composable
private fun TabRow(
    tab: BrowserTab,
    groups: List<TabGroup>,
    selected: Boolean,
    vm: BrowserViewModel,
    onDone: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val blank = stringResource(R.string.blank_page)

    ListItem(
        modifier = Modifier.clickable {
            vm.selectTab(tab.id)
            onDone()
        },
        colors = if (selected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        headlineContent = { Text(tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(if (tab.url == "about:blank") blank else tab.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Icon(
                imageVector = if (tab.incognito) Icons.Filled.VisibilityOff else Icons.Filled.Public,
                contentDescription = null,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.Folder, contentDescription = stringResource(R.string.move_to_group))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.no_group)) },
                            onClick = { menu = false; vm.moveTabToGroup(tab.id, null) },
                        )
                        groups.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = { menu = false; vm.moveTabToGroup(tab.id, g.id) },
                            )
                        }
                    }
                }
                IconButton(onClick = { vm.closeTab(tab.id) }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close_tab))
                }
            }
        },
    )
}

@Composable
private fun NewGroupDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Folder, null) },
        title = { Text(stringResource(R.string.new_group)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.group_name)) },
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun HistorySheet(history: List<HistoryEntry>, vm: BrowserViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleLarge)
                    }
                    TextButton(onClick = { vm.clearHistory() }) {
                        Icon(Icons.Filled.Delete, null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.clear_history))
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.no_history),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(history, key = { it.url + it.time }) { entry ->
                ListItem(
                    modifier = Modifier.clickable {
                        vm.newTab(url = entry.url)
                        onDismiss()
                    },
                    leadingContent = { Icon(Icons.Filled.Public, null) },
                    headlineContent = {
                        Text(entry.title.ifBlank { entry.url }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = { Text(entry.url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSheet(vm: BrowserViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val theme by vm.themeMode.collectAsState()
    val engine by vm.searchEngine.collectAsState()
    val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            item {
                Text(
                    stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            item { SectionHeader(Icons.Filled.Language, stringResource(R.string.section_language)) }
            item {
                RadioRow(stringResource(R.string.lang_system), currentLang.isEmpty()) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            }
            item {
                RadioRow(stringResource(R.string.lang_en), currentLang.startsWith("en")) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                }
            }
            item {
                RadioRow(stringResource(R.string.lang_id), currentLang.startsWith("id") || currentLang.startsWith("in")) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("id"))
                }
            }

            item { SectionHeader(Icons.Filled.DarkMode, stringResource(R.string.section_theme)) }
            item {
                RadioRow(stringResource(R.string.theme_system), theme == ThemeMode.SYSTEM) { vm.setTheme(ThemeMode.SYSTEM) }
            }
            item {
                RadioRow(stringResource(R.string.theme_light), theme == ThemeMode.LIGHT) { vm.setTheme(ThemeMode.LIGHT) }
            }
            item {
                RadioRow(stringResource(R.string.theme_dark), theme == ThemeMode.DARK) { vm.setTheme(ThemeMode.DARK) }
            }

            item { SectionHeader(Icons.Filled.Search, stringResource(R.string.section_search)) }
            items(SearchEngine.entries) { e ->
                RadioRow(e.label, engine == e) { vm.setSearchEngine(e) }
            }

            item { SectionHeader(Icons.Filled.Dns, stringResource(R.string.section_dns)) }
            item {
                Text(
                    stringResource(R.string.dns_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            item {
                OutlinedButton(onClick = {
                    context.startActivity(Intent(AndroidSettings.ACTION_WIRELESS_SETTINGS))
                }) {
                    Icon(Icons.Filled.Dns, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.open_dns_settings))
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
