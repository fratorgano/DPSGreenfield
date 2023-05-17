package cleaning_robot.pollution;

import cleaning_robot.CleaningRobotRep;
import cleaning_robot.simulator.Buffer;
import cleaning_robot.simulator.PM10Simulator;
import cleaning_robot.simulator.Simulator;
import common.logger.MyLogger;
import common.mqtt.MqttReading;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PollutionThread extends Thread{
    private final String topic;
    private final String broker;
    private final CleaningRobotRep crp;
    private boolean isRunning;
    private ReadingBufferThread rbt;
    private MqttAsyncClient mqttClient;
    private final List<MqttMessage> messages = new ArrayList<>();
    private final MyLogger l = new MyLogger("PollutionThread");
    private MqttConnectOptions connOpts;

    public PollutionThread(String broker, String topic, CleaningRobotRep crr) {
        this.broker = broker;
        this.topic = topic;
        this.crp = crr;
        this.isRunning = true;
    }

    @Override
    public synchronized void start() {
        initializeMqtt(broker);
        initializeSimulator();
        super.start();
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
                sendPollutionData(averages);
            } catch (InterruptedException e) {
                l.error("This thread was interrupted while sleeping: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        try {
            IMqttToken disconnectToken = this.mqttClient.disconnect();
            disconnectToken.waitForCompletion(2000);
            this.mqttClient.close();
        } catch (MqttException e) {
            l.log("Failed to close mqttClient: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendPollutionData(List<Double> averages) {
        if (!mqttClient.isConnected()) {
            mqttConnectClient();
        }
        MqttReading reading = new MqttReading(averages, Instant.now().toString(), crp.ID);
        String payload = reading.toJson();
        // l.log(payload);
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(2);
        messages.add(message);
        try {
            for (MqttMessage mqttMessage : messages) {
                mqttClient.publish(topic,mqttMessage);
            }
        } catch (MqttException e) {
            l.error("Failed to publish data: "+e.getMessage());
            l.warn("Will try to send data again along with new data as soon as it is available ("+messages.size()+")");
            return;
        }
        messages.clear();
        l.log("Published sensor readings averages");
    }

    public void initializeMqtt(String broker) {
        String clientId = MqttClient.generateClientId();
        try {
            this.mqttClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            l.error("Failed to create new MqttAsyncClient: " + e.getMessage());
        }
        this.connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        mqttConnectClient();
    }

    public void mqttConnectClient() {
        try {
            IMqttToken token = mqttClient.connect(connOpts);
            token.waitForCompletion(1000);
            l.log("Connected to broker");
        } catch (MqttException e) {
            l.error("Failed to connect to broker: " + e.getMessage());
        }
    }
    public void initializeSimulator() {
        Buffer b = new BufferImpl();

        Simulator sim = new PM10Simulator(b);
        this.rbt = new ReadingBufferThread(b,sim);
        l.log("Starting reading buffer thread");
        this.rbt.start();
        l.log("Starting simulation thread");
        sim.start();
    }


    public void stopRunning() {
        this.isRunning = false;
        this.rbt.stopRunning();
    }
}
