package at.ac.tuwien.ec.thesis.software;

import at.ac.tuwien.ec.model.software.MobileApplication;
import java.io.Serializable;

public abstract class ThesisMobileApplication extends MobileApplication implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;

	public ThesisMobileApplication()
	{
		super();
	}

	public ThesisMobileApplication(int wId)
	{
		super(wId);
	}

	public ThesisMobileApplication(int wId,String uId)
	{
		super(wId, uId);
	}

	@Override
	public double sampleLatency() {
		return Double.MAX_VALUE;
	}
}
