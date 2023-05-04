package cleaning_robot;

import common.logger.MyLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.CleaningRobotServiceGrpc;
import proto.CleaningRobotServiceOuterClass;

import java.util.List;

public class CleaningRobotGRPCUser {
  static MyLogger l = new MyLogger("RobotGRPCUser");
  public static void asyncPresentation(String socket, int x, int y, CleaningRobotRep crp) {
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
    CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
    CleaningRobotServiceOuterClass.Position position =
        CleaningRobotServiceOuterClass.Position.newBuilder()
          .setX(x)
          .setY(y)
          .build();
    CleaningRobotServiceOuterClass.CleaningRobotRep crpService =
        CleaningRobotServiceOuterClass.CleaningRobotRep.newBuilder()
                .setID(crp.ID).setIP(crp.IPAddress).setPort(crp.interactionPort).build();
    CleaningRobotServiceOuterClass.Introduction request =
        CleaningRobotServiceOuterClass.Introduction.newBuilder()
            .setPosition(position)
            .setCrp(crpService)
            .build();
    stub.introduceYourself(request, new StreamObserver<CleaningRobotServiceOuterClass.Ack>() {
      @Override
      public void onNext(CleaningRobotServiceOuterClass.Ack value) {
        l.log("Received Ack from other robot");
      }

      @Override
      public void onError(Throwable t) {
        l.error("Error while waiting for presentation Ack: "+t.getMessage());
      }

      @Override
      public void onCompleted() {
        l.log("Presentation completed, closing channel");
        channel.shutdown();
      }
    });
  }

  public static void leaveCity(List<String> robotsSockets, CleaningRobotRep crp) {
    CleaningRobotServiceOuterClass.CleaningRobotRep crpService =
            CleaningRobotServiceOuterClass.CleaningRobotRep.newBuilder()
                    .setID(crp.ID).setIP(crp.IPAddress).setPort(crp.interactionPort).build();
    // notify other robots of leaving
    for (String socket : robotsSockets) {
      final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
      CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
      stub.leaving(crpService, new StreamObserver<CleaningRobotServiceOuterClass.Ack>() {
        @Override
        public void onNext(CleaningRobotServiceOuterClass.Ack value) {
          l.log("Received Ack from robot at" + socket);
        }

        @Override
        public void onError(Throwable t) {
          l.error("Error while waiting for leaving Ack: "+t.getMessage());
        }

        @Override
        public void onCompleted() {
          l.log(String.format("Robot at %s acknowledged leave request, closing channel",socket));
          channel.shutdown();
        }
      });
    }
  }
}
