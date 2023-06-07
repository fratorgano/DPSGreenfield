package cleaning_robot.grpc;

import cleaning_robot.CleaningRobot;
import cleaning_robot.CleaningRobotRep;
import cleaning_robot.maintenance.MaintenanceHandler;
import common.city.SimpleCity;
import common.logger.MyLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.CleaningRobotServiceGrpc;
import proto.CleaningRobotServiceOuterClass;
import proto.CleaningRobotServiceOuterClass.CRRepService;

import java.time.Instant;
import java.util.List;

public class GRPCUser {
  static MyLogger l = new MyLogger("GRPCUser");
  public static void asyncPresentation(String socket, int x, int y, CleaningRobotRep crp, CleaningRobotRep toCrp, CleaningRobot me) {
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
    CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
    CleaningRobotServiceOuterClass.Position position =
        CleaningRobotServiceOuterClass.Position.newBuilder()
          .setX(x)
          .setY(y)
          .build();
    CRRepService crpService =
            CRRepService.newBuilder()
                .setID(crp.ID).setIP(crp.IPAddress).setPort(crp.interactionPort).build();
    CleaningRobotServiceOuterClass.Introduction request =
        CleaningRobotServiceOuterClass.Introduction.newBuilder()
            .setPosition(position)
            .setCrp(crpService)
            .build();
    stub.introduceYourself(request, new StreamObserver<CleaningRobotServiceOuterClass.Ack>() {
      @Override
      public void onNext(CleaningRobotServiceOuterClass.Ack value) {
        // l.log("Received Ack from other robot");
      }

      @Override
      public void onError(Throwable t) {
        l.error("Error while waiting for presentation Ack: "+t.getCause().getMessage());
        me.removeOtherFromCity(toCrp);
        channel.shutdownNow();
      }

      @Override
      public void onCompleted() {
        // l.log("Presentation completed, closing channel");
        channel.shutdown();
      }
    });
  }

  public static void asyncLeaveCity(List<CleaningRobotRep> robots, CleaningRobotRep crp) {
    CRRepService crpService =
            CRRepService.newBuilder()
                    .setID(crp.ID).setIP(crp.IPAddress).setPort(crp.interactionPort).build();
    // notify other robots of leaving
    for (CleaningRobotRep c : robots) {
      if(c.ID.equals(crp.ID)) continue;
      String socket = c.IPAddress+':'+c.interactionPort;
      final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
      CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
      stub.leaving(crpService, new StreamObserver<CleaningRobotServiceOuterClass.Ack>() {
        @Override
        public void onNext(CleaningRobotServiceOuterClass.Ack value) {
          // l.log("Received Ack from robot at" + socket);
        }

        @Override
        public void onError(Throwable t) {
          l.error("Error while waiting for leaving Ack: "+t.getCause().getMessage());
          channel.shutdownNow();
        }

        @Override
        public void onCompleted() {
          // l.log(String.format("Robot at %s acknowledged leave request, closing channel",socket));
          channel.shutdown();
        }
      });
    }
  }

  public static void asyncHeartbeat(CleaningRobot me) {
    CleaningRobotServiceOuterClass.Ack response = CleaningRobotServiceOuterClass.Ack.newBuilder().build();
    List<CleaningRobotRep> robotSockets = SimpleCity.getCity().getRobotsList();
    // notify other robots of leaving
    for (CleaningRobotRep crp : robotSockets) {
      if (crp.ID.equals(me.crp.ID)) continue;
      String socket = crp.IPAddress+':'+crp.interactionPort;
      final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
      CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
      stub.areYouAlive(response, new StreamObserver<CleaningRobotServiceOuterClass.Ack>() {
        @Override
        public void onNext(CleaningRobotServiceOuterClass.Ack value) {
          // l.log("Received Ack for areYouAlive from robot at " + socket);
        }

        @Override
        public void onError(Throwable t) {
          l.error("Error while waiting for areYouAlive Ack: "+t.getCause().getMessage());
          me.removeOtherFromCity(crp);
          l.log("Updated city: "+SimpleCity.getCity());
          channel.shutdownNow();
        }

        @Override
        public void onCompleted() {
          // l.log(String.format("Robot at %s is still alive, closing channel",socket));
          channel.shutdown();
        }
      });
    }
  }

  public static void asyncSendMaintenanceRequest(CleaningRobotRep crp,
                                                 Instant timestamp,
                                                 List<CleaningRobotRep> otherRobots,
                                                 MaintenanceHandler crm,
                                                 CleaningRobot me) {
    // this should send a maintenance request to each other member of the city
    // and when an answer is received call a method to confirm that an OK was received
    // notify other robots of leaving
    for (CleaningRobotRep c : otherRobots) {
      String socket = c.IPAddress + ':' + c.interactionPort;
      final ManagedChannel channel = ManagedChannelBuilder.forTarget(socket).usePlaintext().build();
      CleaningRobotServiceGrpc.CleaningRobotServiceStub stub = CleaningRobotServiceGrpc.newStub(channel);
      CRRepService serviceCrp =
              CRRepService.newBuilder()
              .setID(crp.ID).setPort(crp.interactionPort).setIP(crp.IPAddress)
              .build();
      CleaningRobotServiceOuterClass.MaintenanceReq maintenanceReq =
          CleaningRobotServiceOuterClass.MaintenanceReq.newBuilder()
            .setCrp(serviceCrp)
            .setTime(timestamp.toString())
            .build();
      stub.maintenanceNeed(maintenanceReq, new StreamObserver<CRRepService>() {
        @Override
        public void onNext(CRRepService value) {
          CleaningRobotRep confirmed = new CleaningRobotRep(
                  value.getID(),
                  value.getIP(),
                  value.getPort()
          );
          crm.confirmMaintenanceRequest(confirmed);
          // l.log("Received Ack from robot for MaintenanceReq at" + socket);
        }

        @Override
        public void onError(Throwable t) {
          l.error("Error while waiting for sendMaintenanceRequest Ack: "+t.getCause().getMessage());
          me.removeOtherFromCity(c);
          channel.shutdownNow();
        }

        @Override
        public void onCompleted() {
          // l.log(String.format("Robot at %s acknowledged maintenance request, closing channel",socket));
          channel.shutdown();
        }
      });
    }
  }
}
