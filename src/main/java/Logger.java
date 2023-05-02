import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger {
  private final DateTimeFormatter dateFormatter;
  String identifier;

  private static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_RESET = "\u001B[0m";
  public Logger(String id) {
    this.identifier = id;
    this.dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
  }
  private void log(Object o) {
    String s = o.toString();
    String date = dateFormatter.format(LocalDateTime.now());
    String toPrint = String.format("%s [%s] %s \n",date,identifier,s);
    System.out.printf(toPrint);
  }
  private void error(Object o) {
    String s = o.toString();
    String date = dateFormatter.format(LocalDateTime.now());
    String toPrint = String.format("%s [%s] %s \n",date,identifier,s);
    System.out.printf(ANSI_RED+toPrint+ANSI_RESET);
  }
  private void warn(Object o) {
    String s = o.toString();
    String date = dateFormatter.format(LocalDateTime.now());
    String toPrint = String.format("%s [%s] %s \n",date,identifier,s);
    System.out.printf(ANSI_YELLOW+toPrint+ANSI_RESET);
  }

  public static void main(String[] args) {
    Logger logger = new Logger("Test");
    ArrayList<Integer> integerArrayList = new ArrayList<>();
    integerArrayList.add(10);
    integerArrayList.add(20);
    logger.log("Normal log");
    logger.warn("Warning log");
    logger.error("Error log");
  }
}
