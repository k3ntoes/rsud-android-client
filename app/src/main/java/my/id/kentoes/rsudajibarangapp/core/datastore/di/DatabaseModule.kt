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
import my.id.kentoes.rsudajibarangapp.core.database.AppDatabase
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenData
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenDataSerializer
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenEncryption
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ── DataStore + Tink ──

    @Provides
    @Singleton
    fun provideTokenEncryption(
        @ApplicationContext context: Context
    ): TokenEncryption {
        return TokenEncryption(context)
    }

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context,
        encryption: TokenEncryption
    ): DataStore<TokenData> {
        val aeadSerializer = AeadSerializer(
            encryption.aead,
            TokenDataSerializer,
            "token_prefs".toByteArray()
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

    // ── Room Database ──

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.create(context)
    }

    @Provides
    @Singleton
    fun provideMasterDataDao(db: AppDatabase): MasterDataDao {
        return db.masterDataDao()
    }

    @Provides
    @Singleton
    fun provideDrafDao(db: AppDatabase): DrafDao {
        return db.drafDao()
    }
}
