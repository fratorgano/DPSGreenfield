package admin.server.rest;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminServerRestThread {

  private static final String HOST = "localhost";
  private static final int PORT = 1337;


  public static void main(String[] args) throws IOException {
    // removes unnecessary messages from jersey logger
    Logger.getLogger("com.sun.jersey").setLevel(Level.SEVERE);

    HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
    server.start();

    System.out.println("Server running!");
    System.out.println("Server started on: http://"+HOST+":"+PORT);

    System.out.println("Hit return to stop...");
    System.in.read();
    System.out.println("Stopping server");
    server.stop(0);
    System.out.println("Server stopped");
  }
}

