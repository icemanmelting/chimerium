package pt.iceman.chimerium.db;

import com.google.gson.Gson;
import pt.iceman.chimerium.config.DbConfig;

import java.util.List;
import java.util.Optional;

public abstract class DataRepository<T> {
    private DbConfig dbConfig;
    private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public DataRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Optional<T> findOne(String queryArgs) {
        return null;
    }

    public List<T> findMany(String queryArgs) {
        return null;
    }

    public synchronized T insertOne(T t) {
        return null;
    }

    public synchronized List<T> insertMany(List<T> ts) {
        return null;
    }

    public synchronized void delete(String query) {
    }

    public synchronized void update(String... queries) {
    }

    public synchronized void execute(String query) {
    }

    public synchronized List<T> query(String query) {
        return null;
    }
}
