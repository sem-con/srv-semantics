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

public class BaseValidationEngine {

    private final Resource permissivePolicy;
    private final Resource containerPolicy;

    private final String baseConfigNG;
    private final String usagePoliciesNG;
    private final String dataModelNG;
    private final String dataConstraintNG;
    private final String dataMappingNG;

    private final Model permissivePolicyModel;
    //    private final Model baseConstraintModel;

    public BaseValidationEngine() {
        permissivePolicy = ResourceFactory.createResource(SwissKnife.semconNS + "PermissivePolicy");
        containerPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "ContainerPolicy");

        baseConfigNG = SwissKnife.semconNS + "BaseConfiguration";
        usagePoliciesNG = SwissKnife.semconNS + "UsagePolicy";
        dataModelNG = SwissKnife.semconNS + "DataModel";
        dataConstraintNG = SwissKnife.semconNS + "DataConstraint";
        dataMappingNG = SwissKnife.semconNS + "DataMapping";

        permissivePolicyModel =
                SwissKnife.initAndLoadModelFromResource("usage-policy/permissive-policy.ttl", Lang.TURTLE);

    }

    public String baseConfigValidation(String baseConfig, String imageConstraints) {
        String result = "";

        InputStream initIS = IOUtils.toInputStream(baseConfig, Charset.forName("UTF-8"));
        Dataset baseDataset = DatasetFactory.create();
        try {
            RDFDataMgr.read(baseDataset, initIS, Lang.TRIG);
        } catch (Exception e) {
            result = "Error reading initFile Config. Is it valid TRIG file?";
        }
        InputStream imageConstraintsIS = IOUtils.toInputStream(imageConstraints, Charset.forName("UTF-8"));
        Dataset imageConstraintsDS = DatasetFactory.create();
        try {
            RDFDataMgr.read(imageConstraintsDS, imageConstraintsIS, Lang.TRIG);
        } catch (Exception e) {
            result = "Error reading imageConstraints. Is it valid TRIG file?";
        }

        if (result.isEmpty()) {
            if (!baseDataset.containsNamedModel(baseConfigNG) || baseDataset.getNamedModel(baseConfigNG).isEmpty()) {
                result = "Init Config Graph does not exist or is empty";
            } else if (!baseDataset.containsNamedModel(usagePoliciesNG) || baseDataset.getNamedModel(usagePoliciesNG)
                    .isEmpty()) {
                result = "Usage Policy does not exist or is empty";
            } else if (!checkValidPolicy(baseDataset.getNamedModel(usagePoliciesNG))) {
                result = "Usage Policy is not valid";
            } else {
                result = checkBaseConfig(baseDataset, imageConstraintsDS);
            }
        }

        return result;
    }

    private String checkBaseConfig(Dataset baseDataset, Dataset imageConstraintsDS) {
        String result = "";
        Resource validationResult = ValidationUtil
                .validateModel(baseDataset.getNamedModel(baseConfigNG), imageConstraintsDS.getDefaultModel(), false);
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
