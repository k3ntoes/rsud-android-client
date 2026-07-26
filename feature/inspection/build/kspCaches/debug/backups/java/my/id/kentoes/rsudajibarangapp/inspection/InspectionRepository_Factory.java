package my.id.kentoes.rsudajibarangapp.inspection;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao;
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao;

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
public final class InspectionRepository_Factory implements Factory<InspectionRepository> {
  private final Provider<DrafDao> drafDaoProvider;

  private final Provider<MasterDataDao> masterDataDaoProvider;

  private InspectionRepository_Factory(Provider<DrafDao> drafDaoProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    this.drafDaoProvider = drafDaoProvider;
    this.masterDataDaoProvider = masterDataDaoProvider;
  }

  @Override
  public InspectionRepository get() {
    return newInstance(drafDaoProvider.get(), masterDataDaoProvider.get());
  }

  public static InspectionRepository_Factory create(Provider<DrafDao> drafDaoProvider,
      Provider<MasterDataDao> masterDataDaoProvider) {
    return new InspectionRepository_Factory(drafDaoProvider, masterDataDaoProvider);
  }

  public static InspectionRepository newInstance(DrafDao drafDao, MasterDataDao masterDataDao) {
    return new InspectionRepository(drafDao, masterDataDao);
  }
}
