package at.ac.tuwien.ec.thesis.sleipnir;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.ThesisSettings;
import at.ac.tuwien.ec.thesis.algorithms.ThesisOffloadScheduler;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPBattery;
import at.ac.tuwien.ec.thesis.algorithms.cpop.CPOPRuntime;
import at.ac.tuwien.ec.thesis.algorithms.heft.ThesisHEFTBattery;
import at.ac.tuwien.ec.thesis.algorithms.heft.ThesisHEFTRuntime;
import at.ac.tuwien.ec.thesis.algorithms.kdla.KDLABattery;
import at.ac.tuwien.ec.thesis.algorithms.kdla.KDLARuntime;
import at.ac.tuwien.ec.thesis.algorithms.mmolb.MMOLBBattery;
import at.ac.tuwien.ec.thesis.algorithms.mmolb.MMOLBRuntime;
import at.ac.tuwien.ec.thesis.algorithms.peft.ThesisPEFTBattery;
import at.ac.tuwien.ec.thesis.algorithms.peft.ThesisPEFTRuntime;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import scala.Tuple5;

public class ThesisEvaluationMain {

  public static void main(String[] arg) throws Exception {
    processArgs(arg);
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);

    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
    Date date = new Date();

    String fileName = SimulationSetup.outfile + dateFormat.format(date) + "_" + ThesisSettings.fileName;
    File outFile = new File(fileName);
    if (!outFile.exists()) {
      outFile.getParentFile().mkdirs();
      try {
        outFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    PrintWriter writer = new PrintWriter(outFile, "UTF-8");
    writer.println(ThesisSettings.header);

    for (String application : ThesisSettings.mobileApplications) {
      SimulationSetup.mobileApplication = application;

      for (String algorithm : ThesisSettings.algorithms) {
        SimulationSetup.algorithms = new String[] {algorithm};
        SimulationSetup.mobileNum = 100;
        SimulationSetup.appNumber = 30;

        execute(algorithm, writer);

        /*
        for (int count : ThesisSettings.parallelInstances) {
          SimulationSetup.mobileNum = count;
          SimulationSetup.appNumber = 1;
          execute(algorithm, writer);
        }

        for (int count : ThesisSettings.sequentialInstances) {
          SimulationSetup.mobileNum = 1;
          SimulationSetup.appNumber = count;
          execute(algorithm, writer);
        }

         */
      }
    }

    writer.flush();
    writer.close();

    System.out.println("Finished > " + fileName);
  }

  private static void execute(String algoName, PrintWriter writer) {
    SparkConf configuration = new SparkConf();
    configuration.setMaster("local");
    configuration.setAppName("Sleipnir");

    JavaSparkContext jscontext = new JavaSparkContext(configuration);
    ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples =
        SetupHelper.generateSamples();

    String data =
        SimulationSetup.mobileApplication
            + "/"
            + algoName
            + "-parallel="
            + SimulationSetup.mobileNum
            + "-sequential="
            + SimulationSetup.appNumber;

    JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> histogram =
        runSparkSimulation(jscontext, inputSamples, algoName);

    System.out.println("Executing: " + data);

    Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> mostFrequent =
        histogram.max(new FrequencyComparator());

    writer.printf(
        "%s;%s;%s;%s;%s;%s;%s;%s;%s\n",
        SimulationSetup.mobileApplication,
        algoName,
        SimulationSetup.mobileNum,
        SimulationSetup.appNumber,
        mostFrequent._2()._2(), // runtime
        mostFrequent._2()._4(), // battery
        mostFrequent._2()._5(), // execution_time
        mostFrequent._2()._4() / SimulationSetup.batteryCapacity,
        SimulationSetup.batteryCapacity
        );
    writer.flush();

    jscontext.close();
  }

  private static JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>
      runSparkSimulation(
          JavaSparkContext jscontext,
          ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples,
          String algoritmName) {

    JavaRDD<Tuple2<MobileApplication, MobileCloudInfrastructure>> input =
        jscontext.parallelize(inputSamples);

    JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> results =
        input.flatMapToPair(
            new PairFlatMapFunction<
                Tuple2<MobileApplication, MobileCloudInfrastructure>,
                OffloadScheduling,
                Tuple5<Integer, Double, Double, Double, Double>>() {
              private static final long serialVersionUID = 1L;

              @Override
              public Iterator<
                      Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>>
                  call(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues) {
                ArrayList<
                        Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>>
                    output =
                        new ArrayList<
                            Tuple2<
                                OffloadScheduling,
                                Tuple5<Integer, Double, Double, Double, Double>>>();
                ThesisOffloadScheduler singleSearch;
                switch (algoritmName) {
                  case "heft-r":
                    singleSearch = new ThesisHEFTRuntime(inputValues);
                    break;
                  case "heft-b":
                    singleSearch = new ThesisHEFTBattery(inputValues);
                    break;
                  case "cpop-r":
                    singleSearch = new CPOPRuntime(inputValues);
                    break;
                  case "cpop-b":
                    singleSearch = new CPOPBattery(inputValues);
                    break;
                  case "kdla-r":
                    singleSearch = new KDLARuntime(inputValues);
                    break;
                  case "kdla-b":
                    singleSearch = new KDLABattery(inputValues);
                    break;
                  case "peft-r":
                    singleSearch = new ThesisPEFTRuntime(inputValues);
                    break;
                  case "peft-b":
                    singleSearch = new ThesisPEFTBattery(inputValues);
                    break;
                  case "mmolb-r":
                    singleSearch = new MMOLBRuntime(inputValues);
                    break;
                  case "mmolb-b":
                    singleSearch = new MMOLBBattery(inputValues);
                    break;
                  default:
                    singleSearch = new ThesisHEFTRuntime(inputValues);
                }

                ArrayList<OffloadScheduling> offloads = singleSearch.findScheduling();
                if (offloads != null)
                  for (OffloadScheduling os : offloads) {
                    output.add(
                        new Tuple2<>(
                            os,
                            new Tuple5<>(
                                1,
                                os.getRunTime(),
                                os.getUserCost(),
                                os.getBatteryLifetime(),
                                os.getExecutionTime())));
                  }
                return output.iterator();
              }
            });

    // System.out.println(results.first());

    JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> aggregation =
        results.reduceByKey(
            new Function2<
                Tuple5<Integer, Double, Double, Double, Double>,
                Tuple5<Integer, Double, Double, Double, Double>,
                Tuple5<Integer, Double, Double, Double, Double>>() {
              /** */
              private static final long serialVersionUID = 1L;

              @Override
              public Tuple5<Integer, Double, Double, Double, Double> call(
                  Tuple5<Integer, Double, Double, Double, Double> off1,
                  Tuple5<Integer, Double, Double, Double, Double> off2)
                  throws Exception {
                // TODO Auto-generated method stub
                return new Tuple5<Integer, Double, Double, Double, Double>(
                    off1._1() + off2._1(),
                    off1._2() + off2._2(),
                    off1._3() + off2._3(),
                    off1._4() + off2._4(),
                    off1._5() + off2._5());
              }
            });

    // System.out.println(aggregation.first());

    JavaPairRDD<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> histogram =
        aggregation.mapToPair(
            new PairFunction<
                Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>,
                OffloadScheduling,
                Tuple5<Integer, Double, Double, Double, Double>>() {

              private static final long serialVersionUID = 1L;

              @Override
              public Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>
                  call(
                      Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>
                          arg0)
                      throws Exception {
                Tuple5<Integer, Double, Double, Double, Double> val = arg0._2();
                Tuple5<Integer, Double, Double, Double, Double> tNew =
                    new Tuple5<Integer, Double, Double, Double, Double>(
                        val._1(),
                        val._2() / val._1(),
                        val._3() / val._1(),
                        val._4() / val._1(),
                        (val._5() / SimulationSetup.iterations) / 1e6);

                return new Tuple2<
                    OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>(
                    arg0._1, tNew);
              }
            });
    return histogram;
  }

  private static void processArgs(String[] args) {
    for (String s : args) {
      if (s.startsWith("-mapM=")) {
        String[] tmp = s.split("=");
        SimulationSetup.MAP_M = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-mapN=")) {
        String[] tmp = s.split("=");
        SimulationSetup.MAP_N = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-edgePlanning=")) {
        String[] tmp = s.split("=");
        SimulationSetup.edgePlanningAlgorithm = tmp[1];
        continue;
      }
      if (s.startsWith("-mobile=")) {
        String[] tmp = s.split("=");
        SimulationSetup.mobileNum = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-traceIn=")) {
        String[] tmp = s.split("=");
        SimulationSetup.electricityTraceFile = tmp[1];
        continue;
      }
      if (s.startsWith("-outfile=")) {
        String[] tmp = s.split("=");
        SimulationSetup.outfile = tmp[1];
        continue;
      }
      if (s.startsWith("-iter=")) {
        String[] tmp = s.split("=");
        SimulationSetup.iterations = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-battery=")) {
        String[] tmp = s.split("=");
        SimulationSetup.batteryCapacity = Double.parseDouble(tmp[1]);
        continue;
      }
      if (s.startsWith("-cloud=")) {
        String[] tmp = s.split("=");
        SimulationSetup.cloudNum = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-edge=")) {
        String[] tmp = s.split("=");
        SimulationSetup.edgeNodes = Integer.parseInt(tmp[1]);
        continue;
      }
      if (s.startsWith("-wl-runs=")) {
        String[] tmp = s.split("=");
        String[] input = tmp[1].split(",");
        int[] wlRuns = new int[input.length];
        for (int i = 0; i < input.length; i++) wlRuns[i] = Integer.parseInt(input[i]);
        SimulationSetup.appNumber = wlRuns[0];
        continue;
      }
      if (s.equals("-batch")) {
        SimulationSetup.batch = true;
        continue;
      }
      if (s.startsWith("-map-size=")) {
        String[] tmp = s.split("=");
        SimulationSetup.navigatorMapSize = (Double.parseDouble(tmp[1]) * 1e3);
        continue;
      }
      if (s.startsWith("-file-size=")) {
        String[] tmp = s.split("=");
        // 1/input, to be used for lambda of exponential distribution
        SimulationSetup.antivirusFileSize = (Double.parseDouble(tmp[1]) * 1e3);
        continue;
      }
      if (s.startsWith("-image-size=")) {
        String[] tmp = s.split("=");
        SimulationSetup.facerecImageSize = (Double.parseDouble(tmp[1]) * 1e3);
        continue;
      }
      if (s.startsWith("-latency=")) {
        String[] tmp = s.split("=");
        SimulationSetup.lambdaLatency = (int) (Double.parseDouble(tmp[1]));
        continue;
      }
      if (s.startsWith("-chess-mi=")) {
        String[] tmp = s.split("=");
        SimulationSetup.chess_mi = (1.0 / Double.parseDouble(tmp[1]));
        continue;
      }
      if (s.startsWith("-alpha=")) {
        String[] tmp = s.split("=");
        SimulationSetup.EchoAlpha = Double.parseDouble(tmp[1]);
      }
      if (s.startsWith("-beta=")) {
        String[] tmp = s.split("=");
        SimulationSetup.EchoBeta = Double.parseDouble(tmp[1]);
      }
      if (s.startsWith("-gamma=")) {
        String[] tmp = s.split("=");
        SimulationSetup.EchoGamma = Double.parseDouble(tmp[1]);
      }
      if (s.startsWith("-eta=")) {
        String[] tmp = s.split("=");
        SimulationSetup.Eta = Double.parseDouble(tmp[1]);
        continue;
      }
      if (s.startsWith("-app=")) {
        String[] tmp = s.split("=");
        SimulationSetup.mobileApplication = tmp[1];
        continue;
      }
      if (s.startsWith("-algo=")) {
        String[] tmp = s.split("=");
        SimulationSetup.algorithms = tmp[1].split(",");
        continue;
      }
      if (s.equals("-cloudonly")) SimulationSetup.cloudOnly = true;
    }
  }

  static class FrequencyComparator
      implements Serializable,
          Comparator<Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>>> {

    /** */
    private static final long serialVersionUID = -2034500309733677393L;

    public int compare(
        Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> o1,
        Tuple2<OffloadScheduling, Tuple5<Integer, Double, Double, Double, Double>> o2) {
      // TODO Auto-generated method stub
      return o1._2()._1() - o2._2()._1();
    }
  }
}
