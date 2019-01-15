package id.semcon.test;

import id.semcon.helper.SwissKnife;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class TestFunction {

    @Test public void testInvalidDataValidation() {
        Model data = SwissKnife.initAndLoadModelFromResource("data/zamg-data-invalid.ttl", Lang.TURTLE);
        Model constraints = SwissKnife.initAndLoadModelFromResource("data/zamg-constraints.ttl", Lang.TURTLE);

        Resource resource = ValidationUtil.validateModel(data, constraints, true);
        //        RDFDataMgr.write(System.out, resource.getModel(), Lang.TURTLE);
        Assert.assertTrue(resource.getModel().contains(null, SH.conforms, constraints.createTypedLiteral(false)));

    }

    @Test public void testValidDataValidation() {
        Model data = SwissKnife.initAndLoadModelFromResource("data/zamg-data.ttl", Lang.TURTLE);
        Model constraints = SwissKnife.initAndLoadModelFromResource("data/zamg-constraints.ttl", Lang.TURTLE);

        Resource resource = ValidationUtil.validateModel(data, constraints, true);
        RDFDataMgr.write(System.out, resource.getModel(), Lang.TURTLE);
        Assert.assertTrue(resource.getModel().contains(null, SH.conforms, constraints.createTypedLiteral(true)));

    }
}
