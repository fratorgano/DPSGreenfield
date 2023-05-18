package cleaning_robot.grpc;

import cleaning_robot.CleaningRobot;
import cleaning_robot.CleaningRobotRep;
import common.city.SimpleCity;
import common.city.Position;
import common.logger.MyLogger;
import io.grpc.stub.StreamObserver;
import proto.CleaningRobotServiceGrpc.CleaningRobotServiceImplBase;
import proto.CleaningRobotServiceOuterClass.*;

public class CleaningRobotGRPCImpl extends CleaningRobotServiceImplBase {
  private final CleaningRobot cr;
  MyLogger l = new MyLogger("CleaningRobotGRPCImpl");

  public CleaningRobotGRPCImpl(CleaningRobot cr) {
    this.cr = cr;
  }
  @Override
  public void introduceYourself(Introduction presentation, StreamObserver<Ack> responseObserver) {
    // answer to an introduction
    l.log("Introduction received from "+presentation.getCrp().getID());
    Ack response =Ack.newBuilder().build();
    responseObserver.onNext(response);
    CleaningRobotRep crp = new CleaningRobotRep(
        presentation.getCrp().getID(),
        presentation.getCrp().getIP(),
        presentation.getCrp().getPort()
    );
    crp.position = new Position(
        presentation.getPosition().getX(),
        presentation.getPosition().getY()
    );
    SimpleCity.getCity().addRobot(crp);
    l.log("Updated city: "+SimpleCity.getCity());
    responseObserver.onCompleted();
  }

  @Override
  public void leaving(CRRepService request, StreamObserver<Ack> responseObserver) {
    l.log(String.format("Robot %s leaving",request.getID()));
    CleaningRobotRep toRemoveCrp = new CleaningRobotRep(
            request.getID(),
            request.getIP(),
            request.getPort()
    );
    l.log("Removing it from SimpleCity");
    SimpleCity.getCity().removeRobot(toRemoveCrp);

    Ack response = Ack.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void areYouAlive(Ack request, StreamObserver<Ack> responseObserver) {
    // l.log("Got a request to know if I'm alive, I am");
    Ack response = Ack.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void maintenanceNeed(MaintenanceReq request, StreamObserver<CRRepService> responseObserver) {
    l.log("Got a maintenance request from "+request.getCrp().getID());
    CleaningRobotRep crp = new CleaningRobotRep(
        request.getCrp().getID(),
        request.getCrp().getIP(),
        request.getCrp().getPort()
    );
    String time = request.getTime();
    // this should answer only if it doesn't need maintenance or if the maintenance is done
    this.cr.failureDetectionThread.receiveMaintenanceRequest(crp, time);
    CRRepService response = CRRepService.newBuilder()
            .setID(cr.crp.ID).setIP(cr.crp.IPAddress).setPort(cr.crp.interactionPort).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();


// needed because it might start some outbound rpc requests
//    Context ctx = Context.current().fork();
//    ctx.run(() -> cr.receiveMaintenanceRequest(requesterCRP,request.getTime()));
//    responseObserver.onCompleted();
  }
}
