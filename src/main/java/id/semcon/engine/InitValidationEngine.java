package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class InitValidationEngine {

    private final Resource permissivePolicy;
    private final Resource containerPolicy;

    private final String initConfigNG;
    private final String usagePoliciesNG;
    private final String dataModelNG;
    private final String dataConstraintNG;
    private final String dataMappingNG;

    private final Model permissivePolicyModel;
    //    private final Model baseConstraintModel;

    public InitValidationEngine() {
        permissivePolicy = ResourceFactory.createResource(SwissKnife.semconNS + "PermissivePolicy");
        containerPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "ContainerPolicy");

        initConfigNG = SwissKnife.semconNS + "InitialConfiguration";
        usagePoliciesNG = SwissKnife.semconNS + "UsagePolicy";
        dataModelNG = SwissKnife.semconNS + "DataModel";
        dataConstraintNG = SwissKnife.semconNS + "DataConstraint";
        dataMappingNG = SwissKnife.semconNS + "DataMapping";

        permissivePolicyModel =
                SwissKnife.initAndLoadModelFromResource("usage-policy/permissive-policy.ttl", Lang.TURTLE);
        //        baseConstraintModel = SwissKnife.initAndLoadModelFromResource("init/base-constraints.ttl", Lang.TURTLE);

    }

    public String initConfigValidation(String initConfig, String baseConstraints) {
        String result = "";

        InputStream initIS = IOUtils.toInputStream(initConfig, Charset.forName("UTF-8"));
        Dataset initDataset = DatasetFactory.create();
        try {
            RDFDataMgr.read(initDataset, initIS, Lang.TRIG);
        } catch (Exception e) {
            result = "Error reading initFile Config. Is it valid TRIG file?";
        }
        InputStream baseConstraintsIS = IOUtils.toInputStream(baseConstraints, Charset.forName("UTF-8"));
        Dataset baseConstraintsDS = DatasetFactory.create();
        try {
            RDFDataMgr.read(baseConstraintsDS, baseConstraintsIS, Lang.TRIG);
        } catch (Exception e) {
            result = "Error reading baseConstraints. Is it valid TRIG file?";
        }

        if (result.isEmpty()) {
            if (!initDataset.containsNamedModel(initConfigNG) || initDataset.getNamedModel(initConfigNG).isEmpty()) {
                result = "Init Config Graph is not exists or empty";
            } else if (!initDataset.containsNamedModel(usagePoliciesNG) || initDataset.getNamedModel(usagePoliciesNG)
                    .isEmpty()) {
                result = "Usage Policy is not exists or empty";
            } else if (!checkValidPolicy(initDataset.getNamedModel(usagePoliciesNG))) {
                result = "Usage Policy is not valid";
            } else {
                result = checkInitConfig(initDataset, baseConstraintsDS);
            }
        }

        return result;
    }

    private String checkInitConfig(Dataset initDataset, Dataset baseConstraintsDS) {
        String result = "";
        Resource validationResult = ValidationUtil
                .validateModel(initDataset.getNamedModel(initConfigNG), baseConstraintsDS.getDefaultModel(), false);
        Model resultModel = validationResult.getModel();
        if (resultModel.listStatements(null, SH.resultSeverity, SH.Violation).toList().size() >= 1) {
            StringWriter resultWriter = new StringWriter();
            RDFDataMgr.write(resultWriter, resultModel, Lang.TURTLE);
            result = resultWriter.toString();
        }

        return result;
    }

    private boolean checkValidPolicy(Model namedModel) {
        Model model = ModelFactory.createDefaultModel();
        model.add(namedModel).add(permissivePolicyModel);
        return SwissKnife.policyCheck(model, containerPolicy, permissivePolicy);
    }

}
