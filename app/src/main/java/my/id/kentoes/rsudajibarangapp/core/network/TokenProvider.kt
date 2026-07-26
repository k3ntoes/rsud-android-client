package my.id.kentoes.rsudajibarangapp.core.network

/**
 * Kontrak untuk Penyedia Token (Access & Refresh).
 * Implementasi konkrit dikelola oleh Auth module.
 *
 * Lihat auth/CONTEXT.md:
 * - Access Token: JWT di header Authorization: Bearer
 * - Refresh Token: Token kedua disimpan terenkripsi
 */
interface TokenProvider {
    /** Ambil Access Token saat ini (bisa null jika belum login) */
    suspend fun getAccessToken(): String?

    /** Ambil Refresh Token (untuk proses refresh) */
    suspend fun getRefreshToken(): String?

    /** Refresh token: dapatkan Access Token baru via Refresh Token.
     *  Return true jika sukses, false jika gagal (Force Logout). */
    suspend fun refreshTokens(): Boolean

    /** Hapus semua token (Force Logout / Logout manual) */
    suspend fun clearTokens()
}
