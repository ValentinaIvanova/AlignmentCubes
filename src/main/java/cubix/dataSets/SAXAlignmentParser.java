package cubix.dataSets;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class SAXAlignmentParser extends DefaultHandler {
	
	private static final String EQU_RELATION = "EQU";
	private static final String OTHER_RELATION = "OTHER";

	private ArrayList<String[]> lines;
	private String onto1;
	private String onto2;
	private String fileName;
	private boolean showOnlyEqu = true;
	
	private String entity1;
	private String entity2;
	private String measure;
	private String relation;
	
	private boolean relationElementExists = false;
	private boolean inElement = false;
	private StringBuilder stringBuilder;
	
	
	
	public SAXAlignmentParser(String name, boolean showEquOnly) {
		this.fileName = name;
		this.showOnlyEqu = showEquOnly;
	}

	public void startDocument( ) throws SAXException {

		lines = new ArrayList<String[]>();
       
    }

	public void startElement(String uri, String localName,String qName, 
	            Attributes attributes) throws SAXException {

		//System.out.println("Start Element :" + qName);

		
		if(qName.equals("entity1")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if(attributes.getLocalName(i).equals("resource")){
					entity1 = attributes.getValue(i);
					break;
				}
			}
		} else if(qName.equals("entity2")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if(attributes.getLocalName(i).equals("resource")){
					entity2 = attributes.getValue(i);
					break;
				}
			}
		} else if(qName.equals("measure")) {
			inElement = true;
			stringBuilder = new StringBuilder();
		} else if(qName.equals("relation")) {
			relationElementExists = true;
			inElement = true;
			stringBuilder = new StringBuilder();
		}
        
	}

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        
        //System.out.println("End Element :" + qName);
        
        if(qName.equals("measure")) {
        	measure = stringBuilder.toString().trim();
        	//System.out.println("measure : " + measure);
			stringBuilder = null;
			inElement = false;
		}
        
        if(qName.equals("relation")) {
        	String content = stringBuilder.toString().trim();
        	if ((content.isEmpty()) || content.equals("=") || content.contains("EquivRelation") || content.contains("BasicRelation"))
        		relation = EQU_RELATION;
        	else
        		relation = OTHER_RELATION;
        	
			stringBuilder = null;
			inElement = false;
		}        
        
        // add the current correspondence to all correspondences
        if(qName.equals("Cell")) {
        	
        	if (showOnlyEqu){
            	// a relationship is an equivalence relationship if:
        		// (1) there is no relation element in the Cell,
        		// (2) the value for the relation element is =
        		// (3) the value for the relation element contains BasicRelation
        		// (4) the value for the relation element contains EquivRelation
        		if (!relationElementExists || relation.equals(EQU_RELATION)) {
                	String[] line = new String[] {fileName, entity1, entity2, measure};
                	//new String(fileName + "," + entity1 + "," + entity2 + "," + measure + ",");
                	lines.add(line);
                	//System.out.println("line : " + line);
                	relationElementExists = false;
        		}
        	} else {
        		String[] line = new String[] {fileName, entity1, entity2, measure};
            	lines.add(line);
            	//System.out.println("line : " + line);
        	}

		}
        
//        if(qName.equals("relation")) {
//        	measure = stringBuilder.toString();
//        	System.out.println("measure : " + measure);
//			stringBuilder = null;
//		}
        
    }
    
    public void characters(char ch[], int start, int length) throws SAXException {

    	if (inElement){
        	stringBuilder.append(new String(ch, start, length));
    	}
		
    }
    
    
    public void endDocument( ) throws SAXException {
        //System.out.println("End of docuement ");
    }
    
    
    public ArrayList<String[]> getContent(){
    	return lines;
    }
    
        
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {}
    
    public void processingInstruction(String target, String data) 	   
    throws SAXException {}
    public void setDocumentLocator(Locator locator) {}
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {}
    public void endPrefixMapping(String prefix) throws SAXException {}
    public void skippedEntity(String name) throws SAXException {}
    
}

