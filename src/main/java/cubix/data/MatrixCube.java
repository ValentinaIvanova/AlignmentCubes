package cubix.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import cubix.helper.Log;
import cubix.helper.NodeLabelComparator;
import cubix.ordering.ClusterOrdering;
import cubix.ordering.MatrixOrdering;
import cubix.ordering.TimeDistanceCalculator;
import cubix.vis.Cell;
import cubix.vis.TimeSlice;
import cubix.vis.HNodeSlice;
import cubix.vis.VNodeSlice;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;


/**
 * The matrix cube is a structure to store and access a three-dimensinal matrices.
 * The matrix cube represents a dynamic network. One dimension of the cube 
 * is time, the two others are nodes. The cube contains {@link cubix.vis.Cell} objects 
 * for any edge that exists in the dynamic networks at any time.
 * 
 * The matrix cube contains {@link cubix.vis.Slice} objects for any row, column and 
 * time objects. Slices are associated with nodes and time objects and can be used
 * to access cells. Thereby slices can be seen as matrices, pointing to a two-dimensional
 * field of cells. 
 * 
 * Slices can be permuated (re-ordered), which causes the cell fields inside
 * slice objects to be updated. For performance reasons the matrix cube stores
 * the order of objects as well as the order of corresponding slices. In 
 * case of reordering, both are updated.
 * 
 * TODO Make class and slices generic.
 * TODO Update cell fields in slices after reordering
 * 
 * @author benjamin.bach@inria.fr
 *
 * @param <CNode>
 * @param <CEdge>
 * @param <CTime>
 */
public class MatrixCube {

	//
	public static final int TIME = 0;
	public static final int ROW = 1;
	public static final int COL = 2;
	
	protected float maxWeight = 0;
	
	
	/// GRAPH
	private TimeGraph<CNode,CEdge,CTime> tGraph = null;


	/// SLICES
	private ArrayList<TimeSlice> tSlices = new ArrayList<TimeSlice>();
	private ArrayList<HNodeSlice> hSlices = new ArrayList<HNodeSlice>();
	private ArrayList<VNodeSlice> vSlices = new ArrayList<VNodeSlice>();

	private ArrayList<HNodeSlice> visibleHSlices = new ArrayList<HNodeSlice>();
	private ArrayList<VNodeSlice> visibleVSlices = new ArrayList<VNodeSlice>();
	
	/** Maps Time objects to Timeslices **/
	private HashMap<CTime, TimeSlice> tSliceMap = new HashMap<CTime, TimeSlice>();
	/** Maps Row objects to VNodeSlices**/
	private HashMap<CNode, VNodeSlice> vSliceMap = new HashMap<CNode, VNodeSlice>();
	/** Maps Column objects to HNodeSlices**/
	private HashMap<CNode, HNodeSlice> hSliceMap = new HashMap<CNode, HNodeSlice>();

	// CELLS
	/** All Cell objects inside the cube **/
	private HashMap<CEdge, Cell> cells = new HashMap<CEdge, Cell>();
	private float MATRIX_CELL_SIZE = 0f;

	/** Stores the order of row elements **/
	protected ArrayList<CNode> rowOrder = new ArrayList<CNode>(); 
	protected ArrayList<CNode> visibleRowOrder = new ArrayList<CNode>(); 
	/** Stores the order of column elements **/
	protected ArrayList<CNode> columnOrder = new ArrayList<CNode>(); 
	protected ArrayList<CNode> visibleColumnOrder = new ArrayList<CNode>(); 
	/** Stores the order of time elements **/
	protected ArrayList<CTime> timeOrder = new ArrayList<CTime>();
	protected ArrayList<String> timeOrderLabels = new ArrayList<String>();
	/** Stores the nodes of the highest level (owl:Thing not included) in the ontology hierarchy. 
	 *  Helps during the hierarchical sorting. **/
	private ArrayList<HierarchicalCNode> firstLevelVerticies = new ArrayList<HierarchicalCNode>();

	private static final int CUTHILL = 0; 
	private static final int CLUSTER = 1;
	// VI changed method from CLUSTER to CUTHILL
    //private int method = CLUSTER; 
	private int method = CUTHILL; 
	
