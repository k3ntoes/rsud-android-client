package my.id.kentoes.rsudajibarangapp.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.model.TokenRefreshHandler;

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
public final class TokenAuthenticator_Factory implements Factory<TokenAuthenticator> {
  private final Provider<TokenRefreshHandler> refreshHandlerProvider;

  private TokenAuthenticator_Factory(Provider<TokenRefreshHandler> refreshHandlerProvider) {
    this.refreshHandlerProvider = refreshHandlerProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return newInstance(refreshHandlerProvider);
  }

  public static TokenAuthenticator_Factory create(
      Provider<TokenRefreshHandler> refreshHandlerProvider) {
    return new TokenAuthenticator_Factory(refreshHandlerProvider);
  }

  public static TokenAuthenticator newInstance(
      javax.inject.Provider<TokenRefreshHandler> refreshHandlerProvider) {
    return new TokenAuthenticator(refreshHandlerProvider);
  }
}
