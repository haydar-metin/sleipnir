package at.ac.tuwien.ec.thesis.software.apps;

import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.software.ThesisMobileWorkload;

public class ThesisWorkloadGenerator {

  public ThesisMobileWorkload setupWorkload(int appExecutions, String mobileId) {
    ThesisMobileWorkload mwl = new ThesisMobileWorkload();
    mwl.setUserId(mobileId);
    mwl.setWorkloadId(0);
    String sApp;

    for (int i = 0; i < appExecutions; i++) {
      sApp = SimulationSetup.mobileApplication;
      switch (sApp) {
        case "NAVI":
          mwl.joinSequentially(new NavigatorApp(i, mobileId));
          break;
        case "CHESS":
          mwl.joinSequentially(new ChessApp(i, mobileId));
          break;
        case "ANTIVIRUS":
          mwl.joinSequentially(new AntivirusApp(i, mobileId));
          break;
        case "FACEREC":
          mwl.joinSequentially(new FacerecognizerApp(i, mobileId));
          break;
        case "FACEBOOK":
          mwl.joinSequentially(new FacebookApp(i, mobileId));
          break;
      }
    }

    return mwl;
  }
}
