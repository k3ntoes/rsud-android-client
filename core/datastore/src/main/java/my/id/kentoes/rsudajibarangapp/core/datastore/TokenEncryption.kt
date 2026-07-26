package my.id.kentoes.rsudajibarangapp.core.datastore

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Penyedia [Aead] dari Tink dengan Android Keystore.
 *
 * Instance [Aead] digunakan oleh [AeadSerializer] untuk mengenkripsi
 * DataStore token secara transparan. Keyset disimpan di SharedPreferences
 * terenkripsi, dengan kunci master di Android Keystore (TEE/StrongBox).
 */
@Singleton
class TokenEncryption @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Instance AEAD untuk enkripsi/dekripsi — lazy, dibuat sekali */
    val aead: Aead by lazy { createAead() }

    private fun createAead(): Aead {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset_prefs", "tink_keyset")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://rsud_ajibarang_token_key")
            .build()
            .keysetHandle

        @Suppress("DEPRECATION")
        return keysetHandle.getPrimitive(Aead::class.java)
    }
}
