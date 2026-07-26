package my.id.kentoes.rsudajibarangapp.core.network;

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
public final class NetworkConnectivityObserver_Factory implements Factory<NetworkConnectivityObserver> {
  private final Provider<Context> contextProvider;

  private NetworkConnectivityObserver_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkConnectivityObserver get() {
    return newInstance(contextProvider.get());
  }

  public static NetworkConnectivityObserver_Factory create(Provider<Context> contextProvider) {
    return new NetworkConnectivityObserver_Factory(contextProvider);
  }

  public static NetworkConnectivityObserver newInstance(Context context) {
    return new NetworkConnectivityObserver(context);
  }
}
