/**
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

/**
 * Test cases for the T5 ontology
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class T5Test extends JenaTest {
  private Model ontology;
  private Model data;
  private InfModel inference;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    Reasoner reasoner;

    OntDocumentManager.getInstance().setProcessImports(false);
    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("foaf.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("rpg.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("t5.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("examples.ttl").toExternalForm());
    //reasoner.setDerivationLogging(true);
    //reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
    //reasoner.setParameter(ReasonerVocabulary.PROPderivationLogging, Boolean.TRUE);
    //((OWLFBRuleReasoner) reasoner).setTraceOn(true);
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.data);
  }

  @Test
  public void testOntology1() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, this.ontology);

    this.validityTest(model.validate());
  }

  @Test
  public void testTestData1() {
    this.validityTest(this.inference.validate());
  }

  @Test
  public void testHasC5() {
    Property p1 = this.inference.createProperty("http://data.travellerrpg.com/ontology/t5#hasC5");
    Property p2 = this.inference.createProperty("http://data.travellerrpg.com/ontology/t5#hasEducation");
    Resource c1 = this.inference.createResource("http://data.travellerrpg.com/examples/JherikAmdalu");
    Resource c5, edu;

    Assert.assertTrue(c1.hasProperty(p1));
    c5 = c1.getPropertyResourceValue(p1);
    Assert.assertTrue(c1.hasProperty(p2));
    edu = c1.getPropertyResourceValue(p2);
    Assert.assertEquals(c5, edu);
   }
}
