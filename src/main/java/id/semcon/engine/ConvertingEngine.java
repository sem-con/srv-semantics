package id.semcon.engine;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class ConvertingEngine {

    private static final String ns_spl = "http://www.specialprivacy.eu/langs/usage-policy#";

    public static String transform(String trigInput) throws IOException {
        return transform(IOUtils.toInputStream(trigInput, "UTF-8"));
    }

    public static String transform(InputStream trigInput) {
        Model model = ModelFactory.createDefaultModel();
        model.read(trigInput, null, "TTL");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("data", getPolicyArray(model, "hasData"));
        jsonObject.put("recipient", getPolicyArray(model, "hasRecipient"));
        jsonObject.put("purpose", getPolicyArray(model, "hasPurpose"));
        jsonObject.put("processing", getPolicyArray(model, "hasProcessing"));
        jsonObject.put("location", getPolicyArray(model, "hasLocation"));
        jsonObject.put("duration", getPolicyArray(model, "hasDuration"));

        return jsonObject.toString(2);
    }

    private static JSONArray getPolicyArray(Model inputModel, String attribute) {
        JSONArray policyArray = new JSONArray();

        Property attrSubj = inputModel.createProperty(ns_spl + attribute);
        ResIterator iterator = inputModel.listSubjectsWithProperty(OWL.onProperty, attrSubj);

        if (iterator.hasNext()) {
            Resource restriction = iterator.next();
            Resource attrValues = restriction.getProperty(OWL.someValuesFrom).getObject().asResource();
            if (attrValues.isAnon()) {
                Statement stmt = attrValues.getProperty(OWL.unionOf);
                RDFList listObjects = stmt.getList();
                listObjects.asJavaList().forEach(object -> {
                    extractJSON(policyArray, object.asResource());
                });

            } else if (attrValues.isResource()) {
                extractJSON(policyArray, attrValues);
            }
        }

        return policyArray;
    }

    private static void extractJSON(JSONArray policyArray, Resource attrValues) {
        JSONObject attrObject = new JSONObject();
        String ns = attrValues.getNameSpace();
        String value = attrValues.getLocalName();
        attrObject.put("ns", ns);
        attrObject.put("value", value);
        policyArray.put(attrObject);
    }
}
