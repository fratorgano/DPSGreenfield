package common.city;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
    buildCity(4);
  }

  public synchronized boolean addRobot(CleaningRobotRep crp) {
    logger.log("Adding robot");
    if (canAdd(crp)) {
      District d = findBalanced();
      crp.position = generateLocation(d);
      d.addRobot(crp);
      return true;
    } else {
      logger.error("Can't add robot, another with the same ID is present");
      return false;
    }
  }

  private boolean canAdd(CleaningRobotRep crp) {
    return districtList.stream().noneMatch(district -> district.canAddRobot(crp));
  }

  public synchronized boolean removeRobot(CleaningRobotRep crp) {
    boolean removed = false;
    for (District district : districtList) {
      boolean removedInDistrict = district.removeRobot(crp);
      removed = removed || removedInDistrict;
    }
    return removed;
  }
  // THIS PART OF THE CODE IS NOT PART OF THE PROJECT ITSELF, IT JUST NOTIFIES THE SERVER THAT IT ENTERED MAINTENANCE
  // AND NOTIFIES THE SERVER THAT IT LEFT MAINTENANCE, IT'S NOT USED TO CHOOSE WHO ENTERS MAINTENANCE
  // THIS IS USED FOR VISUALIZATION PURPOSES ONLY
  // Check README.MD for more information
  public synchronized void goIntoMaintenance(CleaningRobotRep crp) {
    for (District district : districtList) {
      district.setMaintenance(crp,true);
    }
  }
  // THIS PART OF THE CODE IS NOT PART OF THE PROJECT ITSELF, IT JUST NOTIFIES THE SERVER THAT IT ENTERED MAINTENANCE
  // AND NOTIFIES THE SERVER THAT IT LEFT MAINTENANCE, IT'S NOT USED TO CHOOSE WHO ENTERS MAINTENANCE
  // THIS IS USED FOR VISUALIZATION PURPOSES ONLY
  // Check README.MD for more information
  public synchronized void leaveMaintenance(CleaningRobotRep crp) {
    for (District district : districtList) {
      district.setMaintenance(crp,false);
    }
  }

  District findBalanced() {
    return Collections.min(districtList);
  }

  public synchronized List<CleaningRobotRep> getRobotsList() {
    List<CleaningRobotRep> robotsList = new ArrayList<>();
    for (District district : districtList) {
      robotsList.addAll(district.getCleaningRobotRepList());
    }
    return robotsList;
  }
  void buildCity(int n) {
    // mainly used in tests to reset singleton after each test
    districtList.clear();
    for (int i = 0; i < n; i++) {
      districtList.add(new District(String.valueOf(i)));
    }
  }

  public static String getDistrictName(int x,int y) {
    if (x<=4) {
      if(y<=4) {
        return "district1";
      } else {
        return "district2";
      }
    } else {
      if(y<=4) {
        return "district4";
      } else {
        return "district3";
      }
    }
  }
  private Position generateLocation(District d) {
    int x,y;
    switch (d.name) {
      case "0":
        x = ThreadLocalRandom.current().nextInt(0,5);
        y = ThreadLocalRandom.current().nextInt(0,5);
        break;
      case "1":
        x = ThreadLocalRandom.current().nextInt(0,5);
        y = ThreadLocalRandom.current().nextInt(5,10);
        break;
      case "2":
        x = ThreadLocalRandom.current().nextInt(5,10);
        y = ThreadLocalRandom.current().nextInt(5,10);
        break;
      case "3":
        x = ThreadLocalRandom.current().nextInt(5,10);
        y = ThreadLocalRandom.current().nextInt(0,5);
        break;
      default:
        throw new RuntimeException("Something went wrong while creating a new position");
    }
    return new Position(x,y);
  }

  @Override
  public String toString() {
    return "City{" +
        "districtList=" + districtList +
        '}';
  }
}



