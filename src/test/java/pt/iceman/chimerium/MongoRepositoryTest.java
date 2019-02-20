package pt.iceman.chimerium;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.iceman.chimerium.config.DbConfig;
import pt.iceman.chimerium.db.DataRepository;
import pt.iceman.chimerium.db.DataRepositoryFactory;
import pt.iceman.chimerium.testentitites.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("unchecked")
public class MongoRepositoryTest {
    private Gson gson = new Gson();
    private DbConfig dbConfig = new DbConfig("localhost", 27017, "mongoDB", "mongodb");
    private DataRepositoryFactory factory = new DataRepositoryFactory();
    private DataRepository<User> repository = factory.getDataRepository(dbConfig, User.class);
    private User user = new User("Fabio", "Francisco");

    public MongoRepositoryTest() throws IOException {
    }

    @Before
    public void clearDB() {
        System.out.println("Clearing DB");
        repository.delete("{ name: \"Fabio\"}");
    }

    @Test
    public void testInsertion() {
        repository.insertOne(user);

        Optional<User> userQueriedOpt = repository.findOne(gson.toJson(user));

        assertTrue(userQueriedOpt.isPresent());

        User userQueried = userQueriedOpt.get();

        assertEquals(user.getName(), userQueried.getName());
        assertEquals(user.getLastName(), userQueried.getLastName());
    }


    @Test
    public void testUpdate() {
        repository.insertOne(user);

        Optional<User> userQueriedOpt = repository.findOne(gson.toJson(user));

        assertTrue(userQueriedOpt.isPresent());

        User userQueried = userQueriedOpt.get();

        assertEquals(user.getName(), userQueried.getName());
        assertEquals(user.getLastName(), userQueried.getLastName());

        User updated = new User("Fabio", "Dias");

        repository.update(gson.toJson(user), gson.toJson(updated));

        userQueriedOpt = repository.findOne(gson.toJson(user));

        assertFalse(userQueriedOpt.isPresent());

        userQueriedOpt = repository.findOne(gson.toJson(updated));

        assertTrue(userQueriedOpt.isPresent());

        userQueried = userQueriedOpt.get();

        assertEquals(updated.getName(), userQueried.getName());
        assertEquals(updated.getLastName(), userQueried.getLastName());
    }

    @Test
    public void testDelete() {
        repository.insertOne(user);

        Optional<User> userQueriedOpt = repository.findOne(gson.toJson(user));

        assertTrue(userQueriedOpt.isPresent());

        User userQueried = userQueriedOpt.get();

        assertEquals(user.getName(), userQueried.getName());
        assertEquals(user.getLastName(), userQueried.getLastName());

        repository.delete(gson.toJson(user));

        userQueriedOpt = repository.findOne(gson.toJson(user));

        assertFalse(userQueriedOpt.isPresent());
    }

    @Test
    public void testGenericQuery() {
        repository.insertOne(user);
        List<User> users = repository.findMany("{ name: \"Fabio\"}");

        assertEquals(1, users.size());
    }
}
