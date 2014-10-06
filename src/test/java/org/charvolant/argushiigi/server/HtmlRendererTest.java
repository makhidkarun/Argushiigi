/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import static org.junit.Assert.*;

import java.util.Locale;

import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.charvolant.argushiigi.ontology.StatementCollector;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Reference;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

/**
 * Test cases for {@link HtmlRenderer}
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class HtmlRendererTest {
  private static final String PREFIX = "http://data.travellerrpg.com/ontology/argushiigi/test1/";
  
  private Model ontology;
  private Model data;
  private InfModel inference;
  private DisplaySorter sorter;
  private ServerApplication application;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    Reasoner reasoner;

    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("../ontology/skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/argushiigi.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("../ontology/argushiigi-test.rdf").toExternalForm());
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.data);
    this.sorter = new DisplaySorter(ModelFactory.createUnion(this.ontology, this.data));
    this.application = new ServerApplication(new Reference("http://data.travellerrpg.com/"), new Reference("http://localhost:8182/"));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.server.HtmlRenderer#getRepresentation()}.
   */
  @Test
  public void testGetRepresentation() throws Exception {
    Resource r1 = this.inference.createResource(this.PREFIX + "thing1");
    Category top = new Category(this.sorter.getTop());
    StatementCollector collector = new StatementCollector(this.sorter);
    HtmlRenderer renderer = new HtmlRenderer(Locale.ENGLISH, this.sorter, this.application, new Request());
    
    collector.buildReferences(top, r1, this.inference);
    renderer.render(r1, top);
    assertNotNull(renderer.getRepresentation());
  }

}
