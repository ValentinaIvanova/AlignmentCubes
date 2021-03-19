package cubix.dataSets;

import com.opencsv.*;
import cubix.CubixVis;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.TimeGraph;
import cubix.data.TimeGraphUtils;
import cubix.helper.FileNameComparator;
import cubix.helper.Log;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LoadCSVAdajacencyMatrix{

	/*	
	protected File dir;
	
	
	
	
	protected HashMap<String, Color> groupColors = new HashMap<String, Color>();
		
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private static TimeGraphUtils<CNode, CEdge, CTime> utils;
	
	private static float maxWeight;
	private static float minWeight;

	public static boolean selfEdges = true;

	
	private static String[] antennaNames, quasarNames;


	private static boolean NAME_NODES = false;
	
	
	
	public static TimeGraph<CNode, CEdge, CTime> load(File dir){
		return load(dir, "", dir.list(new FilenameContainsFilter(new String[]{"csv"})).length, 1);
	}
	
	public static TimeGraph<CNode, CEdge, CTime> load(File dir, String nameFilter){
		return load(dir, nameFilter, dir.list(new FilenameContainsFilter(new String[]{"csv", nameFilter})).length, 1);
	}


	
	public static TimeGraph<CNode, CEdge, CTime> load(File dir, String nameFilter, int fileAmount, int steps)
	{
		maxWeight = -1;
		minWeight = 100000;
		TimeGraph<CNode, CEdge, CTime> tGraph = new TimeGraph<CNode, CEdge, CTime>();
		utils = new TimeGraphUtils<CNode, CEdge, CTime>();
		
		if(!dir.exists()) {	
			System.err.println("[CSV] File not found: " + dir.getAbsolutePath());
			return null;
		}
		File[] files = dir.listFiles(new FilenameContainsFilter(new String[]{"csv", nameFilter}));
		
		Arrays.sort(files, new FileNameComparator());
		
		int timeAmount = files.length; // one is .svn, the other is description file
		int loaded = 0;
		for(int t = 0; t < timeAmount && loaded < fileAmount; t+=steps)
		{
			loaded++;
			loadCSVFile(files[t], tGraph, t);	
		}
		CubixVis.WEIGHT_MAX = maxWeight;
		CubixVis.WEIGHT_MIN = minWeight;
		return tGraph;
	}
	
	private static void loadCSVFile(File file, TimeGraph<CNode, CEdge, CTime> tGraph, int timeStep) 
	{
		
		HashMap<String,CNode> vertices = new HashMap<String, CNode>();
	        ArrayList<CNode> nodes = new ArrayList<CNode>();

	        float weight = 0;
	        try {
	    	    CSVReader r = new CSVReader(new FileReader(file));
	    	    CNode node;
	    	    
	    	    CTime t;
				t = new CTime(System.currentTimeMillis());
				t.setDateFormat(df);
				t.setLabel("" + timeStep);
				Log.out("timestep: " + timeStep + " -> " + file.getName());
				
	    	    // START PARSE
	    	    String[] line;
	    	    CEdge edge;
	    	    int sourceID = 1;
	    	    while((line = r.readNext()) != null)
	            {
	    	    	//if first line contains node names, parse them
	    	    	// CREATE NODES
	    	    	if(!line[0].contains(".")){
	    	    		Log.out("Create nodes");
	    	    	    for(int n=0 ; n < line.length-1 ; n++){
	    	    	    	node = new CNode(n + "");
	    	    	    	node = tGraph.addVertex(node, t);
	    	    	    	nodes.add(node);
	    	    	    	tGraph.setNodeLabel(node, line[n+1]);
	    	    	    }
	    	    	    continue;
	    	    	}
	    	    		
			    	for(int targetID=1 ; targetID < line.length  ; targetID++){
			    		
					    if(line[targetID-1].length() != 0){
					    	weight = Float.parseFloat(line[targetID-1]);
					    	if(weight != 0){
					    		if(!selfEdges  && targetID == sourceID) continue;
						    	edge = new CEdge(sourceID + "->" + targetID);
				    			edge.setWeight(weight);
				    			tGraph.addEdge(edge, nodes.get(sourceID-1), nodes.get(targetID-1), t, true);
				    			maxWeight = Math.max(weight, maxWeight);
								minWeight = Math.min(weight, minWeight);
					    	}
					    }
			    	}
			    	sourceID++;
	            }
	        }
	        catch (IOException ex){
	            System.err.println("Error loading file " + file);
	            ex.printStackTrace();
	        }
	    }
*/
}
