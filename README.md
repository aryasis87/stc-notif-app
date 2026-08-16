# STC Notif — aplikasi penerima notifikasi (Android/Kotlin)

Aplikasi kecil yang **hanya menerima notifikasi realtime** (deposit, penarikan,
permintaan aktivasi) dari `admin.stcautotrade.id` — **tanpa Google/FCM**. Aplikasi
menarik (poll) endpoint server sendiri lewat HTTPS, jadi **kebal blokir push
Google** yang bikin web push gagal. Notifikasi muncul walau app & web tak dibuka.

## Cara kerja
- Foreground service jalan di latar (ada notifikasi tetap "Memantau…").
- Tiap ±20 dtk cek `GET /api/notify/feed?token=…&since=<cursor>`.
- Event baru → notifikasi (bunyi + getar). Cursor disimpan lokal (tak dobel).
- Penarikan diisi server via scan berkala 5 mnt (otomatis, hanya saat app aktif).

## Build (Android Studio)
1. **File → Open** → pilih folder `notif-app` ini.
2. Tunggu **Gradle Sync** selesai (Android Studio unduh Gradle 8.7 + AGP 8.5.2 +
   SDK yang perlu; ia juga membuat `local.properties` & wrapper otomatis. Kalau
   diminta soal Gradle wrapper, terima saja / **File → Sync Project with Gradle Files**).
3. Colok HP (USB debugging) atau pakai emulator → tombol **Run ▶**.
   Atau bikin APK: **Build → Build App Bundle(s)/APK(s) → Build APK(s)**, lalu
   pasang file `app/build/outputs/apk/debug/app-debug.apk` ke HP.

Syarat: Android Studio terbaru (JDK 17 sudah bawaan). minSdk = Android 8 (API 26).

## Pemakaian (di HP)
1. Buka app **STC Notif**.
2. **URL server**: sudah terisi `https://admin.stcautotrade.id`.
3. **Token**: tempel `NOTIFY_TOKEN` dari server (`~/webadmin-stc/.env` di VPS).
   Token TIDAK disertakan di repo ini demi keamanan — ambil dari server, atau
   dari catatan pribadimu. Kalau diganti di server, ganti juga di app.
4. Tekan **Mulai** → izinkan **notifikasi** saat diminta.
5. Tekan **Kirim notif tes** untuk memastikan notifikasi tampil.
6. Tekan **Abaikan optimasi baterai** → izinkan, biar service tak dibunuh sistem.

Selesai. Begitu ada deposit / penarikan / permintaan aktivasi baru, HP berbunyi
dengan notifikasinya — tanpa perlu buka web.

## Keandalan (penting di HP Android)
- Biarkan notifikasi tetap **"Memantau…"** hidup (itu wajib agar Android tak
  membunuh app). Jangan swipe-hapus.
- **Abaikan optimasi baterai** untuk app ini (tombol tersedia di dalam app).
- Sebagian HP (Xiaomi/Oppo/Vivo/Samsung) punya "auto-start"/penghemat baterai
  agresif — izinkan **Autostart** & kunci app di recent apps bila perlu.
- Reboot HP → app nyala lagi otomatis (kalau tadinya aktif).

## Keamanan
- Token = kunci akses feed. Jangan dibagikan. Ganti dengan menaruh
  `NOTIFY_TOKEN` baru di `.env` server lalu perbarui di app.
- Feed hanya membaca ringkasan event (judul + isi), tak ada kredensial user.

## Ganti nama/ikon app
- Nama: `app/src/main/res/values/strings.xml` (`app_name`).
- Ikon: `app/src/main/res/drawable/ic_launcher_foreground.xml` + warna latar di
  `values/colors.xml` (`ic_launcher_background`).
