package cleaning_robot.maintenance;

import cleaning_robot.CleaningRobotGRPCUser;
import cleaning_robot.CleaningRobotRep;
import common.city.City;
import common.logger.MyLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CleaningRobotMaintenance {
  private final CleaningRobotRep crp;
  Boolean isRunning;
  public Boolean isInMaintenance;
  List<CleaningRobotRep> maintenanceQueue = new ArrayList<>();
  MyLogger l = new MyLogger("CleaningRobotMaintenance");
  List<CleaningRobotRep> confirmationsNeeded = new ArrayList<>();
  public Instant maintenanceInstant = null;

  public CleaningRobotMaintenance(CleaningRobotRep crp) {
    this.isInMaintenance = false;
    this.isRunning = true;
    this.crp = crp;
  }

  private void enterMaintenance() {
    l.log("Entering maintenance");
    synchronized (this) {
      this.isInMaintenance = true;
    }
    try {
      Thread.sleep(10*1000);
    } catch (InterruptedException e) {
      l.error("Failed to sleep for maintenance: "+e.getMessage());
    }
    l.log("Leaving maintenance");
    synchronized (this) {
      this.isInMaintenance = false;
      this.maintenanceInstant = null;
      maintenanceQueue.clear();
      this.notifyAll();
    }
    l.log(maintenanceQueue);

  }
  public void sendMaintenanceRequest() {
    if(maintenanceInstant!=null) {
      l.error("sendMaintenanceRequest was called twice before being done");
      throw new RuntimeException("This function should be called only one");
    }
    this.maintenanceInstant = Instant.now();
    this.confirmationsNeeded = City.getCity().getRobotsList().stream()
        .filter(r->!r.ID.equals(crp.ID))
        .collect(Collectors.toList());
    if (this.confirmationsNeeded.size()>0) {
      l.log("Need maintenance, confirmations needed: "+confirmationsNeeded);
      CleaningRobotGRPCUser.asyncSendMaintenanceRequest(crp, maintenanceInstant,confirmationsNeeded, this);
    } else {
      enterMaintenance();
    }
  }

  public void receiveMaintenanceRequest(CleaningRobotRep crpRequest, String timestamp) {
    if(!doesOtherHasPriority(timestamp)) {
      l.log("My request was earlier, waiting for notify");
      // If request time is after mine, return false and add requester to maintenanceQueue
      synchronized (this) {
        try {
          this.wait(30_000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public void confirmMaintenanceRequest(CleaningRobotRep crpConfirm) {
    this.confirmationsNeeded.removeIf(c->c.ID.equals(crpConfirm.ID));
    if(this.confirmationsNeeded.isEmpty()) {
      enterMaintenance();
    }
  }

  public boolean doesOtherHasPriority(String time) {
    if (maintenanceInstant == null) {
      l.log("I don't need maintenance, go ahead");
      return true;
    }
    Instant requestInstant = Instant.parse(time);
    return requestInstant.compareTo(this.maintenanceInstant)<0;
  }
}
