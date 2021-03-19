package cubix.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import cubix.ordering.ClusterOrdering;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.uci.ics.jung.graph.Graph;

public class MatrixUtils<N,E, T> {
	
	
	public ArrayList<N> reorderCutHillMcKee(TimeGraph<N, E, T> graph, Collection<CEdge> validEdges)
	{
		ArrayList<N> orderedNodes = new ArrayList<N>();
		ArrayList<N> queue = new ArrayList<N>();
		ArrayList<N> initialOrdering = new ArrayList<N>(); 
		initialOrdering.addAll(graph.getVertices());
		
		if(initialOrdering.size() == 1) 
			return initialOrdering;
		
		ArrayList graphs = graph.getGraphs();
		
		Collections.sort(initialOrdering, new NodeDegreeComparator<N>(graphs, validEdges));
		
		N nodeStart;
		while(!initialOrdering.isEmpty())
		{
			nodeStart = initialOrdering.remove(0);
			orderedNodes.add(nodeStart);
			initialOrdering.remove(nodeStart);
			addChildrenToQueue(nodeStart, queue, initialOrdering, graphs, validEdges);
				
			while(!queue.isEmpty())
			{
				N xn = queue.get(0);
				queue.remove(0);
				orderedNodes.add(xn);
				initialOrdering.remove(xn);
				addChildrenToQueue(xn, queue, initialOrdering, graphs, validEdges);
			}
		}
		return orderedNodes;
	}
	
	public ArrayList<CNode> reorderTopologyTSP(TimeGraph<CNode, CEdge, CTime> timeGraph, Collection<CEdge> validEdges)
	{
		ArrayList<CNode> orderedNodes = new ArrayList<CNode>();
		ArrayList<CNode> nodes = new ArrayList<CNode>();
		nodes.addAll(timeGraph.getVertices());
		
		Order o = new Order();
		DoubleMatrix2D distMat = getDistanceMatrix(timeGraph, nodes, validEdges);
		int[] ordering = o.computeOrdering(distMat);
		for(int i=0 ; i<ordering.length ; i++){
			orderedNodes.add(nodes.get(ordering[i]));
		}
		Collections.reverse(orderedNodes);
		return orderedNodes;

	}
	
	public ArrayList<CNode> reorderHierarchical(TimeGraph<CNode, CEdge, CTime> timeGraph, Collection<CEdge> validEdges)
	{
		ArrayList<CNode> orderedNodes = new ArrayList<CNode>();
		ArrayList<CNode> nodes = new ArrayList<CNode>();
		nodes.addAll(timeGraph.getVertices());
		
		ClusterOrdering o = new ClusterOrdering();
		DoubleMatrix2D distMat = getDistanceMatrix(timeGraph, nodes, validEdges);
		int[] ordering = o.order(distMat);
		
		for(int i=0 ; i<ordering.length ; i++){
			orderedNodes.add(nodes.get(ordering[i]));
		}
		Collections.reverse(orderedNodes);
		return orderedNodes;
	}
	
	private DoubleMatrix2D getDistanceMatrix(TimeGraph<CNode, CEdge, CTime> timeGraph, ArrayList<CNode> nodes, Collection<CEdge> validEdges)
	{
		timeGraph.getVertices().size();
		
		DoubleMatrix2D mat = new DenseDoubleMatrix2D(timeGraph.getVertices().size(), timeGraph.getVertices().size());
		mat.assign(0);
		
		float w;
		for(CNode n : timeGraph.getVertices())
		{
			for(CNode m : timeGraph.getVertices())
			{
				w = getCulmulativeWeight(timeGraph, n, m, validEdges);
				mat.set(nodes.indexOf(n), nodes.indexOf(m), w);
			}
		}
		Order o = new Order();
		DoubleMatrix2D distMat = o.computeDistance(mat);
		return distMat; 
	}
	
