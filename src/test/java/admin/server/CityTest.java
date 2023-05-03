package admin.server;

import cleaning_robot.CleaningRobotRep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CityTest {
  @BeforeEach
  void setUp() {
    // so that singleton is reset before each test
    City.getCity().buildCity();
  }

  @Test
  public void checkBalancing1() {
    City c = City.getCity();
    c.districtList.get(0).addRobot(new CleaningRobotRep("asd", "asd", 123));
    int actual = c.findBalanced().getSize();
    int expected = 0;
    Assertions.assertEquals(expected, actual);
  }
  @Test
  public void checkBalancing2() {
    City c = City.getCity();
    c.districtList.get(0).addRobot(new CleaningRobotRep("asd", "asd", 123));
    c.districtList.get(1).addRobot(new CleaningRobotRep("asd", "asd", 123));
    c.districtList.get(2).addRobot(new CleaningRobotRep("asd", "asd", 123));
    c.districtList.get(3).addRobot(new CleaningRobotRep("asd", "asd", 123));
    int actual = c.findBalanced().getSize();
    int expected = 1;
    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void checkBalancing3() {
    City c = City.getCity();
    c.districtList.get(0).addRobot(new CleaningRobotRep("asd", "asd", 123));
    c.districtList.get(2).addRobot(new CleaningRobotRep("asd", "asd", 123));
    c.districtList.get(3).addRobot(new CleaningRobotRep("asd", "asd", 123));
    //for (District district : c.districtList) {
    //  System.out.println(district.getSize());
    //}
    int actual = c.findBalanced().getSize();
    int expected = 0;
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void addRobotWhenEmpty() {
    City c = City.getCity();
    Boolean success = c.addRobot(new CleaningRobotRep("123","123",123));
    // for (District district : c.districtList) {
    //   System.out.println(district.getSize());
    // }
    Assertions.assertEquals(success,true);
    int actual = c.districtList.stream().map(District::getSize).max(Integer::compareTo).get();
    int expected = 1;
    Assertions.assertEquals(expected,actual);
  }

  @Test
  void addSameRobotTwice() {
    City c = City.getCity();
    c.addRobot(new CleaningRobotRep("123","123",123));
    Boolean success = c.addRobot(new CleaningRobotRep("123","123",123));

    // for (District district : c.districtList) {
    //   System.out.println(district.getSize());
    // }
    Assertions.assertEquals(success,false);
    int actual = c.districtList.stream().map(District::getSize).reduce(0, Integer::sum);
    int expected = 1;
    Assertions.assertEquals(expected,actual);
  }

  @AfterEach
  void tearDown() {
  }
}