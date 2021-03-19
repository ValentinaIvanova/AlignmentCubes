package cubix.ordering;

import java.util.ArrayList;
import java.util.Collection;

import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.TimeGraph;
import cubix.helper.Log;
import cubix.helper.Utils;
import cubix.vis.VNodeSlice;
import edu.uci.ics.jung.graph.Graph;

public class TimeDistanceCalculator {

	/** Calcualtes the distance between two nodes over time.
	 * 
	 * @param tGraph
	 * @param n
	 * @param m
	 * @param validEdges
	 * @return
	 */
	public float getEuclideanDistance(TimeGraph<CNode, CEdge, CTime> tGraph, CNode n, CNode m, CNode neighbor, Collection<CEdge> validEdges)
	{
		float w_all = 0;
		float w_n = 0;
		float w_m = 0;
		for(Graph<CNode,CEdge> g : tGraph.getGraphs())
		{
			if(!g.containsVertex(neighbor))
				continue;
			
			w_n = 0;
			if(g.containsVertex(n))
			{
				for(CEdge e : g.findEdgeSet(n, neighbor)){
					if(e == null) continue;
					w_n += e.getWeight();
				}
			}
			w_m = 0;
			if(g.containsVertex(m))
			{
				for(CEdge e : g.findEdgeSet(m, neighbor)){
					if(e == null) continue;
					w_m += e.getWeight();
				}
			}
		
			w_all += Math.pow(w_n - w_m , 2);
		}
		return (float) Math.pow(w_all,.5); 
	}
	
	
	public float getAccumulatedWeightDistance(TimeGraph<CNode, CEdge, CTime> tGraph, CNode n, CNode m, CNode neighbor, Collection<CEdge> validEdges)
	{
		float w_all = 0;
		float wTemp;
		for(Graph<CNode,CEdge> g : tGraph.getGraphs())
		{
			wTemp = 0;
			if(g.containsVertex(neighbor))
			{
				if(g.containsVertex(n))
				{
					for(CEdge e : g.findEdgeSet(n, neighbor)){
						if(e == null) continue;
						w_all += e.getWeight();
					}
				}
				if(g.containsVertex(m))
				{
					for(CEdge e : g.findEdgeSet(m, neighbor)){
						if(e == null) continue;
						w_all -= e.getWeight();
					}
				}
			}
		}
		return Math.abs(w_all);
	}
	public void getEdgePresenceDistance(CEdge[] ee1, CEdge[] ee2, ArrayList<Float> vv1, ArrayList<Float> vv2)
	{
		float v1, v2;
		for(int i=0 ; i < ee1.length ; i++){
			for(int j=0 ; j < ee2.length ; j++){
				if(ee1[i] == null) v1 = 0;
				else v1 = 1;
				if(ee2[j] == null) v2 = 0;
				else v2 = 1;
				vv1.add(v1);
				vv2.add(v2);
			}
		}
	}
	public void getEdgeCount(CEdge[] ee1, CEdge[] ee2, ArrayList<Float> vv1, ArrayList<Float> vv2)
	{
		float v1, v2;
		v1 =0;
		v2 =0;
		for(int i=0 ; i<ee1.length ; i++){
			if(ee1[i] != null) v1 += 1;
			if(ee2[i] != null) v2 += 1;
		}
		vv1.add(v1);
		vv2.add(v2);
	}

	public void getEdgeWeights(CEdge[] ee1, CEdge[] ee2, ArrayList<Float> vv1, ArrayList<Float> vv2)
	{
		float v1, v2;
		for(int i=0 ; i < ee1.length ; i++){
			if(ee1[i] == null) v1 = 0;
			else v1 = ee1[i].getWeight();
			if(ee2[i] == null) v2 = 0;
			else v2 = ee2[i].getWeight();
			vv1.add(v1);
			vv2.add(v2);
		}
	}
}
