package pt.iceman.chimerium.db;

import com.google.gson.Gson;
import pt.iceman.chimerium.config.DbConfig;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class DataRepository<T> {
    private DbConfig dbConfig;
    private Class<T> persistentClass;
    private Gson gson;

    @SuppressWarnings("unchecked")
    public DataRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.gson = new Gson();
    }

    protected synchronized Gson getGson() {
        return this.gson;
    }

    public Optional<T> findOne(String queryArgs) {
        return null;
    }

    public List<T> findMany(String queryArgs) {
        return null;
    }

    public T insertOne(T t) {
        return null;
    }

    public List<T> insertMany(List<T> ts) {
        return null;
    }

    public void delete(String query) {
    }

    public void update(String... queries) {
    }
}
