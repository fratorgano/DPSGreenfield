package admin.server.rest.services;

import admin.server.mqtt.DataStorage;
import cleaning_robot.CleaningRobotRep;
import common.city.City;
import common.city.RobotList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/client")
public class ToClientServices {
    @GET
    @Path("getRobots")
    @Produces("application/json")
    public Response getRobots(){
        List<CleaningRobotRep> list = City.getCity().getRobotsList();
        return Response.ok(new RobotList(list)).build();
    }

    @GET
    @Path("getAverageN/{crpID}/{n}")
    @Produces("text/plain")
    public Response getAverageN(@PathParam("crpID") String crpId,@PathParam("n") Integer n){
        Double result = DataStorage.getInstance().averageN(n,crpId);
        if (result!=null) {
            return Response.ok(result.toString()).build();
        } else {
            return Response.status(400).entity("No data received from robot with that ID").build();
        }
    }

    @GET
    @Path("getAverageTime/{start}/{end}")
    @Produces("text/plain")
    public Response getAverageN(@PathParam("start") String start,@PathParam("end") String end){
        Double result = DataStorage.getInstance().averageTime(start,end);
        if (result!=null) {
            return Response.ok(result.toString()).build();
        } else {
            return Response.status(400).entity("No data received from robot with that ID").build();
        }
    }
}
