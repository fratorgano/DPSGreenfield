package admin.server;

import admin.server.mqtt.DataStorage;
import admin.server.mqtt.MqttThread;
import admin.server.rest.RestThread;
import common.logger.MyLogger;

public class AdminServer {
    MqttThread mqttThread;
    RestThread restThread;
    DataStorage ds = DataStorage.getInstance();
    private final MyLogger l = new MyLogger("AdminServer");

    AdminServer(String host, Integer restThreadPort, String mqttBroker) {
        l.log("Creating RestThread");
        restThread = new RestThread(host,restThreadPort);
        restThread.start();
        l.log("Creating MqttThread");
        mqttThread = new MqttThread(mqttBroker);
        // mqttThread.start();
    }

    public static void main(String[] args) {
        AdminServer as = new AdminServer("localhost",1337,"tcp://localhost:1883");
    }
}
