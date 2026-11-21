# Tasirin VPN Rotasi

Aplikasi VPN Android open source dengan **rotasi server otomatis**, ditulis
dari awal dengan kode orisinal.

VPN tetap nyala di HP, server di belakangnya berganti sendiri mengikuti
**pola lingkaran** — tiap giliran dipilih yang tercepat dari server
berikutnya dalam daftar, dengan interval yang diatur sendiri oleh pengguna.

## Cara pakai

1. Tambah server satu per satu: nama, alamat server, negara, user ID, dan
   password — **diketik sendiri oleh pengguna**. Aplikasi ini tidak membawa
   daftar server bawaan apa pun.
2. Ketuk server lalu **Hubungkan**, atau nyalakan **Rotasi otomatis** dan
   isi intervalnya (menit).

## Mesin VPN

Sambungan dikerjakan IKEv2 bawaan Android (`VpnManager`, butuh Android 11 /
API 30 ke atas untuk menyambung). Aplikasi ini tidak menulis protokol VPN
sendiri; yang diatur aplikasi adalah daftar server dan keputusan rotasinya.
Di bawah Android 11, aplikasi tetap bisa mengelola daftar server.

## Keadaan proyek

Tahap awal (v0.1.0): daftar server, sambung/putus IKEv2, rotasi lingkaran +
tercepat selama aplikasi terbuka, server mati dilewati. Rencana tahap
berikutnya dan aturan kerja ada di `AGENTS.md`.

## Lisensi

Kode orisinal proyek ini dirilis sebagai open source oleh owner
(tasirin1). Komponen pihak ketiga (AndroidX, Material) mengikuti lisensi
masing-masing.
