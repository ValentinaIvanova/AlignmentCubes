package cubix.data;

import java.util.Collection;
import java.util.HashSet;

import edu.uci.ics.jung.graph.Graph;

public class TimeGraphUtils<N,E,T> {

	
	/////////////////
	/// FACTORIES /// 
	/////////////////

	public String createEdgeID(String n1, String n2, T t, boolean directed)
	{
		String id;
		if(!directed){
			if(n1.compareToIgnoreCase(n2) < 0){
				id = n1 + "--" + n2;
			}else{
				id = n2 + "--" + n1;
			}
		}
		else{
			id = n1 + "->" + n2;
		}
		
		return t + ":" + id;
	}

	
	public int createEdgeHash(String n1, String n2)
	{
		int id;
		if(n1.compareToIgnoreCase(n2) < 0){
			id = (n1 + "--" + n2).hashCode();
		}else{
			id = (n2 + "--" + n1).hashCode();
		}
		
		return id;
	}

	

	////////////////////////
	/// SET RELATIONS //////
	////////////////////////
	
	
		////////////////////
		/// INTERSECTION ///
		////////////////////
		
		public Collection<N> geNIntersection(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<N> res = new HashSet<N>(g1.getVertices()); 
			res.retainAll(g2.getVertices());
			return res;
		}
		
		public Collection<E> getEdgeIntersection(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<E> res = new HashSet<E>(g1.getEdges());
			for(E e : g1.getEdges())
			{
				if(!g2.containsEdge(e))
					res.remove(e);
			}
			return res;
		}
		
		public  <T> HashSet<T> longersect(Collection<T> c1, Collection<T> c2)
		{
			HashSet<T> s1 = new HashSet<T>();
			s1.addAll(c1);
			s1.retainAll(c2);
			return s1;
		}
		
		
		/////////////
		/// UNION ///	
		/////////////
		
		public Collection<N> geNUnion(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<N> res = new HashSet<N>(g1.getVertices());
			res.addAll(g2.getVertices());
			return res;
		}
		public Collection<E> getEdgeUnion(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<E> res = new HashSet<E>(g1.getEdges());
			res.addAll(g2.getEdges());
			return res;
		}
		
		
		
		
		
		//////////////
		/// GROWTH ///
		//////////////
		
		public Collection<N> geNGrowth(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<N> res = new HashSet<N>(g2.getVertices());
			res.removeAll(g1.getVertices());
			return res;
		}
		public Collection<E> getEdgeGrowth(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<E> res = new HashSet<E>();
			for(E e : g2.getEdges())
			{
				if(!g1.containsEdge(e))
					res.add(e);
			}
			return res;
		}
		
		public <T> HashSet<T> getGrowth(Collection<T> c1, Collection<T> c2)
		{
			HashSet<T> s2 = new HashSet<T>();
			s2.addAll(c2);
			s2.removeAll(c1);
			return s2;
		}
		
		
		
		
		/////////////////
		/// REDUCTION ///
		/////////////////
		
		public Collection<N> geNReduction(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<N> res = new HashSet<N>(g1.getVertices());
			HashSet<N> remove = new HashSet<N>(g2.getVertices());
			res.removeAll(remove);
			return res;
		}
		public Collection<E> getEdgeReduction(Graph<N, E> g1, Graph<N, E> g2)
		{
			HashSet<E> res = new HashSet<E>();
			for(E e : g1.getEdges())
			{
				if(!g2.containsEdge(e))
					res.add(e);
			}
			return res;
		}
		
		
		public <T> HashSet<T> getReduction(Collection<T> c1, Collection<T> c2)
		{
			HashSet<T> s1 = new HashSet<T>();
			s1.addAll(c1);
			s1.removeAll(c2);
			return s1;
		}
		
	
}
