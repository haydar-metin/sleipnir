package at.ac.tuwien.ec.thesis.algorithms.cpop;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.thesis.ThesisSettings;
import at.ac.tuwien.ec.thesis.algorithms.utils.CalcUtils;
import scala.Tuple2;

public class CPOPBatteryScored extends BaseCPOP {
  public CPOPBatteryScored(MobileApplication A, MobileCloudInfrastructure I) {
    super(A, I);
  }

  public CPOPBatteryScored(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    super(t);
  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    ComputationalNode target = null;

    if (criticalPath.get(currTask.getUserId()).contains(currTask)) {
      ComputationalNode node = bestNode.get(currTask.getUserId());
      if (isValid(scheduling, currTask, node)) {
        target = node;
      }
    } else {
      double minRuntime = Double.MAX_VALUE;
      double minEnergy = Double.MAX_VALUE;

      double maxRuntime = Double.MIN_VALUE;
      double maxEnergy = Double.MIN_VALUE;

      double bestScore = Double.MAX_VALUE;

      for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(currTask.getUserId()))
        if (isMaxValid(scheduling, currTask, cn)) {
          double tmpRuntime =
              CalcUtils.calcEFT(currTask, scheduling, cn, currentApp, currentInfrastructure);
          double tmpEnergy;
          if (currentInfrastructure.getMobileDevices().containsValue(cn)) {
            tmpEnergy =
                cn.getCPUEnergyModel().computeCPUEnergy(currTask, cn, currentInfrastructure)
                    * currTask.getLocalRuntimeOnNode(cn, currentInfrastructure);
          } else {
            tmpEnergy =
                currentInfrastructure
                    .getNodeById(currTask.getUserId())
                    .getNetEnergyModel()
                    .computeNETEnergy(currTask, cn, currentInfrastructure)
                    * currentInfrastructure.getTransmissionTime(
                    currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
          }

          if (tmpRuntime < minRuntime) minRuntime = tmpRuntime;
          if (tmpRuntime > maxRuntime) maxRuntime = tmpRuntime;

          if (tmpEnergy < minEnergy) minEnergy = tmpEnergy;
          if (tmpEnergy > maxEnergy) maxEnergy = tmpEnergy;
        }

      double tmpScore;
      for (ComputationalNode cn :
          currentInfrastructure.getAllNodesWithMobile(currTask.getUserId())) {

        if (isMaxValid(scheduling, currTask, cn)) {
          tmpScore = computeScore(scheduling, currTask, cn, minRuntime, minEnergy, maxRuntime, maxEnergy);

          if (tmpScore < bestScore) {
            bestScore = tmpScore;
            target = cn;
          }
        }
      }
    }

    return target;
  }

  @Override
  protected void initBestNodes() {
    this.criticalPath.forEach(
        (userId, value) -> {
          double bestEnergy = Double.MAX_VALUE;
          ComputationalNode node = null;

          for (ComputationalNode cn : currentInfrastructure.getAllNodesWithMobile(userId)) {
            double result =
                value.stream()
                    .reduce(
                        0.0,
                        (acc, currTask) -> {
                          double energy;

                          if (currentInfrastructure.getMobileDevices().containsValue(cn)) {
                            energy =
                                cn.getCPUEnergyModel().computeCPUEnergy(currTask, cn, currentInfrastructure)
                                    * currTask.getLocalRuntimeOnNode(cn, currentInfrastructure);
                          } else {
                            energy =
                                currentInfrastructure
                                    .getNodeById(currTask.getUserId())
                                    .getNetEnergyModel()
                                    .computeNETEnergy(currTask, cn, currentInfrastructure)
                                    * currentInfrastructure.getTransmissionTime(
                                    currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
                          }

                          return acc + energy;
                        },
                        Double::sum);

            if (result < bestEnergy) {
              bestEnergy = result;
              node = cn;
            }
          }

          this.bestNode.put(userId, node);
        });
  }

  private double computeScore(
      OffloadScheduling scheduling,
      MobileSoftwareComponent currTask,
      ComputationalNode cn,
      double minRuntime,
      double minEnergy,
      double maxRuntime,
      double maxEnergy) {
    double currRuntime = CalcUtils.calcEFT(currTask, scheduling, cn, currentApp, currentInfrastructure);
    double currEnergy;
    if (currentInfrastructure.getMobileDevices().containsValue(cn)) {
      currEnergy =
          cn.getCPUEnergyModel().computeCPUEnergy(currTask, cn, currentInfrastructure)
              * currTask.getLocalRuntimeOnNode(cn, currentInfrastructure);
    } else {
      currEnergy =
          currentInfrastructure
              .getNodeById(currTask.getUserId())
              .getNetEnergyModel()
              .computeNETEnergy(currTask, cn, currentInfrastructure)
              * currentInfrastructure.getTransmissionTime(
              currTask, currentInfrastructure.getNodeById(currTask.getUserId()), cn);
    }

    // max = 4
    // curr = 2

    double runtimeDiff = Math.pow(currRuntime - minRuntime,2.0);
    double energyDiff = Math.pow(currEnergy - minEnergy,2.0);

    return ThesisSettings.ScoreAlpha * adjust(runtimeDiff,1.0) +  (1 - ThesisSettings.ScoreAlpha) * adjust(energyDiff,1.0);
  }


  private double adjust(double x, double factor){
    return x * factor;
  }
}
