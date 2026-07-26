package my.id.kentoes.rsudajibarangapp.core.datastore.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.AppDatabase;
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideMasterDataDaoFactory implements Factory<MasterDataDao> {
  private final Provider<AppDatabase> dbProvider;

  private DatabaseModule_ProvideMasterDataDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MasterDataDao get() {
    return provideMasterDataDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMasterDataDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideMasterDataDaoFactory(dbProvider);
  }

  public static MasterDataDao provideMasterDataDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMasterDataDao(db));
  }
}
