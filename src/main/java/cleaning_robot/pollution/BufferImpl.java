package cleaning_robot.pollution;

import cleaning_robot.simulator.Buffer;
import cleaning_robot.simulator.Measurement;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer {
    ArrayList<Measurement> buffer = new ArrayList<>(8);

    // synchronized because so I can be sure of not having concurrent modifications
    @Override
    public synchronized void addMeasurement(Measurement m) {
        buffer.add(m);
        if (buffer.size()==8) {
            this.notify();
        }
    }

    // synchronized because I don't want to access the list while it's being modified by an add for example
    @Override
    public synchronized List<Measurement> readAllAndClean() {
        ArrayList<Measurement> copy = new ArrayList<>(buffer);
        buffer.subList(0, 4).clear();
        return copy;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
