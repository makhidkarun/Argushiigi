package org.charvolant.argushiigi.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.RDF;

public class StatementCollectorTest extends JenaTest implements CategoryVisitor {
  private static final String PREFIX = "http://data.travellerrpg.com/ontology/argushiigi/test1/";
  private static final OntModel MODEL = ModelFactory.createOntologyModel();
  //private static final Property P1 = MODEL.createProperty(PREFIX, "p1");
  private static final Property P2 = MODEL.createProperty(PREFIX, "p2");
  private static final Property P3 = MODEL.createProperty(PREFIX, "p3");
  private static final Property P4 = MODEL.createProperty(PREFIX, "p4");
  private static final Property P5 = MODEL.createProperty(PREFIX, "p5");

  private Model ontology;
  private Model data;
  private InfModel inference;
  private DisplaySorter sorter;
  private Set<Statement> seen;
  private Set<Property> seenProperties;
  
  
  @Before
  public void setUp() throws Exception {
    Reasoner reasoner;

    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("rpg.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("argushiigi.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("argushiigi-test.rdf").toExternalForm());
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.data);
    this.sorter = new DisplaySorter(ModelFactory.createUnion(this.ontology, this.data));    
    this.seen = new HashSet<Statement>();
    this.seenProperties = new HashSet<Property>();
  }

  @Test
  public void testBuildStatements1() {
    StatementCollector collector = new StatementCollector(this.sorter);
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Category top = new Category(this.sorter.getTop());
    
    collector.buildStatements(top, t1, this.data, false);
    top.visit(this);
    assertEquals(2, top.getStatements().size());
    assertEquals(6, this.seen.size());
    assertTrue(this.seenProperties.contains(RDF.type));
    assertTrue(this.seenProperties.contains(Argushiigi.category));
    assertTrue(this.seenProperties.contains(this.P2));
    assertTrue(this.seenProperties.contains(this.P3));
    assertTrue(this.seenProperties.contains(this.P4));
    assertTrue(this.seenProperties.contains(this.P5));
  }

  @Test
  public void testBuildStatements2() {
    StatementCollector collector = new StatementCollector(this.sorter);
    Resource t1 = this.inference.createResource(this.PREFIX + "thing6");
    Category top = new Category(this.sorter.getTop());
    
    collector.buildStatements(top, t1, this.data, false);
    top.visit(this);
    assertEquals(2, top.getStatements().size());
    assertEquals(2, this.seen.size());
    assertTrue(this.seenProperties.contains(RDF.type));
    assertTrue(this.seenProperties.contains(RPG.template));
  }

  @Test
  public void testBuildStatements3() {
    StatementCollector collector = new StatementCollector(this.sorter);
    Resource t1 = this.inference.createResource(this.PREFIX + "thing6");
    Category top = new Category(this.sorter.getTop());
    
    collector.buildStatements(top, t1, this.data, true);
    top.visit(this);
    assertEquals(3, top.getStatements().size());
    assertEquals(6, this.seen.size());
    assertTrue(this.seenProperties.contains(RDF.type));
    assertTrue(this.seenProperties.contains(RPG.template));
    assertTrue(this.seenProperties.contains(Argushiigi.category));
    assertFalse(this.seenProperties.contains(this.P2));
    assertTrue(this.seenProperties.contains(this.P3));
    assertTrue(this.seenProperties.contains(this.P4));
    assertTrue(this.seenProperties.contains(this.P5));
  }

  @Test
  public void testBuildReferences1() {
    StatementCollector collector = new StatementCollector(this.sorter);
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Category top = new Category(this.sorter.getTop());
    
    collector.buildReferences(top, t1, this.data);
    top.visit(this);
    assertEquals(0, top.getStatements().size());
    assertEquals(1, this.seen.size());
    assertTrue(this.seenProperties.contains(RPG.template));
  }

  @Test
  public void testBuildReferences2() {
    StatementCollector collector = new StatementCollector(this.sorter);
    Resource t1 = this.inference.createResource(this.PREFIX + "thing6");
    Category top = new Category(this.sorter.getTop());
    
    collector.buildReferences(top, t1, this.data);
    top.visit(this);
    assertEquals(0, top.getStatements().size());
    assertEquals(0, this.seen.size());
  }

  @Override
  public void visit(Category category) {
    this.seen.addAll(category.getStatements());
    for (Statement s: category.getStatements())
      this.seenProperties.add(s.getPredicate());
  }

  @Override
  public void visited(Category category) {
  }

}
