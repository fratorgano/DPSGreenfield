package common.city;

import cleaning_robot.CleaningRobotRep;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RobotList {
    public List<CleaningRobotRep> robots;
    public RobotList() {}
    public RobotList(List<CleaningRobotRep> robots) {
        this.robots = new ArrayList<>(robots);
    }

    @Override
    public String toString() {
        return "RobotList{" +
                "robots=" + robots +
                '}';
    }
}
