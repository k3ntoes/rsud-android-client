package my.id.kentoes.rsudajibarangapp.core.datastore.di;

import android.content.Context;
import androidx.datastore.core.DataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenData;
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenEncryption;

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
public final class DataStoreModule_ProvideTokenDataStoreFactory implements Factory<DataStore<TokenData>> {
  private final Provider<Context> contextProvider;

  private final Provider<TokenEncryption> encryptionProvider;

  private DataStoreModule_ProvideTokenDataStoreFactory(Provider<Context> contextProvider,
      Provider<TokenEncryption> encryptionProvider) {
    this.contextProvider = contextProvider;
    this.encryptionProvider = encryptionProvider;
  }

  @Override
  public DataStore<TokenData> get() {
    return provideTokenDataStore(contextProvider.get(), encryptionProvider.get());
  }

  public static DataStoreModule_ProvideTokenDataStoreFactory create(
      Provider<Context> contextProvider, Provider<TokenEncryption> encryptionProvider) {
    return new DataStoreModule_ProvideTokenDataStoreFactory(contextProvider, encryptionProvider);
  }

  public static DataStore<TokenData> provideTokenDataStore(Context context,
      TokenEncryption encryption) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideTokenDataStore(context, encryption));
  }
}
