package at.ac.tuwien.ec.thesis.software.apps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.software.ThesisMobileApplication;

public class AntivirusApp extends ThesisMobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7200390062022627285L;

	public AntivirusApp()
	{
		super();
	}
	
	public AntivirusApp(int wId){
		super(wId);
	}
	
	public AntivirusApp(int wId, String uid)
	{
		super(wId,uid);
	}
	
	@Override
	public void setupTasks() {
		addComponent("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				,4.0e3*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
        		,5e+3*SimulationSetup.task_multiplier
        		,false);
		addComponent("LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2e3*SimulationSetup.task_multiplier
				,5e+3*SimulationSetup.task_multiplier
				,10e+3*SimulationSetup.task_multiplier
				);
		addComponent("SCAN_FILE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,2,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e3*SimulationSetup.task_multiplier
				,55
				,5e+3*SimulationSetup.task_multiplier
				);
		addComponent("COMPARE"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1,1,1)
				,getUserId()
				//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+2.0)
				,2.0e3*SimulationSetup.task_multiplier
				,104
				,5e+3*SimulationSetup.task_multiplier
				);
		addComponent("ANTIVIRUS_OUTPUT"+"_"+getWorkloadId()+","+getUserId()
				,new Hardware(1, 1, 1)
				,getUserId()
				//,ExponentialDistributionGenerator.getNext(1.0/2.0) + 2.0
				,2e3*SimulationSetup.task_multiplier
				,1e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
				,false
				);
	}

	@Override
	public void setupLinks() {
		addLink("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId(), "LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("ANTIVIRUS_UI"+"_"+getWorkloadId()+","+getUserId(), "SCAN_FILE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				5);
		addLink("LOAD_DEFINITIONS"+"_"+getWorkloadId()+","+getUserId(),"COMPARE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("SCAN_FILE"+"_"+getWorkloadId()+","+getUserId(),"COMPARE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("COMPARE"+"_"+getWorkloadId()+","+getUserId(),"ANTIVIRUS_OUTPUT"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
	}

	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub
		
	}

}
