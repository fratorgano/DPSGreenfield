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
  Boolean isInMaintenance;
  List<CleaningRobotRep> maintenanceQueue = new ArrayList<>();
  MyLogger l = new MyLogger("CleaningRobotMaintenance");
  List<CleaningRobotRep> confirmationsNeeded = new ArrayList<>();
  private Instant maintenanceInstant = null;

  public CleaningRobotMaintenance(CleaningRobotRep crp) {
    this.isInMaintenance = true;
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
      this.notify();
    }
    CleaningRobotGRPCUser.asyncConfirmMaintenanceRequest(crp, maintenanceQueue);
  }
  public void sendMaintenanceRequest() {
    this.maintenanceInstant = Instant.now();
    this.confirmationsNeeded = City.getCity().getRobotsList().stream()
        .filter(r->!r.ID.equals(crp.ID))
        .collect(Collectors.toList());
    CleaningRobotGRPCUser.asyncSendMaintenanceRequest(crp, maintenanceInstant,confirmationsNeeded);
  }

  public boolean receiveMaintenanceRequest(CleaningRobotRep crpRequest, String timestamp) {
    l.log("Received a maintenance request");
    if(this.maintenanceInstant == null) {
      l.log("I don't need maintenance, allow request");
      CleaningRobotGRPCUser.asyncConfirmMaintenanceRequest(crp, Collections.singletonList(crpRequest));
      return true;
    }
    Instant requestInstant = Instant.parse(timestamp);
    boolean isBefore = requestInstant.compareTo(this.maintenanceInstant)<0;
    l.log("I need maintenance");
    if(isBefore) {
      l.log("The other request was earlier, allow request");
      // If request time is before mine, return true
      CleaningRobotGRPCUser.asyncConfirmMaintenanceRequest(crp, Collections.singletonList(crpRequest));
      return true;
    } else {
      l.log("My request was earlier, deny request");
      // If request time is after mine, return false and add requester to maintenanceQueue
      maintenanceQueue.add(crpRequest);
      return false;
    }
  }

  public void receiveMaintenanceConfirmation(CleaningRobotRep confirmedCrp) {
    l.log("Received an allow to go into maintenance");
    l.log(confirmedCrp);
    boolean removed = this.confirmationsNeeded.removeIf(cleaningRobotRep -> cleaningRobotRep.ID.equals(confirmedCrp.ID));
    if(!removed) {
      l.error("No elements were removed from confirmations");
    }
    l.log("Missing allows: "+this.confirmationsNeeded);
    if(this.confirmationsNeeded.isEmpty()) {
      l.log("Going into maintenance");
      enterMaintenance();
    }
  }
  public void handleRobotLeaving(CleaningRobotRep leftCrp) {
    l.log("A robot left, consider as if he gave confirmation");
    receiveMaintenanceConfirmation(leftCrp);
  }
}
