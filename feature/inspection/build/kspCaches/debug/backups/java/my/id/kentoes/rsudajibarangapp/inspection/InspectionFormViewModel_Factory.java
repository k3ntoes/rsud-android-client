package my.id.kentoes.rsudajibarangapp.inspection;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao;
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao;

@ScopeMetadata
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
public final class InspectionFormViewModel_Factory implements Factory<InspectionFormViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MasterDataDao> masterDataDaoProvider;

  private final Provider<DrafDao> drafDaoProvider;

  private final Provider<InspectionRepository> inspectionRepositoryProvider;

  private InspectionFormViewModel_Factory(Provider<Context> contextProvider,
      Provider<MasterDataDao> masterDataDaoProvider, Provider<DrafDao> drafDaoProvider,
      Provider<InspectionRepository> inspectionRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.masterDataDaoProvider = masterDataDaoProvider;
    this.drafDaoProvider = drafDaoProvider;
    this.inspectionRepositoryProvider = inspectionRepositoryProvider;
  }

  @Override
  public InspectionFormViewModel get() {
    return newInstance(contextProvider.get(), masterDataDaoProvider.get(), drafDaoProvider.get(), inspectionRepositoryProvider.get());
  }

  public static InspectionFormViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MasterDataDao> masterDataDaoProvider, Provider<DrafDao> drafDaoProvider,
      Provider<InspectionRepository> inspectionRepositoryProvider) {
    return new InspectionFormViewModel_Factory(contextProvider, masterDataDaoProvider, drafDaoProvider, inspectionRepositoryProvider);
  }

  public static InspectionFormViewModel newInstance(Context context, MasterDataDao masterDataDao,
      DrafDao drafDao, InspectionRepository inspectionRepository) {
    return new InspectionFormViewModel(context, masterDataDao, drafDao, inspectionRepository);
  }
}
