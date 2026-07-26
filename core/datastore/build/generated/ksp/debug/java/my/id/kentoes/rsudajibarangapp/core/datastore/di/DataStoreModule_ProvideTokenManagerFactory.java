package my.id.kentoes.rsudajibarangapp.core.datastore.di;

import androidx.datastore.core.DataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenData;
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager;

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
public final class DataStoreModule_ProvideTokenManagerFactory implements Factory<TokenManager> {
  private final Provider<DataStore<TokenData>> dataStoreProvider;

  private DataStoreModule_ProvideTokenManagerFactory(
      Provider<DataStore<TokenData>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public TokenManager get() {
    return provideTokenManager(dataStoreProvider.get());
  }

  public static DataStoreModule_ProvideTokenManagerFactory create(
      Provider<DataStore<TokenData>> dataStoreProvider) {
    return new DataStoreModule_ProvideTokenManagerFactory(dataStoreProvider);
  }

  public static TokenManager provideTokenManager(DataStore<TokenData> dataStore) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideTokenManager(dataStore));
  }
}
