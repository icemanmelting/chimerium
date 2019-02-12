# Chimerium
A small framework for micro services creation and deployment.

## Usage
In order to use this library, it needs to first be checked out and installed locally on the local maven repo.

To create a new method handler or Controller, it is just a matter of extending a class with the `pt.iceman.chimerium.Controller` class.

```Java
    private class UsersController extends Controller {
        public UsersController(String context) {
            super(context);
        }

        @GET(route = "/users/:id/:age", args = {String.class, Integer.class}, description = "")
        public Response get(String id, Integer v) {
            User user1 = new User("Fabio", "Francisco");
            return new Response(200, user1);
        }

        @POST(route = "/users/:id", args = String.class, description = "", bodyType = User.class)
        public Response post(String id) {
            User user2 = new User("Carlos", "Monteiro");
            return new Response(200, user2);
        }
    }
```

Each method specified in the controller needs to be assigned one of the currently supported annotations (GET, POST, PUT, DELETE)
so that the controller knows which method is actually being called when you specify a route.

Each of annotation type, has 4 arguments that can be specified:

- route - the route used to call the specific method (arguments are specified by a string starting with `:`, in the case above
:id is the argument);

- args - a variable array that contains the types of each argument specified (for the get example, we have 2 arguments, a String and an Integer), default value is empty array;

- description - a small description of the function (to be used for automated documentation generation);

- bodyType (All but the GET type) - specifies what is the format(Class) of the body to be parsed, default value is Object.class.

## Starting the server

First select the server type that is to be used, in this case, the only one that is currently implemented is `pt.iceman.chimerium.server.SunServer`.
Then instantiate this server and pass the needed arguments to the constructor (port where the server will run, and the list of supported controllers). 

```Java
public class Main {
  public static void main(String[] args) {
        Controller ctrl = new UserController("/users");
        
        SunServer server = new SunServer(port, new ArrayList<Controller>() {{
            add(ctrl);
        }});
        
        server.start();
    }
}
```

# Future Improvements

Support for database connection and configuration loading, as micro services should be self contained, I chose MongoDB for the first implementation, as it is quite simple to implement.

