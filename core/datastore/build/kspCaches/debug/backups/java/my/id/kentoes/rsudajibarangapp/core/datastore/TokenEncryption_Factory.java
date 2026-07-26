package my.id.kentoes.rsudajibarangapp.core.datastore;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class TokenEncryption_Factory implements Factory<TokenEncryption> {
  private final Provider<Context> contextProvider;

  private TokenEncryption_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TokenEncryption get() {
    return newInstance(contextProvider.get());
  }

  public static TokenEncryption_Factory create(Provider<Context> contextProvider) {
    return new TokenEncryption_Factory(contextProvider);
  }

  public static TokenEncryption newInstance(Context context) {
    return new TokenEncryption(context);
  }
}
