package admin.server;

import admin.server.pollution.PollutionReceiver;
import admin.server.rest.RestServer;
import common.logger.MyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdminServer {
    PollutionReceiver pollutionReceiver;
    RestServer restServer;
    private final MyLogger l = new MyLogger("AdminServer");

    AdminServer(String host, Integer restThreadPort, String mqttBroker) throws InterruptedException {
        l.log("Creating RestServer");
        restServer = new RestServer(host,restThreadPort);
        restServer.startServer();
        l.log("Creating PollutionReceiver");
        pollutionReceiver = new PollutionReceiver(mqttBroker);
        pollutionReceiver.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            br.readLine();
            pollutionReceiver.disconnect();
            restServer.stopServer();
            Thread.sleep(10000);
            l.log("Server is done.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MyLogger.addCategory(MyLogger.Category.GENERAL);
        MyLogger.addCategory(MyLogger.Category.SENSORS);
        new AdminServer("localhost",1337,"tcp://localhost:1883");
    }
}
