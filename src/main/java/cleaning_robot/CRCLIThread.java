package cleaning_robot;

import common.logger.MyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CRCLIThread extends Thread {
  private boolean running;
  MyLogger l = new MyLogger("CRCLIThread");
  CleaningRobot cr;
  public CRCLIThread(CleaningRobot cr) {
    this.cr = cr;
    this.running = true;
  }

  @Override
  public void run() {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while(running) {
      try {
        String s = br.readLine();
        l.log(s);
        switch (s) {
          case "failure":
            failure();
            break;
          case "quit":
            quit();
            break;
          default:
            l.error("Unrecognized command");
            break;
        }
      } catch (IOException e) {
        l.error("Failed to read input from CLI: "+e.getMessage());
      }
    }
  }

  private void quit() {
    l.log("Quit request from CLI");
    cr.leaveCity();
    this.running = false;
  }

  private void failure() {
    l.log("Failure request from CLI");
    cr.requestMaintenance();
  }
}
