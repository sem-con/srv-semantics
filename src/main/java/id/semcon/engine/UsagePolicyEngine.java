package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import java.io.IOException;
import java.io.InputStream;

public class UsagePolicyEngine {

    private final Resource dataSubjectPolicy;
    private final Resource dataControllerPolicy;

    public UsagePolicyEngine() {
        dataSubjectPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataSubjectPolicy");
        dataControllerPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataControllerPolicy");
    }

    //    public String policyCheck(String policyString) throws IOException {
    //        String result = "";
    //        InputStream policy = IOUtils.toInputStream(policyString, "UTF-8");
    //        Model model = ModelFactory.createDefaultModel();
    //        RDFDataMgr.read(model, policy, Lang.TURTLE);
    //        if (model.isEmpty()) {
    //            result = "Usage policy is empty!";
    //        } else if (!SwissKnife.policyCheck(model, dataControllerPolicy, dataSubjectPolicy)) {
    //            result = "DataControllerPolicy does not conform to DataSubjectPolicy!";
    //        }
    //        model.close();
    //
    //        return result;
    //    }

    public String policyCheck(String policyString)
            throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        InputStream is = IOUtils.toInputStream(policyString, "UTF-8");
        InputStream specialIntegrated =
                UsagePolicyEngine.class.getClassLoader().getResourceAsStream("usage-policy/special-integrated.ttl");
        String result = "";

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(is);
        ontology.addAxioms(manager.loadOntologyFromOntologyDocument(specialIntegrated).axioms());
        OWLClass dataControllerCls = df.getOWLClass(IRI.create(dataControllerPolicy.getURI()));
        OWLClass dataSubjectCls = df.getOWLClass(IRI.create(dataSubjectPolicy.getURI()));

        OWLReasonerFactory rf = new Reasoner.ReasonerFactory();
        OWLReasoner r = rf.createReasoner(ontology);
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        NodeSet<OWLClass> subClasses = r.getSubClasses(dataSubjectCls, false);
        Node<OWLClass> equivalentClass = r.getEquivalentClasses(dataSubjectCls);

        if (!subClasses.containsEntity(dataControllerCls) && !equivalentClass.contains(dataControllerCls)) {
            result = "DataControllerPolicy does not conform to DataSubjectPolicy!";
        }

        return result;
    }
}
