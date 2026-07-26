package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kompres gambar ke ukuran maksimal ~300KB.
 * Strategy: resize dimensi → turunkan kualitas JPEG hingga target tercapai.
 */
@Singleton
class ImageCompressor @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val MAX_SIZE_BYTES = 300 * 1024 // 300KB
        private const val MAX_DIMENSION = 1920 // max width/height
        private val COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG
    }

    /**
     * Kompres file foto dan simpan sebagai file baru.
     * Return path file hasil kompresi.
     */
    fun compress(originalPath: String): String {
        val originalFile = File(originalPath)
        if (!originalFile.exists()) return originalPath

        // Jika sudah di bawah target, pakai asli
        if (originalFile.length() <= MAX_SIZE_BYTES) return originalPath

        // Decode dengan sample size
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(originalPath, options)

        // Hitung sample size
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(originalPath, options) ?: return originalPath

        // Resize jika terlalu besar
        val scaledBitmap = if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
            val ratio = minOf(
                MAX_DIMENSION.toFloat() / bitmap.width,
                MAX_DIMENSION.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Simpan dengan kualitas menurun hingga target tercapai
        val compressedFile = createCompressedFile(originalPath)
        var quality = 85
        var attempt = 0
        val maxAttempts = 7

        do {
            FileOutputStream(compressedFile).use { out ->
                scaledBitmap.compress(COMPRESS_FORMAT, quality, out)
                out.flush()
            }
            quality -= 10
            attempt++
        } while (compressedFile.length() > MAX_SIZE_BYTES && attempt < maxAttempts)

        // Cleanup
        if (scaledBitmap != bitmap) scaledBitmap.recycle()
        bitmap.recycle()

        return compressedFile.absolutePath
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > MAX_DIMENSION || height / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun createCompressedFile(originalPath: String): File {
        val dir = File(context.cacheDir, "compressed_photos")
        if (!dir.exists()) dir.mkdirs()
        val name = File(originalPath).nameWithoutExtension
        return File.createTempFile("comp_${name}_", ".jpg", dir)
    }
}
