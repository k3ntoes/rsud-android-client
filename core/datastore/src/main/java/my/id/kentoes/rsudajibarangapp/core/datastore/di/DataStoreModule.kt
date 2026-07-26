package my.id.kentoes.rsudajibarangapp.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.tink.AeadSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenData
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenDataSerializer
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenEncryption
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import my.id.kentoes.rsudajibarangapp.core.model.TokenProvider
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context,
        encryption: TokenEncryption
    ): DataStore<TokenData> {
        val aeadSerializer = AeadSerializer(
            encryption.aead,
            TokenDataSerializer,
            ASSOCIATED_DATA.toByteArray()
        )
        return DataStoreFactory.create(aeadSerializer) {
            File(context.filesDir, "datastore/token_prefs")
        }
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        dataStore: DataStore<TokenData>
    ): TokenManager {
        return TokenManager(dataStore)
    }

    @Provides
    @Singleton
    fun provideTokenProvider(tokenManager: TokenManager): TokenProvider = tokenManager

    private const val ASSOCIATED_DATA = "token_prefs"
}
