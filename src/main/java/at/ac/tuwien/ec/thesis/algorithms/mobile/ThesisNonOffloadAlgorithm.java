package at.ac.tuwien.ec.thesis.algorithms.mobile;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.thesis.algorithms.ThesisOffloadScheduler;
import scala.Tuple2;

public class ThesisNonOffloadAlgorithm extends ThesisOffloadScheduler {
  public ThesisNonOffloadAlgorithm(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public ThesisNonOffloadAlgorithm(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {

  }

  @Override
  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {

      return (ComputationalNode) this.currentInfrastructure.getNodeById(currTask.getUserId());
  };

}
