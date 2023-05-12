package admin.server.mqtt;

import common.logger.MyLogger;
import common.mqtt.MqttReading;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MqttThread extends Thread {
    private final String clientId;
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions connOpts;
    MyLogger l = new MyLogger("MqttThread");
    public MqttThread(String broker) {
        this.clientId = MqttClient.generateClientId();

        try {
            this.mqttClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
            this.connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            IMqttToken token = mqttClient.connect(connOpts);
            token.waitForCompletion(1000);
            l.log("Connected to broker");
            mqttClient.subscribe("greenfield/pollution/+",2);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    l.error("Lost connection: "+cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    l.log("Topic: "+topic+", Message:"+message);
                    if(topic.contains("greenfield/pollution/")) {
                        MqttReading mqttReading = MqttReading.fromJson(message.toString());
                        DataStorage.getInstance().addData(mqttReading != null ? mqttReading.crpID : null,mqttReading);
                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        } catch (MqttException me) {
            l.error("reason " + me.getReasonCode());
            l.error("msg " + me.getMessage());
            l.error("loc " + me.getLocalizedMessage());
            l.error("cause " + me.getCause());
            l.error("exception " + me);
            me.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {

    }
}
