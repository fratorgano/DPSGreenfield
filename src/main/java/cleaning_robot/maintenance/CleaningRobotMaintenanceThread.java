package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.concurrent.ThreadLocalRandom;

public class CleaningRobotMaintenanceThread extends Thread {
    private CleaningRobotMaintenance crm;
    Boolean isRunning;
    MyLogger l = new MyLogger("CleaningRobotMaintenanceThread");

    public CleaningRobotMaintenanceThread(CleaningRobotRep crp) {
        isRunning = true;
        crm = new CleaningRobotMaintenance(crp);
    }

    @Override
    public void run() {
        while(isRunning) {
            /*try {
                crm.isInMaintenance.wait();
            } catch (InterruptedException e) {
                l.error("Failed to wait for isInMaintenance: "+e.getMessage());
            }*/
            try {
                if (ThreadLocalRandom.current().nextInt(0,10) == 9) {
                    crm.sendMaintenanceRequest();
                }
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                l.error("Failed to sleep for maintenance: "+e.getMessage());
            }
        }
    }

    public void triggerMaintenance() {
        crm.sendMaintenanceRequest();
    }
    public boolean receiveMaintenanceRequest(CleaningRobotRep requestedCrp, String timestamp) {
        return crm.receiveMaintenanceRequest(requestedCrp,timestamp);
    }

    public void receiveMaintenanceConfirmation(CleaningRobotRep confirmedCrp) {
        crm.receiveMaintenanceConfirmation(confirmedCrp);
    }
    public void handleRobotLeaving(CleaningRobotRep crpToDelete) {
        crm.handleRobotLeaving(crpToDelete);
    }
    public void stopMaintenanceThread() {
        this.isRunning = false;
    }


}
