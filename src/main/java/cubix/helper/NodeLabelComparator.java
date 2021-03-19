package cubix.helper;

import java.util.Comparator;

import cubix.data.CNode;
import cubix.data.HierarchicalCNode;
import cubix.data.TimeGraph;

public class NodeLabelComparator implements Comparator<CNode> {

	
	private TimeGraph tGraph;

	public NodeLabelComparator(TimeGraph tGraph){
		this.tGraph = tGraph;
	}

	public int compare(CNode n1, CNode n2) {
		return tGraph.getVertexLabel(n1).compareToIgnoreCase(tGraph.getVertexLabel(n2));
	}
}
