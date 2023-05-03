package cleaning_robot;

public class CleaningRobotRep {
    public String ID;
    public String IPAddress;
    public Integer interactionPort;
    // Needed for service to actually work, if removed service will return 400 Bad Request
    public CleaningRobotRep() {}

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
