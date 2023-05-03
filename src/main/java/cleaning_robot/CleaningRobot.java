package cleaning_robot;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.logger.MyLogger;

import java.util.List;
import java.util.Optional;

public class CleaningRobot {
    private final MyLogger l = new MyLogger("CleaningRobot");
    CleaningRobotRep crp;
    List<CleaningRobotRep> others;
    Integer x,y;
    public CleaningRobot(String ID, String IPAddress, Integer interactionPort) {
        this.crp = new CleaningRobotRep(ID, IPAddress,interactionPort);
        l.log("Trying to join the city...");
        Optional<CleaningRobotInit> joined = insertIntoCity();
        if(joined.isPresent()) {
            l.log("Joined the city");
            others = joined.get().robots;
            x = joined.get().x;
            y = joined.get().y;
            l.log(String.format("I'm at position: (%d,%d)",x,y));
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

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            CleaningRobot cr = new CleaningRobot(String.valueOf(i),"localhost",3456+i);
        }
    }
}
