package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper untuk membuat photo file temporer untuk kamera.
 * FileProvider authority: ${applicationId}.fileprovider
 */
object CameraHelper {

    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /** Buat URI file foto temporer untuk kamera capture */
    fun createPhotoUri(context: Context): Uri {
        val photoFile = createPhotoFile(context)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    /** Buat file foto di external files directory */
    private fun createPhotoFile(context: Context): File {
        val timestamp = timestampFormat.format(Date())
        val dir = File(context.getExternalFilesDir(null), "photos")
        if (!dir.exists()) dir.mkdirs()
        return File.createTempFile("IMG_${timestamp}_", ".jpg", dir)
    }
}
