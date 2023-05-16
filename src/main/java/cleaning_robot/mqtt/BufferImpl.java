package cleaning_robot.mqtt;

import cleaning_robot.simulator.Buffer;
import cleaning_robot.simulator.Measurement;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer {
    ArrayList<Measurement> buffer = new ArrayList<>(8);
    public ArrayList<Double> averages = new ArrayList<>();

    @Override
    public synchronized void addMeasurement(Measurement m) {
        buffer.add(m);
        if (buffer.size()==8) {
            /*double sum=0;
            for (int i = 0; i < 4; i++) {
                double removed = buffer.remove(0).getValue();
                sum += removed;
            }
            for (int i = 0; i < 4; i++) {
                double peeked = buffer.get(i).getValue();
                sum += peeked;
            }
            averages.add( ( sum / 8.0));*/
            this.notify();
        }
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        ArrayList<Measurement> copy = new ArrayList<>(buffer);
        buffer.subList(0, 4).clear();
        return copy;
    }
    public synchronized List<Double> readAllAveragesAndClean() {
        ArrayList<Double> copy = new ArrayList<>(averages);
        averages.clear();
        return copy;
    }
}
