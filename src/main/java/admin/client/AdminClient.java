package admin.client;

import admin.client.cli.CliThread;
import cleaning_robot.CleaningRobotRep;
import com.sun.jersey.api.client.*;
import common.city.RobotList;
import common.logger.MyLogger;

import java.util.List;

public class AdminClient {
    private final String serverSocket;
    MyLogger l = new MyLogger("AdminClient");
    public AdminClient(String serverSocket) {
        this.serverSocket = serverSocket;
        CliThread cliThread = new CliThread(this);
        cliThread.start();
    }

    public List<CleaningRobotRep> restGetRobots() {
        RobotList rl = restGetRequest(serverSocket+"/client/getRobots",RobotList.class);
        if(rl!=null) {
            return rl.robots;
        } else {
            return null;
        }
    }

    public Double restGetAverageN(String crpID,Integer n) {
        String s = restGetRequest(serverSocket+"/client/getAverageN/"+crpID+"/"+n,String.class);
        if(s!=null) {
            return Double.valueOf(s);
        } else {
            return null;
        }
    }
    public Double restGetAverageN(String start,String end) {
        String s = restGetRequest(serverSocket+"/client/getAverageTime/"+start+"/"+end,String.class);
        if(s!=null) {
            return Double.valueOf(s);
        } else {
            return null;
        }
    }

    private <T> T restGetRequest(String resource,Class<T> c) {
        Client client = Client.create();
        WebResource webResource = client.resource(resource);
        ClientResponse cr;
        try {
            l.log("Sending Get request");
            cr = webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            l.error("Failed to make Get request");
            cr = null;
        }
        if(cr!=null) {
            l.log("Response received: "+cr);
            if (cr.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                T test =  cr.getEntity(c);
                l.log("Received object: "+c);
                return test;
            } else {
                l.error("Didn't receive a 200 code, request rejected");
            }
        } else {
            l.error("Received empty response");
        }
        return null;
    }

    public static void main(String[] args) {
        MyLogger.addCategory(MyLogger.Category.GENERAL);
        new AdminClient("http://localhost:1337");
    }
}
