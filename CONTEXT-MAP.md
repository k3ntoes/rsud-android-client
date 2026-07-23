# Context Map — RSUD Ajibarang App

Aplikasi Android *offline-first* untuk inspeksi kebersihan rumah sakit oleh Petugas Kebersihan. Empat konteks domain yang saling berhubungan.

## Contexts

| Context | Location | Description |
|---------|----------|-------------|
| [Auth](./app/src/main/java/my/id/kentoes/rsudajibarangapp/auth/CONTEXT.md) | `auth/` | Token management, login/logout, session handling |
| [Inspections](./app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md) | `inspections/` | Form inspeksi dinamis, skoring, validasi bukti foto |
| [Sync](./app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/CONTEXT.md) | `sync/` | WorkManager, offline-first sync, upload dua langkah |
| [Core](./app/src/main/java/my/id/kentoes/rsudajibarangapp/core/CONTEXT.md) | `core/` | App foundation, DI, shared models, base types |

## Relationships

- **Auth → Core**: Auth menyediakan `AuthState` yang dikonsumsi oleh Core (navigation, DI scope)
- **Inspections → Auth**: Setiap request API inspeksi membutuhkan Access Token dari Auth
- **Inspections → Core**: Menggunakan shared models dan base types dari Core
- **Inspections → Sync**: Data inspeksi yang disimpan lokal akan diproses oleh Sync untuk dikirim ke server
- **Sync → Auth**: WorkManager menggunakan Access Token milik sesi terakhir yang tersimpan
- **Sync → Core**: Menggunakan base networking dan dependency injection dari Core
