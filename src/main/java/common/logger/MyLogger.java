package common.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MyLogger {
  private final DateTimeFormatter dateFormatter;
  private final boolean printThread;
  String identifier;

  private static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_RESET = "\u001B[0m";
  public MyLogger(String id) {
    this.identifier = id;
    this.dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    this.printThread = true;
  }
  public MyLogger(String id, boolean printThread) {
    this.identifier = id;
    this.dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    this.printThread = printThread;
  }

  public void log(Object o) {
    String toPrint = formatter(o);
    System.out.printf(toPrint);
  }
  public void error(Object o) {
    String toPrint = formatter(o);
    System.out.printf(ANSI_RED+toPrint+ANSI_RESET);
  }
  public void warn(Object o) {
    String toPrint = formatter(o);
    System.out.printf(ANSI_YELLOW+toPrint+ANSI_RESET);
  }

  private String formatter(Object o) {
    String s = o.toString();
    String date = dateFormatter.format(LocalDateTime.now());
    String threadName = Thread.currentThread().getName().trim();
    if(this.printThread && !threadName.equals(identifier)) {
      return String.format("%s [%s,%s] %s \n",date,identifier,Thread.currentThread().getName(),s);
    } else {
      return String.format("%s [%s] %s \n",date,identifier,s);
    }
  }

  public static void main(String[] args) {
    MyLogger logger = new MyLogger("Test");
    ArrayList<Integer> integerArrayList = new ArrayList<>();
    integerArrayList.add(10);
    integerArrayList.add(20);
    logger.log("Normal log");
    logger.warn("Warning log");
    logger.error("Error log");
  }
}
