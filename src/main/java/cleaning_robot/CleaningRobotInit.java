package cleaning_robot;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CleaningRobotInit {
  public List<CleaningRobotRep> robots;
  public Integer x;
  public Integer y;
  CleaningRobotInit() {}
  public CleaningRobotInit(List<CleaningRobotRep> robots, Integer x, Integer y) {
    this.robots = robots;
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "CleaningRobotInit{" +
        "robots=" + robots +
        ", x=" + x +
        ", y=" + y +
        '}';
  }
}
