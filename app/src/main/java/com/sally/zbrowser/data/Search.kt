package com.sally.zbrowser.data

import java.net.URLEncoder

enum class SearchEngine(val label: String, val template: String) {
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    GOOGLE("Google", "https://www.google.com/search?q=%s"),
    YANDEX("Yandex", "https://yandex.com/search/?text=%s");
}

private val SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")

/**
 * Mengubah isi address bar menjadi URL yang bisa dibuka.
 * - Sudah punya skema (https://, about:) -> dipakai apa adanya.
 * - Tanpa spasi dan ada titik -> dianggap domain, diberi https://.
 * - Selain itu -> dicari di mesin pencari yang dipilih.
 */
fun resolveInput(input: String, engine: SearchEngine): String {
    val text = input.trim()
    if (text.isEmpty()) return "about:blank"
    if (SCHEME_REGEX.containsMatchIn(text) || text.startsWith("about:")) return text
    val looksLikeHost = !text.contains(' ') && text.contains('.')
    return if (looksLikeHost) {
        "https://$text"
    } else {
        engine.template.replace("%s", URLEncoder.encode(text, "UTF-8"))
    }
}
