/**
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.math.BigDecimal;

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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Test cases for the RPG ontology
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class RPGTest extends JenaTest {
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
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("rpg-test.rdf").toExternalForm());
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
  public void testCharacter1() {
    Resource c1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/character1");
    Resource p1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/player1");

    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.Character));
    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.GameEntity));
    Assert.assertTrue(c1.hasProperty(RPG.isPortrayedBy, p1));
    Assert.assertTrue(p1.hasProperty(RPG.portrays, c1));
    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.PlayerCharacter));
  }

  @Test
  public void testCharacter2() {
    Resource c1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/character2");
    Resource p1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/gm1");

    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.Character));
    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.GameEntity));
    Assert.assertTrue(c1.hasProperty(RPG.isPortrayedBy, p1));
    Assert.assertTrue(p1.hasProperty(RPG.portrays, c1));
    Assert.assertTrue(c1.hasProperty(RDF.type, RPG.NonPlayerCharacter));
  }

  @Test
  public void testSkill1() {
    Resource c1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/character2");
    Resource s1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/pl10");
    Resource p1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/picklocks");
    StmtIterator si;
    
    si = c1.listProperties(RPG.hasFeature);
    Assert.assertTrue(si.hasNext());
    Assert.assertEquals(s1, si.next().getResource());
    Assert.assertTrue(s1.hasProperty(RPG.inSkillType, p1));
    Assert.assertTrue(s1.hasLiteral(RDF.value, 10));
    Assert.assertFalse(si.hasNext());
  }

  @Test
  public void testWeapon1() {
    Resource w1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/weapon1");
    Resource cr = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/credit");
    Resource p1, m1;

    Assert.assertTrue(w1.hasProperty(RDF.type, RPG.Weapon));
    Assert.assertTrue(w1.hasProperty(RDF.type, RPG.Item));
    p1 = w1.getPropertyResourceValue(RPG.hasPrice);
    Assert.assertNotNull(p1);
    Assert.assertTrue(p1.hasProperty(RPG.unit, cr));
    Assert.assertTrue(p1.hasLiteral(RDF.value, 100));
    m1 = w1.getPropertyResourceValue(RPG.hasMass);
    Assert.assertNotNull(m1);
    Assert.assertEquals(BigDecimal.valueOf(1.5), m1.getProperty(RDF.value).getLiteral().getValue());
  }

  @Test
  public void testExchangeRate1() {
    Resource ex1 = this.inference.createResource("http://data.travellerrpg.com/ontology/rpg/test1/mcrcr");

    Assert.assertTrue(ex1.hasProperty(RDF.type, RPG.ExchangeRate));
    Assert.assertTrue(ex1.hasProperty(RDF.type, RPG.Conversion));
    Assert.assertTrue(ex1.hasProperty(RDF.type, RPG.SettingState));
    Assert.assertEquals(true, ex1.getProperty(RPG.fixedConversion).getBoolean());
    Assert.assertEquals(1000000, ex1.getProperty(RPG.conversionRatio).getLiteral().getValue());
  }

}
