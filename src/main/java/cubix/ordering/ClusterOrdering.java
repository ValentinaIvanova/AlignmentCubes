package cubix.ordering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import cubix.hac.HierarchicalAgglomerativeClusterer;
import cubix.hac.agglomeration.AgglomerationMethod;
import cubix.hac.agglomeration.AverageLinkage;
import cubix.hac.agglomeration.CompleteLinkage;
import cubix.hac.agglomeration.WardLinkage;
import cubix.hac.dendrogram.Dendrogram;
import cubix.hac.dendrogram.DendrogramBuilder;
import cubix.hac.dendrogram.DNode;
import cubix.hac.experiment.DissimilarityMeasure;
import cubix.hac.experiment.Experiment;
import cubix.helper.Log;
import cern.colt.matrix.DoubleMatrix2D;
import cubix.hac.dendrogram.ObservationNode;

public class ClusterOrdering extends MatrixOrdering implements Experiment, DissimilarityMeasure{

	private int vertexCount;
	private DoubleMatrix2D distanceMatrix;
	private HashMap<DNode, ArrayList<DNode>> leafMap = new HashMap<DNode, ArrayList<DNode>>();
	
	@Override
	public int[] order(DoubleMatrix2D distanceMatrix) 
	{
		this.vertexCount = distanceMatrix.rows();
		this.distanceMatrix = distanceMatrix;
		
		// Create hierarchical clustering
		DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(vertexCount);
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(this, this, new CompleteLinkage());
		clusterer.cluster(dendrogramBuilder);
		Dendrogram d = dendrogramBuilder.getDendrogram();
//		d.dump();
		
		DNode root = d.getRoot();

		long time = System.currentTimeMillis();
		MemoizedBarJoseph mbj = new MemoizedBarJoseph(distanceMatrix, root);
		int[] finalOrder = mbj.order();
//		ArrayList<DNode> order = calculateBarJoseph(root);
		Log.out(this,"Time to calculate order: " + (System.currentTimeMillis() - time)  + "ms");
//		int[] finalOrder = new int[vertexCount];
//		int i=0;
//		for(DNode n : order){
//			finalOrder[i] = ((ObservationNode)n).getObservation();
//			i++;
//		}
		
		return finalOrder;
	}

	/*
	private ArrayList<DNode> calculateBarJoseph(DNode v)
	{
		ArrayList<DNode> order = new ArrayList<DNode>();
		if(isLeaf(v)){
			order =  new ArrayList<DNode>();
			order.add(v);
			return order;
		} 
		
		ArrayList<DNode> o_temp = new ArrayList<DNode>();
		float similarity_max = 1000000;
		float similarity = 0;
		for(DNode i : leafs(v.getLeft())){
			for(DNode j : leafs(v.getRight())){
				o_temp = new ArrayList<DNode>();
				similarity = order(v,i,j, o_temp);

				if(similarity < similarity_max){
					similarity_max = similarity;
					order = o_temp;
				}
			}	
		}
		return order;
	}
	*/
	
	/** Orders the subtree. 
	private float order(DNode v, DNode i, DNode j, ArrayList<DNode> order)
	{
		if(isLeaf(v)){
			order.add(v);
			return 0f;
		}
		
		// swapping sub-trees according to i and j
		DNode l = v.getLeft();
		DNode r = v.getRight();
		DNode w=null, x=null;
		if(leafs(l).contains(i) 
		&& leafs(r).contains(j)){
			w = l;
			x = r;
		}else 
		if(leafs(r).contains(i) 
		&& leafs(l).contains(j)){			
			w = r;
			x = l;
		}else{
			Log.err(this, v.toString() + " not common anscestor of"); 
			Log.err(this, "\t" + i.toString()); 
			Log.err(this, "\t" + j.toString()); 
		}
		
		// restricting domain of k and l
		ArrayList<DNode> ks;
		if(leafs(w.getRight()).contains(i))
			ks = leafs(w.getLeft());
		else
			ks = leafs(w.getRight());
		if(ks.size() == 0)
			ks.add(i);
		
		ArrayList<DNode> ls;
		if(leafs(x.getRight()).contains(j))
			ls = leafs(x.getLeft());
		else
			ls = leafs(x.getRight());
		if(ls.size() == 0)
			ls.add(j);
		
		// maximize similarity
		float s_max = 10000000;
		ArrayList<DNode> w_order = new ArrayList<DNode>();
		ArrayList<DNode> x_order = new ArrayList<DNode>();
		for(DNode k : ks){
			w_order = new ArrayList<DNode>();
			float w_max = order(w,i,k, w_order);
			for(DNode m : ls){
				x_order = new ArrayList<DNode>();
				float x_max = order(x,m,j, x_order);
				float similarity = (float) (w_max + distanceMatrix.get(((ObservationNode)k).getObservation(),((ObservationNode)m).getObservation()) + x_max);
				if(similarity < s_max){
					s_max = similarity;
					order.clear();
					order.addAll(w_order);
					order.addAll(x_order);
				}
			}
		}
		return s_max;
	}
	*/
	
	/** Returns leaf nodes for a cluster. 
	private ArrayList<DNode> leafs(DNode n){
		
		if(leafMap.containsKey(n)){
			return leafMap.get(n);
		}else{
			if(n == null) 
				return new ArrayList<DNode>();
			if(isLeaf(n)){
				ArrayList<DNode> a = new ArrayList<DNode>();
				a.add(n);
				return a;
			} 
			ArrayList<DNode> nns = new ArrayList<DNode>();
			nns.addAll(leafs(n.getLeft()));
			nns.addAll(leafs(n.getRight()));
			leafMap.put(n, nns);
			return nns;
		}
	}
	
	private boolean isLeaf(DNode n){
		return n.getLeft() == null && n.getRight() == null;
	}
	*/
	
	public int getNumberOfObservations() {
		return vertexCount;
	}

	public double computeDissimilarity(Experiment experiment, int v1, int v2) {
		return distanceMatrix.get(v1, v2);
	}

}
