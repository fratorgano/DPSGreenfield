package common.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
public class MQTTClient extends Thread {

  private final String brokerString;
  private final List<Topic> subList;
  private final MqttCallback callback;
  private MqttAsyncClient mqttClient;
  String clientId = MqttClient.generateClientId();

  /**
   * @param broker String that contains host and port
   * @param subList List of topics that the client will subscribe to
   * @param clientCallback MqttCallback that will be set on the Mqtt Client
   */
  public MQTTClient(String broker, List<Topic> subList, MqttCallback clientCallback) {
    this.brokerString = broker;
    this.subList = subList;

    if (clientCallback!=null) {
      this.callback = clientCallback;
    } else {
      this.callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          System.out.println("Lost connection with the broker...");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          String messageText = new String(message.getPayload());
          System.out.printf("Received message: [%s] %s",topic,messageText);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          System.out.println("");
        }
      };
    }


    try {
      this.mqttClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      connOpts.setAutomaticReconnect(true);
      IMqttToken token = mqttClient.connect(connOpts);
      token.waitForCompletion(1000);

    } catch (MqttException me) {
      System.out.println("reason " + me.getReasonCode());
      System.out.println("msg " + me.getMessage());
      System.out.println("loc " + me.getLocalizedMessage());
      System.out.println("cause " + me.getCause());
      System.out.println("excep " + me);
      me.printStackTrace();
    }
  }
  public void publish(Topic t, Object o) {

  }
}
class Topic {
  public String topic;
  public Integer qos;
  public Topic(String topic, Integer qos) {
    this.topic = topic;
    this.qos = qos;
  }
}
