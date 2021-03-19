package cubix.dataSets;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import cubix.CubixVis;
import cubix.data.CEdge;
import cubix.data.CNode;
import cubix.data.CTime;
import cubix.data.HierarchicalCNode;
import cubix.data.OntologyUtils;
import cubix.data.TimeGraph;
import cubix.data.TimeGraphUtils;
import edu.uci.ics.jung.graph.Graph;

public class CSVGDataSet{

	
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
	private static int maxAccumulatedWeight;
	private static int minAccumulatedWeight;
	
	private static TimeGraphUtils<CNode, CEdge, CTime> utils; 
	
	static DateFormat df1 = new SimpleDateFormat("yyyy");
	static DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
	
	/** This is a supporting hashmap used to optimize the computation of the mappings between two nodes. **/
	private static HashMap<HierarchicalCNode, Set<CEdge>> accumulatedMappsPerNodeMap = new HashMap<>();
	
	private static ArrayList<HierarchicalCNode> firstLevelVerticies = new ArrayList<HierarchicalCNode>();
	
	/**
	 * VI
	 */
	private static OntologyUtils ontoUtils;
	/**
	 * how many levels under owl:Thing(level=0) are visible
	 */
	private static int sourceVisibilityLevel = 0; 
	private static int targetVisibilityLevel = 0;

	private static ArrayList<String []> performanceMetricsData = new ArrayList<String[]>();
 
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
	
	/**
	 * VI Load two ontologies (owl format) and their alignments (cubix csv format)
	 **/
	public static TimeGraph loadOntologiesAndAlignments(File sourceOnto, File targetOnto, File alignments, File measures, boolean onlyEquRelations){
		
		maxWeight = -1;
		minWeight = 100000;
		maxAccumulatedWeight = -1;
		minAccumulatedWeight = 100000;
		TimeGraph<CNode, CEdge, CTime> alignGraph = new TimeGraph<CNode, CEdge, CTime>();
		ontoUtils = new OntologyUtils();
	
		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI2016-LargeBio-dataset//oaei_NCI_small_overlapping_fma.owl");		
		//File targetOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI2016-LargeBio-dataset//oaei_FMA_small_overlapping_nci.owl");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LargeBio-AML2013-2016-LogMap2011-2016.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LargeBio-LogMap2011-2016.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LargeBio-AML2013-2016-half.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LargeBio-All-2016.csv");

		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//human.owl");		
		//File targetOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//mouse.owl");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//ReversedAML2013-2016ANDLogMap2011-2016.csv");

		
		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//mouse.owl");
		//File targetOnto = new File("C://Users//valiv11//Desktop//Projects//anatomy-dataset-2016//human.owl");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML2013-2016.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LogMap2011-2016.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML2013-2016ANDLogMap2011-2016.csv");

		
		//File sourceOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI-Conference-alignments//conference//confOf.owl");
		//File targetOnto = new File("C://Users//valiv11//Desktop//Projects//OAEI-Conference-alignments//conference//ekaw.owl");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML2013-2016-Conf.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//LogMap2011-2016-Conf.csv");
		//File alignment = new File("C://Users//valiv11//Desktop//Eclipse//workspace//AlignmentsEvaluationCube//datasets//AML-LogMap2011-2016-Conf.csv");


		// owlThing is level 0 
		sourceVisibilityLevel = 1;
		targetVisibilityLevel = 1;
		
		HashMap<String,ArrayList<HierarchicalCNode>> sourceVertices = loadOntology(sourceOnto, true);
		Iterator<String> it = sourceVertices.keySet().iterator();
		int sourceNodes = 0;
		while (it.hasNext()){
			String next = it.next();
			sourceNodes += sourceVertices.get(next).size();
			//if (sourceVertices.get(next).size() > 1){
			//	System.out.println("MANY: " + next + "            size" + sourceVertices.get(next).size());
			//}
		}
		System.out.println("sourceNodes \t " + sourceNodes);
		
		HashMap<String,ArrayList<HierarchicalCNode>> targetVertices = loadOntology(targetOnto, false);
		Iterator<String> itt = targetVertices.keySet().iterator();
		int targetNodes = 0;
		while (itt.hasNext()){
			String next = itt.next();
			targetNodes += targetVertices.get(next).size();
			//if (targetVertices.get(next).size() > 1){
			//	System.out.println("MANY: " + next + "            size" + targetVertices.get(next).size());
			//}
		}
		System.out.println("targetNodes \t " + targetNodes);
		
		//if (alignments.isFile()) {
			loadAlignment(alignments, sourceVertices, targetVertices, alignGraph, onlyEquRelations);	
		//} else if (alignments.isDirectory()) {
			//loadAlignmentFromFolder(alignments, sourceVertices, targetVertices, alignGraph, onlyEquRelations);	
			//loadAlignmentFromFolder(new File("C:\\Users\\valiv11\\Desktop\\Eclipse\\workspace\\Datasets\\ConferenceLogMapRA-Alignments\\Alignments"),
					//sourceVertices, targetVertices, alignGraph);	
		//}
		
		if (measures != null)
			loadAlignmentsMeasures(measures);
		CubixVis.WEIGHT_MAX = maxWeight;
		CubixVis.WEIGHT_MIN = minWeight;
		CubixVis.ACCUMULATED_WEIGHT_MAX = maxAccumulatedWeight;
		CubixVis.ACCUMULATED_WEIGHT_MIN = minAccumulatedWeight;
		return alignGraph;		
	}
	
