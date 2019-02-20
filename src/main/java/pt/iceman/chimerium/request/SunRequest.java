package pt.iceman.chimerium.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import pt.iceman.chimerium.request.body.GsonParser;
import pt.iceman.chimerium.response.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SunRequest extends Request {
    private HttpExchange httpExchange;

    public SunRequest(HttpExchange httpExchange, Map<String, List<String>> requestHeaders, String body, String route, String verb) {
        super(requestHeaders, body, route, verb);
        this.httpExchange = httpExchange;
    }

    @Override
    public void respond(Response response) {
        Headers headers = (Headers) httpExchange.getResponseHeaders();

        Object result = response.getResult();

        Class<?> baseType = getRootClass(result.getClass());

        String body = (baseType.isAssignableFrom(AbstractCollection.class) || baseType.isAssignableFrom(AbstractMap.class)) ? GsonParser.getGsonInstance().toJson(result, baseType) : GsonParser.getGsonInstance().toJson(result).toString();

        List<String> origins = httpExchange.getRequestHeaders().get("Origin");

        String origin = (origins != null && !origins.isEmpty()) ? origins.get(0) : "*";

        Map<String, String> addedHeaders = response.getHeaders();

        if (addedHeaders != null && !addedHeaders.isEmpty()) {
            addedHeaders.forEach(headers::set);
        }

        List<String> accessControlMethods = headers.get("Access-Control-RoughRequest-Method");

        String accessControl = (accessControlMethods != null && !accessControlMethods.isEmpty()) ? accessControlMethods.get(0) : "GET,HEAD,PUT,POST,DELETE,OPTIONS";

        headers.set("Access-Control-Allow-Origin", origin);
        headers.set("Access-Control-Allow-Methods", accessControl);
        headers.set("Access-Control-Allow-Headers", "X-Requested-With");
        headers.set("Access-Control-Allow-Methods", "GET,HEAD,PUT,POST,DELETE,OPTIONS");
        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Content-Type", String.format("aplication/json" + "; charset=%s", StandardCharsets.UTF_8));

        try {
            httpExchange.sendResponseHeaders(response.getCode(), body.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();

            os.write(body.getBytes());
            os.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
