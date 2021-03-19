package cubix.helper;

import java.util.Comparator;
import java.util.HashMap;

public class TimeNodeDegreeComparator<N> implements Comparator<N> {

	private HashMap<N, Integer> degrees;

	public TimeNodeDegreeComparator(HashMap<N, Integer> degrees){
		this.degrees = degrees;
	}
	
	public int compare(N n, N m) {
		return degrees.get(m) - degrees.get(n);
	}

}
