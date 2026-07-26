package my.id.kentoes.rsudajibarangapp.inspection;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver;

@ScopeMetadata
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
public final class DaftarDrafViewModel_Factory implements Factory<DaftarDrafViewModel> {
  private final Provider<InspectionRepository> repositoryProvider;

  private final Provider<NetworkConnectivityObserver> networkObserverProvider;

  private DaftarDrafViewModel_Factory(Provider<InspectionRepository> repositoryProvider,
      Provider<NetworkConnectivityObserver> networkObserverProvider) {
    this.repositoryProvider = repositoryProvider;
    this.networkObserverProvider = networkObserverProvider;
  }

  @Override
  public DaftarDrafViewModel get() {
    return newInstance(repositoryProvider.get(), networkObserverProvider.get());
  }

  public static DaftarDrafViewModel_Factory create(
      Provider<InspectionRepository> repositoryProvider,
      Provider<NetworkConnectivityObserver> networkObserverProvider) {
    return new DaftarDrafViewModel_Factory(repositoryProvider, networkObserverProvider);
  }

  public static DaftarDrafViewModel newInstance(InspectionRepository repository,
      NetworkConnectivityObserver networkObserver) {
    return new DaftarDrafViewModel(repository, networkObserver);
  }
}
