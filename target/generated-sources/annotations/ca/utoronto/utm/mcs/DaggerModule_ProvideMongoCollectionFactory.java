// Generated by Dagger (https://google.github.io/dagger).
package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DaggerModule_ProvideMongoCollectionFactory implements Factory<MongoCollection> {
  private final DaggerModule module;

  private final Provider<MongoDatabase> mongoDatabaseProvider;

  public DaggerModule_ProvideMongoCollectionFactory(
      DaggerModule module, Provider<MongoDatabase> mongoDatabaseProvider) {
    this.module = module;
    this.mongoDatabaseProvider = mongoDatabaseProvider;
  }

  @Override
  public MongoCollection get() {
    return provideInstance(module, mongoDatabaseProvider);
  }

  public static MongoCollection provideInstance(
      DaggerModule module, Provider<MongoDatabase> mongoDatabaseProvider) {
    return proxyProvideMongoCollection(module, mongoDatabaseProvider.get());
  }

  public static DaggerModule_ProvideMongoCollectionFactory create(
      DaggerModule module, Provider<MongoDatabase> mongoDatabaseProvider) {
    return new DaggerModule_ProvideMongoCollectionFactory(module, mongoDatabaseProvider);
  }

  public static MongoCollection proxyProvideMongoCollection(
      DaggerModule instance, MongoDatabase mongoDatabase) {
    return Preconditions.checkNotNull(
        instance.provideMongoCollection(mongoDatabase),
        "Cannot return null from a non-@Nullable @Provides method");
  }
}
