package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.InputStream;
import java.io.StringReader;

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

    public String policyCheckDebug(String policyString) {
        String result = "";
        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader(policyString);
        RDFDataMgr.read(model, reader, null, Lang.TURTLE);

        model.setNsPrefix("owl","http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("spl","http://www.specialprivacy.eu/langs/usage-policy#");
        model.setNsPrefix("svd","http://www.specialprivacy.eu/vocabs/data#");
        model.setNsPrefix("svr","http://www.specialprivacy.eu/vocabs/recipients#");
        model.setNsPrefix("svpu","http://www.specialprivacy.eu/vocabs/purposes#");
        model.setNsPrefix("svpr","http://www.specialprivacy.eu/vocabs/processing#");
        model.setNsPrefix("svl","http://www.specialprivacy.eu/vocabs/locations#");
        model.setNsPrefix("svdu","http://www.specialprivacy.eu/vocabs/duration#");
        model.setNsPrefix("svd","http://www.specialprivacy.eu/vocabs/data#");
        model.setNsPrefix("scp","http://w3id.org/semcon/ns/policy#");

        model.write(System.out, "TTL");

        if (!SwissKnife.policyCheck(policyString, dataSubjectPolicy, dataControllerPolicy)) {
            result = "DataControllerPolicy does not conform to DataSubjectPolicy!";
        }
        return result;
    }
}
