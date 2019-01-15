package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.IOException;
import java.io.InputStream;

public class DataValidationEngine {

    private Model dataGraph;
    private Model shapesGraph;

    public DataValidationEngine() {
        dataGraph = null;
        shapesGraph = null;
    }

    public String dataCheck(String inputData, String inputConstraints) throws IOException {
        String result = "";

//        System.out.println(inputData);
//        System.out.println(inputConstraints);

        dataGraph = SwissKnife.initAndLoadModelFromStringInput(inputData, Lang.TURTLE);
        shapesGraph = SwissKnife.initAndLoadModelFromStringInput(inputConstraints, Lang.TURTLE);

        if (dataGraph.isEmpty()) {
            result = "input data is empty!";
        } else if (shapesGraph.isEmpty()) {
            result = "data constraint is empty!";
        } else {
            Resource validationResult = ValidationUtil.validateModel(dataGraph, shapesGraph, false);
            if (validationResult.getModel().contains(null, SH.conforms, dataGraph.createTypedLiteral(false))) {
                result = "data is not conformance to the SHACL constraints!";
            }
        }

        // cleaning
        dataGraph.close();
        shapesGraph.close();

        return result;
    }
}
