package admin.server.rest;

import common.logger.MyLogger;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestThread extends Thread {
  public final String host;
  public final Integer port;
  private HttpServer httpServer;

  private final MyLogger l = new MyLogger("RestThread");


  public RestThread(String host, Integer port) {
    this.host = host;
    this.port = port;
  }
  public RestThread() {
    this.host = "localhost";
    this.port = 1337;
  }

  @Override
  public void run() {
    Logger.getLogger("com.sun.jersey").setLevel(Level.SEVERE);
    try {
      httpServer = HttpServerFactory.create("http://"+host+":"+port+"/");
      httpServer.start();
      l.log("Server started on: http://"+host+":"+port);
    } catch (IOException e) {
      l.error("Failed to initialize HttpServer: "+e.getMessage());
    }


    /*System.out.println("Hit return to stop...");
    System.in.read();
    System.out.println("Stopping server");
    server.stop(0);
    System.out.println("Server stopped");*/
  }
  public void stopServer() {
    l.log("Stopping server...");
    httpServer.stop(0);
    l.log("Server stopped");
  }
}

