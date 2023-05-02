package cleaning_robot;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CleaningRobotRep {
    String ID;
    String IPAddress;
    Integer interactionPort;

    public CleaningRobotRep(String ID, String IPAddress, Integer interactionPort) {
        this.ID = ID;
        this.IPAddress = IPAddress;
        this.interactionPort = interactionPort;
    }

    @Override
    public String toString() {
        return "CleaningRobotRep{" +
                "ID='" + ID + '\'' +
                ", IPAddress='" + IPAddress + '\'' +
                ", interactionPort=" + interactionPort +
                '}';
    }
}
