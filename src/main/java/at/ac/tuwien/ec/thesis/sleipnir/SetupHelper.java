package at.ac.tuwien.ec.thesis.sleipnir;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.infrastructure.ThesisNetworkPlanner;
import at.ac.tuwien.ec.thesis.software.ThesisMobileWorkload;
import at.ac.tuwien.ec.thesis.software.apps.ThesisWorkloadGenerator;
import java.util.ArrayList;
import scala.Tuple2;

class SetupHelper {
  public static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> generateSamples() {
    ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> samples = new ArrayList<>();

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

    return samples;
  }
}
