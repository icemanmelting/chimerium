package pt.iceman.chimerium.server;

import com.sun.net.httpserver.HttpServer;
import pt.iceman.chimerium.handler.Controller;
import pt.iceman.chimerium.handler.SunServerHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SunServer implements IServer {
    private static final Logger logger = Logger.getLogger(pt.iceman.chimerium.server.SunServer.class.getName());
    private HashSet<String> origins;
    private int port;
    private List<SunServerHandler> controllers;
    private HttpServer server;

    public SunServer(int port, List<SunServerHandler> controllers) throws IOException {
        this.origins = new HashSet<String>();
        this.port = (port <= 0) ? 8080 : port;
        this.controllers = controllers;
        this.server =  HttpServer.create(new InetSocketAddress(this.port), 0);
    }

    public void addAllowedOrigin(String origin) {
        origins.add(origin);
    }

    public HashSet<String> getOrigins() {
        return origins;
    }

    public boolean isAllowed(String origin) {
        return origins.contains(origin);
    }

    public void startServer() {
        logger.log(Level.INFO, "Starting Server");
        controllers.forEach(c -> this.server.createContext(c.getContext(), c));
        this.server.start();
        logger.log(Level.INFO, "Server listening on port: " + this.port);
    }

    @Override
    public void stopServer() {
        logger.log(Level.INFO, "Stopping Server");
        server.stop(0);
    }
}
