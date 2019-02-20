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
import pt.iceman.chimerium.config.GeneralConfig;
import pt.iceman.chimerium.handler.Controller;
import pt.iceman.chimerium.handler.SunServerHandler;
import pt.iceman.chimerium.server.SunServer;
import pt.iceman.chimerium.testentitites.User;
import pt.iceman.chimerium.testentitites.UserController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ControllerTest {
    private Gson gson = new Gson();
    private User user1 = new User("Fabio", "Francisco");
    private User user3 = new User("Fabio", "Dias");
    private User user2 = new User("Carlos", "Monteiro");
    private String configFromResource = IOUtils.toString(ControllerTest.class.getResourceAsStream("/config.json"), "UTF-8");
    private GeneralConfig config = gson.fromJson(configFromResource, GeneralConfig.class);
    private SunServerHandler ctrl = new UserController("/users", config);
    private SunServer server = new SunServer(config.getPort(), new ArrayList<SunServerHandler>() {{
        add(ctrl);
    }});

    public ControllerTest() throws IOException {
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
        HttpUriRequest request = new HttpGet("http://localhost:" + config.getPort() + "/users/whatever/1");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        User user = gson.fromJson(body, User.class);

        assertEquals(user1.getName(), user.getName());
        assertEquals(user1.getLastName(), user.getLastName());
    }

    @Test
    public void testGetWorngArgumentFormat() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + config.getPort() + "/users/whatever/asd");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(500, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        assertEquals("{\"apiMessage\":\"For input string: \\\"asd\\\"\"}", body);
    }

    @Test
    public void testPost() throws IOException {
        HttpUriRequest request = new HttpPost("http://localhost:" + config.getPort() + "/users/whatever");
        ((HttpPost) request).setEntity(new StringEntity(gson.toJson(user1), ContentType.APPLICATION_JSON));

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        User user = gson.fromJson(body, User.class);

        assertEquals(user1.getName(), user.getName());
        assertEquals(user1.getLastName(), user.getLastName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRouteNotImplemented() throws IOException {
        HttpUriRequest request = new HttpDelete("http://localhost:" + config.getPort() + "/users/whatever");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(404, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        HashMap<String, String> respbody = gson.fromJson(body, HashMap.class);

        assertEquals("No Route Found", respbody.get("apiMessage"));
    }


    @Test
    public void testGetNonExistingContext() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + config.getPort() + "/asdasd/whatever/1");

        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(404, statusCode);

        String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

        assertEquals("<h1>404 Not Found</h1>No context found for request", body);
    }
}
