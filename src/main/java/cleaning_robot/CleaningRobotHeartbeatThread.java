package cleaning_robot;

import common.logger.MyLogger;

public class CleaningRobotHeartbeatThread extends Thread {
  private final CleaningRobot cr;
  private boolean running;
  private final MyLogger l = new MyLogger("CleaningRobotHeartbeatThread");

  public CleaningRobotHeartbeatThread(CleaningRobot cr) {
    this.cr = cr;
    this.running = true;
  }

  @Override
  public void run() {
    l.log("Starting...");
    while(this.running) {
      CleaningRobotGRPCUser.asyncHeartbeat(this.cr);
      try {
        Thread.sleep(15*1000);
      } catch (InterruptedException e) {
        l.error("Failed to sleep");
      }
    }
  }

  public void stopHeartbeats() {
    l.log("Stopping...");
    this.running = false;
  }
}
