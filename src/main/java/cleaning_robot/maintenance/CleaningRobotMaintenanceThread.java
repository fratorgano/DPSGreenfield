package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobot;
import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.concurrent.ThreadLocalRandom;

public class CleaningRobotMaintenanceThread extends Thread {
    public final CleaningRobotMaintenance crm;
    Boolean isRunning;
    MyLogger l = new MyLogger("CleaningRobotMaintenanceThread");

    public CleaningRobotMaintenanceThread(CleaningRobotRep crp, CleaningRobot me) {
        isRunning = true;
        crm = new CleaningRobotMaintenance(crp, me);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10_000);
            // this synchronized crm keeps the crm hostage
                while (isRunning) {
                    if (crm.maintenanceInstant != null) {
                        // l.log("Robot already requested maintenance, waiting for it to leave it");
                        // using wait to wait until a maintenance is finished before triggering a new one
                        /*synchronized (crm) {
                            crm.wait();
                        }*/
                        // l.log("Robot maintenance is done");
                    } else {
                        l.log("Checking for failures");
                        if (ThreadLocalRandom.current().nextInt(0, 10) >3) {
                            l.warn("FAILURE detected, need maintenance");
                            crm.sendMaintenanceRequest();
                        }
                    }

                    Thread.sleep( ThreadLocalRandom.current().nextInt(1_000,10_000));
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
