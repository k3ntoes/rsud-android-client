package my.id.kentoes.rsudajibarangapp.dashboard;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao;
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao;

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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<DrafDao> drafDaoProvider;

  private final Provider<MasterDataDao> masterDataDaoProvider;

  private DashboardViewModel_Factory(Provider<DrafDao> drafDaoProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    this.drafDaoProvider = drafDaoProvider;
    this.masterDataDaoProvider = masterDataDaoProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(drafDaoProvider.get(), masterDataDaoProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<DrafDao> drafDaoProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    return new DashboardViewModel_Factory(drafDaoProvider, masterDataDaoProvider);
  }

  public static DashboardViewModel newInstance(DrafDao drafDao, MasterDataDao masterDataDao) {
    return new DashboardViewModel(drafDao, masterDataDao);
  }
}
