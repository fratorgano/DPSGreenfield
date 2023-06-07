package admin.server.pollution;

import common.mqtt.MqttReading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class DataStorageTest {

  @AfterEach
  void tearDown() {
    DataStorage.getInstance().clear();
  }

  @Test
  void averageN() {
    ArrayList<Double> averages = (ArrayList<Double>) Stream.of(1.,2.,3.).collect(Collectors.toList());
    MqttReading reading = new MqttReading(averages,"2023-05-18T10:15:30Z","0");

    DataStorage ds = DataStorage.getInstance();
    ds.addData("0",reading);
    Double result = ds.averageN(10, "0");
    Assertions.assertEquals(2, result);
  }

  @Test
  void averageNNoData() {
    ArrayList<Double> averages = (ArrayList<Double>) Stream.of(1.,1.,1.).collect(Collectors.toList());
    MqttReading reading = new MqttReading(averages,"2023-05-18T10:15:30Z","0");

    DataStorage ds = DataStorage.getInstance();
    ds.addData("0",reading);
    Double result = ds.averageN(10, "1");
    Assertions.assertNull(result);
  }

  @Test
  void averageTime() {
    ArrayList<Double> averages1 = (ArrayList<Double>) Stream.of(1.,1.,1.).collect(Collectors.toList());
    MqttReading reading = new MqttReading(averages1,"2023-05-18T10:15:30Z","0");
    ArrayList<Double> averages2 = (ArrayList<Double>) Stream.of(2.,2.,2.).collect(Collectors.toList());
    MqttReading reading2 = new MqttReading(averages2,"2023-05-18T10:15:40Z","1");

    DataStorage ds = DataStorage.getInstance();
    ds.addData("0",reading);
    ds.addData("0", reading2);
    Double result = ds.averageTime("2023-05-18T10:15:29Z", "2023-05-18T10:15:32Z");
    Assertions.assertEquals(1,result);
    result = ds.averageTime("2023-05-18T10:15:31Z", "2023-05-18T10:15:41Z");
    Assertions.assertEquals(2,result);
    result = ds.averageTime("2023-05-18T10:15:29Z", "2023-05-18T10:15:41Z");
    Assertions.assertEquals(1.5,result);
  }
}