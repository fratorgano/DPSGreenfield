package admin.client.cli;

import admin.client.AdminClient;
import cleaning_robot.CleaningRobot;
import common.logger.MyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;

public class CliThread extends Thread{
    private final AdminClient ac;
    private boolean running;
    private final MyLogger l = new MyLogger("ClientCliThread");
    public CliThread(AdminClient ac) {
        this.running = true;
        this.ac = ac;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(running) {
            try {
                String s = br.readLine();
                l.log(s);
                switch (s) {
                    case "robotList":
                        l.log("Robot list:");
                        l.log(ac.restGetRobots());
                        break;
                    case "averageN":
                        l.log("insert robot ID:");
                        String crpID = br.readLine();
                        l.log("Insert number of readings to consider:");
                        Integer n = Integer.valueOf(br.readLine());
                        l.log(ac.restGetAverageN(crpID,n));
                        break;
                    case "averageTime":
                        l.log("insert starting date: (format like: '2011-12-03 10:15:30')");
                        String start = br.readLine().trim().replace(' ','T')+"Z";
                        Instant in = Instant.parse(start);
                        l.log("insert ending date: (format like: '2011-12-03 10:15:30)");
                        String end = br.readLine().trim().replace(' ','T')+"Z";
                        l.log(ac.restGetAverageN(start,end));
                        break;
                    default:
                        l.error("Unrecognized command");
                        break;
                }
            } catch (IOException e) {
                l.error("Failed to read input from CLI: "+e.getMessage());
            }
        }
    }
}
