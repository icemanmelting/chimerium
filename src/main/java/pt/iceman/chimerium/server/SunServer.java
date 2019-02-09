package pt.iceman.chimerium.server;

import com.sun.net.httpserver.HttpServer;
import pt.iceman.chimerium.Controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;

public class SunServer implements IServer {
    private HashSet<String> origins;
    private int port;
    private List<Controller> controllers;
    private HttpServer server;

    public SunServer(int port, List<Controller> controllers) throws IOException {
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
        controllers.forEach(c -> this.server.createContext(c.getContext(), c));
        this.server.start();
        System.out.println("Server listening on port: " + this.port);
    }

    @Override
    public void stopServer() {
        server.stop(0);
    }
}
