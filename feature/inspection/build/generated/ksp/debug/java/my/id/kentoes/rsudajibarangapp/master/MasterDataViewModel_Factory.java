package my.id.kentoes.rsudajibarangapp.master;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class MasterDataViewModel_Factory implements Factory<MasterDataViewModel> {
  private final Provider<MasterDataRepository> repositoryProvider;

  private MasterDataViewModel_Factory(Provider<MasterDataRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public MasterDataViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static MasterDataViewModel_Factory create(
      Provider<MasterDataRepository> repositoryProvider) {
    return new MasterDataViewModel_Factory(repositoryProvider);
  }

  public static MasterDataViewModel newInstance(MasterDataRepository repository) {
    return new MasterDataViewModel(repository);
  }
}
