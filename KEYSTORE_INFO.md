# Panduan Rilis & Signature Key LacakPaket

Dokumen ini berisi informasi sertifikat rilis dan panduan untuk mengunggah aplikasi ke **Google Play Store**.

---

## 1. Informasi Keystore Rilis (Upload Key)

* **Nama File:** `my-upload-key.jks`
* **File Base64:** `my-upload-key.jks.base64` (untuk GitHub Secrets)
* **Alias Kunci:** `upload`
* **Keystore Password:** `lacakpaket123`
* **Key Password:** `lacakpaket123`
* **Masa Berlaku:** 10.000 hari (hingga tahun 2054)
* **Algoritma:** RSA 2048-bit (SHA384withRSA)

### Sidik Jari Sertifikat (Certificate Fingerprints):
* **SHA-1:** `BA:C3:38:9B:B2:69:D0:51:1B:D6:B5:37:08:0B:17:71:4B:2B:7E:2F`
* **SHA-256:** `98:8B:AC:81:11:43:26:4A:54:6A:00:E4:EF:58:4E:2E:1D:AF:1B:E5:63:8F:73:54:4D:5A:21:BA:C5:43:25:33`

---

## 2. Alur Otomatisasi GitHub Actions (`.github/workflows/build.yml`)

Workflow ini akan secara otomatis:
1. Membaca rahasia lingkungan dan API key.
2. Mempersiapkan keystore rilis secara otomatis (baik dari GitHub Secrets maupun fallback lokal).
3. Membangun **Release APK** (`app-release.apk`).
4. Membangun **Release AAB** (`app-release.aab`) untuk Google Play Store.
5. Mengunggah kedua file sebagai artifact siap unduh di tab **Actions** repository GitHub Anda.

### Rahasia di GitHub Secrets (Opsional / Dianjurkan):
Jika Anda mengunggah kode ke GitHub, Anda bisa menambahkan rahasia di **Settings > Secrets and variables > Actions**:
* `KEYSTORE_BASE64`: Isi teks dari file `my-upload-key.jks.base64`
* `STORE_PASSWORD`: `lacakpaket123`
* `KEY_PASSWORD`: `lacakpaket123`

*(Catatan: Jika secrets belum diisi di GitHub, workflow telah dirancang dengan fallback cerdas sehingga tetap berhasil membuild dan menandatangani file tanpa error).*

---

## 3. Langkah Upload ke Google Play Console

1. Buka [Google Play Console](https://play.google.com/console).
2. Pilih aplikasi Anda (atau buat aplikasi baru bernama **LacakPaket**).
3. Masuk ke menu **Rilis (Release)** > **Produksi (Production)** atau **Pengujian Internal (Internal Testing)**.
4. Klik **Buat Rilis Baru (Create new release)**.
5. Aktifkan **Play App Signing** (Google Play akan menggunakan signature key upload ini untuk memverifikasi paket Anda).
6. Unggah file **AAB** (`app-release.aab` yang berlokasi di `app/build/outputs/bundle/release/app-release.aab`).
7. Tulis catatan rilis (Release Notes) dan simpan rilis Anda.
