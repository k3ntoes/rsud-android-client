package my.id.kentoes.rsudajibarangapp.inspection

/**
 * State per item dalam form inspeksi.
 * - skor: -1=belum, 0=Berisiko, 1=Minor, 2=Sesuai
 * - fotoPaths: daftar path file foto (mendukung multi-foto)
 * - catatan: catatan opsional
 *
 * isValid: true jika skor sudah diisi, dan jika skor=0 minimal ada 1 foto.
 */
data class ItemState(
    val itemId: Long,
    val nama: String = "",
    val kategori: String = "",
    val deskripsi: String? = null,
    val skor: Int = -1,
    val fotoPaths: List<String> = emptyList(),
    val catatan: String? = null
) {
    /** Apakah skor sudah diisi */
    val isScored: Boolean get() = skor in 0..2

    /** Apakah item valid untuk dikirim:
     *  - skor sudah diisi (0, 1, atau 2)
     *  - Jika skor 0 (Berisiko), wajib minimal 1 foto */
    val isValid: Boolean get() = when {
        !isScored -> false
        skor == 0 -> fotoPaths.isNotEmpty()
        else -> true
    }
}
