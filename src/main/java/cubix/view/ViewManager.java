package cubix.view;

import java.util.ArrayList;
import java.util.Collection;

import cubix.CubixVis;
import cubix.data.MatrixCube;

public class ViewManager {

	
	public static final int VIEW_CUBE = 0;
	public static final int VIEW_FRONT = 1;
	public static final int VIEW_SOURCE_SIDE = 2;
	public static final int VIEW_TIME_SM = 3;
	public static final int VIEW_VNODESLICE_SM = 4;
	public static final int VIEW_TARGET_SIDE = 5;
	public static final int VIEW_HNODESLICE_SM = 6;
	
	protected ArrayList<CView> views = new ArrayList<CView>();

	
	private static ViewManager instance;

	private ViewManager() 
	{
		// Init views
		// VI !!!!!!!! No !!!!!!!!
		views.clear();
        views.add(VIEW_CUBE, new CubeView());
        views.add(VIEW_FRONT, new FrontView());
        views.add(VIEW_SOURCE_SIDE, new SourceSideView());
        views.add(VIEW_TIME_SM, new GraphSMView());
        views.add(VIEW_VNODESLICE_SM, new NodeSMView());
        views.add(VIEW_TARGET_SIDE, new TargetSideView());
        views.add(VIEW_HNODESLICE_SM, new NodeSMView()); /// change this to the proper small multiples
	}

	public static ViewManager getInstance() {
		if (null == instance) {
			instance = new ViewManager();
		}
		return instance;
	}
	
	public void init(CubixVis vis){
		for(CView v : views){
			v.init(vis);
		}
	}

	
	public CView getView(int view){return views.get(view);} 
	public Collection<CView> getViews() {return views;}

	public static void destroyInstance() {
		instance = null;
	}
}
