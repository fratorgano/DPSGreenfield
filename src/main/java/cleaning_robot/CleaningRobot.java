package cleaning_robot;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.logger.MyLogger;

public class CleaningRobot {
    private final MyLogger logger;
    CleaningRobotRep crp;
    public CleaningRobot(String ID, String IPAddress, Integer interactionPort) {
        this.crp = new CleaningRobotRep(ID, IPAddress,interactionPort);
        this.logger = new MyLogger("CleaningRobot");
        insertIntoCity();
    }

    private void insertIntoCity() {
        // Connect to rest admin server to notify it and get location
        Client client = Client.create();
        String serverAddress = "http://localhost:1337";

        // Send request to be inserted in the city
        ClientResponse cr = postInsertRequest(client,serverAddress);
        if(cr!=null) {
            logger.log(cr.toString());
            CleaningRobotInit cri = cr.getEntity(CleaningRobotInit.class);
            logger.log(cri);
        } else {
            logger.log("Received empty response");
        }
    }

    private ClientResponse postInsertRequest(Client client, String serverAddress){
        WebResource webResource = client.resource(serverAddress+"/robots/insert");
        String input = new Gson().toJson(this.crp);
        logger.log(input);
        try {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            logger.log("Failed to make the insert post request");
            return null;
        }
    }

    public static void main(String[] args) {
        CleaningRobot cr = new CleaningRobot("12345","localhost",3456);
    }
}
