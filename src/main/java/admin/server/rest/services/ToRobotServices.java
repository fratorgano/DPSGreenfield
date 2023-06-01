package admin.server.rest.services;

import common.city.City;
import cleaning_robot.CleaningRobotInit;
import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

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
    MyLogger l = new MyLogger("RobotsRestServices");
    l.log("New request to join city");
    l.log("Robot: "+robotRep);
    City c = City.getCity();
    boolean added = c.addRobot(robotRep);
    if(added) {
      l.log("Robot was added successfully, answering with CleaningRobotInit and code 200-Ok ");
      CleaningRobotInit cri = new CleaningRobotInit(c.getRobotsList(),robotRep.position.x,robotRep.position.y);
      return Response.status(200).entity(cri).build();
    } else {
      l.log("Robot couldn't be added, answering with code 409-Conflict ");
      // 409 - Conflict response, failure to add node
      return Response.status(409).build();
    }
  }
  @DELETE
  @Path("delete")
  @Consumes({"application/json"})
  public Response remove(CleaningRobotRep crp) {
    MyLogger l = new MyLogger("RobotsRestServices");
    l.log("Removing: "+crp);
    boolean removed = City.getCity().removeRobot(crp);
    if(removed) {
      l.log("Removed correctly: "+City.getCity());
      return Response.status(200).build();
    } else {
      l.error("Failed to remove "+crp);
      return Response.status(500).build();
    }
  }
}
