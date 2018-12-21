package id.semcon.helper;

import openllet.jena.PelletReasonerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;

import java.io.*;
import java.nio.charset.Charset;

public class SwissKnife {

    public static final String semconNS = "http://semantics.id/ns/semcon#";
    public static final Model specialModel =
            SwissKnife.initAndLoadModelFromResource("usage-policy/special-integrated.ttl", Lang.TURTLE);

    public static File getFileFromResource(String fileName) {
        return new File(SwissKnife.class.getClassLoader().getResource(fileName).getFile());
    }

    public static Model initAndLoadModelFromInput(String dataModelFile, Lang lang) throws FileNotFoundException {
        InputStream dataModelIS = new FileInputStream(dataModelFile);
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
        Model model = ModelFactory.createDefaultModel();
        model.add(policyModel);
        model.add(specialModel);

        Reasoner reasoner = PelletReasonerFactory.theInstance().create();
        InfModel eModel = ModelFactory.createInfModel(reasoner, model);
        Boolean isConform = eModel.contains(dataSubjectPolicy, RDFS.subClassOf, dataControllerPolicy);

        eModel.close();
        model.close();

        return isConform;

    }
}
