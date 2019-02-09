package pt.iceman.chimerium.response;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pt.iceman.chimerium.common.ClassAnalyzer.getRootClass;

public class ResponseHandler {
    private static final Logger logger = Logger.getLogger(ResponseHandler.class.getName());

    private Gson gson;

    public ResponseHandler() {
        this.gson = new Gson();
    }

    public void handleResponse(HttpExchange httpExchange, Response response) {
        Headers headers = (Headers) httpExchange.getResponseHeaders();

        Object result = response.getResult();

        Class<?> baseType = getRootClass(result.getClass());

        String body = (baseType.isAssignableFrom(AbstractCollection.class)) ? gson.toJson(result, baseType) : gson.toJson(result).toString();

        List<String> origins = httpExchange.getRequestHeaders().get("Origin");

        String origin = (origins != null && !origins.isEmpty()) ? origins.get(0) : "*";

        Map<String, String> addedHeaders = response.getHeaders();

        if (addedHeaders != null && !addedHeaders.isEmpty()) {
            addedHeaders.forEach(headers::set);
        }

        headers.set("Access-Control-Allow-Origin", origin);
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
