package admin.server;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.*;
import java.util.List;

public class City {
  private static City c;
  List<District> districtList = new ArrayList<>();
  MyLogger logger = new MyLogger("City");

  public synchronized static City getCity() {
    if (c==null) {
      c = new City();
    }
    return c;
  }
  City() {
    buildCity();
  }

  public synchronized boolean addRobot(CleaningRobotRep crp) {
    logger.log("Adding robot");
    if (canAdd(crp)) {
      District d = findBalanced();
      d.addRobot(crp);
      return true;
    } else {
      logger.error("Can't add robot, another with the same ID is present");
      return false;
    }
  }

  private synchronized boolean canAdd(CleaningRobotRep crp) {
    return districtList.stream().noneMatch(district -> district.canAddRobot(crp));
  }

  synchronized District findBalanced() {
    return Collections.min(districtList);
  }

  public synchronized List<CleaningRobotRep> getRobotsList() {
    List<CleaningRobotRep> robotsList = new ArrayList<>();
    for (District district : districtList) {
      robotsList.addAll(district.getCleaningRobotRepList());
    }
    return robotsList;
  }

  synchronized void buildCity() {
    // mainly used in tests to reset singleton after each test
    districtList.clear();
    for (int i = 0; i < 4; i++) {
      districtList.add(new District(String.valueOf(i)));
    }
  }
}



