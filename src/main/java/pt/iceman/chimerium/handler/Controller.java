package pt.iceman.chimerium.handler;

import com.google.gson.JsonSyntaxException;
import pt.iceman.chimerium.annotations.DELETE;
import pt.iceman.chimerium.annotations.GET;
import pt.iceman.chimerium.annotations.POST;
import pt.iceman.chimerium.annotations.PUT;
import pt.iceman.chimerium.config.GeneralConfig;
import pt.iceman.chimerium.db.DataRepository;
import pt.iceman.chimerium.db.DataRepositoryFactory;
import pt.iceman.chimerium.request.Request;
import pt.iceman.chimerium.request.body.GsonParser;
import pt.iceman.chimerium.response.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Controller<T> implements IHandler {
    private static final String POST_METHOD = "POST";
    private static final String PUT_METHOD = "PUT";
    private static final String GET_METHOD = "GET";
    private static final String DELETE_METHOD = "DELETE";
    private static final String OPTIONS_METHOD = "OPTIONS";
    private static final Logger logger = Logger.getLogger(pt.iceman.chimerium.handler.Controller.class.getName());
    private final Class<T> genericClass;

    private GeneralConfig config;
    private String context;
    private Hashtable<String, Hashtable<String, ControllerMethod>> methods;
    private DataRepository<T> repository;

    public Controller(String context, GeneralConfig config) {
        this.methods = fillMethods();
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

    private Request processVerbMethods(Request request, List<String> routeArguments, Map.Entry<String, ControllerMethod> e) {
        String r = e.getKey();
        ControllerMethod cm = e.getValue();
        Class<?>[] argTypes = cm.getArgumentsTypes();
        Method method = cm.getMethod();

        List<String> route2 = Arrays.stream(e.getKey().split("/")).collect(Collectors.toList());
        route2.remove("");

        List<String> baseValues = route2.stream().filter(s -> !s.startsWith(":")).collect(Collectors.toList());

        if ((routeArguments.size() == route2.size() && routeArguments.containsAll(baseValues))) {
            routeArguments.removeAll(baseValues);
            request.setArgs(cm.getArgs(routeArguments));
            request.setMethod(method);

            String body = request.getBody();

            if (!body.isEmpty()) {
                request.setBodyObject(GsonParser.getGsonInstance().fromJson(body, cm.getBodyType()));
            }

            return request;
        } else {
            return null;
        }
    }

    private Optional<Request> enrichRequest(Request request) throws NumberFormatException, IllegalArgumentException, JsonSyntaxException {
        List<String> routeArguments = Arrays.stream(request.getRoute().split("/")).collect(Collectors.toList());
        routeArguments.remove("");

        Hashtable<String, ControllerMethod> verbMethods = this.methods.get(request.getVerb());

        List<Request> reqs = verbMethods.entrySet()
                                        .stream()
                                        .map(e -> processVerbMethods(request, routeArguments, e))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());

        return !reqs.isEmpty() ? Optional.of(reqs.get(0)) : Optional.empty();
    }

    @Override
    public void handleRequest(Request request) {
        if (request.getVerb().equals(OPTIONS_METHOD)) {
            handleOptionsRequest(request);
            return;
        }

        Response r = null;

        try {
            Optional<Request> requestOpt = enrichRequest(request);

            if (requestOpt.isPresent()) {

                request = requestOpt.get();

                List<Object> args = new ArrayList<>(request.getArgs());
                args.add(0, request);

                r = (Response) request.getMethod().invoke(this, args.toArray());
            } else {
                r = new Response(404, buildDefaultModel("No Route Found"));
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            r = new Response(500, buildDefaultModel(e.getLocalizedMessage()));
        }

        request.respond(r);
    }

    @Override
    public void handleOptionsRequest(Request request) {
        Response response = new Response(200, null);
        request.respond(response);
    }
}
