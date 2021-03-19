package cubix.data;


public class DataModel<N,E,T> {

	
	protected TimeGraph<N,E,T> timeGraph; 
	private static DataModel instance;

	private DataModel() {
	}

	public static DataModel getInstance() {
		if (null == instance) {
			instance = new DataModel();
		}
		return instance;
	}
	
	
	/////////////////
	/// GET & SET ///
	/////////////////

	
	public TimeGraph<N,E,T> getTimeGraph(){ return this.timeGraph; }
	public void setTimeGraph(TimeGraph<N,E,T> timeGraph) { this.timeGraph = timeGraph;}

	public static void destroyInstance() {
		instance = null;
	}
	
	
}
