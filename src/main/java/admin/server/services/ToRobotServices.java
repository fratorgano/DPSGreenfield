package admin.server.services;

import cleaning_robot.CleaningRobotRep;
import common.logger.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/greenfield/robots")
public class ToRobotServices {
  @GET
  @Produces("text/plain")
  public Response serviceStatus(){
    return Response.ok("I'm the Administration server for Greenfield").build();
  }
  @POST
  @Path("insert")
  @Consumes({"application/json", "application/xml"})
  public Response request(CleaningRobotRep robotRep) {
    MyLogger m = new MyLogger("ServerRobotServices");
    m.log(robotRep);
    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }

  @GET
  @Path("coffee")
  @Produces("text/plain")
  public Response teapot(){
    return Response.status(418).build();
  }

  @GET
  @Path("{word}")
  @Produces({"application/json"})
  public Response lookupDefinition(@PathParam("word") String word) {
    /*String definition = Dictionary.getInstance().get(word);
    if (definition.length() > 0) {
      return Response.ok(String.format("{\"word:\":\"%s\",\"definition\":\"%s\"}",word,definition)).build();
    } else {
      return Response.status(404).entity("{\"Error\":\"Not found\"}").build();
    }*/
    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }
  @POST
  @Path("{word}/{definition}")
  @Produces({"application/json"})
  public Response addDefinition(@PathParam("word") String word,@PathParam("definition") String definition) {
    /*boolean stored = Dictionary.getInstance().store(word,definition);
    if (stored) {
      return Response.ok(String.format("{\"word:\":\"%s\",\"definition\":\"%s\"}",word,Dictionary.getInstance().get(word))).build();
    } else {
      return Response.status(404).entity("{\"Error\":\"Word already has a definition\"}").build();
    }*/

    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();

  }

  @PUT
  @Path("{word}/{definition}")
  @Produces({"application/json"})
  public Response updateDefinition(@PathParam("word") String word,@PathParam("definition") String definition) {
    /*boolean updated = Dictionary.getInstance().update(word,definition);
    if (updated) {
      return Response.ok(String.format("{\"word:\":\"%s\",\"definition\":\"%s\"}",word,Dictionary.getInstance().get(word))).build();
    } else {
      return Response.status(404).entity("{\"Error\":\"Word does not have a definition\"}").build();
    }*/

    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }

  @DELETE
  @Path("{word}")
  @Produces({"application/json"})
  public Response deleteDefinition(@PathParam("word") String word) {
    /*if (Dictionary.getInstance().delete(word))
      return Response.ok().build();
    else
      return Response.status(404).entity("{\"Error\":\"Not found\"}").build();
     */

    return Response.status(501).entity("{\"Error\":\"Not implemented\"}").build();
  }
}
