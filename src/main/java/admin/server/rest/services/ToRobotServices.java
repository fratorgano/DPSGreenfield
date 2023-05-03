package admin.server.rest.services;

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
  public Response request(CleaningRobotRep robotRep) {
    MyLogger m = new MyLogger("ServerRobotServices");
    m.log(robotRep);
    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }

  @POST
  @Path("insert")
  public Response r() {
    MyLogger m = new MyLogger("ServerRobotServices");
    m.log("test");
    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }

  @POST
  @Path("test")
  public Response test() {
    MyLogger m = new MyLogger("ServerRobotServices");
    m.log("test");
    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }
}
