package admin.client.cli;

import admin.client.AdminClient;
import common.logger.MyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeParseException;

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
            l.log("Available commands: robotList(0), averageN(1), averageTime(2), quit(9)");
            try {
                String s = br.readLine();
                l.log(s);
                switch (s) {
                    case "0":
                    case "robotList":
                        l.log("Robot list:");
                        l.log(ac.restGetRobots());
                        break;
                    case "1":
                    case "averageN":
                        l.log("insert robot ID:");
                        String crpID = br.readLine();
                        l.log("Insert number of readings to consider:");
                        Integer n = Integer.valueOf(br.readLine());
                        l.log(ac.restGetAverageN(crpID,n));
                        break;
                    case "2":
                    case "averageTime":
                        String start = getInputDate(br,"starting");
                        String end = getInputDate(br,"ending");
                        l.log(ac.restGetAverageN(start,end));
                        break;
                    case "9":
                    case "quit":
                        l.log("Quitting...");
                        this.running = false;
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
    private String getInputDate(BufferedReader br, String type) throws IOException {
        String s = null;
        while(s==null) {
            l.log("insert "+type+" date: (format like: '2011-12-03 10:15:30')");
            s = br.readLine().trim().replace(' ','T')+"Z";
            try {
                Instant.parse(s);
            }catch (DateTimeParseException e) {
                l.error("Wrong input format");
                s = null;
            }
        }
        return s;
    }
}
