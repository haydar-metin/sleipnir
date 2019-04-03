package at.ac.tuwien.ec.sleipnir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;


import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.jgrapht.Graph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.datamodel.DataDistributionGenerator;
import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.datamodel.placement.algorithms.DataPlacementAlgorithm;
import at.ac.tuwien.ec.datamodel.placement.algorithms.FFDCPUPlacement;
import at.ac.tuwien.ec.datamodel.placement.algorithms.RandomDataPlacementAlgorithm;
import at.ac.tuwien.ec.datamodel.placement.algorithms.SteinerTreeHeuristic;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.BestFitCPU;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.FirstFitCPUDecreasing;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.FirstFitCPUIncreasing;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.FirstFitDecreasingSizeVMPlanner;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.VMPlanner;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultIoTPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DistanceBasedNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.model.software.mobileapps.AntivirusApp;
import at.ac.tuwien.ec.model.software.mobileapps.ChessApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacerecognizerApp;
import at.ac.tuwien.ec.model.software.mobileapps.NavigatorApp;
import at.ac.tuwien.ec.model.software.mobileapps.WorkloadGenerator;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HeftEchoResearch;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import scala.Tuple5;

public class SC2019Main {
	
	public static void main(String[] arg)
	{
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		
		class FrequencyComparator implements Comparator<Tuple2<DataPlacement, Tuple4<Integer,Double,Double,Double>>>, Serializable
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -2034500309733677393L;

			@Override
			public int compare(Tuple2<DataPlacement, Tuple4<Integer, Double, Double,Double>> o1,
					Tuple2<DataPlacement, Tuple4<Integer, Double, Double,Double>> o2) {
				// TODO Auto-generated method stub
				return o1._2()._1() - o2._2()._1();
			}
			
		}
				
