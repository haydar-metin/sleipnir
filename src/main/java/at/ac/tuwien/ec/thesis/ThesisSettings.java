package at.ac.tuwien.ec.thesis;

public class ThesisSettings {
  public static int kdla_k = 5;

  public static String header = "App;Algorithm;Parallel;Sequential;Runtime;Battery;Execution Time;Battery Percentage;Battery Capacity";

  public static int[] parallelInstances = {10, 30, 50, 70, 100};
  public static int[] sequentialInstances = {10, 30, 50, 70, 100};

  public static String[] algorithms = {"heft-r", "heft-b", "cpop-r", "cpop-b", "kdla-r", "kdla-b", "peft-r", "peft-b", "mmolb-r", "mmolb-b" };
  public static String[] mobileApplications = { "NAVI", "CHESS", "ANTIVIRUS", "FACEREC", "FACEBOOK"};
}
