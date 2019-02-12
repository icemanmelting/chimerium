package pt.iceman.chimerium.db;

import pt.iceman.chimerium.config.DbConfig;

public class DataRepositoryFactory<T> {
    public DataRepository<T> getDataRepository(DbConfig config, Class<T> genericClass) {
        String dbType = config.getType();

        if (dbType.equals("mongoDB")) {
            return new MongoDBRepository<T>(config, genericClass);
        }

        return null;
    }
}
