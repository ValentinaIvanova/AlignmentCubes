package cubix.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import cubix.helper.Log;


import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;


/**
 * 
 *
 * @author benjamin.bach@inria.fr
 * */
public class TimeGraph<N, E, T>
{
	public static final int DEPTH_LABELS = 11;
	public static final int DEPTH_NODE = 10;
	public static final int DEPTH_EDGE = 9;
	public static final int HALO_SIZE = 4;
	public static final int NODE_RADIUS = 12;
	
	public static final Color COLOR_NODE = Color.LIGHT_GRAY;
	public static final Color COLOR_EDGE = Color.DARK_GRAY;
	
	public static final float TRANSLUCENCY_EDGES = .6f;
	public static final float TRANSLUCENCY_MARKED_HALO = .5f;
	public static final int DEPTH_HALO_HIGHLIGHT = 8;
	public static final double LABEL_OFFSET_X = 20;
	public static final double LABEL_OFFSET_Y = 10; 

	

	protected HashMap<T, Graph<N, E>> sliceGraphs = new HashMap<T, Graph<N, E>>();
	protected HashMap<Graph<N, E>, T> sliceTimes = new HashMap<Graph<N, E>, T>();
	private ArrayList<Graph<N, E>> sliceGraphArr = new ArrayList<Graph<N,E>>();
	protected ArrayList<T> times = new ArrayList<T>(); 

	protected HashMap<N, String> vertexLabels = new HashMap<N, String>(); 
	protected ArrayList<N> vertices = new ArrayList<N>(); 
	protected HashSet<E> edges = new HashSet<E>();
	
		
	public TimeGraph()
	{
		
	}

	
	public Graph<N, E> createSliceGraph(T t)
	{
		Graph<N, E> g = new SparseGraph<N, E>();
		
		sliceGraphs.put(t, g);
		sliceTimes.put(g, t);
		int index = 0;
		TimeComparator<T> tc = new TimeComparator<T>();
		for(int i=0 ; i<sliceGraphArr.size() ; i++){
			if( tc.compare(sliceTimes.get(sliceGraphArr.get(i)), t) < 0){
				index = i;
				break;
			}
		}
		sliceGraphArr.add(index, g);
		times.add(index, t);
		return g;
	}
	
	public N addVertex(N n, T t)
	{
		Graph<N, E> g;
		g = getGraph(t);
		if(g == null){
			g = createSliceGraph(t);
		}	
		
		if(n instanceof String)
		{
			boolean found=false;
			for(N v : vertices){ if(v.equals(n)){ found=true; break; } }
			if(!found) {
				vertices.add(n);
//				System.out.println("[TimeGraph] Add vertex " + n);
			}
		}else
		if(n instanceof CNode){
			for(N v : this.vertices){
				if(((CNode)v).getID().equals(((CNode)n).getID())){
					g.addVertex(v);
					return v;
				}
			}
		}
		if(!vertices.contains(n)){
			vertices.add(n);
			vertexLabels.put(n,n.toString());
		}	

		if(!g.containsVertex(n)){
			g.addVertex(n);
			return n;
		}
		
		return n;
	}
	
	public boolean addEdge(E e, N source, N target, T t, boolean directed)
	{
		Graph<N, E> g = sliceGraphs.get(t);
		if(g == null){
			g = createSliceGraph(t);
		}
		if(e instanceof String)
		{
			boolean found=false;
				for(E ed : edges){ if(ed.equals(e)){ found=true; Log.err(this, "edge " + e + " exists!"); break; } 
			}
			if(!found){
				edges.add(e);
			}
		}else{
			if(!edges.contains(e)){
				edges.add(e);
			}
		}
		
		if(!g.containsEdge(e)){
			g.addEdge(e, source, target, directed?EdgeType.DIRECTED:EdgeType.UNDIRECTED);
				return true;
		}
		
		return false;
	}

	
	///////////////////////
	/// GETTER & SETTER /// 
	///////////////////////
	
	// Graphs
	public int getTimeSliceNumber() { return sliceGraphs.size(); }
	public ArrayList<Graph<N,E>> getGraphs() { return sliceGraphArr; }
	public Graph<N, E> getGraph(T time){if(sliceGraphs.containsKey(time)) return sliceGraphs.get(time); else Log.err(this, "No graph for time " + time.toString()); return null;}
	public T getTime(Graph<N,E> g){ return sliceTimes.get(g);}
	public boolean hasTime(T t) {return sliceGraphs.containsKey(t); }
	
	// Graph elements
	public Collection<N> getVertices() {return vertices; }
	public boolean hasVertex(N n) {return vertices.contains(n); }
	public boolean hasVertex(N n, T t) {return sliceGraphs.get(t).containsVertex(n); }
	public int getVertexNumber() { return this.vertices.size();}
	public Collection<E> getEdges() {return edges; }
	public boolean hasEdge(E e) {return edges.contains(e); }
	public boolean hasEdge(E e, T t) {return sliceGraphs.get(t).containsEdge(e); }
	public int getEdgeNumber() {return this.edges.size(); }
	public Collection<N> getVertices(T t) { return sliceGraphs.get(t).getVertices(); }
	public Collection<E> getEdges(T t) { return sliceGraphs.get(t).getEdges(); }

	// TIME
	public ArrayList<T> getTimes() {
		ArrayList<T> arr = new ArrayList<T>(); 
		arr.addAll(sliceGraphs.keySet());
		Collections.sort(arr, new TimeComparator<T>());
		return arr;
	}
	
	public void setNodeLabel(N vertex, String label) { vertexLabels.put(vertex, label); }
	public String getVertexLabel(N v){return vertexLabels.get(v);}

	public E getEdge(T time, N n, N m) {
		
		Graph<N,E> graph = sliceGraphs.get(time);
		E edge = graph.findEdge(n, m);
		
		return edge;
	}
	
	public Collection<E> getEdges(N n,N m)
	{
		ArrayList<E> edges = new ArrayList<E>();
		E e;
		for(Graph<N,E> g : this.sliceGraphArr)
		{
			e = g.findEdge(n, m);
			if(e != null){ edges.add(e); }
			e = g.findEdge(m, n);
			if(e != null){ edges.add(e); }
		}
		return edges;
	}


	public N getTarget(E e)
	{
		for(Graph<N, E> g : sliceGraphArr){
			if(g.containsEdge(e))
				return g.getEndpoints(e).getSecond();
		}
		return null;
	}
	
	public N getSource(E e)
	{
		for(Graph<N, E> g : sliceGraphArr){
			if(g.containsEdge(e))
				return g.getEndpoints(e).getFirst();
		}
		return null;
	}


	public HashSet<N> getNeighbors(N n) {
		HashSet<N> nn = new HashSet<N>();
		for(Graph g : sliceGraphs.values())
		{
			if(g.containsVertex(n) && g.getNeighbors(n) != null)
				nn.addAll(g.getNeighbors(n));
		}
		return nn;
	}


	public HashSet<E> getIndicentEdges(CNode n) {
		HashSet<E> ee = new HashSet<E>();
		for(Graph g : sliceGraphs.values()){
			ee.addAll(g.getIncidentEdges(n));
		}
		return ee;
	}



}
