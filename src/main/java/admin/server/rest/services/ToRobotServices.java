package admin.server.rest.services;

import common.city.City;
import cleaning_robot.CleaningRobotInit;
import cleaning_robot.CleaningRobotRep;
import common.city.Position;
import common.logger.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.concurrent.ThreadLocalRandom;

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
    MyLogger m = new MyLogger("RobotsRestServices");
    m.log("New request to join city");
    m.log("Robot: "+robotRep);
    City c = City.getCity();
    Integer x = ThreadLocalRandom.current().nextInt(0,10);
    Integer y = ThreadLocalRandom.current().nextInt(0,10);
    robotRep.position = new Position(x,y);
    boolean added = c.addRobot(robotRep);
    if(added) {
      m.log("Robot was added successfully, answering with CleaningRobotInit and code 200-Ok ");
      CleaningRobotInit cri = new CleaningRobotInit(c.getRobotsList(),x,y);
      return Response.status(200).entity(cri).build();
    } else {
      m.log("Robot couldn't be added, answering with code 409-Conflict ");
      // 409 - Conflict response, failure to add node
      return Response.status(409).build();
    }

  }
}
