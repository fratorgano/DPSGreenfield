package cleaning_robot.grpc;

import cleaning_robot.CleaningRobot;
import common.logger.MyLogger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GRPCThread extends Thread{
  private final Integer port;
  private final CleaningRobot cr;
  MyLogger l = new MyLogger("GRPCThread");
  Server server;
  public GRPCThread(Integer port, CleaningRobot cr) {
    this.port = port;
    this.cr = cr;
  }

  @Override
  public void run() {
    server = ServerBuilder.forPort(this.port)
        .addService(new GRPCImpl(cr))
        .build();
    startServer();
  }
  public void startServer() {
    try {
      server.start();
      l.log("Server started");
      server.awaitTermination();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
  public void stopServer() {
    l.log("Stopping server");
    server.shutdown();
  }
}
