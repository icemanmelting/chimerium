package pt.iceman.chimerium.db;

import pt.iceman.chimerium.config.DbConfig;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class DataRepository<T> {
    private DbConfig dbConfig;
    private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public DataRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
//        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

//    public Class<T> getPersistentClass() {
//        return persistentClass;
//    }

    public Optional<T> findOne(String operator, String column, Object value) {
        return null;
    }

    public List<T> findAll() {
        return null;
    }

    public T insertOne(T t) {
        return null;
    }

    public List<T> insertMany(List<T> ts) {
        return null;
    }

    public void deleteOne(String operation, String column, Object Value) {
    }

    public void deleteMany(String operation, String column, Object Value) {
    }

    public T update(T t) {
        return null;
    }
}
