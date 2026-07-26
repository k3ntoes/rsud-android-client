package my.id.kentoes.rsudajibarangapp.inspection;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi;
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
public final class InspectionModule_ProvideMasterDataApiFactory implements Factory<MasterDataApi> {
  private final Provider<Retrofit> retrofitProvider;

  private InspectionModule_ProvideMasterDataApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MasterDataApi get() {
    return provideMasterDataApi(retrofitProvider.get());
  }

  public static InspectionModule_ProvideMasterDataApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new InspectionModule_ProvideMasterDataApiFactory(retrofitProvider);
  }

  public static MasterDataApi provideMasterDataApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(InspectionModule.INSTANCE.provideMasterDataApi(retrofit));
  }
}
