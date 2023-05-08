package cleaning_robot;

import common.logger.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CleaningRobotMaintenanceThread extends Thread {
    Boolean isRunning;
    Boolean needsMaintenance;
    Boolean isInMaintenance;
    List<CleaningRobotRep> maintenanceQueue = new ArrayList<>();
    MyLogger l = new MyLogger("CleaningRobotMaintenanceThread");

    public CleaningRobotMaintenanceThread() {
        needsMaintenance = false;
        isInMaintenance = true;
        isRunning = true;
    }

    public void enterMaintenance() {
        l.log("Entering maintenance");
        this.isInMaintenance = true;
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            l.error("Failed to sleep for maintenance: "+e.getMessage());
        }
        l.log("Leaving maintenance");
        this.needsMaintenance = false;
        this.isInMaintenance = false;
        isInMaintenance.notify();
    }
    public void requestMaintenance() {

    }

    public void handleMaintenance() {
    }

    @Override
    public void run() {
        while(isRunning) {
            try {
                isInMaintenance.wait();
            } catch (InterruptedException e) {
                l.error("Failed to wait for isInMaintenance: "+e.getMessage());
            }
            try {
                if (ThreadLocalRandom.current().nextInt(1,10) == 9) {
                    this.needsMaintenance = true;
                }
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                l.error("Failed to sleep for maintenance: "+e.getMessage());
            }
        }
    }
    public void stopMaintenanceNeed() {
        this.isRunning = false;
    }
}
