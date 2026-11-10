package com.sally.zbrowser.engine

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

/**
 * Pembungkus GeckoView. Runtime hanya boleh dibuat sekali per proses,
 * jadi disimpan di sini.
 */
object GeckoEngine {

    private const val CLOUDFLARE_DOH = "https://cloudflare-dns.com/dns-query"

    private var runtime: GeckoRuntime? = null

    fun runtime(context: Context): GeckoRuntime {
        return runtime ?: GeckoRuntime.create(context.applicationContext, buildSettings())
            .also { runtime = it }
    }

    fun newSession(context: Context, incognito: Boolean): GeckoSession {
        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(incognito)
            .build()
        return GeckoSession(sessionSettings).also { it.open(runtime(context)) }
    }

    private fun buildSettings(): GeckoRuntimeSettings {
        return GeckoRuntimeSettings.Builder().build().apply {
            // DNS-over-HTTPS Cloudflare. TRR_MODE_ONLY: tidak ada fallback ke DNS sistem,
            // jadi lookup tidak bocor ke ISP. Kalau Cloudflare mati, koneksi ikut gagal.
            setTrustedRecursiveResolverMode(GeckoRuntimeSettings.TRR_MODE_ONLY)
            setTrustedRecursiveResolverUri(CLOUDFLARE_DOH)
            setDefaultRecursiveResolverUri(CLOUDFLARE_DOH)
            // Matikan autoselect supaya provider DoH tidak diganti otomatis.
            setDohAutoselectEnabled(false)

            // Pengaturan privasi dan keamanan dasar.
            setRemoteDebuggingEnabled(false)
            setAboutConfigEnabled(false)
            setExtensionsWebAPIEnabled(false)
            setGlobalPrivacyControl(true)
            setFingerprintingProtection(true)
            setFingerprintingProtectionPrivateBrowsing(true)

            // TODO: telemetry, Safe Browsing, captive portal, dan Remote Settings
            // belum diatur di sini. Pengaturannya lewat pref yang masih perlu
            // diverifikasi untuk versi GeckoView yang dipakai.
        }
    }
}
