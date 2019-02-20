package pt.iceman.chimerium.request;

import pt.iceman.chimerium.response.Response;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Request {
    static final Logger logger = Logger.getLogger(pt.iceman.chimerium.response.Response.class.getName());

    private Map<String, List<String>> requestHeaders;
    private String body;
    private String route;
    private String verb;
    private List<Object> args;
    private Method method;
    private Object bodyObject;

    public Request(){}

    public Request(Map<String, List<String>> requestHeaders, String body, String route, String verb) {
        this.requestHeaders = requestHeaders;
        this.body = body;
        this.route = route;
        this.verb = verb;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public String getBody() {
        return body;
    }

    public String getRoute() {
        return route;
    }

    public String getVerb() {
        return verb;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBodyObject() {
        return bodyObject;
    }

    public void setBodyObject(Object bodyObject) {
        this.bodyObject = bodyObject;
    }

    public void respond(Response response) {}

    protected Class getRootClass(Class<?> c) {
        Class<?> spr = c.getSuperclass();

        if (spr.equals(Object.class)) {
            return c;
        } else {
            return getRootClass(spr);
        }
    }
}
