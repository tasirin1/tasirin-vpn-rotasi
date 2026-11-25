# Changelog

Semua perubahan penting proyek Tasirin VPN Rotasi dicatat di berkas ini.
Format mengikuti Keep a Changelog.

## [Unreleased]

## [0.1.1] - 2026-11-21

### Fixed

- Tampilan tertutup keyboard (laporan uji owner di Galaxy A05):
  `MainActivity` sekarang `adjustResize` — layar menyusut rapi di atas
  keyboard, kolom interval dan tombol tetap terlihat; formulir tambah
  server dibungkus ScrollView supaya semua kolom bisa digeser.

## [0.1.0] - 2026-11-21

### Added

- Proyek dimulai dari awal dengan kode orisinal (keputusan owner
  2026-11-21; percobaan fork diarsipkan di repo `tasirin-vpn-rotasi-fork`).
- Daftar server yang diketik sendiri pengguna (nama, alamat, negara,
  user ID, password), tersimpan di penyimpanan internal aplikasi.
- Sambung/putus VPN IKEv2 lewat VpnManager bawaan Android (API 30+).
- Rotasi otomatis pola lingkaran + tercepat (`RotationPlanner`, teruji
  unit test), interval diatur pengguna, server mati dilewati.
- Tes kecepatan tahap awal (waktu resolusi alamat server).
- Tema terang biru–putih, UI Bahasa Indonesia.
- Workflow CI `Build`: assembleDebug + unit test + artefak APK.
- Workflow `Release`: APK bertanda tangan kunci proyek + GitHub Release
  otomatis dari tag `v*`.

### Fixed

- CI: setup-android v3 gagal memasang paket warisan `tools` yang sudah
  dihapus dari repositori SDK — naik ke v4 + paket `platform-tools` saja.
- Compile: nama metode IKEv2 yang benar `setAuthUsernamePassword`
  (bukan `setUsernamePasswordAuthentication`) — build CI pertama merah di
  sini, sesudah diperbaiki CI hijau penuh (APK + 9 unit test).
