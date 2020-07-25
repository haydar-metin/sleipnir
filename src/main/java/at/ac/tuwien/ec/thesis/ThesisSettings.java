package at.ac.tuwien.ec.thesis;

public class ThesisSettings {
  public static boolean EnableProgressDebug = false;

  public static int kdla_k = 5;
  public static double ScoreAlpha;

  public static String header = "App;Algorithm;Parallel;Sequential;Runtime;Battery;Execution Time;Battery Percentage;Battery Capacity;Alpha";
  public static String fileName = "thesis_trade_off_navi_extended.csv";

  public static int[] parallelInstances = {10, 30, 50, 70, 100};
  public static int[] sequentialInstances = {10, 30, 50, 70, 100};

  public static String[] algorithms = {"mobile", "cpop-rs", "cpop-bs"};
  // public static String[] mobileApplications = { "NAVI", "CHESS", "ANTIVIRUS", "FACEREC", "FACEBOOK"};
  public static String[] mobileApplications = { "NAVI"};

}
