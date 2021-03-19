package cubix.data;

import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<Date> {

	public int compare(Date d1, Date d2) {
		return d1.before(d2)? 1 : -1;
	}

}
