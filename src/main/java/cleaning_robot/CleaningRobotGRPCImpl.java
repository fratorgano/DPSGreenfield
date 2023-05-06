package cleaning_robot;

import common.city.City;
import common.city.Position;
import common.logger.MyLogger;
import io.grpc.stub.StreamObserver;
import proto.CleaningRobotServiceGrpc.CleaningRobotServiceImplBase;
import proto.CleaningRobotServiceOuterClass;
import proto.CleaningRobotServiceOuterClass.*;

public class CleaningRobotGRPCImpl extends CleaningRobotServiceImplBase {
  MyLogger l = new MyLogger("CleaningRobotGRPCImpl");
  @Override
  public void introduceYourself(Introduction presentation, StreamObserver<Ack> responseObserver) {
    // answer to an introduction
    l.log("Introduction received");
    l.log("Introduction: "+presentation);
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
    City.getCity().addRobot(crp);
    l.log("Updated city: "+City.getCity());
    responseObserver.onCompleted();
  }

  @Override
  public void leaving(CleaningRobotServiceOuterClass.CleaningRobotRep request, StreamObserver<Ack> responseObserver) {
    l.log(String.format("Robot %s is asking to leave the city",request.getID()));
    CleaningRobotRep toRemoveCrp = new CleaningRobotRep(
            request.getID(),
            request.getIP(),
            request.getPort()
    );
    l.log("Removing it from City");
    City.getCity().removeRobot(toRemoveCrp);

    Ack response = Ack.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void areYouAlive(Ack request, StreamObserver<Ack> responseObserver) {
    l.log("Got a request to know if I'm alive, I am");
    Ack response = Ack.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
