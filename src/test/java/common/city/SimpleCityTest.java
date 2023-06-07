package common.city;

import cleaning_robot.CleaningRobotRep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SimpleCityTest {
    @BeforeEach
    void setUp() {
        // so that singleton is reset before each test
        SimpleCity.getCity().buildCity(1);
    }

    @Test
    public void checkBalancing1() {
        SimpleCity c = SimpleCity.getCity();
        System.out.println(c.districtList);
        c.districtList.get(0).addRobot(new CleaningRobotRep("asd", "asd", 123));
        int actual = c.findBalanced().getSize();
        int expected = 1;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void addRobotWhenEmpty() {
        SimpleCity c = SimpleCity.getCity();
        Boolean success = c.addRobot(new CleaningRobotRep("123","123",123));
        for (District district : c.districtList) {
            System.out.println(district);
        }
        Assertions.assertEquals(success,true);
        int actual = c.districtList.stream().map(District::getSize).max(Integer::compareTo).get();
        int expected = 1;
        Assertions.assertEquals(expected,actual);
    }

    @Test
    void addSameRobotTwice() {
        SimpleCity c = SimpleCity.getCity();
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