	private HashMap<CTime, DoubleMatrix2D> distanceMatrices = new HashMap<CTime, DoubleMatrix2D>(); 
	
	
	public MatrixCube(TimeGraph<CNode,CEdge,CTime> tGraph, float cellSize, boolean rectangular)
	{
		// Init
		this.MATRIX_CELL_SIZE = cellSize;
		this.tGraph = tGraph;
		this.timeOrder.addAll(tGraph.getTimes());
		for (int i = 0; i < tGraph.getTimes().size(); i++) {
			timeOrderLabels.add(tGraph.getTimes().get(i).getLabel());
		}
		
		ArrayList<HierarchicalCNode> sourceVerticies = new ArrayList<>();
		ArrayList<HierarchicalCNode> targetVerticies = new ArrayList<>();
		ArrayList<HierarchicalCNode> visibleSourceVerticies = new ArrayList<>();
		ArrayList<HierarchicalCNode> visibleTargetVerticies = new ArrayList<>();
		ArrayList<String> sourceVertexLabels = new ArrayList<>();
		ArrayList<String> targetVertexLabels = new ArrayList<>();	
		ArrayList<String> visibleSourceVertexLabels = new ArrayList<>();
		ArrayList<String> visibleTargetVertexLabels = new ArrayList<>();		

		Collection<CNode> allVertices = tGraph.getVertices();
		Iterator<CNode> it = allVertices.iterator();
		while (it.hasNext()) {
			CNode nextNode = it.next();
			if (nextNode instanceof HierarchicalCNode) {
				HierarchicalCNode node = (HierarchicalCNode) nextNode;
				if(node.getNodeDepth() == 1)
					firstLevelVerticies.add(node);
				String vertexLabel = tGraph.getVertexLabel(node);
				if (node.belongsToSourceOnto()){
					sourceVerticies.add(node);
					sourceVertexLabels.add(vertexLabel);
					if(node.isVisible()){
						visibleSourceVerticies.add(node);
						visibleSourceVertexLabels.add(vertexLabel);
					}
				}
				else {
					targetVerticies.add(node);
					targetVertexLabels.add(vertexLabel);
					if(node.isVisible()){
						visibleTargetVerticies.add(node);
						visibleTargetVertexLabels.add(vertexLabel);
					}
				}
			}
		}
		Collections.sort(firstLevelVerticies, new NodeLabelComparator(tGraph));
		
    	this.rowOrder.addAll(sourceVerticies);
    	this.visibleRowOrder.addAll(visibleSourceVerticies);
    	this.columnOrder.addAll(targetVerticies);
    	this.visibleColumnOrder.addAll(visibleTargetVerticies);
    	//int nodeCount = columnOrder.size();
    	
		Log.out(this, "rowOrder.size()" + rowOrder.size());
		Log.out(this, "columnOrder.size()" + columnOrder.size());
		Log.out(this, "visibleRowOrder.size()" + visibleRowOrder.size());
		Log.out(this, "visibleColumnOrder.size()" + visibleColumnOrder.size());	
    	
    	TimeSlice timeslice;
    	Graph<CNode,CEdge> graph;

		int timeCount = tGraph.getTimes().size();
		ArrayList<String> timeLabels = new ArrayList<String>();
		for(CTime time : tGraph.getTimes())
		{
			graph = tGraph.getGraph(time);
			timeslice = new TimeSlice(this, visibleRowOrder.size(), visibleColumnOrder.size(), time);
			//timeslice = new TimeSlice(this, rowOrder.size(), columnOrder.size(), time);
			timeslice.setLabel(time.getLabel());
			timeslice.setRowLabels(visibleSourceVertexLabels);
			timeslice.setColumnLabels(visibleTargetVertexLabels);
			//timeslice.setRowLabels(sourceVertexLabels);
			//timeslice.setColumnLabels(targetVertexLabels);
			tSlices.add(timeslice);
        	tSliceMap.put(time, timeslice);
        	timeLabels.add(time.getLabel());
		}

		// CREATE NODE SLICES
		VNodeSlice vs;
		HNodeSlice hs;
		for(CNode n : tGraph.getVertices())
		{
			/*
			 * HNodeSlice - this is a horizontal slice in the cube!
			 * It represents one source node and it has its name for label.
			 * Its rows are the target nodes and columns are the time/alignment values.
			 * Vice versa for the VNodeSlice - vertical slice that represents a target node 
			 * and contains the source nodes for rows in the respective matrix.
			 */
			if (n instanceof HierarchicalCNode){
				HierarchicalCNode hn = (HierarchicalCNode) n;
				if (hn.isVisible()) {
					if(hn.belongsToSourceOnto()){
						hs = new HNodeSlice(this, visibleColumnOrder.size(), timeCount, n);
						//hs = new HNodeSlice(this, columnOrder.size(), timeCount, n); 
						hs.setLabel(tGraph.getVertexLabel(n));
						hs.setRowLabels(visibleTargetVertexLabels);
						//hs.setRowLabels(targetVertexLabels);
						hs.setColumnLabels(timeLabels);
						hSlices.add(hs);
						hSliceMap.put(n, hs);
						//if(hn.isVisible())
							//visibleHSlices.add(hs);
					} else {
						vs = new VNodeSlice(this, visibleRowOrder.size(), timeCount, n);
						//vs = new VNodeSlice(this, rowOrder.size(), timeCount, n);
						vs.setLabel(tGraph.getVertexLabel(n));
						//vs.setRowLabels(sourceVertexLabels);
						vs.setRowLabels(visibleSourceVertexLabels);
						vs.setColumnLabels(timeLabels);
			        	vSlices.add(vs);
						vSliceMap.put(n, vs);
						//if(hn.isVisible())
							//visibleVSlices.add(vs);
					}
				}
			}
		}	
		
		// Create cells
		Cell c;
		CNode source, target;
		Pair<CNode> endPoints;
		Graph<CNode, CEdge> g;
		for(CTime t : tGraph.getTimes())
		{
			g = tGraph.getGraph(t);
			for(CEdge e : g.getEdges())
			{
				if((e.getWeight() == 0) && (e.getAccumulatedWeight() == 0)) 
					continue;
				
	    		endPoints = g.getEndpoints(e);
	    		source = endPoints.getFirst();
	    		target = endPoints.getSecond();
	    		
	    		/**
	    		 * TODO VI document properly - when we are working with two ontologies we are working 
	    		 * with HierarchicalCNode only!!!
	    		 * The source and target nodes should belong to different ontologies.
	    		 */
				if ((source instanceof HierarchicalCNode) && (target instanceof HierarchicalCNode)){
					HierarchicalCNode sourceN = (HierarchicalCNode) source;
					HierarchicalCNode targetN = (HierarchicalCNode) target;
					
					if (sourceN.isVisible() && targetN.isVisible()) {
						c = new Cell(MATRIX_CELL_SIZE, MATRIX_CELL_SIZE, MATRIX_CELL_SIZE);
						c.setOwner(e);
						cells.put(e,c);
			    			
	    				tSliceMap.get(t).setCell(c, visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target));
	    	    		vSliceMap.get(target).setCell(c, visibleRowOrder.indexOf(source), timeOrder.indexOf(t));
	    	    		hSliceMap.get(source).setCell(c, visibleColumnOrder.indexOf(target), timeOrder.indexOf(t));
	    	    		c.setVNodeSlice(vSliceMap.get(target));
	    	    		c.setHNodeSlice(hSliceMap.get(source));
					}
				}
			}
		}
	}	
	
	public MatrixCube(TimeGraph<CNode,CEdge,CTime> tGraph, float cellSize)
	{
		// Init
		this.tGraph = tGraph;
		this.timeOrder.addAll(tGraph.getTimes());
    	this.rowOrder.addAll(tGraph.getVertices());
    	this.columnOrder.addAll(tGraph.getVertices());
    	int nodeCount = columnOrder.size();

    	
    	TimeSlice timeslice;
    	Graph<CNode,CEdge> graph;
    	ArrayList<String> nodeLabels = new ArrayList<String>();
		for(CNode n : rowOrder){
			nodeLabels.add(tGraph.getVertexLabel(n));
		}
		int timeCount = tGraph.getTimes().size();
		ArrayList<String> timeLabels = new ArrayList<String>();
		for(CTime time : tGraph.getTimes())
		{
			graph = tGraph.getGraph(time);
			timeslice = new TimeSlice(this, nodeCount,nodeCount, time);
			timeslice.setLabel(time.getLabel());
			timeslice.setRowLabels(nodeLabels);
			timeslice.setColumnLabels(nodeLabels);
			tSlices.add(timeslice);
        	tSliceMap.put(time, timeslice);
        	timeLabels.add(time.getLabel());
		}

		// CREATE NODE SLICES
		VNodeSlice vs;
		HNodeSlice hs;
		for(CNode n : tGraph.getVertices())
		{
			hs = new HNodeSlice(this, nodeCount, timeCount, n);
			hs.setLabel(tGraph.getVertexLabel(n));
			hs.setRowLabels(nodeLabels);
			hs.setColumnLabels(timeLabels);
			hSlices.add(hs);
			hSliceMap.put(n, hs);

			vs = new VNodeSlice(this, nodeCount, timeCount, n);
			vs.setLabel(tGraph.getVertexLabel(n));
			vs.setRowLabels(nodeLabels);
			vs.setColumnLabels(timeLabels);
        	vSlices.add(vs);
			vSliceMap.put(n, vs);
		}	
		
		// Create cells
		Cell c;
		CNode source, target;
		Pair<CNode> endPoints;
		Graph<CNode, CEdge> g;
		for(CTime t : tGraph.getTimes())
		{
			g = tGraph.getGraph(t);
			for(CEdge e : g.getEdges())
			{
				if(e.getWeight() == 0) 
					continue;
			
				this.maxWeight = Math.max(e.getWeight(), maxWeight);
				c = new Cell(cellSize, cellSize, cellSize);
				c.setOwner(e);
				cells.put(e,c);
	    		endPoints = g.getEndpoints(e);
	    		source = endPoints.getFirst();
	    		target = endPoints.getSecond();
	    		
	    		tSliceMap.get(t).setCell(c, rowOrder.indexOf(source), columnOrder.indexOf(target));
	    		vSliceMap.get(target).setCell(c, rowOrder.indexOf(source), timeOrder.indexOf(t));
	    		hSliceMap.get(source).setCell(c, columnOrder.indexOf(target), timeOrder.indexOf(t));
	    		
	    		c.setGraphSlice(tSliceMap.get(t));
	    		c.setVNodeSlice(vSliceMap.get(target));
	    		c.setHNodeSlice(hSliceMap.get(source));
			
			}
		}
	}
	
	////////////////
	/// ORDERING ///
	////////////////
	
	protected static int ordering = -1;
	public static final int ORDERING_GLOBAL = 0;
	public static final int ORDERING_LOCAL = 1;
	public static final int ORDERING_INDIVIDUAL = 2;
	

	/** Calculates an optimal ordering, taking into account the passed edges.
	 * of the graph and updates the slices.
	 * @param times
	 */
	public void reorderNodes(Collection<CEdge> validEdges)
	{
		ArrayList<CNode> nodeOrder = new ArrayList<CNode>();
        MatrixUtils mu = new MatrixUtils();
		if(method == CLUSTER)
		{
            nodeOrder = mu.reorderHierarchical(tGraph, validEdges);
		}else{
			nodeOrder = mu.reorderCutHillMcKee(tGraph, validEdges);
		}
		
		// Reverse order so that the node with larger degree comes first. 
		if(	tGraph.getNeighbors(nodeOrder.get(0)).size() < tGraph.getNeighbors(nodeOrder.get(nodeOrder.size()-1)).size() ){
			Collections.reverse(nodeOrder);
		}
		
		setNodeOrder(nodeOrder);
	}

	/** Sets the node ordering of the cube and updates cell positions in 
	 * all slices. 
	 * 
	 * @param nodes -- The new node order.
	 */
	public void setNodeOrder(ArrayList<CNode> sentNodes)
	{
		ArrayList<HierarchicalCNode> nodes = setHierarchicalNodeOrder();
		
		ArrayList<HierarchicalCNode> rowNodes = new ArrayList<>();
		ArrayList<HierarchicalCNode> columnNodes = new ArrayList<>();
		ArrayList<HierarchicalCNode> visibleRowNodes = new ArrayList<>();
		ArrayList<HierarchicalCNode> visibleColumnNodes = new ArrayList<>();
		
		ArrayList<String> rowNodesLabels = new ArrayList<>();
		ArrayList<String> columnNodesLabels = new ArrayList<>();	
		ArrayList<String> visibleRowNodesLabels = new ArrayList<>();
		ArrayList<String> visibleColumnNodesLabels = new ArrayList<>();			
		
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i) instanceof HierarchicalCNode){
				HierarchicalCNode hn = (HierarchicalCNode) nodes.get(i);
				String vertexLabel = tGraph.getVertexLabel(hn);
				if (hn.belongsToSourceOnto()) {
					rowNodes.add(hn);
					rowNodesLabels.add(vertexLabel);
					if(hn.isVisible()){
						visibleRowNodes.add(hn);
						visibleRowNodesLabels.add(vertexLabel);
					}
				} else {
					columnNodes.add(hn);
					columnNodesLabels.add(vertexLabel);
					if(hn.isVisible()){
						visibleColumnNodes.add(hn);
						visibleColumnNodesLabels.add(vertexLabel);
					}
				}
			}
		}
		
		if (!rowNodes.isEmpty() && !columnNodes.isEmpty()) {
			rowOrder = (ArrayList<CNode>) rowNodes.clone();		
			columnOrder  = (ArrayList<CNode>) columnNodes.clone();
			visibleRowOrder = (ArrayList<CNode>) visibleRowNodes.clone();		
			visibleColumnOrder  = (ArrayList<CNode>) visibleColumnNodes.clone();
			
			// update slice lists: 
			hSlices.clear();
			vSlices.clear();
			//visibleHSlices.clear();
			//visibleVSlices.clear();
			for(CNode n : nodes){ 
				
				if (n instanceof HierarchicalCNode) {
					HierarchicalCNode hn = (HierarchicalCNode) n;
					if (hn.isVisible()) {
						
						if (hn.belongsToSourceOnto()) {
							HNodeSlice hSlice = hSliceMap.get(hn);
							if (hSlice == null)
								hSlice = new HNodeSlice(this, visibleColumnOrder.size(), tSlices.size(), hn);
							else
								hSlice.reinitializeSlice(visibleColumnOrder.size(), tSlices.size());
							
							hSlice.setRowLabels(visibleColumnNodesLabels);
							hSlice.setColumnLabels(timeOrderLabels);
							hSlice.setLabel(tGraph.getVertexLabel(hn));
							hSlices.add(hSlice); 
							
							if (hSliceMap.get(hn) == null)
								hSliceMap.put(hn, hSlice);
							
							//if(hn.isVisible())
								//visibleHSlices.add(hSlice);
						} else {
							VNodeSlice vSlice = vSliceMap.get(hn);
							if (vSlice == null)
								vSlice = new VNodeSlice(this, visibleRowOrder.size(), tSlices.size(), hn);
							else 
								vSlice.reinitializeSlice(visibleRowOrder.size(), tSlices.size());
							
							vSlice.setRowLabels(visibleRowNodesLabels);
							vSlice.setColumnLabels(timeOrderLabels);
							vSlice.setLabel(tGraph.getVertexLabel(hn));						
							vSlices.add(vSlice); 
							
							if (vSliceMap.get(hn) == null)
								vSliceMap.put(hn, vSlice);							
							//if(hn.isVisible())
								//visibleVSlices.add(vSlice);
						}
					}
				}
			}
		} else {
			Log.err("Either the source or target ontology does not contain any concepts to show.");
		}

		//Log.out(this, "rowOrder.size()" + rowOrder.size());
		//Log.out(this, "columnOrder.size()" + columnOrder.size());
		//Log.out(this, "visibleRowOrder.size()" + visibleRowOrder.size());
		//Log.out(this, "visibleColumnOrder.size()" + visibleColumnOrder.size());		
	
		
		// Update slices
		Pair<CNode> endPoints;
		HierarchicalCNode source, target;
		CEdge e;
		CTime t;

		Graph<CNode, CEdge> g;
		for(TimeSlice s: tSlices)
		{
			t = s.getData();
			s.reinitializeSlice(visibleRowOrder.size(), visibleColumnOrder.size());
			
			// get edges
			g = tGraph.getGraph(t);
			for(CEdge edge : g.getEdges())
			{
	    		endPoints = g.getEndpoints(edge);
	    		source = (HierarchicalCNode) endPoints.getFirst();
	    		target = (HierarchicalCNode) endPoints.getSecond();
	    		
				if (source.isVisible() && target.isVisible()) {
					
					// check if there is a cell for the edge
					Cell cell = cells.get(edge);
					if (cell == null) {
						
						cell = new Cell(MATRIX_CELL_SIZE, MATRIX_CELL_SIZE, MATRIX_CELL_SIZE);
						cell.setOwner(edge);
						cells.put(edge,cell);
			    			
	    				tSliceMap.get(t).setCell(cell, visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target));
	    	    		vSliceMap.get(target).setCell(cell, visibleRowOrder.indexOf(source), timeOrder.indexOf(t));
	    	    		hSliceMap.get(source).setCell(cell, visibleColumnOrder.indexOf(target), timeOrder.indexOf(t));
	    	    		//cell.setVNodeSlice(vSliceMap.get(target));
	    	    		//cell.setHNodeSlice(hSliceMap.get(source));
	    	    		
					} else {
			    		try{
			    			tSliceMap.get(t).setCell(cell, visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target));
			    		}catch(Exception ec){
			    			Log.err(this, source.getLabel() + "-" + target.getLabel());
			    		}
			    		hSliceMap.get(source).setCell(cell, visibleColumnOrder.indexOf(target), timeOrder.indexOf(t));
			    		vSliceMap.get(target).setCell(cell, visibleRowOrder.indexOf(source), timeOrder.indexOf(t));
			    		
/*			    		cell.setGraphPos(tSliceMap.get(t).getRelGridCoords(visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target)));
			    		cell.setHNodePos(hSliceMap.get(source).getRelGridCoords(visibleColumnOrder.indexOf(target), timeOrder.indexOf(t)));
						cell.setVNodePos(vSliceMap.get(target).getRelGridCoords(visibleRowOrder.indexOf(source), timeOrder.indexOf(t)));
						
			    		cell.setGraphSlice(tSliceMap.get(t));
			    		cell.setVNodeSlice(vSliceMap.get(target));
			    		cell.setHNodeSlice(hSliceMap.get(source));

			    		tSliceMap.get(t).setRowLabels(visibleRowNodesLabels);
			    		tSliceMap.get(t).setColumnLabels(visibleColumnNodesLabels);

			    		hSliceMap.get(source).setRowLabels(visibleColumnNodesLabels);
			    		vSliceMap.get(target).setRowLabels(visibleRowNodesLabels);*/
					}
					
		    		cell.setGraphPos(tSliceMap.get(t).getRelGridCoords(visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target)));
		    		cell.setHNodePos(hSliceMap.get(source).getRelGridCoords(visibleColumnOrder.indexOf(target), timeOrder.indexOf(t)));
					cell.setVNodePos(vSliceMap.get(target).getRelGridCoords(visibleRowOrder.indexOf(source), timeOrder.indexOf(t)));
					
		    		cell.setGraphSlice(tSliceMap.get(t));
		    		cell.setVNodeSlice(vSliceMap.get(target));
		    		cell.setHNodeSlice(hSliceMap.get(source));
					
		    		hSliceMap.get(source).setRowLabels(visibleColumnNodesLabels);
		    		vSliceMap.get(target).setRowLabels(visibleRowNodesLabels);
				}
			}
			
			tSliceMap.get(t).setRowLabels(visibleRowNodesLabels);
    		tSliceMap.get(t).setColumnLabels(visibleColumnNodesLabels);
			
			// if not create cell
			// else do as before
			
