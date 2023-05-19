package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobot;
import cleaning_robot.grpc.GRPCUser;
import cleaning_robot.CleaningRobotRep;
import common.city.SimpleCity;
import common.logger.MyLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MaintenanceHandler {
  private final CleaningRobotRep crp;
  private final CleaningRobot me;
  Boolean isRunning;
  public Boolean isInMaintenance;
  List<CleaningRobotRep> maintenanceQueue = new ArrayList<>();
  MyLogger l = new MyLogger("MaintenanceHandler");
  List<CleaningRobotRep> confirmationsNeeded = new ArrayList<>();
  public Instant maintenanceInstant = null;

  public MaintenanceHandler(CleaningRobotRep crp, CleaningRobot me) {
    this.isInMaintenance = false;
    this.isRunning = true;
    this.crp = crp;
    this.me = me;
  }

  private synchronized void enterMaintenance() {
    l.warn("Entering maintenance");
    synchronized (this) {
      this.isInMaintenance = true;
    }
    try {
      Thread.sleep(10*1000);
    } catch (InterruptedException e) {
      l.error("Failed to sleep for maintenance: "+e.getMessage());
    }
    l.warn("Leaving maintenance");
    synchronized (this) {
      this.isInMaintenance = false;
      this.maintenanceInstant = null;
      maintenanceQueue.clear();
      this.notifyAll();
    }
  }
  public void sendMaintenanceRequest() {
    if(maintenanceInstant!=null) {
      l.error("sendMaintenanceRequest was called twice before being done");
      throw new RuntimeException("This function should be called only once");
    }
    this.maintenanceInstant = Instant.now();
    this.confirmationsNeeded = SimpleCity.getCity().getRobotsList().stream()
        .filter(r->!r.ID.equals(crp.ID))
        .collect(Collectors.toList());
    String confirmationIDs = confirmationsNeeded.stream().map(crp -> crp.ID).reduce("",(c1, c2)->c1+' '+c2);
    if (this.confirmationsNeeded.size()>0) {
      l.log("Need maintenance, confirmations needed:"+confirmationIDs);
      GRPCUser.asyncSendMaintenanceRequest(crp, maintenanceInstant,confirmationsNeeded, this, me);
    } else {
      enterMaintenance();
    }
  }

  public synchronized void receiveMaintenanceRequest(CleaningRobotRep requester, String timestamp) {
    if(!doesOtherHasPriority(timestamp)) {
      l.log("I have priority over "+requester.ID+", waiting until I'm done to give Ok");
      try {
        this.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else {
      l.log(requester.ID+" has priority, giving Ok");
    }
  }

  public void confirmMaintenanceRequest(CleaningRobotRep crpConfirm) {
    l.log("Received confirmation from "+crpConfirm.ID);
    this.confirmationsNeeded.removeIf(c->c.ID.equals(crpConfirm.ID));
    if(this.confirmationsNeeded.isEmpty()) {
      enterMaintenance();
    }
  }

  public boolean doesOtherHasPriority(String time) {
    if (maintenanceInstant == null) {
      return true;
    }
    Instant requestInstant = Instant.parse(time);
    return requestInstant.compareTo(this.maintenanceInstant)<0;
  }

  public void handleRobotLeaving(CleaningRobotRep leftCrp) {
    // equal to confirmation
    confirmMaintenanceRequest(leftCrp);
  }

  public synchronized void waitForMaintenanceEnd() {
    if (maintenanceInstant != null) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        l.error("Error while waiting for maintenance end");
      }
    }
  }
}
