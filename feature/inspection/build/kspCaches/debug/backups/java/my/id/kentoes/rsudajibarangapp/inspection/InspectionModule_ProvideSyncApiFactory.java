package my.id.kentoes.rsudajibarangapp.inspection;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi;
import retrofit2.Retrofit;

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
public final class InspectionModule_ProvideSyncApiFactory implements Factory<SyncApi> {
  private final Provider<Retrofit> retrofitProvider;

  private InspectionModule_ProvideSyncApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public SyncApi get() {
    return provideSyncApi(retrofitProvider.get());
  }

  public static InspectionModule_ProvideSyncApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new InspectionModule_ProvideSyncApiFactory(retrofitProvider);
  }

  public static SyncApi provideSyncApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(InspectionModule.INSTANCE.provideSyncApi(retrofit));
  }
}
