package cubix.data;

import java.io.File;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import cubix.helper.Log;

/**
 * 
 * @author VI
 *
 */
public class OntologyUtils {
	
	private OWLOntologyManager owlManager;
	private OWLReasonerFactory owlReasonerFactory;
	private OWLDataFactory owlDataFactory;
	
	public OntologyUtils(){
		owlManager = OWLManager.createOWLOntologyManager();
		owlReasonerFactory = new ReasonerFactory();
		owlDataFactory = owlManager.getOWLDataFactory();
	}
	
	public OWLOntology getOntology(File file){
        OWLOntology ontology = null;
		try {
			ontology = owlManager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			Log.err(this, "Ontology can't be loaded from file: " + file.getName());
			e.printStackTrace();
		}
        return ontology;
	}
	
	public OWLReasoner getReasoner(OWLOntology ontology){
        OWLReasoner reasoner = owlReasonerFactory.createReasoner(ontology);
		return reasoner;
	}
	
	public void disposeReasoner(OWLOntology ontology){
		getReasoner(ontology).dispose();
	}

	public OWLDataFactory getOwlDataFactory() {
		return owlDataFactory;
	}

}