/*			for(Cell c : s.getCells())
			{
				e = c.getData();
				endPoints = tGraph.getGraph(t).getEndpoints(e);
	    		source = (HierarchicalCNode) endPoints.getFirst();
	    		target = (HierarchicalCNode) endPoints.getSecond();

	    		if (source.isVisible() && target.isVisible()) {
		    		try{
		    			tSliceMap.get(t).setCell(c, visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target));
		    		}catch(Exception ec){
		    			Log.err(this, source.getLabel() + "-" + target.getLabel());
		    		}
		    		hSliceMap.get(source).setCell(c, visibleColumnOrder.indexOf(target), timeOrder.indexOf(t));
		    		vSliceMap.get(target).setCell(c, visibleRowOrder.indexOf(source), timeOrder.indexOf(t));
		    		
		    		c.setGraphPos(tSliceMap.get(t).getRelGridCoords(visibleRowOrder.indexOf(source), visibleColumnOrder.indexOf(target)));
		    		c.setHNodePos(hSliceMap.get(source).getRelGridCoords(visibleColumnOrder.indexOf(target), timeOrder.indexOf(t)));
					c.setVNodePos(vSliceMap.get(target).getRelGridCoords(visibleRowOrder.indexOf(source), timeOrder.indexOf(t)));
					
		    		c.setGraphSlice(tSliceMap.get(t));
		    		c.setVNodeSlice(vSliceMap.get(target));
		    		c.setHNodeSlice(hSliceMap.get(source));

		    		tSliceMap.get(t).setRowLabels(visibleRowNodesLabels);
		    		tSliceMap.get(t).setColumnLabels(visibleColumnNodesLabels);

		    		hSliceMap.get(source).setRowLabels(visibleColumnNodesLabels);
		    		vSliceMap.get(target).setRowLabels(visibleRowNodesLabels);
	    		}
	    		else {
	    			//Log.err("Matrix Cube: One of the source or target nodes is not visible. "
	    				//	+ "SOURCE: " + source.getLabel() + "	TARGET: " + target.getLabel());
	    		}
			}*/
		}
	}
	
	private ArrayList<HierarchicalCNode> setHierarchicalNodeOrder(){
		
		ArrayList<HierarchicalCNode> orderedList = new ArrayList<HierarchicalCNode>();
		
		for (int i = 0; i < firstLevelVerticies.size(); i++) {
			
			HierarchicalCNode hn = firstLevelVerticies.get(i);
			
			orderedList.add(hn);
			depthFirstTraversal(hn, orderedList);
			
		}
		
		return orderedList;
	}
	
	private void depthFirstTraversal(HierarchicalCNode node, ArrayList<HierarchicalCNode> orderedList) {
		
		// TODO VI - to optimize - limit the traversal to the number of visible levels
		ArrayList<HierarchicalCNode> children = node.getChildren();
		Collections.sort(children, new NodeLabelComparator(tGraph));
		for (int i = 0; i < children.size(); i++) {
			
			HierarchicalCNode child = children.get(i);
			orderedList.add(child);
			depthFirstTraversal(child, orderedList);
		}
	}
	
	public void reorderTimes(Collection<CEdge> validEdges)
	{
		ArrayList<CTime> timeOrder = new ArrayList<CTime>();
        MatrixUtils mu = new MatrixUtils();
        timeOrder = mu.reorderTimesHierarchical(tGraph, validEdges);
		
		setTimeOrder(timeOrder);
	}
	
	public void setTimeOrder(ArrayList<CTime> times)
	{
		this.timeOrder = times;
		
		// update slice lists: 
		tSlices.clear();
		for(CTime t : timeOrder){ 
			tSlices.add(tSliceMap.get(t)); 
		}
		
		int t;
		int r;
		for(Cell c : getCells()){
			VNodeSlice s = c.getVNodeSlice();
			t = timeOrder.indexOf(c.getTimeSlice().getData());
			r = rowOrder.indexOf(c.getHNodeSlice().getData());
			s.setCell(c, r, t);
			c.setVNodePos(s.getRelGridCoords(r, t));
		}
	}
	
	public void setAlignmentsOrder(String[] order) {
		
		ArrayList<CTime> oldTimeOrder = new ArrayList<>(timeOrder);
		timeOrder.clear();
		timeOrderLabels.clear();
		for (int i = 0; i < order.length; i++) {
			for (CTime t : oldTimeOrder){
				if (t.getLabel().equals(order[i])){
					timeOrder.add(t);
					timeOrderLabels.add(t.getLabel());
					break;
				}
			}
		}
		
		// update slice lists: 
		tSlices.clear();
		for(CTime t : timeOrder){ 
			tSlices.add(tSliceMap.get(t)); 
		}
		
		ArrayList<CNode> unused = new ArrayList<>();
		setNodeOrder(unused);
	}
	


	/////////////////
	/// GET & SET ///
	/////////////////
	
	public TimeSlice getTimeSlice(CTime t){ return tSliceMap.get(t);}
	public HNodeSlice getHNodeSlice(CNode n){ return hSliceMap.get(n);}
	public VNodeSlice getVNodeSlice(CNode n){ return vSliceMap.get(n);}
	public TimeSlice getTimeSlice(int t){ return tSliceMap.get(timeOrder.get(t));}
	
	// VI hope it works now!!! we should get from the visible order not just from all!!!
	//public HNodeSlice getHNodeSlice(int n){ return hSliceMap.get(rowOrder.get(n));}
	//public VNodeSlice getVNodeSlice(int n){ return vSliceMap.get(columnOrder.get(n));}
	public HNodeSlice getVisibleHNodeSlice(int n){ return hSliceMap.get(visibleRowOrder.get(n));}
	public VNodeSlice getVisibleVNodeSlice(int n){ return vSliceMap.get(visibleColumnOrder.get(n));}

	/** Returns the time slices in order **/
	public ArrayList<TimeSlice> getTimeSlices(){ return tSlices; }
	public ArrayList<VNodeSlice> getAllVNodeSlices(){ return vSlices;}
	public ArrayList<HNodeSlice> getAllHNodeSlices(){ return hSlices; }	
	
	// VI changed due to dynamic loading
	//public ArrayList<VNodeSlice> getVisibleVNodeSlices(){ return visibleVSlices;}
	//public ArrayList<HNodeSlice> getVisibleHNodeSlices(){ return visibleHSlices; }

	public ArrayList<VNodeSlice> getVisibleVNodeSlices(){ return vSlices;}
	public ArrayList<HNodeSlice> getVisibleHNodeSlices(){ return hSlices; }
	
	//public int getRowIndex(CNode node){ return rowOrder.indexOf(node); }
	//public int getColumnIndex(CNode node){ return columnOrder.indexOf(node); }
	public int getVisibleRowIndex(CNode node){ return visibleRowOrder.indexOf(node); }
	public int getVisibleColumnIndex(CNode node){ return visibleColumnOrder.indexOf(node); }
	public int getTimeIndex(CTime time){ return timeOrder.indexOf(time); }

	public int getVisibleRowCount(){ return visibleRowOrder.size();  }
	public int getVisibleColumnCount(){ return visibleColumnOrder.size();  }
	public int getAllRowCount(){ return rowOrder.size();  }
	public int getAllColumnCount(){ return columnOrder.size();  }	
	public int getTimeCount(){ return timeOrder.size();  }

	public Collection<Cell> getCells() { return cells.values(); }
	
	public TimeGraph<CNode,CEdge,CTime> getTimeGraph(){return tGraph;}

	public TimeSlice getLastTimeSlice() { return tSliceMap.get(getLastTime());}
	public TimeSlice getFirstTimeSlice() { return tSliceMap.get(timeOrder.get(0));}
	//public VNodeSlice getLastVNodeSlice() { return vSliceMap.get(getLastColumnNode());}
	//public VNodeSlice getFirstVNodeSlice() { return vSliceMap.get(columnOrder.get(0));}
	//public HNodeSlice getLastHNodeSlice() { return hSliceMap.get(getLastRowNode());}
	//public HNodeSlice getFirstHNodeSlice() { return hSliceMap.get(rowOrder.get(0));}
	// VI changed to only take into account the visible ones
	public VNodeSlice getLastVNodeSlice() { return vSliceMap.get(getLastVisibleColumnNode());} 
	public VNodeSlice getFirstVNodeSlice() { return vSliceMap.get(visibleColumnOrder.get(0));}
	public HNodeSlice getLastHNodeSlice() { return hSliceMap.get(getLastVisibleRowNode());}
	public HNodeSlice getFirstHNodeSlice() { return hSliceMap.get(visibleRowOrder.get(0));}
	
	public CTime getLastTime(){return timeOrder.get(timeOrder.size()-1);}
	public CNode getLastRowNode(){return rowOrder.get(rowOrder.size()-1);}
	public CNode getLastColumnNode(){return columnOrder.get(columnOrder.size()-1);}
	
	public CNode getLastVisibleRowNode(){return visibleRowOrder.get(visibleRowOrder.size()-1);}
	public CNode getLastVisibleColumnNode(){return visibleColumnOrder.get(visibleColumnOrder.size()-1);}
	
	public int getOrdering() {return ordering;}


	public Object getCell(CEdge e) {
		return cells.get(e);
	}

}
