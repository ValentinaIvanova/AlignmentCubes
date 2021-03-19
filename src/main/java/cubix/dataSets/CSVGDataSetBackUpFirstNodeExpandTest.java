package cubix.dataSets;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.awt.Color;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import cubix.CubixVis;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.HierarchicalCNode;
import cubix.data.OntologyUtils;
import cubix.data.TimeGraph;
import cubix.data.TimeGraphUtils;
import cubix.helper.Log;
import edu.uci.ics.jung.graph.Graph;

import com.opencsv.*;

public class CSVGDataSetBackUpFirstNodeExpandTest{

	/*
	public static final String TAB_NODES = "node table";
	public static final String COL_GROUP = "node groups";
	public static final String GRAPH_FILE_EXT = "csvg"; //CSV-Geaph
	public static final String DLM = "!"; //CSV delimiter
		
	
	protected File dir;
	protected ArrayList<Color> possibleColors = new ArrayList<Color>();
	private HashMap<Color, String> colors;
	protected static HashMap<String, Color> groupColors = new HashMap<String, Color>();
	protected static HashMap<Color, Integer> colorShape = new HashMap<Color, Integer>();
	protected static int RECTANGLE = 0;
	protected static int CIRCLE = 1;
	
	protected static String header[];
	private static float maxWeight;
	private static float  minWeight;
	
	private static TimeGraphUtils<CNode, CEdge, CTime> utils; 
	
	static DateFormat df1 = new SimpleDateFormat("yyyy");
	static DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
	
	*//**
	 * VI
	 *//*
	private static OntologyUtils ontoUtils;


	public static TimeGraph loadData(File file) 
	{
		maxWeight = -1;
		minWeight = 100000;
		TimeGraph<CNode, CEdge, CTime> tGraph = new TimeGraph<CNode, CEdge, CTime>();
		utils = new TimeGraphUtils<CNode, CEdge, CTime>();
		
		int loaded = 0;
		loadCSVFile(file, tGraph);	
		CubixVis.WEIGHT_MAX = maxWeight;
		CubixVis.WEIGHT_MIN = minWeight;
		return tGraph;
	}
	
	*//**
	 * VI Load two ontologies (owl format) and their alignments (cubix csv format)
	 **//*
	public static TimeGraph loadOntologiesAndAlignments(File sourceOntoo, File targetOntoo, File alignmento){
		
		// VI check the weights
		maxWeight = -1;
		minWeight = 100000;
		TimeGraph<CNode, CEdge, CTime> alignGraph = new TimeGraph<CNode, CEdge, CTime>();
		ontoUtils = new OntologyUtils();
		
		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//mouse.owl");
		//File targetOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//human.owl");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML2013-2016.csv");
		
		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI-Benchmark-alignments//bench//benchmarks//303//de.uni-karlsruhe.aifb.ontology.owl");
		File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI-Conference-alignments//conference//confOf.owl");
		File targetOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI-Conference-alignments//conference//ekaw.owl");
		File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML2013-2016-Conf - Copy.csv");
		
		HashMap<String,HierarchicalCNode> sourceVertices = loadOntology(sourceOnto, true);
		System.out.println("******************");
		HashMap<String,HierarchicalCNode> targetVertices = loadOntology(targetOnto, false);
		loadAlignment(alignment, sourceVertices, targetVertices, alignGraph);	
		CubixVis.WEIGHT_MAX = maxWeight;
		CubixVis.WEIGHT_MIN = minWeight;
		return alignGraph;		
	}
	
	*//**
	 * VI
	 * @param file
	 * @return
	 *//*
	private static HashMap<String,HierarchicalCNode> loadOntology(File file, boolean source){
		
		OWLClass owlThing = ontoUtils.getOwlDataFactory().getOWLThing();
		OWLOntology ontology = ontoUtils.getOntology(file);
		
		String label = extractClassLabel(owlThing, ontology);
		String labelSource = null;
		if(source)
			labelSource = "s";
		else
			labelSource = "t";

		String[] id = new String[]{label, labelSource};
		HierarchicalCNode owlThingNode = new HierarchicalCNode(id);
		if (!source)
			owlThingNode.setBelongsToSourceOnto(false);
		
		HashMap<String,HierarchicalCNode> vertices = new HashMap<String, HierarchicalCNode>();
		vertices.put(label, owlThingNode);
		
		OWLReasoner reasoner = ontoUtils.getReasoner(ontology);	
		
		walkSubclasses(vertices, reasoner, owlThing, owlThingNode, ontology, source);
		
		reasoner.dispose();
		return vertices;
	}

	*//**
	 * VI it only gets the first label!!!
	 * @param clazz
	 * @param ontology
	 * @return
	 *//*
	private static String extractClassLabel(OWLClass clazz, OWLOntology ontology) {
		Optional<OWLAnnotation> anno = getAnnotationObjects(clazz, ontology).filter(annot -> annot.getProperty().isLabel()).findFirst();
		String label = null;
		if (anno.isPresent()){
			label = ((OWLLiteral) anno.get().getValue()).getLiteral();
		} else {
			label = clazz.toStringID();
			// VI abdomen/pelvis/perineum blood vessel???
		}
		return label;
	}
	

	private static void walkSubclasses(HashMap<String, HierarchicalCNode> vertices, 
			OWLReasoner reasoner, OWLClass clazz, HierarchicalCNode parentNode, OWLOntology ontology, boolean source) {
		//OWLReasoner reasoner = ontoUtils.getReasoner(ontology);	
		Stream<OWLClass> subClasses = reasoner.getSubClasses(clazz, true).entities();
		
		HierarchicalCNode node = null;
		String label = null;
		String labelSource = null;
		Iterator<OWLClass> it = subClasses.iterator();
		while (it.hasNext()){
			OWLClass next = it.next();
        	if (!next.equals(clazz) && !next.isBottomEntity()) {
    			label = extractClassLabel(next, ontology);
    			label = label.substring(label.indexOf('#')+1, label.length());
    			// VI nodes from both ontologies should have the same name
    			//if (!source)
    				//label = label + "_N";
        		//System.out.println(label);
    			if(source)
    				labelSource = "s";
    			else 
    				labelSource = "t";
    			String[] id = new String[]{label, labelSource};
        		node = new HierarchicalCNode(id);
        		if (!source)
        			node.setBelongsToSourceOnto(false);
        		node.addParent(parentNode);
        		parentNode.addChild(node);
        		vertices.put(label, node);
        		walkSubclasses(vertices, reasoner, next, node, ontology, source);
            }
        }
	}

	private static void loadAlignment(File alignmentsFile, HashMap<String,HierarchicalCNode> sourceVertices, 
			HashMap<String,HierarchicalCNode> targetVertices, TimeGraph<CNode, CEdge, CTime> alignGraph){


        float weight = 0;
        try {
    	    CSVReader r = new CSVReader(new FileReader(alignmentsFile));
    	    String[] line = r.readNext();
    	    CTime t = null;
    	    String timelabel;
    	    HierarchicalCNode source = null, target = null;
    	    boolean found;
    	    CEdge edge;
    	    while((line = r.readNext()) != null)
    	    {
    	    	if(line[0].contains("_"))
    	    		timelabel = (1950 + line[0].split("_")[1]) + "";
    	    	else if(line[0].contains("_")){
    	    		timelabel = line[0].split("-")[0];
    	    	}
    	    	timelabel = line[0];
    	    	found = false;
    	    	for(CTime ts : alignGraph.getTimes())
    	    	{
    	    		if(ts.getLabel().equals(timelabel))
    	    		{
    	    			found = true;
    	    			t = ts;
    	    			break;
    	    		}
    	    	}
    	    	if(!found){
    	    		try{
    	    			t = new CTime(df1.parse(timelabel).getTime());
//    	    			t.setDateFormat(l);
    	    			t.setLabel(timelabel);
    	    		}catch(ParseException ex){
    	    			try{
    	    				t = new CTime(df2.parse(timelabel).getTime());
    	    				t.setLabel(timelabel);
    	    			}catch(ParseException ex2){
    	    				t = new CTime(System.currentTimeMillis());
    	    				t.setLabel(timelabel);
    	    			}
    	    		}
    	    		alignGraph.createSliceGraph(t);
    	    	}

    	    	if(sourceVertices.containsKey(line[1])) {
    	    		source = sourceVertices.get(line[1]);
    	    		source.setVisible(true); // VI only for the text ; remove!
		    		alignGraph.addVertex(source, t);
		    		alignGraph.setNodeLabel(source, line[1]);    	    	
    	    	} 
    	    	else {
    	    		
    	    		Log.err("The concept/label " + line[1] + " does not exist in the source ontology or has not been loaded");
    	    	}
    	    	
    	    	if(targetVertices.containsKey(line[2])) {
    	    		target = targetVertices.get(line[2]);
    	    		target.setVisible(true);  // VI only for the test ; remove!
    	    		alignGraph.addVertex(target, t);
    	    		alignGraph.setNodeLabel(target, line[2]);
    	    	}
    	    	else{

    	    		Log.err("The concept/label " + line[2] + " does not exist in the target ontology or has not been loaded");
    	    		
    	    	}
    	    	
    	    	weight = Float.parseFloat(line[3]);
    	    	  
    	    	// VI for now assume only equivalence relations, in the future implement subsumption as well
				edge = new CEdge(source.getID()[1] + ":" + source.getID()[0] + "--" + target.getID()[1]+ ":" + target.getID()[0]);
		    	edge.setWeight(weight);
		    	alignGraph.addEdge(edge, source, target, t, true);
		    	//edge = new CEdge(target.getID() + "--" + source.getID()); //do not need to do it for the vis 
		    	//edge.setWeight(weight);
		    	//alignGraph.addEdge(edge, target, source, t, true);
		    	maxWeight = Math.max(weight, maxWeight);
				minWeight = Math.min(weight, minWeight);
			 }
    	    
    	    // Add all ontology nodes which are not in the alignment to the alignGraph for each time 
    	    // Question to Benjamin: Add to only one time should be enough, shouldn;t it?
    	    Set<String> sourceIt;
    	    Set<String> targetIt;
    	    CNode sourceNode;
    	    CNode targetNode;
    	    ArrayList<CTime> times = alignGraph.getTimes();
    	    for (int i = 0; i < times.size(); i++) {
    	    	sourceIt = sourceVertices.keySet();
        		Iterator<String> sourceNodesIterator = sourceIt.iterator();
				while(sourceNodesIterator.hasNext()){
        			String next = sourceNodesIterator.next();
        			sourceNode = sourceVertices.get(next);
        			if (!alignGraph.hasVertex(sourceNode)){
    		    		alignGraph.addVertex(sourceNode, times.get(i));
    		    		alignGraph.setNodeLabel(sourceNode, sourceNode.getID()[0]);    
        			}
        		}
        		targetIt = targetVertices.keySet();
        		Iterator<String> targetNodesIterator = targetIt.iterator();
				while (targetNodesIterator.hasNext()) {
					String next = targetNodesIterator.next();
					targetNode = targetVertices.get(next);
					if (!alignGraph.hasVertex(targetNode)) {
						alignGraph.addVertex(targetNode, times.get(i));
						alignGraph.setNodeLabel(targetNode, targetNode.getID()[0]);
					}
				}
			}

    	    
    	    
        }catch (IOException ex){
	        System.err.println("Error loading file " + alignmentsFile);
	        ex.printStackTrace();
	    } 
	}
	
	private static void loadCSVFile(File file, TimeGraph<CNode, CEdge, CTime> tGraph) 
	{
		
		HashMap<String,CNode> vertices = new HashMap<String, CNode>();
	        ArrayList<CNode> nodes = new ArrayList<CNode>();

	        float weight = 0;
	        try {
	    	    CSVReader r = new CSVReader(new FileReader(file));
	    	    String[] line = r.readNext();
	    	    String groupname, nodeLabel;
	    	    CTime t = null;
	    	    String timelabel;
	    	    CNode source, target;
	    	    boolean found;
	    	    CEdge edge;
	    	    while((line = r.readNext()) != null)
	    	    {
	    	    	if(line[0].contains("_"))
	    	    		timelabel = (1950 + line[0].split("_")[1]) + "";
	    	    	else if(line[0].contains("_")){
	    	    		timelabel = line[0].split("-")[0];
	    	    	}
	    	    	timelabel = line[0];
	    	    	found = false;
	    	    	for(CTime ts : tGraph.getTimes())
	    	    	{
	    	    		if(ts.getLabel().equals(timelabel))
	    	    		{
	    	    			found = true;
	    	    			t = ts;
	    	    			break;
	    	    		}
	    	    	}
	    	    	if(!found){
	    	    		try{
	    	    			t = new CTime(df1.parse(timelabel).getTime());
//	    	    			t.setDateFormat(l);
	    	    			t.setLabel(timelabel);
	    	    		}catch(ParseException ex){
	    	    			try{
	    	    				t = new CTime(df2.parse(timelabel).getTime());
	    	    				t.setLabel(timelabel);
	    	    			}catch(ParseException ex2){
	    	    				t = new CTime(System.currentTimeMillis());
	    	    				t.setLabel(timelabel);
	    	    			}
	    	    		}
	    	    		tGraph.createSliceGraph(t);
	    	    	}
	    	    	
	    	    	if(vertices.containsKey(line[1]))
	    	    		source = vertices.get(line[1]);
	    	    	else{
	    	    		String[] sNode = new String[]{line[1], "s"};
	    	    		source = new CNode(sNode);
	    	    		vertices.put(line[1], source);
	    	    		tGraph.addVertex(source, t);
	    	    		tGraph.setNodeLabel(source, line[1]);
	    	    	}
	    	    	if(vertices.containsKey(line[2]))
	    	    		target = vertices.get(line[2]);
	    	    	else{
	    	    		// VI this is from the original code when both sides were supposed to be he same
	    	    		// so maybe use sNode from the above?
	    	    		String[] tNode = new String[]{line[2], "t"}; 
	    	    		target = new CNode(tNode);
	    	    		vertices.put(line[2], target);
	    	    		tGraph.addVertex(target, t);
	    	    		tGraph.setNodeLabel(target, line[2]);
	    	    	}
	    	    	
	    	    	weight = Float.parseFloat(line[3]);
	    	    	  
					edge = new CEdge(source.getID()[1] + ":" + source.getID()[0] + "--" + target.getID()[1]+ ":" + target.getID()[0]);
			    	edge.setWeight(weight);
			    	tGraph.addEdge(edge, source, target, t, true);
			    	edge = new CEdge(target.getID()[1] + ":" + target.getID()[0] + "--" + source.getID()[1]+ ":" + source.getID()[0]);
			    	edge.setWeight(weight);
			    	tGraph.addEdge(edge, target, source, t, true);
			    	maxWeight = Math.max(weight, maxWeight);
					minWeight = Math.min(weight, minWeight);
				 }
	        }catch (IOException ex){
		        System.err.println("Error loading file " + file);
		        ex.printStackTrace();
		    } 
	   }
	
	
	
	public static void exportGraph(TimeGraph graph, JFrame frame, String fileName, boolean singleFile)
	{
		File f;
		File d = new File("export");
		if(!d.exists()){
			d.mkdir();
		}
		if(singleFile)
			f = new File(d.getAbsolutePath() + "/" + fileName + ".csv" );
		else
			f = new File(d.getAbsolutePath() + "/" + fileName );
			
		System.out.println("[CSVGDataSet] export file: " + f.getAbsolutePath());
		boolean success = exportGraph(f, graph, singleFile);
		
		if(!fileName.equals(""))
			return;
		if(success){
			JOptionPane.showMessageDialog(frame, 
			"Export successful");
		}else{
			JOptionPane.showMessageDialog(frame, 
			"Export faliure");
		}
	}
	
	
	public static void exportOBJ(TimeGraph graph, JFrame frame, String fileName, boolean singleFile)
	{
		File f;
		File d = new File("export");
		if(!d.exists()){
			d.mkdir();
		}
		if(singleFile)
			f = new File(d.getAbsolutePath() + "/" + fileName + ".csv" );
		else
			f = new File(d.getAbsolutePath() + "/" + fileName );
			
		System.out.println("[CSVGDataSet] export file: " + f.getAbsolutePath());
		boolean success = exportOBJ(f, graph, singleFile);
		
		if(!fileName.equals(""))
			return;
		if(success){
			JOptionPane.showMessageDialog(frame, 
			"Export successful");
		}else{
			JOptionPane.showMessageDialog(frame, 
			"Export faliure");
		}
	}
	
	//////////////////////
	/// EXPORT/IMPPORT ///
	//////////////////////
	
	*//** Exports the given graph into the passed directory. For each time slices
	 * a file of the format <code>time_</code><em>number<em> is created.*//*
	protected static boolean exportGraph(File f, TimeGraph<CNode, CEdge, CTime> timeGraph, boolean singleFile) 
	{
		int fields = 4;
		
		header = new String[fields];
		header[0] = "time";
		header[1] = "n1";
		header[2] = "n2";
		header[3] = "weight";
		
		
		CSVWriter fw = null;
		
		if(singleFile){
			try {
				fw = new CSVWriter(new FileWriter(f));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			fw.writeNext(header);
			Graph<CNode, CEdge> g;

			try {
				String[] line;
				for(CTime t : timeGraph.getTimes())
				{
					g = timeGraph.getGraph(t);
					for(CEdge e : g.getEdges())
					{
						// WRITE GRAPH ATTRIBUTES
						line = new String[fields];
						line[0] = t.getLabel();
						line[1] = timeGraph.getVertexLabel(g.getEndpoints(e).getFirst());
						line[2] = timeGraph.getVertexLabel(g.getEndpoints(e).getSecond());
						line[3] = e.getWeight() + "";
						fw.writeNext(line);
					}
				}
				fw.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}else
		{
			if(!f.exists())
				f.mkdir();
			
			String[] line;
			for(CTime t : timeGraph.getTimes())
			{
				try {
					File ft = new File(f.getAbsoluteFile() + "/" + t.getLabel()+ ".csv");
					fw = new CSVWriter(new FileWriter(ft));
					fw.writeNext(header);
					Graph<CNode, CEdge> g;
					g = timeGraph.getGraph(t);
					for(CEdge e : g.getEdges())
					{
						// WRITE GRAPH ATTRIBUTES
						line = new String[fields];
						line[0] = t.getLabel();
						line[1] = timeGraph.getVertexLabel(g.getEndpoints(e).getFirst());
						line[2] = timeGraph.getVertexLabel(g.getEndpoints(e).getSecond());
						line[3] = e.getWeight() + "";
						fw.writeNext(line);
					}
					fw.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return true;
	}
	
	*//** Exports the given graph into the passed directory. For each time slices
	 * a file of the format <code>time_</code><em>number<em> is created.*//*
	protected static boolean exportOBJ(File f, TimeGraph<CNode, CEdge, CTime> timeGraph, boolean singleFile) 
	{
		int fields = 4;
		
		header = new String[fields];
		header[0] = "time";
		header[1] = "n1";
		header[2] = "n2";
		header[3] = "weight";
		
		
		CSVWriter fw = null;
		
		if(singleFile){
			try {
				fw = new CSVWriter(new FileWriter(f));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			fw.writeNext(header);
			Graph<CNode, CEdge> g;

			try {
				String[] line;
				for(CTime t : timeGraph.getTimes())
				{
					g = timeGraph.getGraph(t);
					for(CEdge e : g.getEdges())
					{
						// WRITE GRAPH ATTRIBUTES
						line = new String[fields];
						line[0] = t.getLabel();
						line[1] = timeGraph.getVertexLabel(g.getEndpoints(e).getFirst());
						line[2] = timeGraph.getVertexLabel(g.getEndpoints(e).getSecond());
						line[3] = e.getWeight() + "";
						fw.writeNext(line);
					}
				}
				fw.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}else
		{
			if(!f.exists())
				f.mkdir();
			
			String[] line;
			for(CTime t : timeGraph.getTimes())
			{
				try {
					File ft = new File(f.getAbsoluteFile() + "/" + t.getLabel()+ ".csv");
					fw = new CSVWriter(new FileWriter(ft));
					fw.writeNext(header);
					Graph<CNode, CEdge> g;
					g = timeGraph.getGraph(t);
					for(CEdge e : g.getEdges())
					{
						// WRITE GRAPH ATTRIBUTES
						line = new String[fields];
						line[0] = t.getLabel();
						line[1] = timeGraph.getVertexLabel(g.getEndpoints(e).getFirst());
						line[2] = timeGraph.getVertexLabel(g.getEndpoints(e).getSecond());
						line[3] = e.getWeight() + "";
						fw.writeNext(line);
					}
					fw.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return true;
	}
	
*/	
	
}
