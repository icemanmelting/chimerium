package pt.iceman.chimerium.server;

import java.util.HashSet;

public interface IServer {
   public void addAllowedOrigin(String origin);
   public HashSet<String> getOrigins();
   public boolean isAllowed(String origin);
   public void startServer();
   public void stopServer();
}
