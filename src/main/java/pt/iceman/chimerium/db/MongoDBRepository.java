package pt.iceman.chimerium.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
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

    private FindIterable<T> queryMongo(BasicDBObject bson) {
        if (bson != null) {
            return this.collection.find(bson);
        } else {
            return this.collection.find();
        }
    }

    private void executeMongo(DbOperation operation, BasicDBObject... bson) {
        if (operation.equals(DbOperation.UPDATE)) {
            this.collection.updateOne(bson[0], bson[1], new UpdateOptions().upsert(true));
        } else if (operation.equals(DbOperation.DELETE)) {
            this.collection.deleteMany(bson[0]);
        }
    }


    @Override
    public Optional<T> findOne(String query) {
        BasicDBObject bson = getGson().fromJson(query, BasicDBObject.class);
        return Optional.ofNullable(queryMongo(bson).first());
    }

    @Override
    public List<T> findMany(String query) {
        BasicDBObject bson = getGson().fromJson(query, BasicDBObject.class);

        List<T> ts = new ArrayList<>();

        this.queryMongo(bson).forEach(new Consumer<T>() {
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


    @Override
    public void delete(String query) {
        BasicDBObject bson = getGson().fromJson(query, BasicDBObject.class);
        executeMongo(DbOperation.DELETE, bson);
    }


    @Override
    public void update(String... queries) {
        if (queries.length == 2) {
            BasicDBObject bson = getGson().fromJson(queries[0], BasicDBObject.class);
            BasicDBObject bson1 = getGson().fromJson("{$set:"+queries[1]+"}", BasicDBObject.class);

            executeMongo(DbOperation.UPDATE, bson, bson1);
        }
    }
}
