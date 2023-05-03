package admin.server;

import admin.server.rest.RestThread;
import common.logger.MyLogger;

public class AdminServer {
    RestThread restThread;
    private final MyLogger l = new MyLogger("AdminServer");

    AdminServer(String host, Integer restThreadPort) {
        l.log("Creating RestThread");
        restThread = new RestThread(host,restThreadPort);
        restThread.start();
    }

    public static void main(String[] args) {
        AdminServer as = new AdminServer("localhost",1337);
    }
}
