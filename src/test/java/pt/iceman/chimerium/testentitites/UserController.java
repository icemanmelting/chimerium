package pt.iceman.chimerium.testentitites;

import pt.iceman.chimerium.Controller;
import pt.iceman.chimerium.ControllerTest;
import pt.iceman.chimerium.Request;
import pt.iceman.chimerium.annotations.GET;
import pt.iceman.chimerium.annotations.POST;
import pt.iceman.chimerium.config.GeneralConfig;
import pt.iceman.chimerium.db.DataRepository;
import pt.iceman.chimerium.response.Response;

public class UserController extends Controller<User> {
    private DataRepository<User> repository = getRepository();
    private User user1 = new User("Fabio", "Francisco");
    private User user2 = new User("Carlos", "Monteiro");

    public UserController(String context, GeneralConfig config) {
        super(context, config);
    }

    @GET(route = "/users/:id/:age", args = {String.class, Integer.class}, description = "")
    public Response get(Request request, String id, Integer v) {
        return new Response(200, user1);
    }

    @POST(route = "/users/:id", args = String.class, description = "", bodyType = User.class)
    public Response post(Request request, String id) {
        System.out.println("");
        return new Response(200, user1);
    }
}