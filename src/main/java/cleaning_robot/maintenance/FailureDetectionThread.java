package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobot;
import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.concurrent.ThreadLocalRandom;

public class FailureDetectionThread extends Thread {
    public final MaintenanceHandler maintenanceHandler;
    Boolean isRunning;
    MyLogger l = new MyLogger("FailureDetectionThread", MyLogger.Category.MAINTENANCE);

    public FailureDetectionThread(CleaningRobotRep crp, CleaningRobot me) {
        isRunning = true;
        maintenanceHandler = new MaintenanceHandler(crp, me);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("FailureDetectionThread");
        try {
            Thread.sleep(10_000);
            while (isRunning) {
                if (maintenanceHandler.maintenanceInstant == null) {
                    l.log("Checking for failures");
                    if (ThreadLocalRandom.current().nextDouble()>=0.9) {
                        l.warn("FAILURE detected, need maintenance");
                        maintenanceHandler.sendMaintenanceRequest();
                    }
                }
                Thread.sleep(10_000);
                //Thread.sleep(ThreadLocalRandom.current().nextInt(1_000,10_000));
            }
        } catch (InterruptedException e) {
            l.error("Failed to run: "+e.getMessage());
        }
    }

    public void requestMaintenance() {
        maintenanceHandler.sendMaintenanceRequest();
    }
    public void receiveMaintenanceRequest(CleaningRobotRep requester, String timestamp) {
        maintenanceHandler.receiveMaintenanceRequest(requester, timestamp);
    }
    public void stopRunning() {
        this.isRunning = false;
    }
}
