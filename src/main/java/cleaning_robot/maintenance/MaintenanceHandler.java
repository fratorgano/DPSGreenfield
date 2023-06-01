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
  MyLogger l = new MyLogger("MaintenanceHandler", MyLogger.Category.MAINTENANCE);
  final List<CleaningRobotRep> confirmationsNeeded = new ArrayList<>();
  public Instant maintenanceInstant = null;

  public MaintenanceHandler(CleaningRobotRep crp, CleaningRobot me) {
    this.crp = crp;
    this.me = me;
  }

  private synchronized void enterMaintenance() {
    l.warn("Entering maintenance");
    try {
      Thread.sleep(5*1000);
    } catch (InterruptedException e) {
      l.error("Failed to sleep for maintenance: "+e.getMessage());
    }
    l.warn("Leaving maintenance");
    this.maintenanceInstant = null;
    this.notifyAll();
  }
  public void sendMaintenanceRequest() {
    if(maintenanceInstant!=null) {
      l.error("sendMaintenanceRequest was called twice before being done");
      throw new RuntimeException("This function should be called only once");
    }
    this.maintenanceInstant = Instant.now();
    if(this.confirmationsNeeded.isEmpty()) {
      this.confirmationsNeeded.addAll(SimpleCity.getCity().getRobotsList().stream()
          .filter(r->!r.ID.equals(crp.ID))
          .collect(Collectors.toList()));
    } else {
      throw new RuntimeException("sendMaintenanceRequest was called before the previous maintenance could be completed");
    }
    String confirmationIDs = "["+confirmationsNeeded.stream().map(crp -> crp.ID).reduce("",(c1, c2)->c1+' '+c2)+" ]";
    if (this.confirmationsNeeded.size()>0) {
      l.log("Need maintenance, confirmations needed:"+confirmationIDs);
      GRPCUser.asyncSendMaintenanceRequest(crp, maintenanceInstant,confirmationsNeeded, this, me);
      synchronized (this.confirmationsNeeded) {
        while(!this.confirmationsNeeded.isEmpty()){
          try {
            this.confirmationsNeeded.wait();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        enterMaintenance();
      }
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
    synchronized (this.confirmationsNeeded) {
      this.confirmationsNeeded.removeIf(c->c.ID.equals(crpConfirm.ID));
//      if(this.confirmationsNeeded.isEmpty()) {
//        enterMaintenance();
//      }
      this.confirmationsNeeded.notify();
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
