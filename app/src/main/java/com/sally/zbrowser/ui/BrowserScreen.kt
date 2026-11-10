@file:OptIn(ExperimentalMaterial3Api::class)

package com.sally.zbrowser.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sally.zbrowser.data.BrowserTab
import com.sally.zbrowser.data.HistoryEntry
import com.sally.zbrowser.data.SearchEngine
import com.sally.zbrowser.data.ThemeMode
import com.sally.zbrowser.data.TabGroup
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserScreen(vm: BrowserViewModel, onExit: () -> Unit) {
    val tabs by vm.tabs.collectAsState()
    val activeId by vm.activeId.collectAsState()
    val groups by vm.groups.collectAsState()
    val history by vm.history.collectAsState()
    val active = tabs.firstOrNull { it.id == activeId }

    var showTabs by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") },
                    label = { Text("Kembali") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.forward() },
                    enabled = active?.canGoForward == true,
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Maju") },
                    label = { Text("Maju") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.reload() },
                    icon = { Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang") },
                    label = { Text("Muat ulang") },
                )
                NavigationBarItem(
                    selected = showTabs,
                    onClick = { showTabs = true },
                    icon = { Icon(Icons.Filled.Tab, contentDescription = "Tab") },
                    label = { Text("Tab ${tabs.size}") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showMenu = true },
                    icon = {
                        Box {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Tab baru") }, onClick = {
                                    showMenu = false
                                    vm.newTab()
                                })
                                DropdownMenuItem(text = { Text("Tab incognito") }, onClick = {
                                    showMenu = false
                                    vm.newTab(incognito = true)
                                })
                                DropdownMenuItem(text = { Text("Riwayat") }, onClick = {
                                    showMenu = false
                                    showHistory = true
                                })
                                DropdownMenuItem(text = { Text("Pengaturan") }, onClick = {
                                    showMenu = false
                                    showSettings = true
                                })
                            }
                        }
                    },
                    label = { Text("Menu") },
                )
            }
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { ctx -> GeckoView(ctx) },
            update = { view ->
                active?.let { tab ->
                    val session = vm.sessionFor(tab.id)
                    if (session != null && view.session !== session) {
                        view.setSession(session)
                    }
                }
            },
        )
    }

    if (showTabs) {
        TabsSheet(
            tabs = tabs,
            groups = groups,
            activeId = activeId,
            vm = vm,
            onDismiss = { showTabs = false },
        )
    }
    if (showHistory) {
        HistorySheet(history = history, vm = vm, onDismiss = { showHistory = false })
    }
    if (showSettings) {
        SettingsDialog(vm = vm, onDismiss = { showSettings = false })
    }
}

@Composable
private fun UrlBar(url: String, incognito: Boolean, loading: Boolean, onSubmit: (String) -> Unit) {
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
            placeholder = { Text("Cari atau ketik URL") },
            leadingIcon = {
                Icon(
                    imageVector = if (incognito) Icons.Filled.VisibilityOff else Icons.Filled.Lock,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = { vm.newTab(); onDismiss() }) { Text("Tab baru") }
                    OutlinedButton(onClick = { vm.newTab(incognito = true); onDismiss() }) { Text("Incognito") }
                    OutlinedButton(onClick = { showNewGroup = true }) { Text("Grup baru") }
                }
            }

            groups.forEach { group ->
                item(key = "group-${group.id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { vm.deleteGroup(group.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus grup")
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
                        text = "Tanpa grup",
                        style = MaterialTheme.typography.titleSmall,
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
        headlineContent = {
            Text(tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                if (tab.url == "about:blank") "Kosong" else tab.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
                        Icon(Icons.Filled.MoreVert, contentDescription = "Pindah grup")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Tanpa grup") }, onClick = {
                            menu = false
                            vm.moveTabToGroup(tab.id, null)
                        })
                        groups.forEach { g ->
                            DropdownMenuItem(text = { Text(g.name) }, onClick = {
                                menu = false
                                vm.moveTabToGroup(tab.id, g.id)
                            })
                        }
                    }
                }
                IconButton(onClick = { vm.closeTab(tab.id) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup tab")
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
        title = { Text("Grup baru") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Nama grup") },
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Buat") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun HistorySheet(history: List<HistoryEntry>, vm: BrowserViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Riwayat", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { vm.clearHistory() }) { Text("Hapus semua") }
                }
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        "Belum ada riwayat",
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
                    headlineContent = {
                        Text(
                            entry.title.ifBlank { entry.url },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    supportingContent = {
                        Text(entry.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(vm: BrowserViewModel, onDismiss: () -> Unit) {
    val theme by vm.themeMode.collectAsState()
    val engine by vm.searchEngine.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Tema",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                ThemeMode.entries.forEach { mode ->
                    RadioRow(
                        label = when (mode) {
                            ThemeMode.SYSTEM -> "Ikuti sistem"
                            ThemeMode.LIGHT -> "Terang"
                            ThemeMode.DARK -> "Gelap"
                        },
                        selected = theme == mode,
                        onClick = { vm.setTheme(mode) },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Mesin pencari",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                SearchEngine.entries.forEach { e ->
                    RadioRow(
                        label = e.label,
                        selected = engine == e,
                        onClick = { vm.setSearchEngine(e) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
    )
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
