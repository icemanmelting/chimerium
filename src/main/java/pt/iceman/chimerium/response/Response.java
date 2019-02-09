package pt.iceman.chimerium.response;

import java.util.Map;

public class Response {
    private int code;
    private Object result;
    private Map<String, String> headers;

    public Response(int code, Object o) {
        this.code = code;
        this.result = o;
    }

    public Response(int code, Object result, Map<String, String> headers) {
        this.code = code;
        this.result = result;
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }

    public Object getResult() {
        return result;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
