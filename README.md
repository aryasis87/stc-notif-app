# STC Notif — app Android (Kotlin / Jetpack Compose)

Aplikasi native untuk memantau **STC & KOALA** dari HP: notifikasi realtime
(deposit, penarikan, aktivasi) **tanpa FCM/Google**, plus dashboard admin penuh —
cukup **login dengan password**, tanpa isi URL/token.

## Fitur (v2.0)
- **Login password saja** — token diambil dari server otomatis (tidak ditanam di
  APK). Password kamu ditukar jadi token feed lewat `/api/notify/login`.
- **Beranda** (native, Apple-like): ringkasan STC & KOALA — total user, aktif,
  login 24 jam, sesi aktif, mode jalan.
- **Notifikasi** (native): riwayat deposit/penarikan/aktivasi + tombol pantau
  mulai/berhenti. Notifikasi tetap muncul walau app ditutup (foreground service).
- **Dashboard**: seluruh dashboard web (admin.stcautotrade.id) tertanam & **auto-
  login** — semua fitur seperti bot @san103abot (user, saldo, deposit, penarikan,
  aktivasi, dll).
- **Setelan**: pantau latar on/off, notif tes, abaikan optimasi baterai, keluar.

Tanpa FCM → **kebal blokir push Google**. Poll server sendiri lewat HTTPS 443,
jadi jalan di jaringan mana pun HP berada.

## Pasang
Unduh APK dari **Releases** (repo publik):
`https://github.com/aryasis87/stc-notif-app/releases/latest`
Buka di HP → izinkan "pasang dari sumber ini" → Install. minSdk Android 8 (API 26).

> Kalau sebelumnya memasang versi lama dan update ditolak "signature berbeda",
> uninstall dulu yang lama, lalu pasang yang baru.

## Pakai
1. Buka app → **masukkan password** (`Aryasis87@`). Itu saja — tak perlu URL/token.
2. Izinkan **notifikasi** saat diminta.
3. Buka **Setelan → Abaikan optimasi baterai** (biar tak dibunuh sistem).
4. Selesai. Beranda, Notifikasi, dan Dashboard langsung siap.

## Keandalan (Android)
- Biarkan notifikasi tetap **"Memantau…"** hidup (jangan swipe-hapus).
- Sebagian HP (Xiaomi/Oppo/Vivo) perlu izin **Autostart** & kunci di recent apps.
- Reboot → app nyala lagi otomatis kalau tadinya aktif.

## Build sendiri
Buka folder ini di **Android Studio** → Gradle sync → Run/Build APK.
Toolchain: AGP 8.10.1, Kotlin 2.0.21, Gradle 8.14.3, Compose BOM 2024.09.00,
compileSdk 35 / minSdk 26 / targetSdk 34. Keystore rilis (`*.jks` +
`keystore.properties`) tidak ikut di repo — buat sendiri untuk build release.

## Keamanan
- Token **tidak** ada di APK — hanya didapat setelah password benar.
- Password dipakai lokal (app-private) untuk auto-login dashboard.
- Repo publik, tapi **tanpa** token/keystore/kredensial apa pun.
