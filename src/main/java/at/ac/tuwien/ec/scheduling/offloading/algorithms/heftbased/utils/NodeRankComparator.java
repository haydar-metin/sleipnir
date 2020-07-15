package at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils;

import java.util.Comparator;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class NodeRankComparator implements Comparator<MobileSoftwareComponent> {

	@Override
	public int compare(MobileSoftwareComponent o1, MobileSoftwareComponent o2) {
		int val = Double.compare(o2.getRank(),o1.getRank());
		if (val == 0) {
			return o1.getId().compareTo(o2.getId());
		}

		return val;
	}

}
