package cleaning_robot;

import common.city.City;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.city.Position;
import common.logger.MyLogger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CleaningRobot {
    private final MyLogger l = new MyLogger("CleaningRobot");
    CleaningRobotRep crp;
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
            // initialize a city with only one district, we don't care about which
            // district they are in
            City.getCity(1);
            for (CleaningRobotRep other : others) {
                City.getCity().addRobot(other);
            }
            this.crp.position = new Position(joined.get().x,joined.get().y);
            l.log(String.format("I'm at position: (%d,%d)",this.crp.position.x,this.crp.position.y));
            l.log("Starting GRPC server at port: "+interactionPort);
            this.crgt = new CleaningRobotGRPCThread(interactionPort);
            crgt.start();
            l.log("Introducing myself to others");
            introduceMyself();
            l.log("Starting heartbeat thread");
            startHeartbeats();

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
        City simpleCity = City.getCity();
        for (CleaningRobotRep cleaningRobotRep : simpleCity.getRobotsList()) {
            if (Objects.equals(cleaningRobotRep.interactionPort, crp.interactionPort)) continue;
            String socket = cleaningRobotRep.IPAddress + ':' + cleaningRobotRep.interactionPort;
            l.log("Introducing myself to "+socket);
            CleaningRobotGRPCUser.asyncPresentation(
                    socket,
                    crp.position.x,
                    crp.position.y,
                    crp
            );
        }
        l.log("Introductions are done");
    }

    public void leaveCity() {
        // leaves the city in a controlled way
        l.log("I'm gonna leave the city");
        List<String> robotSockets = City.getCity().getRobotsList().stream().map(
                crp->crp.IPAddress+':'+crp.interactionPort
        ).collect(Collectors.toList());
        CleaningRobotGRPCUser.asyncLeaveCity(robotSockets,this.crp);

        Client client = Client.create();
        String serverAddress = "http://localhost:1337";
        ClientResponse cr = deleteRemoveRequest(client,serverAddress,this.crp);
        if(cr!=null) {
            l.log("Answer to deletion received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                l.log("Leaving accepted from server, stopping...");
                this.crgt.stopServer();
                this.crht.stopHeartbeats();
                this.crgt.interrupt();
                this.crht.interrupt();
            } else {
                l.error("Leaving refused from server");
            }
        } else {
            l.error("Received empty response to delete");
        }
    }

    public boolean removeFromCityAndNotifyServer(CleaningRobotRep crpToDelete) {
        l.error("Removing from city: "+crpToDelete);
        City.getCity().removeRobot(crpToDelete);
        l.log("Updated city: "+City.getCity());
        Client client = Client.create();
        String serverAddress = "http://localhost:1337";

        // Send request to be inserted in the city
        ClientResponse cr = deleteRemoveRequest(client,serverAddress, crpToDelete);
        if(cr!=null) {
            l.log("Answer to deletion received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                l.log("Removed unresponsive node from server");
                return true;
            } else {
                l.error("Server didn't remove node, it was probably already removed");
                return false;
            }
        } else {
            l.error("No response received from server");
            return false;
        }
    }
    private void startHeartbeats() {
        this.crht = new CleaningRobotHeartbeatThread(this);
        crht.start();
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
            l.log("Sending DELETE remove request for "+crp);
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
