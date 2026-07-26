package my.id.kentoes.rsudajibarangapp.core.datastore;

import androidx.datastore.core.DataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class TokenManager_Factory implements Factory<TokenManager> {
  private final Provider<DataStore<TokenData>> dataStoreProvider;

  private TokenManager_Factory(Provider<DataStore<TokenData>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public TokenManager get() {
    return newInstance(dataStoreProvider.get());
  }

  public static TokenManager_Factory create(Provider<DataStore<TokenData>> dataStoreProvider) {
    return new TokenManager_Factory(dataStoreProvider);
  }

  public static TokenManager newInstance(DataStore<TokenData> dataStore) {
    return new TokenManager(dataStore);
  }
}
