package cleaning_robot;

import cleaning_robot.grpc.CleaningRobotGRPCThread;
import cleaning_robot.grpc.CleaningRobotGRPCUser;
import cleaning_robot.maintenance.CleaningRobotMaintenanceThread;
import cleaning_robot.mqtt.BufferImpl;
import cleaning_robot.mqtt.CleaningRobotMqttThread;
import cleaning_robot.simulator.PM10Simulator;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.city.Position;
import common.city.SimpleCity;
import common.logger.MyLogger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CleaningRobot {
    private final MyLogger l = new MyLogger("CleaningRobot");
    private CleaningRobotMqttThread mqttThread;
    private BufferImpl sensorBuffer;
    private PM10Simulator sensorSimulatorThread;
    private String district;
    public CleaningRobotMaintenanceThread crmt;
    public CleaningRobotRep crp;
    List<CleaningRobotRep> others;
    CleaningRobotGRPCThread crgt;
    private CleaningRobotHeartbeatThread crht;

    public CleaningRobot(String ID, String IPAddress, Integer interactionPort) {
        this.crp = new CleaningRobotRep(ID, IPAddress,interactionPort);
        l.log("Trying to join the city...");
        Optional<CleaningRobotInit> joined = insertIntoCity();
        if(joined.isPresent()) {
            l.log("Joined the city");
            others = joined.get().robots;
            this.district = SimpleCity.getDistrictName(joined.get().x,joined.get().y);
            l.log("got district: "+this.district);
            // initialize a city with only one district, we don't care about which
            // district they are in
            SimpleCity.getCity();
            for (CleaningRobotRep other : others) {
                SimpleCity.getCity().addRobot(other);
            }
            this.crp.position = new Position(joined.get().x,joined.get().y);
            l.log(String.format("I'm at position: (%d,%d)",this.crp.position.x,this.crp.position.y));
            l.log("Starting GRPC server at port: "+interactionPort);
            this.crgt = new CleaningRobotGRPCThread(interactionPort, this);
            crgt.start();
            l.log("Introducing myself to others");
            introduceMyself();
            // l.log("Starting heartbeat thread");
            // startHeartbeats();
            l.log("Starting maintenance thread");
            this.crmt = new CleaningRobotMaintenanceThread(this.crp, this);
            crmt.start();

            l.log("Starting sensor simulator");
            this.sensorBuffer = new BufferImpl();
            this.sensorSimulatorThread = new PM10Simulator(sensorBuffer);
            this.sensorSimulatorThread.start();
            l.log("Starting mqtt thread");
            this.mqttThread = new CleaningRobotMqttThread(
                    "tcp://localhost:1883",
                    "greenfield/pollution/"+this.district,
                    this.sensorBuffer,
                    crp,
                    this.sensorSimulatorThread
                );
            this.mqttThread.start();

            CleaningRobotCLIThread crct = new CleaningRobotCLIThread(this);
            crct.start();
        }
    }

    private Optional<CleaningRobotInit> insertIntoCity() {
        // Connect to rest admin server to notify it and get location
        Client client = Client.create();
        String serverAddress = "http://localhost:1337";

        // Send request to be inserted in the city
        ClientResponse cr = postInsertRequest(client,serverAddress);
        if(cr!=null) {
            l.log("Response received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                CleaningRobotInit cri = cr.getEntity(CleaningRobotInit.class);
                l.log("Received initialization: "+cri);
                return Optional.of(cri);
            } else {
                l.error("Request to insert into city was rejected");
                return Optional.empty();
            }
        } else {
            l.error("Received empty response");
            return Optional.empty();
        }
    }

    private void introduceMyself() {
        SimpleCity simpleCity = SimpleCity.getCity();
        for (CleaningRobotRep cleaningRobotRep : simpleCity.getRobotsList()) {
            if (Objects.equals(cleaningRobotRep.interactionPort, crp.interactionPort)) continue;
            String socket = cleaningRobotRep.IPAddress + ':' + cleaningRobotRep.interactionPort;
            l.log("Introducing myself to "+socket);
            CleaningRobotGRPCUser.asyncPresentation(
                    socket,
                    crp.position.x,
                    crp.position.y,
                    crp,
                    cleaningRobotRep,
                    this
            );
        }
        l.log("Introductions are done");
    }

    public void leaveCity() {
        // leaves the city in a controlled way
        l.log("I'm gonna leave the city");

        l.log("Waiting for maintenance to finish");
        this.crmt.crm.waitForMaintenanceEnd();
        l.log("Maintenance is done");
        List<CleaningRobotRep> robots = SimpleCity.getCity().getRobotsList();
        CleaningRobotGRPCUser.asyncLeaveCity(robots,this.crp);

        Client client = Client.create();
        String serverAddress = "http://localhost:1337";
        ClientResponse cr = deleteRemoveRequest(client,serverAddress,this.crp);
        if(cr!=null) {
            l.log("Answer to deletion received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                l.log("Leaving accepted from server, stopping...");
                this.crgt.stopServer();
                this.crmt.stopMaintenanceThread();
                // the mqtt thread will also stop the simulator and reading buffer thread
                this.mqttThread.stopRunning();
                try {
                    // waiting for eventual end of mqttThread
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                l.error("Leaving refused from server");
            }
        } else {
            l.error("Received empty response to delete");
        }
    }

    public void removeOtherFromCity (CleaningRobotRep crpToDelete) {
        l.error("Removing from city: "+crpToDelete);
        SimpleCity.getCity().removeRobot(crpToDelete);
        // l.log("Updated city: "+SimpleCity.getCity());
        Client client = Client.create();
        String serverAddress = "http://localhost:1337";

        // Send request to be inserted in the city
        ClientResponse cr = deleteRemoveRequest(client,serverAddress, crpToDelete);
        if(cr!=null) {
            l.log("Answer to deletion received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                l.log("Removed unresponsive node from server");
            } else {
                l.error("Server didn't remove node, it was probably already removed");
            }
        } else {
            l.error("No response received from server");
        }

        // notify others that a node was crashed
        l.log("Notifying that "+crpToDelete.ID + " was removed");
        CleaningRobotGRPCUser.asyncLeaveCity(
            SimpleCity.getCity().getRobotsList(),
            crpToDelete);

        // let maintenance thread know that a robot left
        crmt.crm.handleRobotLeaving(crpToDelete);
    }
    private void startHeartbeats() {
        this.crht = new CleaningRobotHeartbeatThread(this);
        crht.start();
    }


    public void requestMaintenance() {
        crmt.triggerMaintenance();
    }
    public void receiveMaintenanceRequest(CleaningRobotRep requestedCrp, String timestamp) {
        crmt.receiveMaintenanceRequest(requestedCrp,timestamp);
    }

    private ClientResponse postInsertRequest(Client client, String serverAddress){
        WebResource webResource = client.resource(serverAddress+"/robots/insert");
        String input = new Gson().toJson(this.crp);
        l.log(input);
        try {
            l.log("Sending POST insert request");
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            l.error("Failed to make the insert post request");
            return null;
        }
    }

    private ClientResponse deleteRemoveRequest(Client client, String serverAddress, CleaningRobotRep crp) {
        WebResource webResource = client.resource(serverAddress+"/robots/delete");
        String input = new Gson().toJson(crp);
        l.log(input);
        try {
            l.log("Sending DELETE remove request for "+crp.ID);
            return webResource.type("application/json").delete(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            l.error("Failed to make the insert post request");
            return null;
        }
    }

    public static void main(String[] args) {
        String ID = args[0];
        Integer port = Integer.valueOf(args[1]);
        CleaningRobot cr = new CleaningRobot(ID,"localhost",port);
    }
}
