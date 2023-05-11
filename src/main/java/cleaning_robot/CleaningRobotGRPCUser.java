package cleaning_robot;

import cleaning_robot.maintenance.CleaningRobotMaintenance;
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

public class CleaningRobotGRPCUser {
  static MyLogger l = new MyLogger("RobotGRPCUser");
  public static void asyncPresentation(String socket, int x, int y, CleaningRobotRep crp,CleaningRobotRep toCrp, CleaningRobot me) {
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
        l.error("Error while waiting for presentation Ack: "+t.getMessage());
        me.removeFromCityAndNotifyServer(toCrp);
      }

      @Override
      public void onCompleted() {
        // l.log("Presentation completed, closing channel");
        channel.shutdown();
      }
    });
  }

  public static void asyncLeaveCity(List<CleaningRobotRep> robots, CleaningRobotRep crp, CleaningRobot me) {
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
          l.error("Error while waiting for leaving Ack: "+t.getMessage());
          me.removeFromCityAndNotifyServer(c);
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
          l.error("Error while waiting for areYouAlive Ack: "+t.getMessage());
          me.removeFromCityAndNotifyServer(crp);
          l.log("Updated city: "+SimpleCity.getCity());
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
                                                 CleaningRobotMaintenance crm,
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
          l.error("Error while waiting for sendMaintenanceRequest Ack: "+t.getMessage());
          me.removeFromCityAndNotifyServer(c);
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
