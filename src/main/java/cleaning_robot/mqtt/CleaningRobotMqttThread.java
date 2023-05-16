package cleaning_robot.mqtt;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;
import common.mqtt.MqttReading;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.Instant;
import java.util.List;

public class CleaningRobotMqttThread extends Thread{
    private final String brokerString;
    private final String topic;
    private final String clientId;
    private final BufferImpl buffer;
    private final CleaningRobotRep crp;
    private boolean isRunning;
    private ReadingBufferThread rbt;
    private MqttConnectOptions connOpts;
    private MqttAsyncClient mqttClient;
    private final MyLogger l = new MyLogger("MQTTThread");

    public CleaningRobotMqttThread(String broker, String topic, BufferImpl buffer, CleaningRobotRep crp) {
        this.brokerString = broker;
        this.topic = topic;
        this.clientId = MqttClient.generateClientId();
        this.buffer = buffer;
        this.crp = crp;
        this.isRunning = true;

        try {
            this.mqttClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
            this.connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            IMqttToken token = mqttClient.connect(connOpts);
            token.waitForCompletion(1000);
            l.log("Connected to broker");
            this.rbt = new ReadingBufferThread(buffer);
            this.rbt.start();

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
    public void run() {
        // every 15 seconds send an object containing the data of the last readings
        while(isRunning) {
            try {
                Thread.sleep(15*1000);
                l.log("Reading averages from ReadingBufferThread");
                List<Double> averages = this.rbt.readAllAveragesAndClean();
                // l.log("Current averages:"+averages);
                MqttReading reading = new MqttReading(averages, Instant.now().toString(),crp.ID);
                String payload = reading.toJson();
                // l.log(payload);
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(2);
                mqttClient.publish(topic,message);
                l.log("Published sensor readings averages");

            } catch (InterruptedException | MqttException e) {
                l.error("Error in mqtt run loop");
                l.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void stopRunning() {
        this.isRunning = false;
        this.rbt.stopRunning();
    }
}
