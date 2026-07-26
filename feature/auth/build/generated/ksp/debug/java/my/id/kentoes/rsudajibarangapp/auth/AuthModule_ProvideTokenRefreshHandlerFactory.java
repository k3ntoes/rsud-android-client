package my.id.kentoes.rsudajibarangapp.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AuthModule_ProvideTokenRefreshHandlerFactory implements Factory<TokenRefreshHandler> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private AuthModule_ProvideTokenRefreshHandlerFactory(
      Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public TokenRefreshHandler get() {
    return provideTokenRefreshHandler(authRepositoryProvider.get());
  }

  public static AuthModule_ProvideTokenRefreshHandlerFactory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new AuthModule_ProvideTokenRefreshHandlerFactory(authRepositoryProvider);
  }

  public static TokenRefreshHandler provideTokenRefreshHandler(AuthRepository authRepository) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideTokenRefreshHandler(authRepository));
  }
}
