package my.id.kentoes.rsudajibarangapp.sync;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao;
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository;
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi;

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
public final class SyncManager_Factory implements Factory<SyncManager> {
  private final Provider<InspectionRepository> inspectionRepositoryProvider;

  private final Provider<DrafDao> drafDaoProvider;

  private final Provider<SyncApi> syncApiProvider;

  private final Provider<ImageCompressor> imageCompressorProvider;

  private SyncManager_Factory(Provider<InspectionRepository> inspectionRepositoryProvider,
      Provider<DrafDao> drafDaoProvider, Provider<SyncApi> syncApiProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    this.inspectionRepositoryProvider = inspectionRepositoryProvider;
    this.drafDaoProvider = drafDaoProvider;
    this.syncApiProvider = syncApiProvider;
    this.imageCompressorProvider = imageCompressorProvider;
  }

  @Override
  public SyncManager get() {
    return newInstance(inspectionRepositoryProvider.get(), drafDaoProvider.get(), syncApiProvider.get(), imageCompressorProvider.get());
  }

  public static SyncManager_Factory create(
      Provider<InspectionRepository> inspectionRepositoryProvider,
      Provider<DrafDao> drafDaoProvider, Provider<SyncApi> syncApiProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    return new SyncManager_Factory(inspectionRepositoryProvider, drafDaoProvider, syncApiProvider, imageCompressorProvider);
  }

  public static SyncManager newInstance(InspectionRepository inspectionRepository, DrafDao drafDao,
      SyncApi syncApi, ImageCompressor imageCompressor) {
    return new SyncManager(inspectionRepository, drafDao, syncApi, imageCompressor);
  }
}
