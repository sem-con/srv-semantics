package id.semcon.engine;

import id.semcon.helper.SwissKnife;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.io.InputStream;

public class UsagePolicyEngine {

    private final Resource dataSubjectPolicy;
    private final Resource dataControllerPolicy;

    public UsagePolicyEngine() {
        dataSubjectPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataSubjectPolicy");
        dataControllerPolicy = ResourceFactory.createResource(SwissKnife.semconNS + "DataControllerPolicy");
    }

    public String policyCheck(String policyString) throws IOException {
        String result = "";
        InputStream policy = IOUtils.toInputStream(policyString, "UTF-8");
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, policy, Lang.TURTLE);
        if (model.isEmpty()) {
            result = "Usage policy is empty!";
        } else if (!SwissKnife.policyCheck(model, dataControllerPolicy, dataSubjectPolicy)) {
            result = "DataControllerPolicy in not conform to the DataSubjectPolicy!";
        }
        model.close();

        return result;
    }
}
