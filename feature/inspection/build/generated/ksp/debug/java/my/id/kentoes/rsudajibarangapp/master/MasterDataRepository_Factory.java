package my.id.kentoes.rsudajibarangapp.master;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao;
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi;

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
public final class MasterDataRepository_Factory implements Factory<MasterDataRepository> {
  private final Provider<MasterDataApi> masterDataApiProvider;

  private final Provider<MasterDataDao> masterDataDaoProvider;

  private MasterDataRepository_Factory(Provider<MasterDataApi> masterDataApiProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    this.masterDataApiProvider = masterDataApiProvider;
    this.masterDataDaoProvider = masterDataDaoProvider;
  }

  @Override
  public MasterDataRepository get() {
    return newInstance(masterDataApiProvider.get(), masterDataDaoProvider.get());
  }

  public static MasterDataRepository_Factory create(Provider<MasterDataApi> masterDataApiProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    return new MasterDataRepository_Factory(masterDataApiProvider, masterDataDaoProvider);
  }

  public static MasterDataRepository newInstance(MasterDataApi masterDataApi,
      MasterDataDao masterDataDao) {
    return new MasterDataRepository(masterDataApi, masterDataDao);
  }
}
