package pt.iceman.chimerium;

import java.lang.reflect.Method;
import java.util.List;


public class Request {
    private List<Object> args;
    private Method method;
    private Object body;

    public Request(List<Object> args, Method method) {
        this.args = args;
        this.method = method;
    }

    public List<Object> getArgs() {
        return args;
    }

    public Method getMethod() {
        return method;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
