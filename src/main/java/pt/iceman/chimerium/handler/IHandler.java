package pt.iceman.chimerium.handler;

import pt.iceman.chimerium.request.Request;

public interface IHandler {
    public void handleRequest(Request request);
    public void handleOptionsRequest(Request request);
}
