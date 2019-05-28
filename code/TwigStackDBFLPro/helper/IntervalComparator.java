package helper;

import java.util.Comparator;

public class IntervalComparator implements Comparator<Interval>{

	@Override
	public int compare(Interval o1, Interval o2) {
		int rs = o1.mStart - o2.mStart;
		return rs;
	}

}
