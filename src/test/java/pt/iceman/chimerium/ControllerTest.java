package pt.iceman.chimerium;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.iceman.chimerium.annotations.GET;
import pt.iceman.chimerium.annotations.POST;
import pt.iceman.chimerium.response.Response;
import pt.iceman.chimerium.server.SunServer;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ControllerTest {
    private User user1 = new User("Fabio", "Francisco");
    private User user2 = new User("Carlos", "Monteiro");

    private Controller ctrl = new UsersController("/users");
    private int port = 8080;
    private SunServer server = new SunServer(port, new ArrayList<Controller>() {{
        add(ctrl);
    }});
    private Gson gson = new Gson();

    public ControllerTest() throws IOException {
    }

    private class User extends Model {
        private String name;
        private String lastName;

        User(String name, String lastName) {
            this.name = name;
            this.lastName = lastName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    private class UsersController extends Controller<User> {
        public UsersController(String context) {
            super(context);
        }

        @GET(route = "/users/:id/:age", args = {String.class, Integer.class}, description = "")
        public Response get(String id, Integer v) {

            return new Response(200, user1);
        }

        @POST(route = "/users/:id", args = String.class, description = "", bodyType = User.class)
        public Response post(String id) {
            System.out.println(getRequest().get());
            return new Response(200, user2);
        }
    }

    @Before
    public void setup() {
        server.startServer();
    }

    @After
    public void tearDown() {
        server.stopServer();
    }

    @Test
    public void testGet() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + port + "/users/whatever/1");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        User user = gson.fromJson(body, User.class);

        assertEquals(user1.name, user.name);
        assertEquals(user1.lastName, user.lastName);
    }

    @Test
    public void testPost() throws IOException {
        HttpUriRequest request = new HttpPost("http://localhost:" + port + "/users/whatever");
        ((HttpPost) request).setEntity(new StringEntity(gson.toJson(user1), ContentType.APPLICATION_JSON));

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        User user = gson.fromJson(body, User.class);

        assertEquals(user2.name, user.name);
        assertEquals(user2.lastName, user.lastName);
    }

    @Test
    public void testRouteNotImplemented() throws IOException {
        HttpUriRequest request = new HttpDelete("http://localhost:" + port + "/users/whatever");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(404, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Model model = gson.fromJson(body, Model.class);

        assertEquals("No Route Found", model.getApiMessage());
    }


    @Test
    public void testGetNonExistingContext() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + port + "/asdasd/whatever/1");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(404, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        assertEquals("<h1>404 Not Found</h1>No context found for request", body);
    }
}
