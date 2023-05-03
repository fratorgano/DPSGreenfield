package admin.server.rest.services;

import cleaning_robot.CleaningRobotInit;
import cleaning_robot.CleaningRobotRep;
import com.google.gson.Gson;
import common.logger.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/robots")
public class ToRobotServices {
  @GET
  @Produces("text/plain")
  public Response serviceStatus(){
    return Response.ok("I'm the Administration server for Greenfield").build();
  }
  @POST
  @Path("insert")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public Response request(CleaningRobotRep robotRep) {
    MyLogger m = new MyLogger("ServerRobotServices");
    m.log(robotRep);
    List<CleaningRobotRep> robots = new ArrayList<>();
    Integer x = 1;
    Integer y = 2;
    robots.add(robotRep);
    CleaningRobotInit cri = new CleaningRobotInit(robots,x,y);
    return Response.status(501).entity(cri).build();
  }
}