		processArgs(arg);
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);
		ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>> test = generateSamples(SimulationSetup.iterations);
		
		JavaRDD<Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure>> input = jscontext.parallelize(test);
		
		ArrayList<VMPlanner> planners = new ArrayList<VMPlanner>();
		//planners.add(new FirstFitCPUIncreasing());
		//planners.add(new FirstFitCPUDecreasing());
		//planners.add(new BestFitCPU());
		planners.add(new FirstFitDecreasingSizeVMPlanner());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(SimulationSetup.filename, true));
			writer.append("AREA:\t" + SimulationSetup.area+"\n");
			writer.append("IoT-NUM:\t" + SimulationSetup.iotDevicesNum+"\n");
			writer.append("MOBILE-NUM:\t" + SimulationSetup.mobileNum+"\n");
			writer.append("DATA-RATE:\t" + SimulationSetup.dataRate+"\n");
			writer.append("WORKLOAD:\t" + SimulationSetup.workloadType+"\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
		for(int i = 0; i < planners.size(); i++) {
			VMPlanner currentPlanner = planners.get(i);
			System.out.println(currentPlanner.getClass().getSimpleName());
			JavaPairRDD<DataPlacement,Tuple4<Integer,Double, Double,Double>> results = input.flatMapToPair(new 
					PairFlatMapFunction<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>, 
					DataPlacement, Tuple4<Integer,Double, Double,Double>>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Iterator<Tuple2<DataPlacement, Tuple4<Integer,Double, Double,Double>>> call(Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure> inputValues)
						throws Exception {
					ArrayList<Tuple2<DataPlacement,Tuple4<Integer,Double, Double,Double>>> output = 
							new ArrayList<Tuple2<DataPlacement,Tuple4<Integer,Double, Double,Double>>>();
					//HEFTResearch search = new HEFTResearch(inputValues);
					DataPlacementAlgorithm search;
					switch(SimulationSetup.placementAlgorithm)
					{
					case "FFDCPU":
						search = new FFDCPUPlacement(currentPlanner, inputValues);
						break;
					case "STH":
						search = new SteinerTreeHeuristic(currentPlanner, inputValues);
						break;
					default:
						search = new SteinerTreeHeuristic(currentPlanner, inputValues);
					}
					//RandomDataPlacementAlgorithm search = new RandomDataPlacementAlgorithm(new FirstFitDecreasingSizeVMPlanner(),inputValues);
					//SteinerTreeHeuristic search = new SteinerTreeHeuristic(currentPlanner, inputValues);
					ArrayList<DataPlacement> offloads = (ArrayList<DataPlacement>) search.findScheduling();
					if(offloads != null)
						for(DataPlacement dp : offloads) 
						{
							if(dp!=null)
								output.add(
										new Tuple2<DataPlacement,Tuple4<Integer,Double, Double, Double>>(dp,
												new Tuple4<Integer,Double, Double,Double>(
														1,
														dp.getAverageLatency(),
														dp.getMaxLatency(),
														dp.getCost()
														)));
						}
					return output.iterator();
				}
			});

			//System.out.println(results.first());

			JavaPairRDD<DataPlacement,Tuple4<Integer,Double, Double, Double>> aggregation = 
					results.reduceByKey(
							new Function2<Tuple4<Integer,Double, Double,Double>,
							Tuple4<Integer,Double,Double,Double>,
							Tuple4<Integer,Double,Double,Double>>()
							{
								/**
								 * 
								 */
								private static final long serialVersionUID = 1L;

								@Override
								public Tuple4<Integer, Double, Double, Double> call(
										Tuple4<Integer, Double,Double, Double> off1,
										Tuple4<Integer, Double,Double, Double> off2) throws Exception {
									// TODO Auto-generated method stub
									return new Tuple4<Integer, Double, Double, Double>(
											off1._1() + off2._1(),
											off1._2() + off2._2(),
											off1._3() + off2._3(),
											off1._4() + off2._4()
											);
								}

							}
							);

			//System.out.println(aggregation.first());

			JavaPairRDD<DataPlacement,Tuple4<Integer,Double, Double, Double>> histogram = 
					aggregation.mapToPair(
							new PairFunction<Tuple2<DataPlacement,Tuple4<Integer, Double, Double, Double>>,
							DataPlacement,Tuple4<Integer, Double, Double, Double>>()
							{

								private static final long serialVersionUID = 1L;

								@Override
								public Tuple2<DataPlacement, Tuple4<Integer, Double, Double, Double>> call(
										Tuple2<DataPlacement, Tuple4<Integer, Double, Double, Double>> arg0)
												throws Exception {
									Tuple4<Integer, Double, Double, Double> val = arg0._2();
									Tuple4<Integer, Double, Double, Double> tNew 
									= new Tuple4<Integer, Double, Double, Double>
									(
											val._1(),
											val._2()/val._1(),
											val._3()/val._1(),
											val._4()/val._1()
											);

									return new Tuple2<DataPlacement,Tuple4<Integer, Double, Double, Double>>(arg0._1,tNew);
								}


							}

							);
			
			Tuple2<DataPlacement, Tuple4<Integer, Double, Double, Double>> mostFrequent = histogram.max(new FrequencyComparator());
			try {
				 writer.append("\n");
				 writer.append("VM SELECTION: " + currentPlanner.getClass().getSimpleName()+"\n");
				 writer.append("VM PLACEMENT: " + SimulationSetup.placementAlgorithm+"\n");
				 writer.append("#\tFREQUENCY\tAVERAGE-RT\tMAX-RT\tCOST\tENTRY-NUM\n");
				 writer.append(" \t"+mostFrequent._2()._1()+"\t"+mostFrequent._2()._2()+"\t"
						 +mostFrequent._2()._3()+"\t"+mostFrequent._2()._4() + "\t"+mostFrequent._1.values().size()+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			System.out.println(mostFrequent._2());
			System.out.println(mostFrequent._1.values().size());
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jscontext.close();
	}

	private static ArrayList<Tuple2<ArrayList<DataEntry>, MobileDataDistributionInfrastructure>> generateSamples(int iterations) {
		ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>> samples = new ArrayList<Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>>();
		DataDistributionGenerator ddg = new DataDistributionGenerator(SimulationSetup.dataEntryNum);
		ArrayList<DataEntry> globalWorkload = ddg.getGeneratedData();
		for(int i = 0; i < iterations; i++)
		{
			//ArrayList<DataEntry> globalWorkload = ddg.getGeneratedData();
			//WorkloadGenerator generator = new WorkloadGenerator();
			//globalWorkload = generator.setupWorkload(2, "mobile_0");
			//MobileApplication app = new FacerecognizerApp(0,"mobile_0");
			MobileDataDistributionInfrastructure inf = new MobileDataDistributionInfrastructure();
			DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
			RandomEdgePlanner.setupEdgeNodes(inf);
			DefaultIoTPlanner.setupIoTNodes(inf, SimulationSetup.iotDevicesNum);
			DefaultMobileDevicePlanner.setupMobileDevices(inf,SimulationSetup.mobileNum);
			DefaultNetworkPlanner.setupNetworkConnections(inf);
			Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> singleSample = new Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure>(globalWorkload,inf);
			samples.add(singleSample);
		}
		return samples;
	}

	private static void processArgs(String[] args)
	{
		for(String arg: args)
		{
			if(arg.startsWith("-placement="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.placementAlgorithm = pars[1];
			}
			if(arg.startsWith("-filename="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.filename = pars[1];
			}
			if(arg.startsWith("-area="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.area = pars[1];
				switch(pars[1])
				{
				case "HERNALS":
					SimulationSetup.MAP_M = 6;
					SimulationSetup.MAP_N = 6;
					SimulationSetup.iotDevicesNum = 36;
					SimulationSetup.mobileNum = 24;
					break;
				case "LEOPOLDSTADT":
					SimulationSetup.MAP_M = 10;
					SimulationSetup.MAP_N = 10;
					SimulationSetup.iotDevicesNum = 100;
					SimulationSetup.mobileNum = 40;
					break;
				case "SIMMERING":
					SimulationSetup.MAP_M = 12;
					SimulationSetup.MAP_N = 12;
					SimulationSetup.iotDevicesNum = 144;
					SimulationSetup.mobileNum = 48;
					break;
				}
			}
			if(arg.startsWith("-traffic="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.traffic = pars[1];
			}
			if(arg.startsWith("-workload="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.workloadType = pars[1];
			}
			if(arg.startsWith("-iter="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.iterations = Integer.parseInt(pars[1]);
			}
			if(arg.startsWith("-dr="))
			{
				String[] pars = arg.split("=");
				SimulationSetup.dataRate = Double.parseDouble(pars[1]);
			}
				
		}
	}
	
		//Creates samples for each spark worker
	

}