	protected static float getCulmulativeWeight(TimeGraph<CNode, CEdge, CTime> tGraph, CNode n, CNode m, Collection<CEdge> validEdges)
	{
		float w = 0;
		for(Graph<CNode,CEdge> g : tGraph.getGraphs()){
			if(g.containsVertex(n) && g.containsVertex(m)){
				for(CEdge e : g.findEdgeSet(n,m)){
					if(e == null || !validEdges.contains(e)) continue;
					w += e.getWeight();
				}
			}
		}
		return w / tGraph.getTimeSliceNumber();
	}
	
//	public ArrayList<N> reorderTemporalActivity(TimeGraph<N, E, ?> timeGraph){
//		ArrayList<N> nodeOrder = new ArrayList<N>();
//		HashMap<N, Integer> nodeDegree = new HashMap<N, Integer>();
//		int deg;
//		for(Graph<N, E> g : timeGraph.getGraphs()){
//			for(N n : g.getVertices()){
//				if(!nodeOrder.contains(n))
//					nodeOrder.add(n);
//
//				if(nodeDegree.containsKey(n)){
//					nodeDegree.put(n, nodeDegree.get(n) + g.degree(n));
//				}else{
//					nodeDegree.put(n, g.degree(n));
//				}
//			}
//		}
//		
//		Collections.sort(nodeOrder, new TimeNodeDegreeComparator(nodeDegree));
//
//		return nodeOrder;
//	}
	
	public void addChildrenToQueue(N xn, ArrayList<N> queue, ArrayList<N> initialOrdering, ArrayList<Graph> graphs, Collection<CEdge> validEdges)
	{
		 	ArrayList<N> orderedChildren = new ArrayList<N>();
		 	N neighbor;
		 	for(Graph<N,E> g : graphs){
		 		if(!g.containsVertex(xn))
		 			continue;
		 		for(E e : g.getIncidentEdges(xn))
		 		{
		 			if(!validEdges.contains(e))
		 				continue;
		 			neighbor = g.getOpposite(xn, e);
		 			if(xn == neighbor) 
		 				continue;
		 			if(!initialOrdering.contains(neighbor)) 
		 				continue;
		 			orderedChildren.add(neighbor);
		 			initialOrdering.remove(neighbor);
		 		}
		 	}

			Collections.sort(orderedChildren, new NodeDegreeComparator<N>(graphs, validEdges));
			queue.addAll(orderedChildren);
		}
	
	
	public ArrayList<CTime> reorderTimesHierarchical(TimeGraph<CNode, CEdge, CTime> timeGraph, Collection<CEdge> validEdges)
	{
		ArrayList<CTime> orderedTimes = new ArrayList<CTime>();
		ArrayList<CTime> times = new ArrayList<CTime>();
		times.addAll(timeGraph.getTimes());
		
		ClusterOrdering o = new ClusterOrdering();
		DoubleMatrix2D distMat = getTimeDistanceMatrix(timeGraph, times, validEdges);
		int[] ordering = o.order(distMat);
		
		for(int i=0 ; i<ordering.length ; i++){
			orderedTimes.add(times.get(ordering[i]));
		}
		Collections.reverse(orderedTimes);
		return orderedTimes;
	}
	
	public static DoubleMatrix2D getTimeDistanceMatrix(TimeGraph<CNode, CEdge, CTime> timeGraph, ArrayList<CTime> times, Collection<CEdge> validEdges)
	{		
		DoubleMatrix2D mat = new DenseDoubleMatrix2D(timeGraph.getTimes().size(), timeGraph.getTimes().size());
		mat.assign(0);
		
		float w;
		for(CTime t1 : timeGraph.getTimes())
		{
			for(CTime t2 : timeGraph.getTimes())
			{
				w = getCulmulativeWeight(timeGraph, t1, t2, validEdges);
				mat.set(times.indexOf(t1), times.indexOf(t2), w);
			}
		}
		Order o = new Order();
		DoubleMatrix2D distMat = o.computeDistance(mat);
		return distMat; 
	}
	
	protected static float getCulmulativeWeight(TimeGraph<CNode, CEdge, CTime> tGraph, CTime t1, CTime t2, Collection<CEdge> validEdges)
	{
		float w = 0;
		Graph<CNode, CEdge> g1 = tGraph.getGraph(t1);
		Graph<CNode, CEdge> g2 = tGraph.getGraph(t2);
		float w1, w2;
		for(CNode n : tGraph.getVertices()){
			for(CNode m : tGraph.getVertices()){
				w1=0; w2=0;
				if(g1.containsVertex(n) && g1.containsVertex(m)){
					for(CEdge e : g1.findEdgeSet(n, m)){
						if(e != null && validEdges.contains(e)) 
							w1 += e.getWeight();
				}
				}
				if(g2.containsVertex(n) && g2.containsVertex(m)){
					for(CEdge e : g2.findEdgeSet(n, m)){
						if(e != null && validEdges.contains(e)) 
							w2 += e.getWeight();
					}
				}
				w += Math.abs(w1-w2);
			}
		}
		return w / tGraph.getVertexNumber();
	}

}
