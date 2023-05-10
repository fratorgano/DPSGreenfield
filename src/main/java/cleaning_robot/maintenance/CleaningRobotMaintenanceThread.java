package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.concurrent.ThreadLocalRandom;

public class CleaningRobotMaintenanceThread extends Thread {
    public final CleaningRobotMaintenance crm;
    Boolean isRunning;
    MyLogger l = new MyLogger("CleaningRobotMaintenanceThread");

    public CleaningRobotMaintenanceThread(CleaningRobotRep crp) {
        isRunning = true;
        crm = new CleaningRobotMaintenance(crp);
    }

    @Override
    public void run() {
        try {
            synchronized (crm) {
                while (isRunning) {
                    if (crm.maintenanceInstant != null) {
                        l.log("Cleaning robot needs maintenance, waiting for it to finish");
                        // using wait to wait until a maintenance is finished before triggering a new one
                        crm.wait();
                    }
                    l.log("Checking for failures");
                    if (ThreadLocalRandom.current().nextInt(0, 10) >6) {
                        l.log("FAILURE detected, going into maintenance");
                        crm.sendMaintenanceRequest();
                    }
                    Thread.sleep(10 * 1000);
                }
            }
        } catch (InterruptedException e) {
            l.error("Failed to run: "+e.getMessage());
        }
    }

    public void triggerMaintenance() {
        crm.sendMaintenanceRequest();
    }
    public void receiveMaintenanceRequest(CleaningRobotRep requestedCrp, String timestamp) {
        crm.receiveMaintenanceRequest(requestedCrp,timestamp);
    }
    public void stopMaintenanceThread() {
        this.isRunning = false;
    }


}
