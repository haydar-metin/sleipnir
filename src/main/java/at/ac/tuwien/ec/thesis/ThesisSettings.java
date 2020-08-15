package at.ac.tuwien.ec.thesis;

public class ThesisSettings {
  public static boolean EnableProgressDebug = false;

  public static int kdla_k = 5;
  public static double ScoreAlpha;

  public static String header = "App;Algorithm;Parallel;Sequential;Runtime;Battery;Execution Time;Battery Percentage;Battery Capacity;Alpha";
  public static String fileName = "thesis_scored.csv";

  public static int[] parallelInstances = {100};
  public static int[] sequentialInstances = {10, 30, 50, 70, 100};

  // public static String[] algorithms = {"mobile", "heft-r", "heft-b", "cpop-r", "cpop-b", "kdla-r", "kdla-b", "mmolb-r", "mmolb-b" }; // , "peft-r", "peft-b"
  public static String[] algorithms = {"mobile", "cpop-rs", "cpop-bs"};

  // public static String[] mobileApplications = { "NAVI", "CHESS", "ANTIVIRUS", "FACEREC", "FACEBOOK"};
  public static String[] mobileApplications = { "NAVI"};

}
