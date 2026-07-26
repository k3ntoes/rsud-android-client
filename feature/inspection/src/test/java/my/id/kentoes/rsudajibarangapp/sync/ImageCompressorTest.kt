package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ImageCompressorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var compressor: ImageCompressor

    @Before
    fun setup() {
        context = mockk<Context>(relaxed = true)
        every { context.cacheDir } returns tempFolder.root
        compressor = ImageCompressor(context)
    }

    @Test
    fun `compress returns original path when file does not exist`() {
        val result = compressor.compress("/nonexistent/photo.jpg")

        assertEquals("/nonexistent/photo.jpg", result)
    }

    @Test
    fun `compress returns original path when file already under max size`() {
        val file = tempFolder.newFile("small_photo.jpg")
        file.writeBytes(ByteArray(100 * 1024)) // 100KB < 300KB

        val result = compressor.compress(file.absolutePath)

        assertEquals(file.absolutePath, result)
    }

    @Test
    fun `compress does not create compressed directory for small file`() {
        val file = tempFolder.newFile("small_photo.jpg")
        file.writeBytes(ByteArray(100 * 1024))

        compressor.compress(file.absolutePath)

        // File < 300KB → returns early, no compression dir created
        val compressedDir = java.io.File(tempFolder.root, "compressed_photos")
        assertFalse(compressedDir.exists())
    }

    @Test
    fun `compress handles empty file`() {
        val file = tempFolder.newFile("empty_photo.jpg")

        val result = compressor.compress(file.absolutePath)

        assertEquals(file.absolutePath, result)
    }
}
