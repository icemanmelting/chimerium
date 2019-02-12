package pt.iceman.chimerium.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import pt.iceman.chimerium.config.DbConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBRepository<T> extends DataRepository<T> {
    private final CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings
            .getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    private MongoDatabase mongoDatabase;
    private MongoCollection<T> collection;

    public MongoDBRepository(DbConfig dbConfig, Class<T> genericClass) {
        super(dbConfig);
        this.mongoDatabase = MongoClients.create("mongodb://" + dbConfig.getHost() + ":" + dbConfig.getPort())
                                         .getDatabase(dbConfig.getDb())
                                         .withCodecRegistry(pojoCodecRegistry);
        this.collection = mongoDatabase.getCollection(genericClass.getSimpleName(), genericClass);
    }

    private Optional<T> convertFind(String operation, String column, Object value) {
        T t = null;

        switch (operation) {
            case "=":
                t = this.collection.find(Filters.eq(column, value)).first();
                break;
            case ">":
                t = this.collection.find(Filters.gt(column, value)).first();
                break;
            case ">=":
                t = this.collection.find(Filters.gte(column, value)).first();
                break;
            case "<":
                t = this.collection.find(Filters.lt(column, value)).first();
                break;
            case "<=":
                t = this.collection.find(Filters.lte(column, value)).first();
                break;
        }

        return Optional.ofNullable(t);
    }

    @Override
    public Optional<T> findOne(String operation, String column, Object value) {
        return convertFind(operation, column, value);
    }

    @Override
    public List<T> findAll() {
        List<T> ts = new ArrayList<>();

        this.collection.find().forEach(new Consumer<T>() {
            @Override
            public void accept(T t) {
                ts.add(t);
            }
        });

        return ts;
    }

    @Override
    public T insertOne(T t) {
        this.collection.insertOne(t);
        return t;
    }

    @Override
    public List<T> insertMany(List<T> ts) {
        this.collection.insertMany(ts);
        return ts;
    }

    private void convertDeleteOne(String operation, String column, Object value) {
        switch (operation) {
            case "=":
                this.collection.deleteOne(Filters.eq(column, value));
                break;
            case ">":
                this.collection.deleteOne(Filters.gt(column, value));
                break;
            case ">=":
                this.collection.deleteOne(Filters.gte(column, value));
                break;
            case "<":
                this.collection.deleteOne(Filters.lt(column, value));
                break;
            case "<=":
                this.collection.deleteOne(Filters.lte(column, value));
                break;
        }
    }

    @Override
    public void deleteOne(String operation, String column, Object value) {
        convertDeleteOne(operation, column, value);
    }

    private void convertDeleteMany(String operation, String column, Object value) {
        switch (operation) {
            case "=":
                this.collection.deleteMany(Filters.eq(column, value));
                break;
            case ">":
                this.collection.deleteMany(Filters.gt(column, value));
                break;
            case ">=":
                this.collection.deleteMany(Filters.gte(column, value));
                break;
            case "<":
                this.collection.deleteMany(Filters.lt(column, value));
                break;
            case "<=":
                this.collection.deleteMany(Filters.lte(column, value));
                break;
        }
    }

    @Override
    public void deleteMany(String operation, String column, Object value) {
     convertDeleteMany(operation, column, value);
    }

    @Override
    public T update(T t) {
        return super.update(t);
    }
}
