package id.semcon.test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileOutputStream;
import java.io.IOException;

import static id.semcon.helper.YamlToTurtle.getModelFromYamlFile;

public class TestYamlService {

    /**
     * for testing
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        Model model = getModelFromYamlFile("yaml/test-policy.yml");
        RDFDataMgr.write(new FileOutputStream("output.ttl"), model, Lang.TURTLE);
    }
}
