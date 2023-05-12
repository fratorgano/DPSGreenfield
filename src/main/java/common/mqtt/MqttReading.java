package common.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class MqttReading {
    public List<Double> averages;
    public String timestamp;
    public String crpID;
    public MqttReading(List<Double> averages, String timestamp, String crpID) {
        this.averages = new ArrayList<>(averages);
        this.timestamp = timestamp;
        this.crpID = crpID;
    }
    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }
    public static MqttReading fromJson(String jsonString) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            return gson.fromJson(jsonString, MqttReading.class);
        } catch (JsonSyntaxException jse) {
            System.out.println(jse.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return "MqttReading{" +
                "averages=" + averages +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
