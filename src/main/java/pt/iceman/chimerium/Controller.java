package pt.iceman.chimerium;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import pt.iceman.chimerium.annotations.DELETE;
import pt.iceman.chimerium.annotations.GET;
import pt.iceman.chimerium.annotations.POST;
import pt.iceman.chimerium.annotations.PUT;
import pt.iceman.chimerium.config.GeneralConfig;
import pt.iceman.chimerium.db.DataRepository;
import pt.iceman.chimerium.db.DataRepositoryFactory;
import pt.iceman.chimerium.response.Response;
import pt.iceman.chimerium.response.ResponseHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Controller<T> implements HttpHandler {
    private static final String POST_METHOD = "POST";
    private static final String PUT_METHOD = "PUT";
    private static final String GET_METHOD = "GET";
    private static final String DELETE_METHOD = "DELETE";
    private static final String OPTIONS_METHOD = "OPTIONS";
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static final Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    private final ResponseHandler responseHandler = new ResponseHandler();
    private final Class<T> genericClass;

    private GeneralConfig config;
    private String context;
    private MongoDatabase mongoDatabase;
    private Hashtable<String, Hashtable<String, ControllerMethod>> methods;
    private Optional<Request> requestOpt;
    private DataRepository<T> repository;

    @SuppressWarnings("unchecked")
    public Controller(String context, GeneralConfig config) {
        this.context = context;
        this.config = config;
        this.genericClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.methods = fillMethods();

        if (config.getDbConfig() != null) {
            DataRepositoryFactory<T> repositoryFactory = new DataRepositoryFactory<T>();
            this.repository = repositoryFactory.getDataRepository(config.getDbConfig(), genericClass);
        }
    }

    public String getContext() {
        return context;
    }

    public Optional<Request> getRequest() {
        return requestOpt;
    }

    public DataRepository<T> getRepository() {
        return this.repository;
    }

    private Hashtable<String, Hashtable<String, ControllerMethod>> fillMethods() {
        Method[] methods = this.getClass().getDeclaredMethods();

        logger.log(Level.INFO, "Finding declared methods");

        Hashtable<String, ControllerMethod> getMethods = new Hashtable<>();
        Hashtable<String, ControllerMethod> postMethods = new Hashtable<>();
        Hashtable<String, ControllerMethod> putMethods = new Hashtable<>();
        Hashtable<String, ControllerMethod> deleteMethods = new Hashtable<>();

        Arrays.stream(methods).forEach(m -> {
            m.setAccessible(true);

            GET g = m.getAnnotation(GET.class);
            POST p = m.getAnnotation(POST.class);
            DELETE d = m.getAnnotation(DELETE.class);
            PUT pt = m.getAnnotation(PUT.class);

            if (g != null) {
                logger.log(Level.INFO, "Get method found: " + m.toString());
                getMethods.put(g.route(), new ControllerMethod(g.args(), m));
            } else if (p != null) {
                logger.log(Level.INFO, "Post method found: " + m.toString());
                postMethods.put(p.route(), new ControllerMethod(p.args(), p.bodyType(), m));
            } else if (d != null) {
                logger.log(Level.INFO, "Delete method found: " + m.toString());
                deleteMethods.put(d.route(), new ControllerMethod(d.args(), d.bodyType(), m));
            } else if (pt != null) {
                logger.log(Level.INFO, "Put method found: " + m.toString());
                putMethods.put(pt.route(), new ControllerMethod(pt.args(), pt.bodyType(), m));
            }
        });

        return new Hashtable<String, Hashtable<String, ControllerMethod>>() {{
            put(POST_METHOD, postMethods);
            put(PUT_METHOD, putMethods);
            put(DELETE_METHOD, deleteMethods);
            put(GET_METHOD, getMethods);
        }};
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> buildDefaultModel(String message) {
        return new HashMap<String, String>() {{
            put("apiMessage", message);
        }};
    }

    private Optional<Request> getRequest(Hashtable<String, ControllerMethod> verbMethods, String route, String body) throws JsonSyntaxException {
        List<String> routeArguments = Arrays.stream(route.split("/")).collect(Collectors.toList());
        routeArguments.remove("");

        List<Request> reqs = verbMethods.entrySet()
                                        .stream()
                                        .map(e -> {
                                            String r = e.getKey();
                                            ControllerMethod cm = e.getValue();
                                            Class<?>[] argTypes = cm.getArgumentsTypes();
                                            Method method = cm.getMethod();

                                            List<String> route2 = Arrays.stream(e.getKey().split("/")).collect(Collectors.toList());
                                            route2.remove("");

                                            List<String> baseValues = route2.stream().filter(s -> !s.startsWith(":")).collect(Collectors.toList());

                                            if ((routeArguments.size() == route2.size() && routeArguments.containsAll(baseValues))) {
                                                routeArguments.removeAll(baseValues);
                                                Request req = new Request(cm.getArgs(routeArguments), method);

                                                if (!body.isEmpty()) {
                                                    req.setBody(gson.fromJson(body, cm.getBodyType()));
                                                }

                                                return req;
                                            } else {
                                                return null;
                                            }
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());

        return !reqs.isEmpty() ? Optional.of(reqs.get(0)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public void handle(HttpExchange httpExchange) {
        String verb = httpExchange.getRequestMethod();

        if (verb.equals(OPTIONS_METHOD)) {
            handleOptionsRequest(httpExchange);
            return;
        }

        String route = httpExchange.getRequestURI().getPath();

        String body = null;

        try {
            body = IOUtils.toString(httpExchange.getRequestBody(), "UTF-8");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't parse body. Is it available?" + e.getMessage());
        }

        this.requestOpt = getRequest(methods.get(verb), route, body);

        Response r = null;

        if (this.requestOpt.isPresent()) {
            try {
                Request request = this.requestOpt.get();

                r = (Response) request.getMethod().invoke(this, request.getArgs().toArray());

            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                r = new Response(500, buildDefaultModel(e.getMessage()));
            }
        } else {
            r = new Response(404, buildDefaultModel("No Route Found"));
        }

        responseHandler.handleResponse(httpExchange, r);
    }

    private void handleOptionsRequest(HttpExchange httpExchange) {
        Headers headers = (Headers) httpExchange.getRequestHeaders();
        List<String> origins = headers.get("Origin");

        String origin = (origins != null && !origins.isEmpty()) ? origins.get(0) : "*";

        List<String> accessControlMethods = headers.get("Access-Control-Request-Method");

        String accessControl = (accessControlMethods != null && !accessControlMethods.isEmpty()) ? accessControlMethods.get(0) : "GET,HEAD,PUT,POST,DELETE,OPTIONS";

        Headers responseHeaders = httpExchange.getResponseHeaders();

        responseHeaders.set("Access-Control-Allow-Origin", origin);
        responseHeaders.set("Access-Control-Allow-Methods", accessControl);
        responseHeaders.set("Access-Control-Allow-Headers", "X-Requested-With");
        responseHeaders.set("Access-Control-Allow-Credentials", "true");
        responseHeaders.set("Content-Type", String.format("text/plain; charset=%s", StandardCharsets.UTF_8));

        try {
            httpExchange.sendResponseHeaders(200, "".getBytes().length);
            OutputStream os = httpExchange.getResponseBody();

            os.write("".getBytes());
            os.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
