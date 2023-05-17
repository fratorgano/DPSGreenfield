package admin.server.rest;

import common.logger.MyLogger;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestServer {
  public final String host;
  public final Integer port;
  private HttpServer httpServer;

  private final MyLogger l = new MyLogger("RestThread");


  public RestServer(String host, Integer port) {
    this.host = host;
    this.port = port;
  }
  public void startServer() {
    Logger.getLogger("com.sun.jersey").setLevel(Level.SEVERE);
    try {
      httpServer = HttpServerFactory.create("http://"+host+":"+port+"/");
      httpServer.start();
      l.log("Server started on: http://"+host+":"+port);
    } catch (IOException e) {
      l.error("Failed to initialize HttpServer: "+e.getMessage());
    }
  }

  public void stopServer() {
    httpServer.stop(0);
    l.log("Server stopped");
  }
}

