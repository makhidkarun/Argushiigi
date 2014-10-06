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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Test cases for the Argushiigi ontology
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class ArgushiigiTest extends JenaTest {
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
    this.ontology.read(this.getClass().getResource("argushiigi.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("argushiigi-test.rdf").toExternalForm());
    //reasoner.setDerivationLogging(true);
    //reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
    //reasoner.setParameter(ReasonerVocabulary.PROPderivationLogging, Boolean.TRUE);
    //((OWLFBRuleReasoner) reasoner).setTraceOn(true);
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.data);
  }

  @Test
  public void testOntology1() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RULES_INF, this.ontology);

    this.validityTest(model.validate());
  }

  @Test
  public void testTestData1() {
    this.validityTest(this.inference.validate());
  }

  @Test
  public void testClass1() {
    Resource cl1 = this.inference.createResource("http://data.travellerrpg.com/ontology/argushiigi/test1/C4");
    Resource c1 = this.inference.createResource("http://data.travellerrpg.com/ontology/argushiigi/test1/c4-1");

    Assert.assertTrue(c1.hasProperty(RDF.type, cl1));
   }
}
