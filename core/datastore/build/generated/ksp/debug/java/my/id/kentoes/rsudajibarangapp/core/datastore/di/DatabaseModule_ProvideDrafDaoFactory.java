package my.id.kentoes.rsudajibarangapp.core.datastore.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.AppDatabase;
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao;

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
public final class DatabaseModule_ProvideDrafDaoFactory implements Factory<DrafDao> {
  private final Provider<AppDatabase> dbProvider;

  private DatabaseModule_ProvideDrafDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DrafDao get() {
    return provideDrafDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDrafDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideDrafDaoFactory(dbProvider);
  }

  public static DrafDao provideDrafDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDrafDao(db));
  }
}
