# AGENTS.md — Tasirin VPN Rotasi

Aturan kerja repo ini. Bila aturan di sini bertabrakan dengan instruksi
langsung owner (tasirin1), instruksi owner yang menang.

## Identitas proyek

- Aplikasi VPN Android open source dengan **rotasi server otomatis**,
  ditulis **dari awal dengan kode orisinal** (keputusan owner 2026-11-21:
  "Ulang dari awal, aplikasi bikinan sendiri"). Percobaan fork ics-openvpn
  diarsipkan di repo terpisah `tasirin-vpn-rotasi-fork` — jangan diambil
  lagi tanpa kata owner.
- Paket aplikasi: `id.tasirin.vpnrotasi`. Bahasa: Kotlin. UI: Bahasa
  Indonesia, tema **terang biru–putih** (seragam Download Manager dan
  Disc Server).
- Mesin VPN: aplikasi ini TIDAK menulis protokol VPN sendiri. Sambungan
  dikerjakan **IKEv2 bawaan Android** lewat `VpnManager` /
  `Ikev2VpnProfile` (API 30+). Yang orisinal di sini: daftar server,
  keputusan rotasi, tes kecepatan, dan seluruh tampilan.
  Dukungan OpenVPN (.ovpn) adalah tahap terpisah — hanya bila owner
  memintanya lagi, dan caranya diputuskan saat itu.

## Desain yang SUDAH DIPUTUSKAN owner (2026-11-21) — jangan diubah diam-diam

1. Rotasi = ganti server **otomatis TANPA memutus VPN dari sisi HP**: wadah
   VPN tetap nyala, profil server di belakangnya yang diganti. Jeda lalu
   lintas singkat (±1–3 detik) saat jabat tangan ulang diterima.
   **BUKAN multi-hop berlapis** — yang dimaksud owner "kayak Tor" hanyalah
   rotasi otomatisnya.
2. Server + user ID + password **diketik sendiri oleh pengguna di aplikasi**.
   Repo ini TIDAK BOLEH berisi daftar server, sandi, atau kredensial apa pun.
3. Ada pilihan **ganti tiap berapa menit** (diatur pengguna).
4. Tiap giliran dipilih server **TERCEPAT**, tapi wajib ikut **POLA
   LINGKARAN**: muter ke server berikutnya dalam daftar, sesudah yang
   terakhir balik ke pertama. Logikanya di `RotationPlanner` (murni,
   teruji unit test) — jangan ubah aturannya tanpa kata owner.
5. Fitur yang DIPILIH owner:
   - (1) tombol putus darurat / kill switch;
   - (2) server mati dilewati otomatis di putaran berikutnya;
   - (3) tes kecepatan diam-diam sebelum giliran ganti;
   - (5) hemat baterai saat layar mati lama;
   - (6) pilihan per aplikasi (split tunneling);
   - (8) simpan daftar server ke berkas untuk impor ulang (berkas disimpan
     pengguna sendiri, TIDAK di repo);
   - (9) peringatan bila sandi ditolak server;
   - (10) lingkaran bisa dibatasi satu negara.
6. Fitur yang TIDAK dipilih owner — jangan dipasang: (4) nyala otomatis
   saat HP hidup; (7) tombol cepat di layar utama.

## Keadaan tahap awal (v0.1.0) — jujur, jangan dilebih-lebihkan

- Rotasi berjalan selama aplikasi terbuka (pengatur waktu di layar);
  layanan latar supaya rotasi jalan saat aplikasi tertutup = tahap
  berikutnya.
- Tes kecepatan tahap awal = waktu resolusi alamat (`SpeedProbe`);
  pengukuran latensi UDP IKEv2 yang akurat = tahap berikutnya.
- Alur izin VPN sistem saat pertama menyambung (persetujuan pengguna di
  dialog sistem) perlu uji perangkat nyata; tangani hasilnya dengan jujur
  di pesan status, jangan dianggap pasti mulus.

## Aturan kerja

- **Dilarang commit secret**: daftar server, user ID, password, keystore,
  token. Pelanggaran aturan ini = temuan Kritikal.
- **Tanpa build lokal**: jangan install SDK/toolchain di mesin agen.
  Verifikasi lokal hanya baca/grep/diff ringan. Build penuh lewat GitHub
  Actions (workflow `Build`).
- Audit = baca-saja dulu; tiap temuan terverifikasi dibuatkan SATU issue
  sebelum patch. Perbaiki = maraton: satu temuan, satu commit akar masalah,
  build/CI gagal didahulukan.
- Commit gaya `type(scope): deskripsi` ASCII. UI Inggris tidak dipakai di
  repo ini; komentar kode Bahasa Indonesia.
- Satu maraton = satu versi rilis; komponen versi tidak mencapai 1000.
- Perubahan kode wajib entri CHANGELOG (bagian `[Unreleased]`).
