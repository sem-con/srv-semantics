package id.semcon.helper;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class YamlToTurtle {

    public static String PREFIXES = "prefixes";

    public static String NS_SC = "http://w3id.org/semcon/ns/ontology#";
    public static String NS_SPL = "http://www.specialprivacy.eu/langs/usage-policy#";

    public static Resource DATA_SUBJECT_POLICY = ResourceFactory.createResource(NS_SC + "DataSubjectPolicy");
    public static Resource DATA_CONTROLLER_POLICY = ResourceFactory.createResource(NS_SC + "DataControllerPolicy");

    public static Resource SPL_HAS_DATA = ResourceFactory.createResource(NS_SPL + "hasData");
    public static Resource SPL_HAS_PROCESSING = ResourceFactory.createResource(NS_SPL + "hasProcessing");
    public static Resource SPL_HAS_PURPOSE = ResourceFactory.createResource(NS_SPL + "hasPurpose");
    public static Resource SPL_HAS_RECIPIENT = ResourceFactory.createResource(NS_SPL + "hasRecipient");
    public static Resource SPL_HAS_STORAGE = ResourceFactory.createResource(NS_SPL + "hasStorage");
    public static Resource SPL_HAS_LOCATION = ResourceFactory.createResource(NS_SPL + "hasLocation");
    public static Resource SPL_HAS_DURATION = ResourceFactory.createResource(NS_SPL + "hasDuration");

    public static Model getModelFromYamlFile(String inputFile) throws IOException {

        InputStream policyIS = YamlToTurtle.class.getClassLoader().getResourceAsStream(inputFile);
        YamlMapping yamlMapping = Yaml.createYamlInput(policyIS).readYamlMapping();
        Model model = getInitializedModel(yamlMapping);

        model.add(getPolicyModel(yamlMapping, DATA_SUBJECT_POLICY));
        model.add(getPolicyModel(yamlMapping, DATA_CONTROLLER_POLICY));

        StringWriter writer = new StringWriter();
        model.write(writer, "TURTLE");

        return model;
    }

    public static String getModelFromYamlString(String inputString) throws IOException {

        YamlMapping yamlMapping = Yaml.createYamlInput(inputString).readYamlMapping();
        Model model = getInitializedModel(yamlMapping);

        model.add(getPolicyModel(yamlMapping, DATA_SUBJECT_POLICY));
        model.add(getPolicyModel(yamlMapping, DATA_CONTROLLER_POLICY));

        StringWriter writer = new StringWriter();
        model.write(writer, "TURTLE");

        return writer.toString();
    }

    public static Model getPolicyModel(YamlMapping yamlMapping, Resource policyResource) {

        Model model = ModelFactory.createDefaultModel();

        YamlMapping prefixes = yamlMapping.yamlMapping(PREFIXES);
        prefixes.keys().stream().forEach(prefix -> {
            String prex = prefix.asScalar().value();
            String value = prefixes.string(prex);
            model.setNsPrefix(prex, value);
        });

        YamlMapping userPolicy = yamlMapping.yamlMapping(policyResource.getLocalName());
        YamlMapping userStorage = userPolicy.yamlMapping(SPL_HAS_STORAGE.getLocalName());

        Resource equivalentCls = model.createResource();

        Resource bnodeData = getBnode(model, userPolicy, SPL_HAS_DATA);
        Resource bnodeProcessing = getBnode(model, userPolicy, SPL_HAS_PROCESSING);
        Resource bnodePurpose = getBnode(model, userPolicy, SPL_HAS_PURPOSE);
        Resource bnodeRecipient = getBnode(model, userPolicy, SPL_HAS_RECIPIENT);
        Resource bnodeLocation = getBnode(model, userStorage, SPL_HAS_LOCATION);
        Resource bnodeDuration = getBnode(model, userStorage, SPL_HAS_DURATION);

        RDFList storageList = model.createList().with(bnodeLocation);
        storageList.add(bnodeDuration);
        Resource storageIntersectionOf = model.createResource();
        storageIntersectionOf.addProperty(OWL.intersectionOf, storageList);

        Resource bnodeStorage = model.createResource();
        bnodeStorage.addProperty(RDF.type, OWL.Restriction);
        bnodeStorage.addProperty(OWL.onProperty, SPL_HAS_STORAGE);
        bnodeStorage.addProperty(OWL.someValuesFrom, storageIntersectionOf);

        RDFList policyList = model.createList().with(bnodeData);
        policyList.add(bnodeProcessing);
        policyList.add(bnodePurpose);
        policyList.add(bnodeRecipient);
        policyList.add(bnodeStorage);

        model.add(policyResource, RDF.type, OWL.Class);
        model.add(policyResource, OWL.equivalentClass, equivalentCls);
        equivalentCls.addProperty(OWL.intersectionOf, policyList);

        return model;
    }

    public static Model getInitializedModel(YamlMapping yamlMapping) {

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("spl", NS_SPL);
        model.setNsPrefix("sc", NS_SC);

        YamlMapping prefixes = yamlMapping.yamlMapping(PREFIXES);
        prefixes.keys().stream().forEach(prefix -> {
            String prex = prefix.asScalar().value();
            String value = prefixes.string(prex);
            model.setNsPrefix(prex, value);
        });

        return model;
    }

    public static void getPolicyValues(Resource bnode, String inputString) {
        Model model = bnode.getModel();

        if (inputString.contains(";")) {
            RDFList unionOfList = model.createList();

            String[] dataArray = inputString.split("\\s*;\\s*");
            for (String data : dataArray) {
                Resource dataResource = getResource(bnode, data);
                if (unionOfList.isEmpty()) {
                    unionOfList = unionOfList.with(dataResource);
                } else {
                    unionOfList.add(dataResource);
                }
            }

            Resource unionOf = model.createResource();
            unionOf.addProperty(OWL.unionOf, unionOfList);
            bnode.addProperty(OWL.someValuesFrom, unionOf);
        } else {

            bnode.addProperty(OWL.someValuesFrom, getResource(bnode, inputString));
        }
    }

    public static Resource getResource(Resource bnode, String inputString) {

        String[] res = inputString.split(":");
        return bnode.getModel().createResource(bnode.getModel().getNsPrefixURI(res[0]) + res[1]);
    }

    public static Resource getBnode(Model model, YamlMapping userPolicy, Resource property) {
        Resource bnodeData = model.createResource();
        bnodeData.addProperty(RDF.type, OWL.Restriction);
        bnodeData.addProperty(OWL.onProperty, property);
        getPolicyValues(bnodeData, userPolicy.string(property.getLocalName()));

        return bnodeData;
    }
}
