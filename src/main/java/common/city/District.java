package common.city;

import cleaning_robot.CleaningRobotRep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class District implements Comparable<District> {
  private List<CleaningRobotRep> cleaningRobotRepList = new ArrayList<>();
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
  // THIS PART OF THE CODE IS NOT PART OF THE PROJECT ITSELF, IT JUST NOTIFIES THE SERVER THAT IT ENTERED MAINTENANCE
  // AND NOTIFIES THE SERVER THAT IT LEFT MAINTENANCE, IT'S NOT USED TO CHOOSE WHO ENTERS MAINTENANCE
  // THIS IS USED FOR VISUALIZATION PURPOSES ONLY
  // Check README.MD for more information
  public void setMaintenance(CleaningRobotRep crp,boolean value) {
    for (CleaningRobotRep cleaningRobotRep : cleaningRobotRepList) {
      if(cleaningRobotRep.ID.equals(crp.ID)) {
        cleaningRobotRep.maintenance = value;
        return;
      }
    }
  }

  public List<CleaningRobotRep> getCleaningRobotRepList() {
    return new ArrayList<>(cleaningRobotRepList);
  }

  @Override
  public int compareTo(District o) {
    return Integer.compare(this.getSize(),o.getSize());
  }

  @Override
  public String toString() {
    return "District{" +
        "cleaningRobotRepList=" + cleaningRobotRepList +
        ", name='" + name + '\'' +
        '}';
  }
}
