package admin.server;

import cleaning_robot.CleaningRobotRep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class District implements Comparable<District> {
  List<CleaningRobotRep> cleaningRobotRepList = new ArrayList<>();
  String name;
  public District(String name) {
    this.name = name;
  }
  public void addRobot(CleaningRobotRep crp) {
    cleaningRobotRepList.add(crp);
  }
  public boolean removeRobot(CleaningRobotRep crp) {
    return cleaningRobotRepList.removeIf(rob-> Objects.equals(rob.ID, crp.ID));
  }
  public Integer getSize() {
    return cleaningRobotRepList.size();
  }
  public boolean canAddRobot(CleaningRobotRep crp) {
    return cleaningRobotRepList.stream().anyMatch(c-> Objects.equals(c.ID, crp.ID));
  }

  @Override
  public int compareTo(District o) {
    return Integer.compare(this.getSize(),o.getSize());
  }
}
