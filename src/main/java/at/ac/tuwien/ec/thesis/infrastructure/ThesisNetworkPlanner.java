package at.ac.tuwien.ec.thesis.infrastructure;

import static java.util.Arrays.asList;

import at.ac.tuwien.ec.model.QoS;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

/* In the default planner, there is a link between each mobile device and each computational node.
 * 
 */

public class ThesisNetworkPlanner {
	public static void setupNetworkConnections(MobileCloudInfrastructure inf)
	{
		for(MobileDevice d: inf.getMobileDevices().values())
		{
			double firstHopWiFiHQBandwidth = 32.0;
			QoSProfile qosUL;//,qosDL;
			qosUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0, firstHopWiFiHQBandwidth), 1.0)
					));

			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(d,en,qosUL);
				inf.addLink(en,d,qosUL);
			}
			double CloudWiFiHQBandwidth = 16.0;
			double cloudLatency = SimulationSetup.MAP_M;

			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 1.0)));

			for(CloudDataCenter cn : inf.getCloudNodes().values())
			{
				inf.addLink(d, cn, qosCloudUL);
				inf.addLink(cn, d, qosCloudUL);
			}

		}
		
		for(CloudDataCenter cn : inf.getCloudNodes().values())
		{
			
			double CloudWiFiHQBandwidth = 16.0;
			double cloudLatency = SimulationSetup.MAP_M;
			
			QoSProfile qosCloudUL;//,qosCloudDL
			qosCloudUL = new QoSProfile(asList(
					new Tuple2<QoS,Double>(new QoS(15.0 + cloudLatency, CloudWiFiHQBandwidth), 1.0)));

			for(EdgeNode en : inf.getEdgeNodes().values()) 
			{
				inf.addLink(en, cn, qosCloudUL);
				inf.addLink(cn, en, qosCloudUL);
			}
		}
		
		
	}
}


