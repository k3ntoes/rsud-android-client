package my.id.kentoes.rsudajibarangapp.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import my.id.kentoes.rsudajibarangapp.core.database.AppDatabase
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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
