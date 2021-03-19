package cubix.data;

import java.util.Comparator;
import java.util.Date;

public class TimeComparator<T> implements Comparator<T> {

	public int compare(T t, T r) {
		if(t instanceof Long){
			long diff = (((Long) t) - ((Long) r));
			return diff>0?1:-1;
		}
		else if(t instanceof CTime){
			long diff = (((CTime) t).getID() - ((CTime) r).getID());
			return diff>0?1:-1;
		}

		return 0;

	}

}
