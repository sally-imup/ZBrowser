# Z Browser

Browser Android ringan dengan Kotlin, Jetpack Compose (Material 3), dan GeckoView.
Package: `com.sally.zbrowser`

## Fitur versi pertama
- Material 3, aksen pink, mode terang/gelap (ikuti sistem atau dipaksa)
- Mesin pencari: DuckDuckGo, Google, Yandex
- Tab, tab group, riwayat, mode incognito
- DNS-over-HTTPS via Cloudflare (`https://cloudflare-dns.com/dns-query`)

## Build
Build otomatis di GitHub Actions (`.github/workflows/android.yml`). APK debug dan release
bisa diunduh dari tab Actions > artifact `z-browser-apk`.

Build lokal (butuh Gradle 8.10.2 dan JDK 17):
    gradle :app:assembleDebug

## Yang belum selesai
- Telemetry, Safe Browsing, captive portal, dan Remote Settings belum dimatikan
  (lihat TODO di `engine/GeckoEngine.kt`). Butuh verifikasi pref untuk versi GeckoView yang dipakai.
- Versi GeckoView (`geckoviewVersion` di `gradle.properties`) belum diverifikasi ke versi stabil terbaru.
- Belum ada pengujian di perangkat.
