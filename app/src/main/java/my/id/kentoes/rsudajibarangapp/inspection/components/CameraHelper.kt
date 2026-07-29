package my.id.kentoes.rsudajibarangapp.inspection.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Buat URI file foto temporer untuk kamera capture */
fun createTempPhotoUri(context: Context): Uri {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val dir = File(context.getExternalFilesDir(null), "photos")
    if (!dir.exists()) dir.mkdirs()
    val photoFile = File.createTempFile("IMG_${timestamp}_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
}
