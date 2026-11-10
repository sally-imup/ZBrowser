package com.sally.zbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sally.zbrowser.data.ThemeMode
import com.sally.zbrowser.ui.BrowserScreen
import com.sally.zbrowser.ui.BrowserViewModel
import com.sally.zbrowser.ui.theme.ZBrowserTheme

class MainActivity : ComponentActivity() {

    private val vm: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mode by vm.themeMode.collectAsState()
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            ZBrowserTheme(darkTheme = dark) {
                BrowserScreen(vm = vm, onExit = { finish() })
            }
        }
    }
}
