package id.semcon.helper;

import id.semcon.engine.UsagePolicyEngine;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

public class SwissKnife {

    public static final String semconNS = "http://w3id.org/semcon/ns/ontology#";
    public static final Model specialModel =
            SwissKnife.initAndLoadModelFromResource("usage-policy/special-integrated.ttl", Lang.TURTLE);
    private static final Logger log = LoggerFactory.getLogger(SwissKnife.class);

    public static File getFileFromResource(String fileName) {
        return new File(SwissKnife.class.getClassLoader().getResource(fileName).getFile());
    }

    public static Model initAndLoadModelFromInput(String dataModelFile, Lang lang) throws FileNotFoundException {
        InputStream dataModelIS = new FileInputStream(dataModelFile);
        Model dataModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(dataModel, dataModelIS, lang);
        return dataModel;
    }

    public static Model initAndLoadModelFromStringInput(String inputString, Lang lang) throws IOException {
        InputStream dataModelIS = IOUtils.toInputStream(inputString, "UTF-8");
        Model dataModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(dataModel, dataModelIS, lang);
        return dataModel;
    }

    public static Model initAndLoadModelFromResource(String dataModelFile, Lang lang) {
        InputStream dataModelIS = SwissKnife.class.getClassLoader().getResourceAsStream(dataModelFile);
        Model dataModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(dataModel, dataModelIS, lang);
        return dataModel;
    }

    public static ParameterizedSparqlString initAndLoadQueryFromResource(String queryFile) throws IOException {
        InputStream dataModelIS = SwissKnife.class.getClassLoader().getResourceAsStream(queryFile);
        String string = IOUtils.toString(dataModelIS, Charset.forName("UTF-8"));
        ParameterizedSparqlString query = new ParameterizedSparqlString(string);
        return query;
    }

    public static String initAndLoadStringFromResource(String file) throws IOException {
        InputStream fileString = SwissKnife.class.getClassLoader().getResourceAsStream(file);
        return IOUtils.toString(fileString, Charset.forName("UTF-8"));
    }

    public static boolean policyCheck(Model policyModel, Resource dataSubjectPolicy, Resource dataControllerPolicy) {
        StringWriter writer = new StringWriter();
        policyModel.write(writer);
        String policyString = writer.toString();
        return policyCheck(policyString, dataSubjectPolicy, dataControllerPolicy);

    }

    /**
     * Function to check whether dataControllerPolicy conforms to dataSubjectPolicy
     *
     * @param policyString         a String containing dataSubjectPolicy and dataControllerPolicy
     * @param dataSubjectPolicy    Resource/URI of the dataSubjectPolicy
     * @param dataControllerPolicy Resource/URI of the dataControllerPolicy
     * @return true if dataControllerPolicy conform to dataSubjectPolicy
     */
    public static boolean policyCheck(String policyString, Resource dataSubjectPolicy,
            Resource dataControllerPolicy) {
        boolean result = true;

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        try {

            InputStream is = IOUtils.toInputStream(policyString, "UTF-8");
            InputStream specialIntegrated = UsagePolicyEngine.class.getClassLoader()
                    .getResourceAsStream("usage-policy/special-integrated.ttl");

            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(is);

            ontology.addAxioms(manager.loadOntologyFromOntologyDocument(specialIntegrated).axioms());
            OWLClass dataControllerCls = df.getOWLClass(IRI.create(dataControllerPolicy.getURI()));
            OWLClass dataSubjectCls = df.getOWLClass(IRI.create(dataSubjectPolicy.getURI()));

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);
            OWLAxiom axiom = df.getOWLSubClassOfAxiom(dataControllerCls, dataSubjectCls);

            return r.isEntailed(axiom);

        } catch (Exception e) {
            result = false;
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
