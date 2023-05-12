package admin.server.mqtt;

import common.mqtt.MqttReading;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataStorage {
    private HashMap<String, List<MqttReading>> data;
    private static DataStorage ds;
    public static DataStorage getInstance() {
        if(ds==null) {
            ds = new DataStorage();
        }
        return ds;
    }
    public DataStorage() {
        data = new HashMap<String,List<MqttReading>>();
    }
    public synchronized void addData(String crpID,MqttReading reading) {
        if(data.containsKey(crpID)) {
            List<MqttReading> previousData = new ArrayList<>(data.get(crpID));
            previousData.add(reading);
            data.put(crpID,previousData);
        } else {
            List<MqttReading> newList = new ArrayList<>();
            newList.add(reading);
            data.put(crpID,newList);
        }

    }

    public synchronized Double averageN(Integer n,String crpID) {
        double sum = 0.0;
        int count = 0;
        List<MqttReading> mqttReadings = data.get(crpID);
        if(mqttReadings==null) {return null;}
        for (int i = mqttReadings.size() - 1; i >= 0; i--) {
            List<Double> averages = mqttReadings.get(i).averages;
            for (int j = averages.size() - 1; j >= 0; j--) {
                sum+= averages.get(j);
                count+=1;
                if(count==n) {break;}
            }
            if(count==n) {break;}
        }
        return sum/count;
    }
    public synchronized Double averageTime(String start, String end) {
        Instant startInstant = Instant.parse(start);
        Instant endInstant = Instant.parse(end);
        final double[] sum = {0.0};
        final int[] count = {0};
        data.forEach((k,v)-> v.forEach(reading-> {
            Instant time = Instant.parse(reading.timestamp);
            if(time.isAfter(startInstant) && time.isBefore(endInstant)) {
                sum[0] += reading.averages.stream().reduce(Double::sum).get();
                count[0] += reading.averages.size();
            }
        }));
        return sum[0]/count[0];
    }
}
