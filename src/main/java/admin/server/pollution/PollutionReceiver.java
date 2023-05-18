package admin.server.pollution;

import common.logger.MyLogger;
import common.mqtt.MqttReading;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PollutionReceiver {
    private final String broker;
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions connOpts;
    MyLogger l = new MyLogger("PollutionReceiver");
    public PollutionReceiver(String broker) {
        this.broker = broker;

    }
    public void connect() {
        initializeMqtt(this.broker);
    }

//    @Override
//    public synchronized void start() {
//        initializeMqtt(this.broker);
//        super.start();
//    }

    private void initializeMqtt(String broker) {
        String clientId = MqttClient.generateClientId();
        try {
            this.mqttClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            l.error("Failed to create new MqttAsyncClient: " + e.getMessage());
        }
        this.connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        mqttConnectClient();
        mqttSubscribe();
    }

    private void mqttConnectClient() {
        try {
            IMqttToken token = mqttClient.connect(connOpts);
            token.waitForCompletion(1000);
            l.log("Connected to broker");
        } catch (MqttException e) {
            l.error("Failed to connect to broker: " + e.getMessage());
        }
    }

    private void mqttSubscribe() {
        try {
            mqttClient.subscribe("greenfield/pollution/+",2);
        } catch (MqttException e) {
            l.error("Failed to subscribe to topics: "+e.getMessage());
        }
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                l.error("Lost connection: "+cause.getMessage());
                l.warn("Trying to reconnect...");
                mqttRetryConnection(6);
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                l.log("Topic: "+topic+", Message:"+message);
                if(topic.contains("greenfield/pollution/")) {
                    MqttReading mqttReading = MqttReading.fromJson(message.toString());
                    DataStorage.getInstance().addData(mqttReading != null ? mqttReading.crpID : null,mqttReading);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    private void mqttRetryConnection(int n) {
        int count = 0;
        while(!mqttClient.isConnected()) {
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                l.error("Interrupted during sleep: "+e.getMessage());
                throw new RuntimeException(e);
            }
            mqttConnectClient();
            count++;
            if (count>n) {
                throw new RuntimeException(String.format("Failed to reconnect after %d tries.",n));
            }
        }
        mqttSubscribe();
    }
    public void disconnect() {
        try {
            IMqttToken token = this.mqttClient.disconnect(0);
            token.waitForCompletion();
            this.mqttClient.close();
            l.log("Disconnected from broker");
        } catch (MqttException e) {
            l.error("Failed to disconnect and close mqttAsyncClient: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
