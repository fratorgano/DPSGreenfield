package cleaning_robot;

import cleaning_robot.grpc.GRPCThread;
import cleaning_robot.grpc.GRPCUser;
import cleaning_robot.maintenance.FailureDetectionThread;
import cleaning_robot.pollution.PollutionThread;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.city.Position;
import common.city.SimpleCity;
import common.logger.MyLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CleaningRobot {
    private final MyLogger l = new MyLogger("CleaningRobot");
    public CleaningRobotRep crp;
    private final String serverAddress = "http://localhost:1337";
    private PollutionThread pollutionThread;
    public FailureDetectionThread failureDetectionThread;
    private GRPCThread grpcThread;
    // private CleaningRobotHeartbeatThread crht;

    public CleaningRobot(String ID, String IPAddress, Integer interactionPort) {
        this.crp = new CleaningRobotRep(ID, IPAddress,interactionPort);
        l.log("Trying to join the city...");
        Optional<CleaningRobotInit> joined = insertIntoCity();
        if(joined.isPresent()) {
            l.log("Joined the city");
            List<CleaningRobotRep> others = joined.get().robots;
            String district = SimpleCity.getDistrictName(joined.get().x, joined.get().y);
            l.log("got district: "+ district);
            // initialize a city with only one district, we don't care about which
            // district they are in
            SimpleCity.getCity();
            for (CleaningRobotRep other : others) {
                SimpleCity.getCity().addRobot(other);
            }
            this.crp.position = new Position(joined.get().x,joined.get().y);
            l.log(String.format("I'm at position: (%d,%d)",this.crp.position.x,this.crp.position.y));
            l.log("Starting GRPC server at port: "+interactionPort);
            this.grpcThread = new GRPCThread(interactionPort, this);
            grpcThread.start();
            l.log("Introducing myself to others");
            introduceMyself();
            // l.log("Starting heartbeat thread");
            // startHeartbeats();
            l.log("Starting maintenance thread");
            this.failureDetectionThread = new FailureDetectionThread(this.crp, this);
            failureDetectionThread.start();

            l.log("Starting pollution thread");
            this.pollutionThread = new PollutionThread(
                    "tcp://localhost:1883",
                    "greenfield/pollution/"+ district,
                    crp
                );
            this.pollutionThread.start();

            CRCLIThread cliThread = new CRCLIThread(this);
            cliThread.start();
        } else {
            l.warn("Shutting down...");
            shutdownThreads();
        }
    }

    private Optional<CleaningRobotInit> insertIntoCity() {
        // Connect to rest admin server to notify it and get location
        // Send request to be inserted in the city
        Optional<ClientResponse> optCr = postInsertRequest();
        if(optCr.isPresent()) {
            ClientResponse cr = optCr.get();
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
            GRPCUser.asyncPresentation(
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
        this.failureDetectionThread.maintenanceHandler.waitForMaintenanceEnd();
        l.log("Maintenance is done");
        List<CleaningRobotRep> robots = SimpleCity.getCity().getRobotsList();
        GRPCUser.asyncLeaveCity(robots,this.crp);

        Optional<ClientResponse> optCr = deleteRemoveRequest(this.crp);
        if(optCr.isPresent()) {
            ClientResponse cr = optCr.get();
            l.log("Answer to deletion received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                l.log("Leaving accepted from server, stopping...");
                shutdownThreads();
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
        // Send request to be inserted in the city
        Optional<ClientResponse> optCr = deleteRemoveRequest(crpToDelete);
        if(optCr.isPresent()) {
            ClientResponse cr = optCr.get();
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
        GRPCUser.asyncLeaveCity(
            SimpleCity.getCity().getRobotsList(),
            crpToDelete);

        // let maintenance thread know that a robot left
        failureDetectionThread.maintenanceHandler.handleRobotLeaving(crpToDelete);
    }

    /*private void startHeartbeats() {
        this.crht = new CleaningRobotHeartbeatThread(this);
        crht.start();
    }*/

    public void requestMaintenance() {
        failureDetectionThread.requestMaintenance();
    }

    /*public void receiveMaintenanceRequest(CleaningRobotRep requester, String timestamp) {
        failureDetectionThread.receiveMaintenanceRequest(requester, timestamp);
    }*/

    private Optional<ClientResponse> postInsertRequest(){
        Client client = Client.create();
        WebResource webResource = client.resource(serverAddress+"/robots/insert");
        String input = new Gson().toJson(this.crp);
        l.log(input);
        try {
            l.log("Sending POST insert request");
            return Optional.of(webResource.type("application/json").post(ClientResponse.class, input));
        } catch (ClientHandlerException e) {
            l.error("Failed to make the insert POST request");
            return Optional.empty();
        }
    }

    private Optional<ClientResponse> deleteRemoveRequest(CleaningRobotRep crp) {
        Client client = Client.create();
        WebResource webResource = client.resource(serverAddress+"/robots/delete");
        String input = new Gson().toJson(crp);
        try {
            l.log("Sending DELETE remove request for "+crp.ID);
            return Optional.of(webResource.type("application/json").delete(ClientResponse.class, input));
        } catch (ClientHandlerException e) {
            l.error("Failed to make the remove DELETE request");
            return Optional.empty();
        }
    }

    // THIS PART OF THE CODE IS NOT PART OF THE PROJECT ITSELF, IT JUST NOTIFIES THE SERVER THAT IT ENTERED MAINTENANCE
    // AND NOTIFIES THE SERVER THAT IT LEFT MAINTENANCE, IT'S NOT USED TO ENTER WHO ENTERS MAINTENANCE
    // THIS IS USED FOR VISUALIZATION PURPOSES ONLY
    // Check README.MD for more information
    public void enterMaintenanceInformation() {
        // notifies the server that it's entering maintenance
        Client client = Client.create();
        WebResource webResource = client.resource(serverAddress+"/robots/maintenanceEnterInformation");
        String input = new Gson().toJson(crp);
        try {
            webResource.type("application/json").put(ClientResponse.class, input);
            l.log("Sending PUT Maintenance information (enter) for "+crp.ID);
        } catch (ClientHandlerException e) {
            l.error("Failed to make the maintenance PUT request");
        }
    }
    // THIS PART OF THE CODE IS NOT PART OF THE PROJECT ITSELF, IT JUST NOTIFIES THE SERVER THAT IT ENTERED MAINTENANCE
    // AND NOTIFIES THE SERVER THAT IT LEFT MAINTENANCE, IT'S NOT USED TO CHOOSE WHO ENTERS MAINTENANCE
    // THIS IS USED FOR VISUALIZATION PURPOSES ONLY
    // Check README.MD for more information
    public void leaveMaintenanceInformation() {
        // notifies the server that it's leaving maintenance
        Client client = Client.create();
        WebResource webResource = client.resource(serverAddress+"/robots/maintenanceExitInformation");
        String input = new Gson().toJson(crp);
        try {
            webResource.type("application/json").put(ClientResponse.class, input);
            l.log("Sending PUT Maintenance information (leave) for "+crp.ID);
        } catch (ClientHandlerException e) {
            l.error("Failed to make the maintenance PUT request");
        }
    }

    private void shutdownThreads() {
        if(this.grpcThread!=null) {
            this.grpcThread.stopServer();
        }
        if(this.failureDetectionThread!=null) {
            this.failureDetectionThread.stopRunning();
        }
        if(this.pollutionThread!=null) {
            // the mqtt thread will also stop the simulator and reading buffer thread
            this.pollutionThread.stopRunning();
        }
    }

    public static void main(String[] args) {
        MyLogger.addCategory(MyLogger.Category.GENERAL);
        MyLogger.addCategory(MyLogger.Category.MAINTENANCE);
        MyLogger.addCategory(MyLogger.Category.SENSORS);
        String ID; //= args[0];
        ID = String.valueOf(UUID.randomUUID());
        int port; //= Integer.valueOf(args[1]);
        try (
                ServerSocket socket = new ServerSocket(0);
        ) {
            port = socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new CleaningRobot(ID,"localhost",port);
    }
}
