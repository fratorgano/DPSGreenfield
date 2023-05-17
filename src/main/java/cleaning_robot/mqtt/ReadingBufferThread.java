package cleaning_robot.mqtt;

import cleaning_robot.simulator.Buffer;
import cleaning_robot.simulator.Measurement;
import cleaning_robot.simulator.Simulator;
import common.logger.MyLogger;

import java.util.ArrayList;
import java.util.List;

public class ReadingBufferThread extends Thread {
  private final MyLogger l = new MyLogger("ReadingBufferThread");
  private final Buffer buffer;
  private final Simulator sim;
  private boolean isRunning;
  public ArrayList<Double> averages = new ArrayList<>();

  public ReadingBufferThread(Buffer buffer, Simulator sim) {
    this.isRunning = true;
    this.buffer = buffer;
    this.sim = sim;
  }

  @Override
  public void run() {
    while(this.isRunning) {
      try {
        // l.log("Waiting for buffer...");
        synchronized (this.buffer) {
          this.buffer.wait();
        }
        // l.log("Readings from buffer and calculating averages");
        calculateAverages();
        if(!this.isRunning) {
          // stop simulator after sending last reading
          this.sim.stopMeGently();
          break;
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public synchronized void calculateAverages() {
    List<Measurement> bufferReadings = buffer.readAllAndClean();
    double sum=0;
    for (int i = 0; i < 4; i++) {
      double removed = bufferReadings.remove(0).getValue();
      sum += removed;
    }
    for (int i = 0; i < 4; i++) {
      double peeked = bufferReadings.get(i).getValue();
      sum += peeked;
    }
    averages.add( ( sum / 8.0));
  }

  public synchronized List<Double> readAllAveragesAndClean() {
    ArrayList<Double> copy = new ArrayList<>(averages);
    averages.clear();
    return copy;
  }

  public void stopRunning() {
    this.isRunning = false;
  }
}
