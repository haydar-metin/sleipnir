package at.ac.tuwien.ec.thesis.sleipnir;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.ThesisSettings;
import at.ac.tuwien.ec.thesis.algorithms.ThesisOffloadScheduler;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPBattery;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPBatteryScored;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPRuntime;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPRuntimeScored;
import at.ac.tuwien.ec.thesis.algorithms.heft.ThesisHEFTBattery;
import at.ac.tuwien.ec.thesis.algorithms.heft.ThesisHEFTRuntime;
import at.ac.tuwien.ec.thesis.algorithms.kdla.KDLABattery;
import at.ac.tuwien.ec.thesis.algorithms.kdla.KDLARuntime;
import at.ac.tuwien.ec.thesis.algorithms.mmolb.MMOLBBattery;
import at.ac.tuwien.ec.thesis.algorithms.mmolb.MMOLBRuntime;
import at.ac.tuwien.ec.thesis.algorithms.peft.ThesisPEFTBattery;
import at.ac.tuwien.ec.thesis.algorithms.peft.ThesisPEFTRuntime;
import at.ac.tuwien.ec.thesis.infrastructure.ThesisNetworkPlanner;
import at.ac.tuwien.ec.thesis.software.ThesisMobileWorkload;
import at.ac.tuwien.ec.thesis.software.apps.ThesisWorkloadGenerator;
import java.util.ArrayList;
import scala.Tuple2;

public class ThesisMain {
  public static void main(String[] args) {
    System.out.println("Testing started");
    SimulationSetup.mobileNum = 100;
    SimulationSetup.appNumber = 10;
    SimulationSetup.mobileApplication = "NAVI";

    ThesisSettings.EnableProgressDebug = false;
    ThesisSettings.ScoreAlpha = 0.8;
    boolean isDebug = false;
    String[] names =
        new String[] {
          "HEFT-R", "HEFT-B", "CPOP-R", "CPOP-B", "KDLA-R", "KDLA-B", "PEFT-R", "PEFT-B", "LLOBM-R",
          "LLOBM-B", "CPOP-RS", "CPOP-BS"
        };
    // Integer[] ids = new Integer[] {0, 1, 2, 3, 4, 5, 8, 9};
    Integer[] ids = new Integer[] {2, 3, 10, 11};
    int rounds = 1;

    for (Integer id : ids) {
      double avgRunTime = 0;
      double avgBatteryConsumption = 0;
      double avgExecutionTime = 0;

      ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples =
          generateSamples(rounds);
      int i = 0;
      for (Tuple2<MobileApplication, MobileCloudInfrastructure> sample : inputSamples) {
        ThesisOffloadScheduler scheduler = null;

        switch (id) {
          case 0:
            scheduler = new ThesisHEFTRuntime(sample);
            break;
          case 1:
            scheduler = new ThesisHEFTBattery(sample);
            break;
          case 2:
            scheduler = new CPOPRuntime(sample);
            break;
          case 3:
            scheduler = new CPOPBattery(sample);
            break;
          case 4:
            scheduler = new KDLARuntime(sample);
            break;
          case 5:
            scheduler = new KDLABattery(sample);
            break;
          case 6:
            scheduler = new ThesisPEFTRuntime(sample);
            break;
          case 7:
            scheduler = new ThesisPEFTBattery(sample);
            break;
          case 8:
            scheduler = new MMOLBRuntime(sample);
            break;
          case 9:
            scheduler = new MMOLBBattery(sample);
            break;
          case 10:
            scheduler = new CPOPRuntimeScored(sample);
            break;
          case 11:
            scheduler = new CPOPBatteryScored(sample);
            break;
        }

        ArrayList<OffloadScheduling> offloads = scheduler.findScheduling();

        if (offloads != null) {
          for (OffloadScheduling os : offloads) {
            if (isDebug) {
              os.forEach(
                  (key, value) -> {
                    System.out.println(key.getId() + "->" + value.getId());
                  });

              System.out.println(
                  "[i] = "
                      + i
                      + " | "
                      + os.getRunTime()
                      + ", "
                      + os.getBatteryLifetime()
                      + " [ "
                      + os.getExecutionTime() / Math.pow(10, 9)
                      + "]");
            }
            avgRunTime += os.getRunTime();
            avgBatteryConsumption += os.getBatteryLifetime();
            avgExecutionTime += os.getExecutionTime();
          }
        }

        i++;
      }

      double avg_seconds = (avgExecutionTime / rounds) / Math.pow(10, 9);
      double sum_seconds = (avgExecutionTime) / Math.pow(10, 9);

      System.out.println();
      System.out.println(
          "["
              + names[id]
              + "] Result: "
              + avgRunTime / rounds
              + ", "
              + (avgBatteryConsumption / rounds) / SimulationSetup.batteryCapacity
              + " ["
              + avg_seconds
              + ", "
              + sum_seconds
              + "]");
    }
  }

  public static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>>
  generateSamples(int iterations) {
    ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> samples =
        new ArrayList<>();

    for (int i = 0; i < iterations; i++) {
      ThesisMobileWorkload globalWorkload = new ThesisMobileWorkload();
      ThesisWorkloadGenerator generator = new ThesisWorkloadGenerator();

      for (int j = 0; j < SimulationSetup.mobileNum; j++) {
        globalWorkload.joinParallel(
            generator.setupWorkload(SimulationSetup.appNumber, "mobile_" + j));
      }

      MobileCloudInfrastructure inf = new MobileCloudInfrastructure();

      DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
      EdgeAllCellPlanner.setupEdgeNodes(inf);
      DefaultMobileDevicePlanner.setupMobileDevices(inf, SimulationSetup.mobileNum);
      ThesisNetworkPlanner.setupNetworkConnections(inf);

      Tuple2<MobileApplication, MobileCloudInfrastructure> singleSample =
          new Tuple2<>(globalWorkload, inf);

      samples.add(singleSample);
    }

    return samples;
  }
}
