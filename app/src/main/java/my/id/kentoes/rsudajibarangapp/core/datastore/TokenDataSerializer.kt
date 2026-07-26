package my.id.kentoes.rsudajibarangapp.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer untuk [TokenData] — digunakan oleh DataStore.
 * Data akan dienkripsi otomatis oleh AeadSerializer wrapper di DataStoreModule.
 */
object TokenDataSerializer : Serializer<TokenData> {

    override val defaultValue: TokenData = TokenData()

    override suspend fun readFrom(input: InputStream): TokenData {
        return try {
            Json.decodeFromString(
                TokenData.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Gagal membaca TokenData", e)
        }
    }

    override suspend fun writeTo(t: TokenData, output: OutputStream) {
        output.write(
            Json.encodeToString(TokenData.serializer(), t).toByteArray()
        )
    }
}
