package cubix.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;


import edu.uci.ics.jung.graph.Graph;


public class NodeDegreeComparator<N> implements Comparator<N> {

	ArrayList<Graph> graphs;
	private Collection validEdges;
	
	public NodeDegreeComparator(ArrayList<Graph> graphs, Collection validEdges){
		this.graphs = graphs;
		this.validEdges = validEdges;
	}
	

	public int compare(N n1, N n2) 
	{ 
		int degree1 = 0;
		int degree2 = 0;
		for(Graph<N,?> g : graphs){
			if(g.containsVertex(n1)){
				for(Object e : g.getIncidentEdges(n1)){
					if(validEdges.contains(e))
						degree1++;
				}
			}
			if(g.containsVertex(n2)){
				for(Object e : g.getIncidentEdges(n2)){
					if(validEdges.contains(e))
						degree2++;
				}
			}
		}
		
		return degree2 - degree1;
	}
		
	
	
}
