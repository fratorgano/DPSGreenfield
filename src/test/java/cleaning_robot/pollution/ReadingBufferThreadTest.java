package cleaning_robot.pollution;

import cleaning_robot.simulator.Buffer;
import cleaning_robot.simulator.Measurement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReadingBufferThreadTest {

  @Test
  void calculateAverages() {
    Buffer b = new BufferImpl();
    ReadingBufferThread t = new ReadingBufferThread(b,null);
    // add 8 measurements, half 1 half 2, average should be 1.5
    for (int i = 0; i < 8; i++) {
      double v;
      if (i <= 3) {
        v = 1.;
      } else {
        v = 2.;
      }
      b.addMeasurement(new Measurement("", "", v, 0));
    }
    System.out.println(b);
    // buffer = 1 1 1 1 2 2 2 2
    t.calculateAverages();
    // buffer = 2 2 2 2
    System.out.println(b);
    assertEquals(1.5,t.averages.get(0));
    // add 4 measurements, all 3
    // buffer = 2 2 2 2 3 3 3 3
    // so the 2nd average should be 2.5
    for (int i = 0; i < 4; i++) {
      b.addMeasurement(new Measurement("", "", 3., 0));
    }
    t.calculateAverages();
    assertEquals(2.5,t.averages.get(1));
    System.out.println(t.averages);
  }
}