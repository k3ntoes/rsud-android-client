package my.id.kentoes.rsudajibarangapp.core.datastore.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager;
import my.id.kentoes.rsudajibarangapp.core.model.TokenProvider;

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
public final class DataStoreModule_ProvideTokenProviderFactory implements Factory<TokenProvider> {
  private final Provider<TokenManager> tokenManagerProvider;

  private DataStoreModule_ProvideTokenProviderFactory(Provider<TokenManager> tokenManagerProvider) {
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public TokenProvider get() {
    return provideTokenProvider(tokenManagerProvider.get());
  }

  public static DataStoreModule_ProvideTokenProviderFactory create(
      Provider<TokenManager> tokenManagerProvider) {
    return new DataStoreModule_ProvideTokenProviderFactory(tokenManagerProvider);
  }

  public static TokenProvider provideTokenProvider(TokenManager tokenManager) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideTokenProvider(tokenManager));
  }
}
