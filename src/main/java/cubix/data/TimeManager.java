package cubix.data;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * Manages time selections while user interacts with the system. 
 * This manager does not care about filling and creating the graph.
 * */

public class TimeManager{

	protected  int currentLayer = 0;	// latest time in system.

//	protected static ArrayList<TimeEventListener> timeListeners = new ArrayList<TimeEventListener>();
	
//	protected  ArrayList<Integer> selectedTimes = new ArrayList<Integer>(); 	// can be set by user to keep consistent should not be used extensively, aka current state of system.
//	protected  ArrayList<Integer> selectionHistory = new ArrayList<Integer>(); 	// can be set by user to keep consistent should not be used extensively, aka current state of system.
	
	protected  Date timeMax;
	protected  Date timeMin;
//	protected  int creationTime = 0;
	
	// TIME CONSTANTS
	public static final long SEC = 1000;
	public static final long MIN = 60 * SEC;
	public static final long HOUR = 60 * MIN;
	public static final long DAY = 24 * HOUR;
	public static final long WEEK = 7 * DAY;
	public static final long MONTH = 30 * DAY;
	public static final long YEAR = 365 * DAY;
	
	public static final String AGG_DAY_NAME = "Day";
	public static final String AGG_WEEK_NAME = "Week";
	public static final String AGG_MONTH_NAME = "Month";
	public static final String AGG_YEAR_NAME = "Year";
	public static final String AGG_QUART_NAME = "Quartal";
	public static final String AGG_HOUR_NAME = "Hour";
	public static final String AGG_MINUTE_NAME = "Minute";
	public static final String AGG_SECOND_NAME = "Second";
	
	
	public int currentAggregation = AGG_YEAR;

	// ordered times according to aggregation.
	private ArrayList<Date> times;
	
	public static final int AGG_YEAR = 0;
	public static final int AGG_QUART = 1;
	public static final int AGG_MONTH = 2;
	public static final int AGG_WEEK = 3;
	public static final int AGG_DAY = 4;
	public static final int AGG_HOUR = 5;
	public static final int AGG_MINUTE = 6;
	public static final int AGG_SECOND = 7;
	
	
	
	protected static TimeManager instance;
	
	protected TimeManager()
	{
	}
	
	public static void reset(){
		instance = new TimeManager();
	}
	
	public static TimeManager getInstance(){
		if(instance == null)
			instance = new TimeManager();
		return instance;
	}
	
	
	
	
//	/** Changes the current time layer and then sets new time. If 
//	 * the time is the same as the previous one, the layer is set
//	 * nevertheless.*/
//	public void setCurrentTime(int time, int layer)
//	{
//		currentLayer = layer;
//		setCurrentTime(time);
//	}
//	
//	
//	public void setCurrentTime(int time)
//	{
//		
//		if(time > timeMax || time < 0)
//			return;
//		
//		instance.dispatchTimeEvent(new CurrentTimeSetEvent(instance, selectedTimes));	
//	}


	/////////////////////
	/// CREATION TIME ///
	/////////////////////
	
	public  void startNewCreationTime(Date d)
	{
		if(timeMax.before(d))
			timeMax = d;
		times.add(d);
		Collections.sort(times, new DateComparator());
	}
		
	//////////////////////
	/// EVENT LISTENER ///
	//////////////////////
	
	
//	@Override
//	public void addTimeEventListener(TimeEventListener l) {
//		timeListeners.add(l);
//	}
//	
//	public void addTimeEventListener(TimeEventListener l, int position) {
//		timeListeners.add(position, l);
//		timeListeners.add(l);
//	}
//
//	@Override
//	public void removeTimeListener(TimeEventListener l) {
//		timeListeners.remove(l);	
//	}
//
//	@Override
//	public void removeAllTimeListeners() {	
//		timeListeners = new ArrayList<TimeEventListener>();
//	}
//
//	@Override
//	public void dispatchTimeEvent(TimeEvent e) 
//	{
//		for (TimeEventListener l : timeListeners){
//			l.handleTimeEvent(e);
//		}
//	}
//	
//	
	
	//////////////////////
	/// TIME UTILITIES ///
	//////////////////////
	
	
	/** Current time set by the user.*/
	public  ArrayList<Date> getTimes(){
		return times;
	}

	public  Date getMaxTime(){
		return timeMax;
	}
	
	public Date getMinTime() {
		return timeMin;
	}
	
	/** Returns the latest time point for a given one.*/
	public <V> long getLastTimeBefore(long t, HashMap map, boolean selfAllowed)
	{
		// if the map explicitly contains t, return t.

		if(selfAllowed && map.containsKey(t)) 
			return t;
	
		for(long i = map.size()-1 ; i >= 0; i--){
			if(map.containsKey(i)){
				return i;
			} 
		}
		return -1;
	}
	
//	/** Just another helper method that avoids for the user to test
//	 * if the returned time is -1.
//	 * @param <V>*/
//	public  <V> V getPreviousEntry(int t, HashMap<Integer,V> map)
//	{
//		// if the map explicitly contains t, return t.
//		if(map.containsKey(t)) 
//			return map.get(t);
//	
//		ArrayList<Integer> keys = new ArrayList<Integer>(map.keySet());
//		Collections.sort(keys);
//
//		for(int i = keys.size() -1 ; i >= 0; i-- ){
//			if(keys.get(i) < t){
//				return map.get(keys.get(i));
//			} 
//		}
//		return null;
//	}

	public <V> Integer getNextTimeAfter(long t, HashMap<Integer,V> map)
	{
		ArrayList<Integer> keys = new ArrayList<Integer>(map.keySet());
		Collections.sort(keys);

		for(int i = 0 ; i < keys.size() ; i++ ){
			if(keys.get(i) > t){
				return keys.get(i);
			} 
		}
		return -1;
	}

	
	public  int getCurrentLayer()
	{
		return currentLayer;
	}
	
	
	public static String[] getAggregationStepNames()
	{
		String[] arr = new String[8];
		arr[AGG_SECOND] = AGG_SECOND_NAME;
		arr[AGG_MINUTE] = AGG_MINUTE_NAME;
		arr[AGG_HOUR] = AGG_HOUR_NAME;
		arr[AGG_DAY] = AGG_DAY_NAME;
		arr[AGG_WEEK] = AGG_WEEK_NAME;
		arr[AGG_MONTH] = AGG_MONTH_NAME;
		arr[AGG_QUART] = AGG_QUART_NAME;
		arr[AGG_YEAR] = AGG_YEAR_NAME;
		
		return arr;
	}

	public void setAggregation(int i) 
	{
		currentAggregation = i;
		
		// update times
	}
	
	public static Date getDate(long time){
		Date d = new Date(time);
		return d;
	}
	

}
