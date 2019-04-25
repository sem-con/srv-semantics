package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.InputStream;

public class UsagePolicyEngine {

    private final Resource dataSubjectPolicy;
    private final Resource dataControllerPolicy;

    public UsagePolicyEngine() {
        dataSubjectPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataSubjectPolicy");
        dataControllerPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataControllerPolicy");
    }

    public String policyCheck(String policyString) {
        String result = "";
        if (!SwissKnife.policyCheck(policyString, dataSubjectPolicy, dataControllerPolicy)) {
            result = "DataControllerPolicy does not conform to DataSubjectPolicy!";
        }
        return result;
    }
}
