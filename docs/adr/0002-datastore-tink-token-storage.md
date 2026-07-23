# ADR-0002: Proto DataStore + Tink for Token Storage

**Status**: Accepted

Menggunakan Jetpack Proto DataStore dengan enkripsi Google Tink (`datastore-tink`) untuk menyimpan Access Token dan Refresh Token secara aman, menggantikan `EncryptedSharedPreferences` yang sudah deprecated.

## Context

Aplikasi perlu menyimpan JWT tokens (Access + Refresh) secara lokal. Token adalah aset kritis — jika bocor, pihak ketiga bisa mengakses API rumah sakit. Saat ini `EncryptedSharedPreferences` (dari library `security-crypto`) sudah di-deprecate oleh Google karena masalah performa (main-thread I/O) dan keyset corruption pada perangkat OEM tertentu.

## Considered Options

- **EncryptedSharedPreferences (ESP)**: Sederhana, tinggal pakai. Tapi deprecated, synchronous I/O, rawan korupsi keyset. Tidak direkomendasikan untuk proyek baru.
- **DataStore saja (tanpa enkripsi)**: Paling simpel, performa baik via Coroutines. Tapi token tersimpan sebagai plain text — tidak aman jika perangkat di-root atau backup tidak sengaja terekspos.
- **Proto DataStore + Google Tink (via `datastore-tink`) — dipilih**: Asinkron (tidak blocking main thread), enkripsi menggunakan AEAD dengan kunci di Android Keystore. Ini adalah rekomendasi resmi Google 2025-2026 melalui artifact `androidx.datastore:datastore-tink`.

## Consequences

- Setup lebih berat: perlu Google Tink + `AeadSerializer` + Proto schema.
- Keamanan token terjamin: AEAD encryption + Android Keystore (TEE/StrongBox jika didukung hardware).
- Migrasi dari ESP dimungkinkan via `SharedPreferencesMigration` callback jika di masa depan ada data legacy.
- Module `:core:datastore` menjadi dependen ke DataStore 1.3.0-alpha09 (masih alpha).
