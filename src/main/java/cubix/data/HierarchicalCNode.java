package cubix.data;

import java.util.ArrayList;
import java.util.HashMap;

import cubix.helper.Log;

/**
 * 
 * @author VI
 *
 */
public class HierarchicalCNode extends CNode{

	/** The node can have only one parent in the visual hierarchy although in the ontology ot has multiple.
	 * 	To account for this the identicalNodes variable stores the nodes with the same IRI found during the
	 * 	traversal of the ontology. */
	private HierarchicalCNode parent = null;
	private ArrayList<HierarchicalCNode> children = new ArrayList<HierarchicalCNode>();
	private ArrayList<String> labels = new ArrayList<>();
	
	/** In order to support multiple inheritance, when an ontology concept has more than one parent 
	 * a new HierarchicalCNode is created and the nodes are added to this list. 
	 * This will likely simplify the visualization. */
	private ArrayList<HierarchicalCNode> identicalNodes = new ArrayList<HierarchicalCNode>();
	
	/** Depicts the depth/level of the node in the hierarchy. */
	private int nodeDepth = 0;
	
	/**  source = true - the node belongs to the source (first) ontology; false - to the second */
	private boolean belongsToSourceOnto = true;
	
	/**
	 *  if the node is visible, i.e., we can't show all nodes;
	 *  if the node is visible, the respective slice must be visible as well
	 */
	private boolean visible = false;
	
	/**
	 * This structure stores the regular edges in which this node participates.
	 * Regular edges are those in the alignment, i.e., a regular edge represents a mapping 
	 * between two concepts and their similarity value (between 0 and 1).
	 * It is used for optimization purposes when calculating the accumulated edges.
	 * Accumulated edges represent the number of mappings this node and its children participate.
	 * (Just an example) For owl:Thing the value for the accumulated edges is equal to the number of mappings.
	 */
	private HashMap<CTime, ArrayList<CEdge>> mappingEdges = new HashMap<CTime, ArrayList<CEdge>>();
	
	/** Shows if the node has been expanded in the visualization **/
	private boolean expanded = false;
	

	public HierarchicalCNode(String id) {
		super(id);
	}
	
	public ArrayList<HierarchicalCNode> getIdenticalNodes() {
		return identicalNodes;
	}

	public void setIdenticalNodes(ArrayList<HierarchicalCNode> identicalNodes) {
		this.identicalNodes = identicalNodes;
	}
	
	public void addIdenticalNode(HierarchicalCNode identicalNode){
		identicalNodes.add(identicalNode);
	}
	
	public void addIdenticalNodes(ArrayList<HierarchicalCNode> identicalNodes){
		this.identicalNodes.addAll(identicalNodes);
	}

	public void addMappingEdge(CEdge mappingEdge, CTime time) {
		ArrayList<CEdge> edges = this.mappingEdges.get(time);
		if (edges == null) 
			edges = new ArrayList<CEdge>();
		edges.add(mappingEdge);
		this.mappingEdges.put(time, edges);
	}
	
	public ArrayList<CEdge> getMappingEdges(CTime time) {
		return mappingEdges.get(time);
	}

	public void setMappingEdges(HashMap<CTime, ArrayList<CEdge>> mappingEdges) {
		this.mappingEdges = mappingEdges;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public HierarchicalCNode getParent() {
		return parent;
	}

	public void setParent(HierarchicalCNode parent) {
		if(this.parent == null)
			this.parent = parent;
		else
			Log.err("[HierarchicalCNode] trying to set multiple parents for node: " + getLabel());
	}

	public int getNodeDepth() {
		return nodeDepth;
	}

	public void setNodeDepth(int nodeDepth) {
		this.nodeDepth = nodeDepth;
	}

	public String getLabel(){
		// always use the first label to present; the other are available when the user needs to see all of them
		return labels.get(0);
	}
	
	public ArrayList<String> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}


	public ArrayList<HierarchicalCNode> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<HierarchicalCNode> children) {
		this.children = children;
	}
	
	public void addChild(HierarchicalCNode child){
		this.children.add(child);
	}
	
	public boolean hasChild(HierarchicalCNode child){
		
		return children.contains(child);
	}
	
	public boolean belongsToSourceOnto() {
		return belongsToSourceOnto;
	}

	public void setBelongsToSourceOnto(boolean belongsToSourceOnto) {
		this.belongsToSourceOnto = belongsToSourceOnto;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