	private static void loadAlignmentsMeasures(File measures) {
		
		performanceMetricsData.clear();
		CSVReader r;
		try {
			r = new CSVReader(new FileReader(measures));
			String[] line = r.readNext();
			performanceMetricsData.add(line);
			while((line = r.readNext()) != null)
			{
				performanceMetricsData.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String[]> getPerformanceMetricsData(){
		return performanceMetricsData;
	}

	/**
	 * VI
	 * @param file
	 * @return
	 */
	private static HashMap<String,ArrayList<HierarchicalCNode>> loadOntology(File file, boolean source){
		
		OWLClass owlThing = ontoUtils.getOwlDataFactory().getOWLThing();
		OWLOntology ontology = ontoUtils.getOntology(file);
		OWLReasoner reasoner = ontoUtils.getReasoner(ontology);	
		OWLOntologyID ontologyID = ontology.getOntologyID();
		if (source && CubixVis.SOURCE_ONTO.equals("")){
			if (ontologyID.getOntologyIRI().isPresent()){
				CubixVis.SOURCE_ONTO = "IRI: " + ontologyID.getOntologyIRI().get().toString();
			} else {
				CubixVis.SOURCE_ONTO = file.getName();
			}
			CubixVis.SOURCE_ONTO_NUM_CLASSES = reasoner.getSubClasses(owlThing, false).entities().filter(e -> e.isOWLClass()).count(); 
		}
		if (!source && CubixVis.TARGET_ONTO.equals("")){
			if (ontologyID.getOntologyIRI().isPresent()){
				CubixVis.TARGET_ONTO = "IRI: " + ontologyID.getOntologyIRI().get().toString();
			} else {
				CubixVis.TARGET_ONTO = file.getName();
			}
			CubixVis.TARGET_ONTO_NUM_CLASSES = reasoner.getSubClasses(owlThing, false).entities().filter(e -> e.isOWLClass()).count(); 
		}

		HashMap<String,ArrayList<HierarchicalCNode>> vertices = new HashMap<String,ArrayList<HierarchicalCNode>>();
		
		int depth = 0;
		walkSubclasses(vertices, reasoner, owlThing, null, ontology, source, depth+1);
		
		reasoner.dispose();
		return vertices;
	}

	/**
	 * VI TODO VI it only gets the first label!!!
	 * @param clazz
	 * @param ontology
	 * @return
	 */
	private static ArrayList<String> extractClassLabel(OWLClass clazz, OWLOntology ontology) {
		Iterator<OWLAnnotation> annotations = getAnnotationObjects(clazz, ontology).filter(annot -> annot.getProperty().isLabel()).iterator();
		ArrayList<String> labels = new ArrayList<>();
		while(annotations.hasNext()){
			OWLAnnotation annotation = annotations.next();
			String label = ((OWLLiteral) annotation.getValue()).getLiteral();
			label = label.substring(label.indexOf('#')+1, label.length());
			labels.add(label);
			//System.out.println("CSVGDataSet: annotationssssss     " + ((OWLLiteral)annotation.getValue()).getLiteral());
		} 
		
		if(labels.isEmpty()) {
			String label = clazz.toStringID();
			label = label.substring(label.indexOf('#')+1, label.length());
			labels.add(label);
		}
			// VI maybe needs check abdomen/pelvis/perineum blood vessel???
		return labels;
	}
	

	private static void walkSubclasses(HashMap<String,ArrayList<HierarchicalCNode>> vertices, 
			OWLReasoner reasoner, OWLClass clazz, HierarchicalCNode parentNode, 
			OWLOntology ontology, boolean source, int depth) {

		Stream<OWLClass> subClasses = reasoner.getSubClasses(clazz, true).entities();

		int visibilityLevel = 0;
		if(source)
			visibilityLevel = sourceVisibilityLevel;
		else 
			visibilityLevel = targetVisibilityLevel;
		
		HierarchicalCNode node = null;
		Iterator<OWLClass> it = subClasses.iterator();
		while (it.hasNext()){
			OWLClass next = it.next();
			
        	if (!next.equals(clazz) && !next.isBottomEntity()) {

    			String iri = next.getIRI().getIRIString();
    			/* Overcomplicated code to account for multiple inheritance. */
				ArrayList<HierarchicalCNode> existingNodes = vertices.get(iri);
				String id = null;
				if((existingNodes != null) && !existingNodes.isEmpty()){
					//System.out.println("[CSVGDataSet] multiple inheritance for: " + iri);
					if(existingNodes.size() == 1){
						id = iri + "%1";
					} else {
						id = iri + "%" + existingNodes.size();
						//System.out.println("[CSVGDataSet] multiple inheritance for: " + id);
					}
				}
    			
				if(id != null)
					node = new HierarchicalCNode(id);
				else 
					node = new HierarchicalCNode(iri);
        		
        		node.setLabels(extractClassLabel(next, ontology));
        		
        		if (!source)
        			node.setBelongsToSourceOnto(false);
        		
        		if(parentNode != null) {
        			node.setParent(parentNode);
            		parentNode.addChild(node);
        		}
        		
        		node.setNodeDepth(depth);
        		
        		if(visibilityLevel >= depth)
        			node.setVisible(true);

        		if((existingNodes != null) && !existingNodes.isEmpty()){
        			for (int i = 0; i < existingNodes.size(); i++) {
        				existingNodes.get(i).addIdenticalNode(node);
            			node.addIdenticalNode(existingNodes.get(i));
					}
        			
        			existingNodes.add(node);
        			vertices.put(iri, existingNodes);
        		} else {
        			ArrayList<HierarchicalCNode> nodesToAdd = new ArrayList<HierarchicalCNode>();
        			nodesToAdd.add(node);
        			vertices.put(iri, nodesToAdd);
        		}
        		
				if(node.getNodeDepth() == 1)
					firstLevelVerticies.add(node);        		
        		
        		walkSubclasses(vertices, reasoner, next, node, ontology, source, depth+1);
        		
        		if (!node.getChildren().isEmpty() && (visibilityLevel - 1 >= depth))
        			node.setExpanded(true);
            }
        }
	}
	
	private static ArrayList<String[]> loadAlignmentFromFolder(File alignments, boolean onlyEquRelations) {

		ArrayList<String[]> result = new ArrayList<String[]>();
		File[] listOfFiles = alignments.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//System.out.println("File " + listOfFiles[i].getName());

				try {

					XMLReader parser = null;
					try {
						parser = XMLReaderFactory.createXMLReader( );
					} catch (SAXException e) {
						System.err.println("Couldn't locate a SAX parser");
					}

					int index = listOfFiles[i].getName().lastIndexOf(".");
					String fileName = listOfFiles[i].getName().substring(0, index);
					SAXAlignmentParser contentHandler = new SAXAlignmentParser(fileName, onlyEquRelations);
					parser.setContentHandler(contentHandler);

					parser.parse(listOfFiles[i].getAbsolutePath());

					result.addAll(contentHandler.getContent());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private static void loadAlignment(File alignmentsFile, HashMap<String,ArrayList<HierarchicalCNode>> sourceVertices, 
			HashMap<String,ArrayList<HierarchicalCNode>> targetVertices, TimeGraph<CNode, CEdge, CTime> alignGraph, boolean onlyEquRelations){


		float weight = 0;
		try {
			Iterator<String[]> iterator = null;
			String[] line;
			if (alignmentsFile.isFile()){
				CSVReader r = new CSVReader(new FileReader(alignmentsFile));
				iterator = r.iterator();
				// skip the first line since it is a header
				line = iterator.next();
			} else if (alignmentsFile.isDirectory()){
				iterator = loadAlignmentFromFolder(alignmentsFile, onlyEquRelations).iterator();
			}
			
			
			int lineCount = 0;
			CTime t = null;
			String timelabel;
			ArrayList<HierarchicalCNode> sources = new ArrayList<HierarchicalCNode>();  
			ArrayList<HierarchicalCNode> targets = new ArrayList<HierarchicalCNode>();  
			boolean found;
			CEdge edge;
			
//			HashMap<String,ArrayList<HierarchicalCNode>> swapVertices = new HashMap<>();
//			boolean ontoOrderChecked = false;
			
			while (iterator.hasNext())
			//while((line = r.readNext()) != null)
			{
				
				line = iterator.next();
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
					t = new CTime(System.currentTimeMillis());
					t.setLabel(timelabel);
					alignGraph.createSliceGraph(t);
				}
				
//				if (!ontoOrderChecked){
//					
//					if (!sourceVertices.containsKey(line[1]) && !targetVertices.containsKey(line[2])) {
//						swapVertices = sourceVertices;
//						sourceVertices = targetVertices;
//						targetVertices = swapVertices;
//						swapVertices = null;
//					}
//					ontoOrderChecked = true;
//				}
				
				lineCount++;
				sources.clear();
				targets.clear();

				if(sourceVertices.containsKey(line[1])) {
					ArrayList<HierarchicalCNode> sourceNodes = sourceVertices.get(line[1]);
					sources.addAll(sourceNodes);
					for (int i = 0; i < sourceNodes.size(); i++) {
						alignGraph.addVertex(sourceNodes.get(i), t);
						alignGraph.setNodeLabel(sourceNodes.get(i), sourceNodes.get(i).getLabel());    	 
					}
				} else if (sourceVertices.containsKey(line[2])) {
					ArrayList<HierarchicalCNode> sourceNodes = sourceVertices.get(line[2]);
					sources.addAll(sourceNodes);
					for (int i = 0; i < sourceNodes.size(); i++) {
						alignGraph.addVertex(sourceNodes.get(i), t);
						alignGraph.setNodeLabel(sourceNodes.get(i), sourceNodes.get(i).getLabel());    	 
					}
				}

				if(targetVertices.containsKey(line[2])) {
					ArrayList<HierarchicalCNode> targetNodes = targetVertices.get(line[2]);
					targets.addAll(targetNodes);
					for (int i = 0; i < targetNodes.size(); i++) {
						alignGraph.addVertex(targetNodes.get(i), t);
						alignGraph.setNodeLabel(targetNodes.get(i), targetNodes.get(i).getLabel());						
					}
				} else if(targetVertices.containsKey(line[1])) {
					ArrayList<HierarchicalCNode> targetNodes = targetVertices.get(line[1]);
					targets.addAll(targetNodes);
					for (int i = 0; i < targetNodes.size(); i++) {
						alignGraph.addVertex(targetNodes.get(i), t);
						alignGraph.setNodeLabel(targetNodes.get(i), targetNodes.get(i).getLabel());						
					}
				}

				if (line[3] != null){
					weight = Float.parseFloat(line[3]);
				} else {
					weight = 1.0f;
				}
				

				// TODO VI for now assume only equivalence relations, in the future implement subsumption as well
				for (int i = 0; i < sources.size(); i++) {
					for (int j = 0; j < targets.size(); j++) {
						edge = new CEdge(sources.get(i).getID() + "--" + targets.get(j).getID());
						//System.out.println("[CSVGDataSet] edge: " + sources.get(i).getID() + "--" + targets.get(j).getID());
						edge.setWeight(weight);
						alignGraph.addEdge(edge, sources.get(i), targets.get(j), t, false);
						sources.get(i).addMappingEdge(edge, t);
						targets.get(j).addMappingEdge(edge, t);
					}
				}

				//edge = new CEdge(target.getID() + "--" + source.getID()); //do not need to do it for the vis 
				//edge.setWeight(weight);
				//alignGraph.addEdge(edge, target, source, t, true);
				maxWeight = Math.max(weight, maxWeight);
				minWeight = Math.min(weight, minWeight);
			}

			CubixVis.MAPPINGS_COUNT = lineCount;
			
			/**
			 * Adds the nodes not in the alignment to the respective time/alignment matrices.
			 * For the visible nodes calculates the number of mappings in which their children nodes participate.
			 * This number is then assigned to another weight variable in the CEdge class.
			 */
			Set<String> sourceIt;
			Set<String> targetIt;
			ArrayList<HierarchicalCNode> sourceNodes;
			ArrayList<HierarchicalCNode> targetNodes;
			ArrayList<HierarchicalCNode> tempSourceNodes;
			ArrayList<HierarchicalCNode> tempTargetNodes;
			ArrayList<CTime> times = alignGraph.getTimes();
			for (int i = 0; i < times.size(); i++) {
				tempSourceNodes = new ArrayList<HierarchicalCNode>();
				tempTargetNodes = new ArrayList<HierarchicalCNode>();

				sourceIt = sourceVertices.keySet();
				Iterator<String> sourceNodesIriIterator = sourceIt.iterator();
				while(sourceNodesIriIterator.hasNext()){
					String nextIri = sourceNodesIriIterator.next();
					sourceNodes = sourceVertices.get(nextIri);
					for (int j = 0; j < sourceNodes.size(); j++) {
						HierarchicalCNode node = sourceNodes.get(j);
						if (!alignGraph.hasVertex(node)){
							alignGraph.addVertex(node, times.get(i));
							alignGraph.setNodeLabel(node, ((HierarchicalCNode) node).getLabel());    
						}
						//if (node.getNodeDepth() <= sourceVisibilityLevel){
							tempSourceNodes.add(node);
						//}
					}
				}
				targetIt = targetVertices.keySet();
				Iterator<String> targetNodesIriIterator = targetIt.iterator();
				while (targetNodesIriIterator.hasNext()) {
					String nextIri = targetNodesIriIterator.next();
					targetNodes = targetVertices.get(nextIri);
					for (int j = 0; j < targetNodes.size(); j++) {
						HierarchicalCNode node = targetNodes.get(j);
						if (!alignGraph.hasVertex(node)) {
							alignGraph.addVertex(node, times.get(i));
							alignGraph.setNodeLabel(node, ((HierarchicalCNode) node).getLabel());   
						}
						//if (node.getNodeDepth() <= targetVisibilityLevel){
							tempTargetNodes.add(node);
						//}
					}
				}
				
				//System.out.println("TIME *******************" + times.get(i));
				for (int v = 0; v < firstLevelVerticies.size(); v++) {
					
					getAllEdgesForNode(firstLevelVerticies.get(v), times.get(i));
				}
				
				//System.out.println(tempSourceNodes.size());
				//System.out.println(tempTargetNodes.size());

				Iterator<HierarchicalCNode> accMappsIterator = accumulatedMappsPerNodeMap.keySet().iterator();
				while (accMappsIterator.hasNext()) {
					HierarchicalCNode n = (HierarchicalCNode) accMappsIterator.next();
					if (accumulatedMappsPerNodeMap.get(n).isEmpty()) {
						if (n.belongsToSourceOnto()) {
							tempSourceNodes.remove(n);
						} else {
							tempTargetNodes.remove(n);
						}
					}
				}
				
				System.out.println("[CSVGDataSet] calculate accumulated edge weights");
				//System.out.println(tempSourceNodes.size());
				//System.out.println(tempTargetNodes.size());
				
				for (int j = 0; j < tempSourceNodes.size(); j++) {
					HierarchicalCNode tempSourceNode = tempSourceNodes.get(j);
					Set<CEdge> sourceSet = accumulatedMappsPerNodeMap.get(tempSourceNode);
					for (int k = 0; k < tempTargetNodes.size(); k++) {
						
						HierarchicalCNode tempTargetNode = tempTargetNodes.get(k);
						
						Set<CEdge> targetSet = accumulatedMappsPerNodeMap.get(tempTargetNode);
						Set<CEdge> intersection = new HashSet<>();
						
						intersection.addAll(sourceSet);
						intersection.retainAll(targetSet);
						
						int accumulatedWeight = intersection.size();
						
						if (accumulatedWeight != 0) {
							maxAccumulatedWeight = Math.max(accumulatedWeight, maxAccumulatedWeight);
							minAccumulatedWeight = Math.min(accumulatedWeight, minAccumulatedWeight);
							CEdge existingEdge = alignGraph.getEdge(times.get(i), tempSourceNode, tempTargetNode);
							if (existingEdge != null) {
								existingEdge.setAccumulatedWeight(accumulatedWeight);
							} else {
								CEdge accumulatedEdge = new CEdge(tempSourceNode.getID() + "--" + tempTargetNode.getID());
								accumulatedEdge.setAccumulatedWeight(accumulatedWeight);
								accumulatedEdge.setWeight(0);
								alignGraph.addEdge(accumulatedEdge, tempSourceNode, tempTargetNode, times.get(i), false);
							}
							
							
							//System.out.println("[CSVGDataSet] accumulatedWeight: " + accumulatedWeight + " for: " + visibleSourceNode.getLabel() + " and " + visibleTargetNode.getLabel());

						}
					}
				}				
			}


		}catch (IOException ex){
			System.err.println("Error loading file " + alignmentsFile);
			ex.printStackTrace();
		} 
	}
	
//	private static int calculateAccumulatedWeight(HierarchicalCNode visibleSourceNode,
//			HierarchicalCNode visibleTargetNode, CTime time) {
//
//		Set<CEdge> sourceSet = new HashSet<CEdge>();
//		Set<CEdge> targetSet = new HashSet<CEdge>();
//
//		getAllEdgesForNode(visibleSourceNode, sourceSet, time);
//		getAllEdgesForNode(visibleTargetNode, targetSet, time);
//		
//		sourceSet.retainAll(targetSet);
//		
//		return sourceSet.size();
//	}

//	private static void getAllEdgesForNode(HierarchicalCNode visibleNode, Set<CEdge> set, CTime time) {
//
//		if(visibleNode.getMappingEdges(time) != null)
//			set.addAll(visibleNode.getMappingEdges(time));
//		
//		ArrayList<HierarchicalCNode> children = visibleNode.getChildren();
//		for (int i = 0; i < children.size(); i++) {
//			getAllEdgesForNode(children.get(i), set, time);
//		}
//		
//	}
	
	private static Set<CEdge> getAllEdgesForNode(HierarchicalCNode node, CTime time) {

		Set<CEdge> edges = new HashSet();
		
		ArrayList<HierarchicalCNode> children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			edges.addAll(getAllEdgesForNode(children.get(i), time));
		}
		
		//if (node.getID().contains("Event") || node.getID().contains("Event"))
		//	System.out.println("Stop!!!");
		
		if(node.getMappingEdges(time) != null)
			edges.addAll(node.getMappingEdges(time));
		
		accumulatedMappsPerNodeMap.put(node, edges);
		
		//System.out.println("[CSVGDataSet] node: " + node.getID() + "     set size: " + edges.size());		
		
		return edges;
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
	    	    		source = new CNode(line[1]);
	    	    		vertices.put(line[1], source);
	    	    		tGraph.addVertex(source, t);
	    	    		tGraph.setNodeLabel(source, line[1]);
	    	    	}
	    	    	if(vertices.containsKey(line[2]))
	    	    		target = vertices.get(line[2]);
	    	    	else{
	    	    		target = new CNode(line[2]);
	    	    		vertices.put(line[2], target);
	    	    		tGraph.addVertex(target, t);
	    	    		tGraph.setNodeLabel(target, line[2]);
	    	    	}
	    	    	
	    	    	weight = Float.parseFloat(line[3]);
	    	    	  
					edge = new CEdge(source.getID() + "--" + target.getID());
			    	edge.setWeight(weight);
			    	tGraph.addEdge(edge, source, target, t, true);
			    	edge = new CEdge(target.getID() + "--" + source.getID());
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
	
	/** Exports the given graph into the passed directory. For each time slices
	 * a file of the format <code>time_</code><em>number<em> is created.*/
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
	
	/** Exports the given graph into the passed directory. For each time slices
	 * a file of the format <code>time_</code><em>number<em> is created.*/
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
	
}
