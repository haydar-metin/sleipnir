package at.ac.tuwien.ec.thesis.software.apps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.thesis.software.ThesisMobileApplication;
import java.io.Serializable;

public class FacebookApp extends ThesisMobileApplication implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8294521123131080443L;

	public FacebookApp(){
		super();
	}
	
	public FacebookApp(int wId)
	{
		super(wId);
	}
	
	public FacebookApp(int wId, String uid)
	{
		super(wId,uid);
	}
	
	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupTasks() {
		double img_size = SimulationSetup.facebookImageSize;
		addComponent("FACEBOOK_GUI"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		,false
				);
		addComponent("GET_TOKEN"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,3.0e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
				);
		addComponent("POST_REQUEST"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,2.0e3*SimulationSetup.task_multiplier 
        		,1e3//*SimulationSetup.task_multiplier
        		,5e3//*SimulationSetup.task_multiplier
        		);
		addComponent("PROCESS_RESPONSE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,2.0e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("FILE_UPLOAD"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,5.0e3 *SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		,false
        		);
		addComponent("APPLY_FILTER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,8.0e3 *SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier);
		addComponent("FACEBOOK_POST"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				,2.0e3 *SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,false
        		);
	}

	@Override
	public void setupLinks() {
		addLink("FACEBOOK_GUI"+"_"+getWorkloadId()+","+getUserId()
				, "GET_TOKEN"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("FACEBOOK_GUI"+"_"+getWorkloadId()+","+getUserId(),
				"POST_REQUEST"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("GET_TOKEN"+"_"+getWorkloadId()+","+getUserId()
				, "POST_REQUEST"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("POST_REQUEST"+"_"+getWorkloadId()+","+getUserId()
				, "PROCESS_RESPONSE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		//addLink("POST_REQUEST"+"_"+getWorkloadId(), "FILE_UPLOAD"+"_"+getWorkloadId(), Integer.MAX_VALUE, Double.MIN_VALUE);
		addLink("PROCESS_RESPONSE"+"_"+getWorkloadId()+","+getUserId()
				, "FILE_UPLOAD"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("FILE_UPLOAD"+"_"+getWorkloadId()+","+getUserId()
				, "APPLY_FILTER"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("APPLY_FILTER"+"_"+getWorkloadId()+","+getUserId()
				, "FACEBOOK_POST"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		
	}

}
