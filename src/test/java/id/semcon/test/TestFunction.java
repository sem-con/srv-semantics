package id.semcon.test;

import id.semcon.engine.ConvertingEngine;
import id.semcon.helper.SwissKnife;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestFunction {

    @Test
    public void testInvalidDataValidation() {
        Model data = SwissKnife.initAndLoadModelFromResource("data/zamg-data-invalid.ttl", Lang.TURTLE);
        Model constraints = SwissKnife.initAndLoadModelFromResource("data/zamg-constraints.ttl", Lang.TURTLE);

        Resource resource = ValidationUtil.validateModel(data, constraints, true);
        Assert.assertTrue(resource.getModel().contains(null, SH.conforms, constraints.createTypedLiteral(false)));

    }

    @Test
    public void testValidDataValidation() {
        Model data = SwissKnife.initAndLoadModelFromResource("data/zamg-data.ttl", Lang.TURTLE);
        Model constraints = SwissKnife.initAndLoadModelFromResource("data/zamg-constraints.ttl", Lang.TURTLE);

        Resource resource = ValidationUtil.validateModel(data, constraints, true);
        RDFDataMgr.write(System.out, resource.getModel(), Lang.TURTLE);
        Assert.assertTrue(resource.getModel().contains(null, SH.conforms, constraints.createTypedLiteral(true)));

    }

    @Test
    public void testConvertingEngine() throws IOException {
        InputStream is = new FileInputStream("./src/test/resources/prettify/policy-1.ttl");
        InputStream controlIS = new FileInputStream("./src/test/resources/prettify/output.json");
        String expectedOutput = IOUtils.toString(controlIS, "UTF-8");
        String realOutput = ConvertingEngine.transform(is);
        is.close();
        controlIS.close();

        Assert.assertEquals(expectedOutput, realOutput);
    }
}
