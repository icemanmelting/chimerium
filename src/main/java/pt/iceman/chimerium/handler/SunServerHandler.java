package pt.iceman.chimerium.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import pt.iceman.chimerium.config.GeneralConfig;
import pt.iceman.chimerium.request.SunRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SunServerHandler<T> extends Controller<T> implements HttpHandler {
    private static final Logger logger = Logger.getLogger(pt.iceman.chimerium.handler.SunServerHandler.class.getName());

    public SunServerHandler(String context, GeneralConfig config) {
        super(context, config);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String, List<String>> requestHeaders = httpExchange.getRequestHeaders()
                                                               .entrySet()
                                                               .stream()
                                                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String verb = httpExchange.getRequestMethod();

        String body = null;

        try {
            body = IOUtils.toString(httpExchange.getRequestBody(), "UTF-8");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't parse body. Is it available?" + e.getMessage());
        }

        String route = httpExchange.getRequestURI().getPath();

        super.handleRequest(new SunRequest(httpExchange, requestHeaders, body, route, verb));
    }
}
